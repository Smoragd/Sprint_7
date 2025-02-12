package ru.qa_scooter;

public class CourierJSON {

    private String login;
    private String password;
    private String firstName;

    //конструктор для создания
    public CourierJSON(String login, String password, String firstName){
        this.login = login;
        this.password = password;
        this.firstName = firstName;
    }

    //конструктор для авторизации
    public CourierJSON(String login, String password){
        this.login = login;
        this.password = password;
    }

    public CourierJSON() { }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

}
