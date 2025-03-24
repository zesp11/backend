package adventure.go.goadventure.session;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.sql.Date;
import java.sql.Timestamp;

public class Session {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_ses;
    private Integer id_user;
    private Integer id_game;
    private Integer current_step;
    private Timestamp start_date;
    private Timestamp end_date;
    private Integer id_choice; // Dodane pole

    public Session() {
    }

    public Session(Integer id_ses, Integer id_user, Integer id_game, Integer current_step, Timestamp start_date, Timestamp end_date, Integer id_choice) {
        this.id_ses = id_ses;
        this.id_user = id_user;
        this.id_game = id_game;
        this.current_step = current_step;
        this.start_date = start_date;
        this.end_date = end_date;
        this.id_choice = id_choice;
    }

    // Gettery i Settery
    public Integer getId_ses() {
        return id_ses;
    }

    public void setId_ses(Integer id_ses) {
        this.id_ses = id_ses;
    }

    public Integer getId_user() {
        return id_user;
    }

    public void setId_user(Integer id_user) {
        this.id_user = id_user;
    }

    public Integer getId_game() {
        return id_game;
    }

    public void setId_game(Integer id_game) {
        this.id_game = id_game;
    }

    public Integer getCurrent_step() {
        return current_step;
    }

    public void setCurrent_step(Integer current_step) {
        this.current_step = current_step;
    }

    public Timestamp getStart_date() {
        return start_date;
    }

    public void setStart_date(Timestamp start_date) {
        this.start_date = start_date;
    }

    public Timestamp getEnd_date() {
        return end_date;
    }

    public void setEnd_date(Timestamp end_date) {
        this.end_date = end_date;
    }

    public Integer getId_choice() {
        return id_choice;
    }

    public void setId_choice(Integer id_choice) {
        this.id_choice = id_choice;
    }
}