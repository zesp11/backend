package adventure.go.goadventure.step_choice;

public class StepChoice {
    private Integer id_step_choice;
    private Integer id_step;
    private Integer id_choice;
    public StepChoice() {
    }
    public StepChoice(Integer id_step_choice, Integer id_step, Integer id_choice) {
        this.id_step_choice = id_step_choice;
        this.id_step = id_step;
        this.id_choice = id_choice;
    }
    // Gettery i Settery

    public Integer getId_step_choice() {
        return id_step_choice;
    }

    public void setId_step_choice(Integer id_step_choice) {
        this.id_step_choice = id_step_choice;
    }

    public Integer getId_step() {
        return id_step;
    }

    public void setId_step(Integer id_step) {
        this.id_step = id_step;
    }

    public Integer getId_choice() {
        return id_choice;
    }

    public void setId_choice(Integer id_choice) {
        this.id_choice = id_choice;
    }
}
