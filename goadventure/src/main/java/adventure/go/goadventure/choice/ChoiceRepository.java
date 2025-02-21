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

    // Metoda create - Tworzy nową opcję wyboru
    public void create(Choice choice) {
        String sql = "INSERT INTO public.\"Choice\" (id_choice, text, id_next_step) " +
                "VALUES (:id_choice, :text, :id_next_step)";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id_choice", choice.getId_choice())
                .addValue("text", choice.getText())
                .addValue("id_next_step", choice.getId_next_step());

        jdbcTemplate.update(sql, params);
    }

    // Metoda update - Aktualizuje opcję wyboru po id_choice
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
}
