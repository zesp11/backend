package adventure.go.goadventure.dto;

public class CreateScenarioDTO {
    private String name;
    private Integer limitPlayers;

    // Getters and Setters
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
}