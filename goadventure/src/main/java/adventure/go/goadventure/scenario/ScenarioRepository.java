package adventure.go.goadventure.scenario;

import adventure.go.goadventure.choice.Choice;
import adventure.go.goadventure.dto.CreateScenarioDTO;
import adventure.go.goadventure.step.Step;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
public class ScenarioRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ScenarioRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Scenario> findAll() {
        String sql = "SELECT s.id_scen AS id, s.name AS title, st.id_step, st.title AS step_title, st.text AS step_text, st.longitude, st.latitude " +
                "FROM public.\"Scenario\" s " +
                "LEFT JOIN public.\"Step\" st ON s.id_first_step = st.id_step";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Step firstStep = new Step(
                    rs.getInt("id_step"),
                    rs.getString("step_title"),
                    rs.getString("step_text"),
                    rs.getObject("longitude", Double.class),
                    rs.getObject("latitude", Double.class),
                    new ArrayList<>()
            );

            return new Scenario(
                    rs.getInt("id"),
                    rs.getString("title"),
                    firstStep
            );
        });
    }

    public Optional<Scenario> findById(Integer id) {
        String sql = "SELECT s.id_scen AS id, s.name AS title, s.limit_players, s.id_author, st.id_step, st.title AS step_title, st.text AS step_text, st.longitude, st.latitude " +
                "FROM public.\"Scenario\" s " +
                "LEFT JOIN public.\"Step\" st ON s.id_first_step = st.id_step " +
                "WHERE s.id_scen = :id";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("id", id);

        List<Scenario> scenarios = jdbcTemplate.query(sql, params, (rs, rowNum) -> {
            Step firstStep = new Step(
                    rs.getInt("id_step"),
                    rs.getString("step_title"),
                    rs.getString("step_text"),
                    rs.getObject("longitude", Double.class),
                    rs.getObject("latitude", Double.class),
                    new ArrayList<>()
            );

            return new Scenario(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getInt("limit_players"),
                    rs.getInt("id_author"),
                    firstStep
            );
        });
        return scenarios.isEmpty() ? Optional.empty() : Optional.of(scenarios.get(0));
    }

    public Optional<Scenario> findByName(String name) {
        String sql = "SELECT s.id_scen AS id, s.name AS title, st.id_step, st.title AS step_title, st.text AS step_text, st.longitude, st.latitude " +
                "FROM public.\"Scenario\" s " +
                "LEFT JOIN public.\"Step\" st ON s.id_first_step = st.id_step " +
                "WHERE s.name = :name";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("name", name);

        List<Scenario> scenarios = jdbcTemplate.query(sql, params, (rs, rowNum) -> {
            Step firstStep = new Step(
                    rs.getInt("id_step"),
                    rs.getString("step_title"),
                    rs.getString("step_text"),
                    rs.getObject("longitude", Double.class),
                    rs.getObject("latitude", Double.class),
                    new ArrayList<>()
            );

            return new Scenario(
                    rs.getInt("id"),
                    rs.getString("title"),
                    firstStep
            );
        });
        return scenarios.isEmpty() ? Optional.empty() : Optional.of(scenarios.get(0));
    }

    public void delete(Integer id) {
        String sql = "DELETE FROM public.\"Scenario\" WHERE id_scen = :id";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("id", id);

        int deleted = jdbcTemplate.update(sql, params);
        Assert.state(deleted == 1, "Scenario not found");
    }

    public long count() {
        String sql = "SELECT COUNT(*) FROM public.\"Scenario\"";
        return jdbcTemplate.queryForObject(sql, new MapSqlParameterSource(), Long.class);
    }

    public List<Scenario> findAllWithPagination(int offset, int limit) {
        String sql = "SELECT s.id_scen AS id, s.name AS title, st.id_step, st.title AS step_title, st.text AS step_text, st.longitude, st.latitude " +
                "FROM public.\"Scenario\" s " +
                "LEFT JOIN public.\"Step\" st ON s.id_first_step = st.id_step " +
                "LIMIT :limit OFFSET :offset";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("limit", limit)
                .addValue("offset", offset);

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> {
            Step firstStep = new Step(
                    rs.getInt("id_step"),
                    rs.getString("step_title"),
                    rs.getString("step_text"),
                    rs.getObject("longitude", Double.class),
                    rs.getObject("latitude", Double.class),
                    new ArrayList<>()
            );

            return new Scenario(
                    rs.getInt("id"),
                    rs.getString("title"),
                    firstStep
            );
        });
    }

    public Optional<Map<String, Object>> findByIdWithFirstStep(Integer id) {
        String sql = "SELECT s.id_scen AS id, s.name AS title, s.limit_players, s.id_author, st.id_step AS step_id, st.title AS step_title, st.text AS step_text, st.longitude, st.latitude, " +
                "c.id_choice, c.text AS choice_text, c.id_next_step " +
                "FROM public.\"Scenario\" s " +
                "LEFT JOIN public.\"Step\" st ON s.id_first_step = st.id_step " +
                "LEFT JOIN public.\"Step_Choices\" sc ON st.id_step = sc.id_step " +
                "LEFT JOIN public.\"Choice\" c ON sc.id_choice = c.id_choice " +
                "WHERE s.id_scen = :id";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("id", id);

        return jdbcTemplate.query(sql, params, rs -> {
            if (!rs.next()) {
                return Optional.empty();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("id", rs.getInt("id"));
            response.put("name", rs.getString("title"));
            response.put("limit_players", rs.getInt("limit_players"));
            response.put("id_author", rs.getInt("id_author"));

            Map<String, Object> firstStep = new HashMap<>();
            firstStep.put("id_step", rs.getInt("step_id"));
            firstStep.put("title", rs.getString("step_title"));
            firstStep.put("text", rs.getString("step_text"));
            firstStep.put("longitude", rs.getObject("longitude", Double.class));
            firstStep.put("latitude", rs.getObject("latitude", Double.class));

            List<Map<String, Object>> choices = new ArrayList<>();
            do {
                Integer choiceId = rs.getInt("id_choice");
                if (choiceId != null && choiceId != 0) {
                    Map<String, Object> choice = new HashMap<>();
                    choice.put("id_choice", choiceId);
                    choice.put("text", rs.getString("choice_text"));
                    choice.put("id_next_step", rs.getInt("id_next_step"));
                    choices.add(choice);
                }
            } while (rs.next());

            firstStep.put("choices", choices);
            response.put("first_step", firstStep);

            return Optional.of(response);
        });
    }

    public void create(Scenario scenario) {
        String sql = "INSERT INTO public.\"Scenario\" (name, limit_players, id_author) VALUES (:name, :limit_players, :id_author) RETURNING id_scen";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", scenario.getTitle())
                .addValue("limit_players", scenario.getLimitPlayers())
                .addValue("id_author", scenario.getAuthorId());

        Integer id = jdbcTemplate.queryForObject(sql, params, Integer.class);
        scenario.setId(id);
    }

    public void update(Scenario scenario) {
        String sql = "UPDATE public.\"Scenario\" SET name = :name, limit_players = :limit_players WHERE id_scen = :id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", scenario.getTitle())
                .addValue("limit_players", scenario.getLimitPlayers())
                .addValue("id", scenario.getId());

        int updated = jdbcTemplate.update(sql, params);
        Assert.state(updated == 1, "Scenario not found");
    }


}