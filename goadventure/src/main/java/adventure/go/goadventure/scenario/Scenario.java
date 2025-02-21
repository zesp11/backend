package adventure.go.goadventure.scenario;

import adventure.go.goadventure.step.Step;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

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

    @ManyToOne
    private Step firstStep;

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

    public Step getFirstStep() {
        return firstStep;
    }

    public void setFirstStep(Step firstStep) {
        this.firstStep = firstStep;
    }
}