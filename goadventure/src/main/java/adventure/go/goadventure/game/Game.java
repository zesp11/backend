package adventure.go.goadventure.game;

import java.sql.Timestamp;
import jakarta.persistence.*;

@Entity
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_game;
    private Integer id_scen;
    private Timestamp startTime;
    private Timestamp endTime;

    public Game() {
    }

    public Game(Integer id_game, Integer id_scen, Timestamp startTime, Timestamp endTime) {
        this.id_game = id_game;
        this.id_scen = id_scen;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getters and Setters
    public Integer getId_game() {
        return id_game;
    }

    public void setId_game(Integer id_game) {
        this.id_game = id_game;
    }

    public Integer getId_scen() {
        return id_scen;
    }

    public void setId_scen(Integer id_scen) {
        this.id_scen = id_scen;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }
}