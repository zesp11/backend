// UserScenarioDTO.java
package adventure.go.goadventure.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Map;

public class UserScenarioDTO {
    @JsonProperty("id")
    private Integer id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("limit_players")
    private Integer limitPlayers;

    @JsonProperty("creation_date")
    private LocalDateTime creationDate;

    @JsonProperty("description")
    private String description;

    @JsonProperty("photo_url")
    private String photoUrl;

    @JsonProperty("first_step")
    private Map<String, Object> firstStep;

    public UserScenarioDTO(Integer id, String name, Integer limitPlayers, LocalDateTime creationDate, String description, String photoUrl, Map<String, Object> firstStep) {
        this.id = id;
        this.name = name;
        this.limitPlayers = limitPlayers;
        this.creationDate = creationDate;
        this.description = description;
        this.photoUrl = photoUrl;
        this.firstStep = firstStep;
    }

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

    public Map<String, Object> getFirstStep() {
        return firstStep;
    }

    public void setFirstStep(Map<String, Object> firstStep) {
        this.firstStep = firstStep;
    }
}