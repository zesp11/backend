package adventure.go.goadventure.dto;

import java.time.LocalDateTime;

public class UserDTO {
    private Integer id_user;
    private String login;
    private String email;
    private String bio;
    private LocalDateTime creation_date;
    private String photo_url;

    public UserDTO() {
    }

    public UserDTO(Integer id_user, String login, String email, String bio, LocalDateTime creation_date, String photoUrl) {
        this.id_user = id_user;
        this.login = login;
        this.email = email;
        this.bio = bio;
        this.creation_date = creation_date;
        this.photo_url = photoUrl;
    }

    public UserDTO(Integer idUser, String login, String email) {
        this.id_user = id_user;
        this.login = login;
        this.email = email;
        this.creation_date = creation_date;
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

    public String getBio() {
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

}