package adventure.go.goadventure.step;

import adventure.go.goadventure.choice.Choice;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Step")
public class Step {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id_step")
    private Integer id_step;

    private String title;
    private String text;
    private Double longitude;
    private Double latitude;
    private String photoUrl;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "step_id")
    private List<Choice> choices = new ArrayList<>();

    public Step() {}

    public Step(Integer id_step, String title, String text, Double longitude, Double latitude, String photoUrl, List<Choice> choices) {
        this.id_step = id_step;
        this.title = title;
        this.text = text;
        this.longitude = longitude;
        this.latitude = latitude;
        this.choices = choices;
        this.photoUrl = photoUrl;
    }

    // Getters and setters
    public Integer getId_step() {
        return id_step;
    }

    public void setId_step(Integer id) {
        this.id_step = id;
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

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}