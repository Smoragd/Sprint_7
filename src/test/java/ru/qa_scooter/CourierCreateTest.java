package ru.qa_scooter;

import io.restassured.RestAssured;
import org.junit.Before;
import static ru.qa_scooter.CourierSteps.*;
import static ru.qa_scooter.UsefulData.*;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Test;

public class CourierCreateTest {

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
    }

    @Test
    @DisplayName("Успешное создание курьера")
    @Description("Проверка, что курьера можно создать")
    public void testCreateCourierSuccess() {
        Response response = createCourierRequest(login, password, firstName);
        validateResponseOk(response, 201, true);
    }

    @Test
    @DisplayName("Создание дубликата курьера")
    @Description("Проверка, что нельзя создать двух одинаковых курьеров")
    public void testCreateDuplicateCourier() {
        // Создание первого курьера
        createCourierRequest(login, password, firstName);
        // Создание второго курьера с такими же данными
        Response response = createCourierRequest(login, password, firstName);
        // Проверка, что возвращается ошибка
        validateResponseMistake(response, 409, "Этот логин уже используется. Попробуйте другой.");
    }

    @Test
    @DisplayName("Создание курьера с уже существующим логином")
    @Description("Проверка, что нельзя создать курьера с уже существующим логином, но с другими паролем и именем")
    public void testCreateCourierWithExistingLogin() {
        // Создание первого курьера
        createCourierRequest(login, password, firstName);
        // Создаем второго курьера с тем же логином, но другими паролем и именем
        Response response = createCourierRequest(login, password+1, firstName+1);
        // Проверка, что возвращается ошибка
        validateResponseMistake(response, 409, "Этот логин уже используется. Попробуйте другой.");
    }

    @Test
    @DisplayName("Создание курьера без логина")
    @Description("Проверка создания курьера без обязательного поля - логин")
    public void testCreateCourierWithoutLogin() {
        Response response = createCourierRequest("", password, firstName);
        // Проверка, что возвращается ошибка
        validateResponseMistake(response, 400, "Недостаточно данных для создания учетной записи");
    }

    @Test
    @DisplayName("Создание курьера без пароля")
    @Description("Проверка создания курьера без обязательного поля - пароль")
    public void testCreateCourierWithoutPassword() {
        Response response = createCourierRequest(login, "", firstName);
        // Проверка, что возвращается ошибка
        validateResponseMistake(response, 400, "Недостаточно данных для создания учетной записи");
    }

    @Test
    @DisplayName("Создание курьера без имени")
    @Description("Проверка создания курьера без необязательного поля - имя")
    public void testCreateCourierWithoutFirstName() {
        Response response = createCourierRequest(login, password, "");
        // Проверка успешного создания
        validateResponseOk(response, 201, true);
    }

    @After
    //Удаление данных после теста
    public void deleteData() {
        deleteCourier();
    }

}
