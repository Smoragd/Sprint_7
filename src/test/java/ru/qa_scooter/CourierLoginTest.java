package ru.qa_scooter;

import io.restassured.RestAssured;
import org.junit.Before;
import static org.hamcrest.CoreMatchers.notNullValue;
import static ru.qa_scooter.UsefulData.*;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Test;
import io.qameta.allure.Description;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class CourierLoginTest {

    private String login;
    private String password;
    private String firstName;
    private String courierId;

    String bearerToken = USER_TOKEN;

    @Before
    public void setUp() {
        RestAssured.baseURI = BASE_URI;

        // Генерация случайных данных перед каждым тестом
        login = generateRandomLogin();
        password = generateRandomPassword();
        firstName = generateRandomFirstName();

        // Создание курьера для тестов
        createCourier(login, password, firstName);
    }

    @Test
    @DisplayName("Успешная авторизация курьера")
    @Description("Проверка, что курьер может авторизоваться с корректными данными")
    public void testCourierLoginSuccess() {
        Response response = loginCourier(login, password);
        response.then()
                .statusCode(200)
                .body("id", notNullValue()); // Проверка, что при авторизации возвращается непустой id
    }

    @Test
    @DisplayName("Авторизация с неправильным паролем")
    @Description("Проверка, что система возвращает ошибку при неправильном пароле")
    public void testCourierLoginWrongPassword() {
        Response response = loginCourier(login, "wrong_password");
        response.then()
                .statusCode(404)
                .body("message", equalTo("Учетная запись не найдена"));
    }

    @Test
    @DisplayName("Авторизация с неправильным логином")
    @Description("Проверка, что система возвращает ошибку при неправильном логине")
    public void testCourierLoginWrongLogin() {
        Response response = loginCourier("wrong_login", password);
        response.then()
                .statusCode(404)
                .body("message", equalTo("Учетная запись не найдена"));
    }

    @Test
    @DisplayName("Авторизация без логина")
    @Description("Проверка, что система возвращает ошибку при отсутствии логина")
    public void testCourierLoginWithoutLogin() {
        Response response = loginCourier("", password);
        response.then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для входа"));
    }

    @Test
    @DisplayName("Авторизация без пароля")
    @Description("Проверка, что система возвращает ошибку при отсутствии пароля")
    public void testCourierLoginWithoutPassword() {
        Response response = loginCourier(login, "");
        response.then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для входа"));
    }

    @Test
    @DisplayName("Авторизация несуществующего курьера")
    @Description("Проверка, что система возвращает ошибку при авторизации несуществующего курьера")
    public void testCourierLoginNonExistent() {
        Response response = loginCourier("non_existent_login", "non_existent_password");
        response.then()
                .statusCode(404)
                .body("message", equalTo("Учетная запись не найдена"));
    }

    @Step("Создание курьера")
    private void createCourier(String login, String password, String firstName) {
        // создание тела запроса для создания курьера
        String body = String.format("{\"login\":\"%s\",\"password\":\"%s\",\"firstName\":\"%s\"}", login, password, firstName);
        given()
                .auth().oauth2(bearerToken)
                .header("Content-type", "application/json")
                .body(body)
                .when()
                .post("/api/v1/courier")
                .then()
                .statusCode(201);
    }

    @Step("Авторизация курьера")
    private Response loginCourier(String login, String password) {
        // создание тела запроса для авторизации курьера
        String body = String.format("{\"login\":\"%s\",\"password\":\"%s\"}", login, password);
        return given()
                .auth().oauth2(bearerToken)
                .header("Content-type", "application/json")
                .body(body)
                .when()
                .post("/api/v1/courier/login");
    }

    @After
    //Удаление курьера после теста
    public void deleteCourier() {
        if (login != null && !login.isEmpty() && password != null && !password.isEmpty()) {
            // Авторизация курьера для получения его id
            Response loginResponse = loginCourier(login, password);

            if (loginResponse.statusCode() == 200) {
                // Извлечение id курьера из ответа - для запроса на удаление
                courierId = loginResponse.then().extract().path("id").toString();

                // Отправка запроса на удаление курьера
                given()
                        .auth().oauth2(bearerToken)
                        .header("Content-type", "application/json")
                        .when()
                        .delete("/api/v1/courier/" + courierId)
                        .then()
                        .statusCode(200); // Проверка успешного удаления
            }
        }
    }



}
