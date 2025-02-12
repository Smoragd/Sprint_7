package ru.qa_scooter;

import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.response.Response;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.equalTo;
import static ru.qa_scooter.UsefulData.*;

public class CourierSteps {

    static String login;
    static String password;
    static String courierId;

    static String bearerToken = USER_TOKEN;

    // Создание JSON для передачи в body запроса
    static CourierJSON courierJSON;

    @Step("Cоздание курьера")
    static Response createCourierRequest(String login, String password, String firstName) {
        courierJSON = new CourierJSON(login, password, firstName);
        Response response = given()
                .filter(new AllureRestAssured())
                .auth().oauth2(bearerToken)
                .header("Content-type", "application/json")
                .body(courierJSON)
                .when()
                .post(COURIER_API);

        return response;
    }

    @Step("Проверка ответа ОШИБКА создания курьера")
    static void validateResponseMistake(Response response, int expectedStatusCode, String expectedMessage) {
        response.then()
                .statusCode(expectedStatusCode)
                .body("message", equalTo(expectedMessage));
    }

    @Step("Проверка ответа УСПЕХ создания курьера")
    static void validateResponseOk(Response response, int expectedStatusCode, Boolean expectedMessage) {
        response.then()
                .statusCode(expectedStatusCode)
                .body("ok", equalTo(expectedMessage));
    }

    @Step("Проверка ответа УСПЕХ авторизации курьера")
    static void loginResponseOk(Response response) {
        response.then()
                .statusCode(200)
                .body("id", notNullValue()); // Проверка, что при авторизации возвращается непустой id
    }

    @Step("Проверка ответа ОШИБКА авторизации курьера")
    static void loginResponseMistake(Response response, int expectedStatusCode, String expectedMessage) {
        response.then()
                .statusCode(expectedStatusCode)
                .body("message", equalTo(expectedMessage)); // Проверка, что при авторизации возвращается непустой id
    }

    @Step("Авторизация курьера")
    static Response loginCourierRequest(String login, String password) {
        // создание тела запроса для авторизации курьера
        courierJSON = new CourierJSON(login, password);
        Response loginResponse = given()
                .filter(new AllureRestAssured())
                .auth().oauth2(bearerToken)
                .header("Content-type", "application/json")
                .body(courierJSON)
                .when()
                .post(COURIER_API+"/login");

        return loginResponse;
    }

    @Step("Удаление курьера")
    static void deleteCourier() {
        if (login != null && !login.isEmpty() && password != null && !password.isEmpty()) {
            // Авторизация курьера для получения его id
            Response loginResponse = loginCourierRequest(login, password);
            // Извлечение id курьера из ответа - для запроса на удаление
            courierId = loginResponse.then().extract().path("id").toString();

            if (loginResponse.statusCode() == 200) {

                // Отправка запроса на удаление курьера
                given()
                        .log().all()
                        .filter(new AllureRestAssured())
                        .auth().oauth2(bearerToken)
                        .header("Content-type", "application/json")
                        .when()
                        .delete(COURIER_API + courierId)
                        .then()
                        .log().all()
                        .statusCode(200);// Проверка успешного удаления
            }
        }
    }



}
