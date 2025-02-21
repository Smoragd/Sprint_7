package ru.qa_scooter;

import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.response.Response;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static ru.qa_scooter.UsefulData.*;

public class OrderSteps {

    static String bearerToken = USER_TOKEN;
    static String orderTrack;

    // Создание JSON для передачи в body запроса
    static OrderJSON orderJSON;

    @Step("Отправка запроса на создание заказа")
    static Response sendCreateOrderRequest(String firstName, String lastName, String address, String metroStation,
                                            String phone, String rentTime, String deliveryDate, String comment,
                                            String[] colors) {
        orderJSON = new OrderJSON(firstName, lastName, address, metroStation, phone, rentTime, deliveryDate, comment, colors);
        Response response = given()
                .filter(new AllureRestAssured())
                .auth().oauth2(bearerToken)
                .header("Content-type", "application/json")
                .body(orderJSON)
                .when()
                .post(ORDERS_API);

        // Получение track заказа для его удаления
        orderTrack = response.then()
                .extract()
                .path("track").toString();
        System.out.println("Created order with track: " + orderTrack); // Логирование track созданного заказа

        return response;
    }

    @Step("Проверка создания заказа: код ответа 201 и непустой track")
    static void validateCreateOrderResponse(Response response) {
        response.then()
                .statusCode(201)
                .body("track", notNullValue());
    }

    @Step("Проверка получения списка заказов: код ответа 200 и непустой список")
    static void validateGetOrderList(Response response) {
        response.then()
                .statusCode(200)
                .body("orders", not(empty()));
    }

    @Step("Отмена заказа")
    static void cancelOrder() {
        if (orderTrack != null) {
            given()
                    .filter(new AllureRestAssured())
                    .auth().oauth2(bearerToken)
                    .header("Content-type", "application/json")
                    .when()
                    .put(ORDERS_API + "/cancel?track=" + orderTrack)
                    .then()
                    .statusCode(200);// Проверка успешного удаления
            System.out.println("Deleted order with track: " + orderTrack); // Логирование track удаленного заказа
        }
    }

    @Step("Получение списка заказов")
    static Response getOrderList() {
        return given()
                .filter(new AllureRestAssured())
                .auth().oauth2(bearerToken)
                .header("Content-type", "application/json")
                .when()
                .get(ORDERS_API);
    }

}
