package ru.qa_scooter;

import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import java.util.Arrays;
import java.util.Collection;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;
import static ru.qa_scooter.UsefulData.*;
import io.qameta.allure.Description;

@RunWith(Parameterized.class)
public class OrderCreateParamTest {

    private String[] colors;
    private String trackId;

    String bearerToken = USER_TOKEN;

    public OrderCreateParamTest(String[] colors) {
        this.colors = colors;
    }

    @Before
    public void setUp() {
        RestAssured.baseURI = BASE_URI;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {new String[]{"BLACK"}}, // цвет BLACK
                {new String[]{"GREY"}},  // цвет GREY
                {new String[]{"BLACK", "GREY"}}, // оба цвета
                {new String[]{null}} // без цвета
        });
    }

    @Test
    @DisplayName("Создание заказа с разными комбинациями цветов")
    @Description("Проверка, что заказ можно создать с разными комбинациями цветов: BLACK, GREY, оба цвета, без цвета")
    public void testCreateOrderWithDifferentColors() {
        // создание тела запроса
        String body = createOrderRequestBody(colors);
        // получение ответа на запрос на создание заказа
        Response response = sendCreateOrderRequest(body);
        //проверка ответа на 201 и непустой track
        validateResponse(response);
        // получение track для дальнейшего удаления заказа
        trackId = response.then().extract().path("track").toString();
    }

    @Step("Создание тела запроса для заказа")
    private String createOrderRequestBody(String[] colors) {
        StringBuilder colorsJson = new StringBuilder();
        if (colors.length > 0) {
            colorsJson.append("\"color\":[");
            for (int i = 0; i < colors.length; i++) {
                colorsJson.append("\"").append(colors[i]).append("\"");
                if (i < colors.length - 1) {
                    colorsJson.append(",");
                }
            }
            colorsJson.append("]");
        }
        return String.format("{\"firstName\":\"Тест\",\"lastName\":\"Тестов\",\"address\":\"Москва\"," +
                "\"metroStation\":4,\"phone\":\"+79999999999\",\"rentTime\":5,\"deliveryDate\":\"2025-03-01\"" +
                ",\"comment\":\"Тестовый комментарий\",%s}", colorsJson);
    }

    @Step("Отправка запроса на создание заказа")
    private Response sendCreateOrderRequest(String body) {
        return given()
                .auth().oauth2(bearerToken)
                .header("Content-type", "application/json")
                .body(body)
                .when()
                .post("/api/v1/orders");
    }

    @Step("Проверка ответа: код ответа 201 и непустой track")
    private void validateResponse(Response response) {
        response.then()
                .statusCode(201)
                .body("track", notNullValue());
    }

    @After
    //"Отмена заказа после теста"
    public void cancelOrder() {
        if (trackId != null) {
            given()
                    .auth().oauth2(bearerToken)
                    .header("Content-type", "application/json")
                    .when()
                    .put("/api/v1/orders/cancel?track=" + trackId)
                    .then()
                    .statusCode(200); // Проверка успешного удаления
        }
    }

}
