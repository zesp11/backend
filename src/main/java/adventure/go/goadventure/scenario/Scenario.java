package adventure.go.goadventure.scenario;

import adventure.go.goadventure.step.Step;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Scenario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_scen")
    @JsonProperty("id")
    private Integer id;

    @JsonProperty("name")
    private String title;

    @JsonProperty("limit_players")
    @Column(name = "limit_players")
    private Integer limitPlayers;

    @JsonProperty("id_author")
    @Column(name = "id_author", nullable = false)
    private Integer authorId;

    @JsonProperty("creation_date")
    @Column(name = "creation_date")
    private LocalDateTime creationDate;

    @JsonProperty("description")
    @Column(name = "description")
    private String description;

    @JsonProperty("photo_url")
    @Column(name = "photo_url")
    private String photoUrl;

    @ManyToOne
    private Step firstStep;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "scenario_id")
    private List<Step> steps;

    // Constructors, getters, and setters
    public Scenario() {
    }

    public Scenario(Integer id, String title, Step firstStep) {
        this.id = id;
        this.title = title;
        this.firstStep = firstStep;
    }

    public Scenario(Integer id, String title, Integer limitPlayers, Integer authorId, Step firstStep) {
        this.id = id;
        this.title = title;
        this.limitPlayers = limitPlayers;
        this.authorId = authorId;
        this.firstStep = firstStep;
    }

    public Scenario(Integer id, String title, Integer limitPlayers, Integer authorId, LocalDateTime creationDate, String description, String photoUrl, Step firstStep) {
        this.id = id;
        this.title = title;
        this.limitPlayers = limitPlayers;
        this.authorId = authorId;
        this.creationDate = creationDate;
        this.description = description;
        this.photoUrl = photoUrl;
        this.firstStep = firstStep;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getLimitPlayers() {
        return limitPlayers;
    }

    public void setLimitPlayers(Integer limitPlayers) {
        this.limitPlayers = limitPlayers;
    }

    public Integer getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Integer authorId) {
        this.authorId = authorId;
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

    public Step getFirstStep() {
        return firstStep;
    }

    public void setFirstStep(Step firstStep) {
        this.firstStep = firstStep;
    }

    public List<Step> getSteps() {
        return steps;
    }

    public void setSteps(List<Step> steps) {
        this.steps = steps;
    }
}