package adventure.go.goadventure.choice;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

@Repository
public class ChoiceRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ChoiceRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Metoda findAll - Pobiera wszystkie opcje wyboru
    public List<Choice> findAll() {
        String sql = "SELECT * FROM public.\"Choice\"";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Choice(
                rs.getInt("id_choice"),
                rs.getString("text"),
                rs.getInt("id_next_step")
        ));
    }

    // Metoda findById - Pobiera opcję wyboru po id_choice
    public Optional<Choice> findById(Integer id) {
        String sql = "SELECT * FROM public.\"Choice\" WHERE id_choice = :id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);

        List<Choice> choices = jdbcTemplate.query(sql, params, (rs, rowNum) -> new Choice(
                rs.getInt("id_choice"),
                rs.getString("text"),
                rs.getInt("id_next_step")
        ));
        return choices.isEmpty() ? Optional.empty() : Optional.of(choices.get(0));
    }

    // Method create - Creates a new choice and returns the generated id_choice
    public void create(Choice choice) {
        String sql = "INSERT INTO public.\"Choice\" (text, id_next_step) " +
                "VALUES (:text, :id_next_step) RETURNING id_choice";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("text", choice.getText())
                .addValue("id_next_step", choice.getId_next_step());

        Integer generatedId = jdbcTemplate.queryForObject(sql, params, Integer.class);
        choice.setId_choice(generatedId);
    }

    // ChoiceRepository.java
    public void update(Choice choice, Integer id) {
        String sql = "UPDATE public.\"Choice\" " +
                "SET text = :text, id_next_step = :id_next_step " +
                "WHERE id_choice = :id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("text", choice.getText())
                .addValue("id_next_step", choice.getId_next_step())
                .addValue("id", id);

        int updated = jdbcTemplate.update(sql, params);
        Assert.state(updated == 1, "Choice not found");
    }

    // Metoda delete - Usuwa opcję wyboru po id_choice
    public void delete(Integer id) {
        String sql = "DELETE FROM public.\"Choice\" WHERE id_choice = :id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);

        int deleted = jdbcTemplate.update(sql, params);
        Assert.state(deleted == 1, "Choice not found");
    }

    public String findChoiceTextById(Optional<Integer> choiceId) {
        if (choiceId.isEmpty()) {
            throw new IllegalArgumentException("Choice ID cannot be null or empty");
        }

        String sql = "SELECT text FROM public.\"Choice\" WHERE id_choice = :choiceId";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("choiceId", choiceId.get());

        return jdbcTemplate.queryForObject(sql, params, String.class);
    }
}
