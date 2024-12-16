package adventure.go.goadventure.user;

public class User {
    private Integer id_user;
    private String login;
    private String email;
    private String password;

    public User() {
    }

    public User(Integer id_user, String login, String email, String password) {
        this.id_user = id_user;
        this.login = login;
        this.email = email;
        this.password = password;
    }

    public Integer getId_user() {
        return id_user;
    }

    public void setId_user(Integer id_user) {
        this.id_user = id_user;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
