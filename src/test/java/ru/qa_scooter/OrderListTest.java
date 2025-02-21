package ru.qa_scooter;

import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import io.qameta.allure.Description;
import static ru.qa_scooter.OrderSteps.*;
import static ru.qa_scooter.UsefulData.*;

public class OrderListTest {

    @Before
    public void setUp() {
        RestAssured.baseURI = BASE_URI;

        // Создание заказа и получение его track для удаления
        sendCreateOrderRequest("Тест","Тестов","Москва","4", "+79999999999",
                "5", "2025-03-01", "Тестовый комментарий", new String[] {"BLACK"});
    }

    @Test
    @DisplayName("Получение списка заказов")
    @Description("Проверка, что в теле ответа возвращается список заказов")
    public void testGetOrderList() {

        Response response = getOrderList();
        validateGetOrderList(response);
    }

    @After
    //Удаление данных после теста
    public void deleteData() {
        cancelOrder();
    }
}
