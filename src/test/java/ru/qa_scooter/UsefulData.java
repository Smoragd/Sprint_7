package ru.qa_scooter;
import java.util.Random;

public class UsefulData {

    // мой токен
    public static final String USER_TOKEN =
            "Cartoshka-legacy=true; Cartoshka=true; _yasc=JxCQwUxDn6UZqyqTmxCQjNTDyEYP+DXjO5G4cTJnIFasljvhxhrRuYYZxVjKyF8DDMOE";

    // uri ресурса
    public static final String BASE_URI = "https://qa-scooter.praktikum-services.ru/";

    // константы для api
    public static final String COURIER_API = "/api/v1/courier";
    public static final String ORDERS_API = "/api/v1/orders";

    // блок для генерации рандомных тестовых значений:
    // список всех цифр и латинских букв:
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final Random random = new Random();
    // Генерация случайного логина
    public static String generateRandomLogin() {
        return generateRandomString(6); // Логин из 6 символов
    }
    // Генерация случайного пароля
    public static String generateRandomPassword() {
        return generateRandomString(6); // Пароль из 6 символов
    }
    // Генерация случайного имени
    public static String generateRandomFirstName() {
        return "Courier" + generateRandomString(6); // Имя вида "CourierXXXXXX"
    }
    // Доп. метод для генерации случайного имени
    private static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }

    // еще вариант, как сгенерить случайный логин и пароль:
    // login = "test_login_" + System.currentTimeMillis();

}
