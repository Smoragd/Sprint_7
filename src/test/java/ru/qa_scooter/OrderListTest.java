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
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static ru.qa_scooter.UsefulData.*;

public class OrderListTest {

    private String[] orderTracks = new String[2]; // массив для track созданных заказов

    String bearerToken = USER_TOKEN;

    @Before
    public void setUp() {
        RestAssured.baseURI = BASE_URI;

        // Создание двух заказов и добавление их track в массив
        orderTracks[0] = createOrder().then().extract().path("track").toString();
        orderTracks[1] = createOrder().then().extract().path("track").toString();
        System.out.println("orderTrack0: " + orderTracks[0]); // Логирование track созданных заказов
        System.out.println("orderTrack1: " + orderTracks[1]); // Логирование track созданных заказов
    }

    @Test
    @DisplayName("Получение списка заказов")
    @Description("Проверка, что в теле ответа возвращается список заказов")
    public void testGetOrderList() {

        // Проверка, что запрос на получение списка заказов проходит успешно, и список заказов не пустой
        Response response = getOrderList();
        response.then()
                .statusCode(200)
                .body("orders", not(empty()));
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

    @Step("Получение списка заказов")
    private Response getOrderList() {
        return given()
                .filter(new AllureRestAssured())
                .auth().oauth2(bearerToken)
                .header("Content-type", "application/json")
                .when()
                .get("/api/v1/orders");
    }

    @After
    //Удаление данных после теста
    public void deleteData() {

        // Удаление заказов
        for (String orderTrack : orderTracks) {
            System.out.println("Canceling order with track: " + orderTrack); // Логирование track
            Response response = given()
                    .filter(new AllureRestAssured())
                    .auth().oauth2(bearerToken)
                    .header("Content-type", "application/json")
                    .queryParam("track", orderTrack)
                    .when()
                    .put("/api/v1/orders/cancel");

            response.then().log().all(); // Логирование ответа
            response.then().statusCode(200); // Проверка успешного удаления

        }
    }
}
