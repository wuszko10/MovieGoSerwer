package api;

public class RegistrationResponse {

    private String login;
    private String password;
    private String email;
    private String message;

    public RegistrationResponse(){

    }

    public RegistrationResponse(String login, String password, String email) {
        this.login = login;
        this.password = password;
        this.email = email;
    }

    public RegistrationResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
