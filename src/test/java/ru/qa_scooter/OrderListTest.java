package ru.qa_scooter;

import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import io.qameta.allure.Description;
import java.util.ArrayList;
import java.util.List;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static ru.qa_scooter.UsefulData.*;

public class OrderListTest {

    private String login;
    private String password;
    private String firstName;
    private String courierId;
    private String[] orderTracks = new String[2]; // Сохраняем track созданных заказов
    private List<String> orderIds = new ArrayList<>(); // Сохраняем id заказов

    String bearerToken = USER_TOKEN;

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";

        // Генерация случайных данных для курьера перед каждым тестом
        login = generateRandomLogin();
        password = generateRandomPassword();
        firstName = generateRandomFirstName();

        // Создание курьера
        createCourier(login, password, firstName);

        // Авторизация курьера для получения его id
        courierId = loginCourier(login, password).then().extract().path("id").toString();

        // Создание двух заказов и добавление их track в массив
        orderTracks[0] = createOrder().then().extract().path("track").toString();
        orderTracks[1] = createOrder().then().extract().path("track").toString();

        // Получение id заказов по их track и добавление id в список
        orderIds.add(getOrderIdByTrack(orderTracks[0]));
        orderIds.add(getOrderIdByTrack(orderTracks[1]));

        // Присвоение заказов курьеру (принятие заказов курьером)
        assignOrdersToCourier(courierId, orderIds.toArray(new String[0]));
    }

    @Test
    @DisplayName("Получение списка заказов для курьера")
    @Description("Проверка, что в теле ответа возвращается список заказов для конкретного курьера")
    public void testGetOrderListForCourier() {

        // Проверка, что запрос на получение списка заказов проходит успешно, и список заказов не пустой
        Response response = getOrderListForCourier(courierId);
        response.then()
                .statusCode(200)
                .body("orders", not(empty()));

        // Проверяем, что заказы присвоены конкретному курьеру:
        // то есть что заказы, которые мы присвоили курьеру в @Before,
        // содержатся среди всех заказов по курьеру из response
        List<Integer> orderIdsFromResponse = response.then().extract().path("orders.id");
        for (String orderId : orderIds) {
            assert orderIdsFromResponse.contains(Integer.parseInt(orderId));
        }
    }

    @Step("Создание курьера")
    private void createCourier(String login, String password, String firstName) {
        // создание тела запроса для создания курьера
        String body = String.format("{\"login\":\"%s\",\"password\":\"%s\",\"firstName\":\"%s\"}", login, password, firstName);
        given()
                .filter(new AllureRestAssured())
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
                .filter(new AllureRestAssured())
                .auth().oauth2(bearerToken)
                .header("Content-type", "application/json")
                .body(body)
                .when()
                .post("/api/v1/courier/login");
    }

    @Step("Создание заказа")
    private Response createOrder() {
        // создание тела запроса для создания заказа
        String body = "{\"firstName\":\"Тест\",\"lastName\":\"Тестов\"," + "\"address\":\"Москва\"," +
                "\"metroStation\":4,\"phone\":\"+79999999999\"," + "\"rentTime\":5,\"deliveryDate\":\"2025-03-01\","
                + "\"comment\":\"Тестовый комментарий\",\"color\":[\"BLACK\"]}";
        return given()
                .filter(new AllureRestAssured())
                .auth().oauth2(bearerToken)
                .header("Content-type", "application/json")
                .body(body)
                .when()
                .post("/api/v1/orders");
    }

    @Step("Получение id заказа по его track")
    private String getOrderIdByTrack(String track) {
        Response response = given()
                .filter(new AllureRestAssured())
                .auth().oauth2(bearerToken)
                .header("Content-type", "application/json")
                .when()
                .get("/api/v1/orders/track?t=" + track);

        return response.then()
                .statusCode(200)
                .extract()
                .path("order.id").toString(); // Извлекаем id заказа в виде строки
    }

    @Step("Присвоение заказов курьеру")
    private void assignOrdersToCourier(String courierId, String[] orderIds) {
        for (String orderId : orderIds) {
            given()
                    .filter(new AllureRestAssured())
                    .auth().oauth2(bearerToken)
                    .header("Content-type", "application/json")
                    .queryParam("courierId", courierId)
                    .when()
                    .put("/api/v1/orders/accept/" + orderId)
                    .then()
                    .statusCode(200);
        }
    }

    @Step("Получение списка заказов для курьера")
    private Response getOrderListForCourier(String courierId) {
        return given()
                .filter(new AllureRestAssured())
                .auth().oauth2(bearerToken)
                .header("Content-type", "application/json")
                .queryParam("courierId", courierId)
                .when()
                .get("/api/v1/orders");
    }

    @After
    //Удаление данных после теста
    public void deleteData() {
        // Удаление заказов
        for (String orderTrack : orderTracks) {
            given()
                    .filter(new AllureRestAssured())
                    .auth().oauth2(bearerToken)
                    .header("Content-type", "application/json")
                    .queryParam("track", orderTrack)
                    .when()
                    //.put("/api/v1/orders/cancel?track=" + orderTrack)
                    .then()
                    .statusCode(200); // Проверка успешного удаления
        }

        // Удаление курьера
        if (courierId != null) {
            given()
                    .filter(new AllureRestAssured())
                    .auth().oauth2(bearerToken)
                    .header("Content-type", "application/json")
                    .when()
                    .delete("/api/v1/courier/" + courierId)
                    .then()
                    .statusCode(200); // Проверка успешного удаления
        }
    }
}
