package ru.qa_scooter;

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
import static ru.qa_scooter.OrderSteps.*;
import static ru.qa_scooter.UsefulData.*;
import io.qameta.allure.Description;

@RunWith(Parameterized.class)
public class OrderCreateParamTest {

    String[] colors;

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

        // создание заказа и получение track для дальнейшего удаления заказа
        Response response = sendCreateOrderRequest("Тест", "Тестов", "Москва",
                "4", "+79999999999", "5", "2025-03-01",
                "Тестовый комментарий", colors);
        //проверка ответа на 201 и непустой track
        validateCreateOrderResponse(response);
    }

    @After
    //Удаление данных после теста
    public void deleteData() {
        cancelOrder();
    }

}
