package adventure.go.goadventure.user;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_user;
    private String login;
    private String email;
    private String password;
    private String bio;
    private LocalDateTime creation_date;
    private String photo_url;
    private String role;


    public User() {
    }

    public User(Integer id_user, String login, String email, String password, String bio, LocalDateTime creation_date, String photoUrl) {
        this.id_user = id_user;
        this.login = login;
        this.email = email;
        this.password = password;
        this.bio = bio;
        this.creation_date = creation_date;
        this.photo_url = photoUrl;
    }

    public User(Integer id_user, String login, String email, String password, String bio, LocalDateTime creation_date, String photoUrl, String role) {
        this.id_user = id_user;
        this.login = login;
        this.email = email;
        this.password = password;
        this.bio = bio;
        this.creation_date = creation_date;
        this.photo_url = photoUrl;
        this.role = role;
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

    public Object getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public LocalDateTime getCreation_date() {
        return creation_date;
    }

    public void setCreation_date(LocalDateTime creation_date) {
        this.creation_date = creation_date;
    }

    public String getPhoto_url() {
        return photo_url;
    }

    public void setPhoto_url(String photo_url) {
        this.photo_url = photo_url;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

}
