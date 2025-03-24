package adventure.go.goadventure.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class ScenarioDTO {
    private Integer id;
    private String name;
    @JsonProperty("limit_players")
    private Integer limitPlayers;

    private AuthorDTO author;

    @JsonProperty("creation_date")
    private LocalDateTime creationDate;

    private String description;

    @JsonProperty("photo_url")
    private String photoUrl;

    public ScenarioDTO(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public ScenarioDTO(Integer id, String name, Integer limitPlayers, AuthorDTO author, LocalDateTime creationDate, String description, String photoUrl) {
        this.id = id;
        this.name = name;
        this.limitPlayers = limitPlayers;
        this.author = author;
        this.creationDate = creationDate;
        this.description = description;
        this.photoUrl = photoUrl;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getLimitPlayers() {
        return limitPlayers;
    }

    public void setLimitPlayers(Integer limitPlayers) {
        this.limitPlayers = limitPlayers;
    }

    public AuthorDTO getAuthor() {
        return author;
    }

    public void setAuthor(AuthorDTO author) {
        this.author = author;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public static class AuthorDTO {
        private Integer id;
        private String login;
        private String email;
        private String bio;
        @JsonProperty("creation_date")
        private LocalDateTime creationDate;
        @JsonProperty("photo_url")
        private String photoUrl;

        public AuthorDTO(Integer id, String login, String email, String bio, LocalDateTime creationDate, String photoUrl) {
            this.id = id;
            this.login = login;
            this.email = email;
            this.bio = bio;
            this.creationDate = creationDate;
            this.photoUrl = photoUrl;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
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

        public LocalDateTime getCreationDate() {
            return creationDate;
        }

        public void setCreationDate(LocalDateTime creationDate) {
            this.creationDate = creationDate;
        }

        public String getPhotoUrl() {
            return photoUrl;
        }

        public void setPhotoUrl(String photoUrl) {
            this.photoUrl = photoUrl;
        }
    }
}