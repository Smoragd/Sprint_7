package ru.qa_scooter;

import io.restassured.RestAssured;
import org.junit.Before;
import static ru.qa_scooter.CourierSteps.*;
import static ru.qa_scooter.UsefulData.*;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Test;
import io.qameta.allure.Description;

public class CourierLoginTest {

    private String login;
    private String password;
    private String firstName;

    @Before
    public void setUp() {
        RestAssured.baseURI = BASE_URI;

        // Генерация случайных данных для курьера перед каждым тестом
        login = generateRandomLogin();
        password = generateRandomPassword();
        firstName = generateRandomFirstName();

        // Создание курьера для тестов
        createCourierRequest(login, password, firstName);
    }

    @Test
    @DisplayName("Успешная авторизация курьера")
    @Description("Проверка, что курьер может авторизоваться с корректными данными")
    public void testCourierLoginSuccess() {
        Response response = loginCourierRequest(login, password);
        loginResponseOk(response); // Проверка, что при авторизации возвращается непустой id
    }

    @Test
    @DisplayName("Авторизация с неправильным паролем")
    @Description("Проверка, что система возвращает ошибку при неправильном пароле")
    public void testCourierLoginWrongPassword() {
        Response response = loginCourierRequest(login, "wrong_password");
        loginResponseMistake(response, 404, "Учетная запись не найдена");
    }

    @Test
    @DisplayName("Авторизация с неправильным логином")
    @Description("Проверка, что система возвращает ошибку при неправильном логине")
    public void testCourierLoginWrongLogin() {
        Response response = loginCourierRequest("wrong_login", password);
        loginResponseMistake(response, 404, "Учетная запись не найдена");
    }

    @Test
    @DisplayName("Авторизация без логина")
    @Description("Проверка, что система возвращает ошибку при отсутствии логина")
    public void testCourierLoginWithoutLogin() {
        Response response = loginCourierRequest("", password);
        loginResponseMistake(response, 400, "Недостаточно данных для входа");
    }

    @Test
    @DisplayName("Авторизация без пароля")
    @Description("Проверка, что система возвращает ошибку при отсутствии пароля")
    public void testCourierLoginWithoutPassword() {
        Response response = loginCourierRequest(login, "");
        loginResponseMistake(response, 400, "Недостаточно данных для входа");
    }

    @Test
    @DisplayName("Авторизация несуществующего курьера")
    @Description("Проверка, что система возвращает ошибку при авторизации несуществующего курьера")
    public void testCourierLoginNonExistent() {
        Response response = loginCourierRequest("non_existent_login", "non_existent_password");
        loginResponseMistake(response, 404, "Учетная запись не найдена");
    }

    @After
    //Удаление данных после теста
    public void deleteData() {
        deleteCourier();
    }

}
