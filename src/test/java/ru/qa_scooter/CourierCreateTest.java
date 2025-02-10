package ru.qa_scooter;

import io.restassured.RestAssured;
import org.junit.Before;
import static ru.qa_scooter.UsefulData.*;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class CourierCreateTest {

    private String login;
    private String password;
    private String firstName;

    String bearerToken = USER_TOKEN;

    @Before
    public void setUp() {
        RestAssured.baseURI = BASE_URI;

        // Генерация случайных данных перед каждым тестом
        login = generateRandomLogin();
        password = generateRandomPassword();
        firstName = generateRandomFirstName();
    }

    @Test
    @DisplayName("Успешное создание курьера")
    @Description("Проверка, что курьера можно создать")
    public void testCreateCourierSuccess() {
        String body = createRequestBody(login, password, firstName);
        Response response = sendCreateCourierRequest(body);
        validateResponseOk(response, 201, true);
    }

    @Test
    @DisplayName("Создание дубликата курьера")
    @Description("Проверка, что нельзя создать двух одинаковых курьеров")
    public void testCreateDuplicateCourier() {
        // Создание первого курьера
        String body = createRequestBody(login, password, firstName);
        sendCreateCourierRequest(body);

        // Создание второго курьера с такими же данными
        Response response = sendCreateCourierRequest(body);

        // Проверка, что возвращается ошибка
        validateResponse(response, 409, "Этот логин уже используется. Попробуйте другой.");
    }

    @Test
    @DisplayName("Создание курьера с уже существующим логином")
    @Description("Проверка, что нельзя создать курьера с уже существующим логином, но с другими паролем и именем")
    public void testCreateCourierWithExistingLogin() {
        // Создание первого курьера
        String firstCourierBody = createRequestBody(login, password, firstName);
        sendCreateCourierRequest(firstCourierBody);

        // Создаем второго курьера с тем же логином, но другими паролем и именем
        String secondCourierBody = createRequestBody(login, (password+1), (firstName+1));
        Response response = sendCreateCourierRequest(secondCourierBody);

        // Проверка, что возвращается ошибка
        validateResponse(response, 409, "Этот логин уже используется. Попробуйте другой.");
    }

    @Test
    @DisplayName("Создание курьера без логина")
    @Description("Проверка создания курьера без обязательного поля - логин")
    public void testCreateCourierWithoutLogin() {
        String body = createRequestBody("", password, firstName);
        Response response = sendCreateCourierRequest(body);
        // Проверка, что возвращается ошибка
        validateResponse(response, 400, "Недостаточно данных для создания учетной записи");
    }

    @Test
    @DisplayName("Создание курьера без пароля")
    @Description("Проверка создания курьера без обязательного поля - пароль")
    public void testCreateCourierWithoutPassword() {
        String body = createRequestBody(login, "", firstName);
        Response response = sendCreateCourierRequest(body);
        // Проверка, что возвращается ошибка
        validateResponse(response, 400, "Недостаточно данных для создания учетной записи");
    }

    @Test
    @DisplayName("Создание курьера без имени")
    @Description("Проверка создания курьера без необязательного поля - имя")
    public void testCreateCourierWithoutFirstName() {
        String body = createRequestBody(login, password, "");
        Response response = sendCreateCourierRequest(body);
        // Проверка успешного создания
        validateResponseOk(response, 201, true);
    }

    @Step("Создание тела запроса для курьера")
    private String createRequestBody(String login, String password, String firstName) {
        return String.format("{\"login\":\"%s\",\"password\":\"%s\",\"firstName\":\"%s\"}", login, password, firstName);
    }

    @Step("Отправка запроса на создание курьера")
    private Response sendCreateCourierRequest(String body) {
        Response response = given()
                .auth().oauth2(bearerToken)
                .header("Content-type", "application/json")
                .body(body)
                .when()
                .post("/api/v1/courier");
        return response;
    }

    @Step("Проверка ответа ОШИБКА")
    private void validateResponse(Response response, int expectedStatusCode, String expectedMessage) {
        response.then()
                .statusCode(expectedStatusCode)
                .body("message", equalTo(expectedMessage));

    }

    @Step("Проверка ответа УСПЕХ")
    private void validateResponseOk(Response response, int expectedStatusCode, Boolean expectedMessage) {
        response.then()
                .statusCode(expectedStatusCode)
                .body("ok", equalTo(expectedMessage));

    }

    @After
    //Удаление курьера после теста
    public void deleteCourier() {
        if (login != null && !login.isEmpty() && password != null && !password.isEmpty()) {
            // Авторизация курьера для получения его id
            Response loginResponse = given()
                    .auth().oauth2(bearerToken)
                    .header("Content-type", "application/json")
                    .body(String.format("{\"login\":\"%s\",\"password\":\"%s\"}", login, password))
                    .when()
                    .post("/api/v1/courier/login");

            if (loginResponse.statusCode() == 200) {
                // Извлечение id курьера из ответа - для запроса на удаление
                String courierId = loginResponse.then().extract().path("id").toString();

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
