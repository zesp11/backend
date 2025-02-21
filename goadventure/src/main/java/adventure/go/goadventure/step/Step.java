package adventure.go.goadventure.step;

import adventure.go.goadventure.choice.Choice;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "Step")
public class Step {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String title;
    private String text;
    private Double longitude;
    private Double latitude;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "step_id")
    private List<Choice> choices;

    public Step() {}

    public Step(Integer id, String title, String text, Double longitude, Double latitude, List<Choice> choices) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.longitude = longitude;
        this.latitude = latitude;
        this.choices = choices;
    }

    // Getters and setters
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public List<Choice> getChoices() {
        return choices;
    }

    public void setChoices(List<Choice> choices) {
        this.choices = choices;
    }
}