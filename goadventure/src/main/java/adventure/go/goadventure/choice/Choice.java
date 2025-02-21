package adventure.go.goadventure.choice;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Choice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_choice;
    private String text;
    private Integer id_next_step;
    public Choice() {
    }
    public Choice(Integer id_choice, String text, Integer id_next_step) {
        this.id_choice = id_choice;
        this.text = text;
        this.id_next_step = id_next_step;
    }
    // Gettery i Settery

    public Integer getId_choice() {
        return id_choice;
    }

    public void setId_choice(Integer id_choice) {
        this.id_choice = id_choice;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getId_next_step() {
        return id_next_step;
    }

    public void setId_next_step(Integer id_next_step) {
        this.id_next_step = id_next_step;
    }
}
