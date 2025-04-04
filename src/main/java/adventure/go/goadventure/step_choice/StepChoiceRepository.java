package adventure.go.goadventure.step_choice;


import adventure.go.goadventure.choice.Choice;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

@Repository
public class StepChoiceRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public StepChoiceRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Metoda findAll - Pobiera wszystkie powiązania kroków i wyborów
    public List<StepChoice> findAll() {
        String sql = "SELECT * FROM public.\"Step_Choices\"";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new StepChoice(
                rs.getInt("id_step_choice"),
                rs.getInt("id_step"),
                rs.getInt("id_choice")
        ));
    }

    // Metoda findById - Pobiera powiązanie kroków i wyborów po id_step_choice
    public Optional<StepChoice> findById(Integer id) {
        String sql = "SELECT * FROM public.\"Step_Choices\" WHERE id_step_choice = :id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);

        List<StepChoice> stepChoices = jdbcTemplate.query(sql, params, (rs, rowNum) -> new StepChoice(
                rs.getInt("id_step_choice"),
                rs.getInt("id_step"),
                rs.getInt("id_choice")
        ));
        return stepChoices.isEmpty() ? Optional.empty() : Optional.of(stepChoices.get(0));
    }

    // Method create - Creates a new step choice
    public void create(StepChoice stepChoice) {
        String sql = "INSERT INTO public.\"Step_Choices\" (id_step, id_choice) " +
                "VALUES (:id_step, :id_choice) RETURNING id_step_choice";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id_step", stepChoice.getId_step())
                .addValue("id_choice", stepChoice.getId_choice());

        Integer generatedId = jdbcTemplate.queryForObject(sql, params, Integer.class);
        stepChoice.setId_step_choice(generatedId);
    }

    // Metoda update - Aktualizuje powiązanie kroków i wyborów po id_step_choice
    public void update(StepChoice stepChoice, Integer id) {
        String sql = "UPDATE public.\"Step_Choices\" " +
                "SET id_step = :id_step, id_choice = :id_choice " +
                "WHERE id_step_choice = :id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id_step", stepChoice.getId_step())
                .addValue("id_choice", stepChoice.getId_choice())
                .addValue("id", id);

        int updated = jdbcTemplate.update(sql, params);
        Assert.state(updated == 1, "StepChoice not found");
    }

    // Metoda delete - Usuwa powiązanie kroków i wyborów po id_step_choice
    public void delete(Integer id) {
        String sql = "DELETE FROM public.\"Step_Choices\" WHERE id_step_choice = :id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);

        int deleted = jdbcTemplate.update(sql, params);
        Assert.state(deleted == 1, "StepChoice not found");
    }

    // StepChoiceRepository.java
    public void deleteByChoiceId(Integer choiceId) {
        String sql = "DELETE FROM public.\"Step_Choices\" WHERE id_choice = :choiceId";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("choiceId", choiceId);

        jdbcTemplate.update(sql, params);
    }
}
