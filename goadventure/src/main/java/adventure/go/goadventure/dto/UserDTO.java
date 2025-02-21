package adventure.go.goadventure.dto;

public class UserDTO {
    private Integer id_user;
    private String login;
    private String email;

    public UserDTO() {
    }

    public UserDTO(Integer id_user, String login, String email) {
        this.id_user = id_user;
        this.login = login;
        this.email = email;
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
}