package adventure.go.goadventure.step;

import adventure.go.goadventure.choice.Choice;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.sql.SQLException;
import java.util.*;

@Repository
public class StepRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public StepRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Method findAll - Retrieves all steps
    public List<Step> findAll() {
        String sql = "SELECT st.*, c.id_choice, c.text AS choice_text, c.id_next_step " +
                "FROM public.\"Step\" st " +
                "LEFT JOIN public.\"Step_Choices\" sc ON st.id_step = sc.id_step " +
                "LEFT JOIN public.\"Choice\" c ON sc.id_choice = c.id_choice";

        return jdbcTemplate.query(sql, rs -> {
            Map<Integer, Step> stepMap = new HashMap<>();

            while (rs.next()) {
                Integer stepId = rs.getInt("id_step");
                Step step = stepMap.computeIfAbsent(stepId, id -> {
                    try {
                        return new Step(
                                rs.getInt("id_step"),
                                rs.getString("title"),
                                rs.getString("text"),
                                rs.getObject("longitude", Double.class),
                                rs.getObject("latitude", Double.class),
                                new ArrayList<>()
                        );
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });

                Integer choiceId = rs.getInt("id_choice");
                if (choiceId != null) {
                    Choice choice = new Choice(
                            choiceId,
                            rs.getString("choice_text"),
                            rs.getInt("id_next_step")
                    );
                    step.getChoices().add(choice);
                }
            }

            return new ArrayList<>(stepMap.values());
        });
    }

    // Method findById - Retrieves step by id_step
    public Optional<Step> findById(Integer id) {
        String sql = "SELECT st.*, c.id_choice, c.text AS choice_text, c.id_next_step " +
                "FROM public.\"Step\" st " +
                "LEFT JOIN public.\"Choice\" c ON st.id_step = c.id_step " +
                "WHERE st.id_step = :id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);

        Step step = jdbcTemplate.query(sql, params, rs -> {
            Step resultStep = null;
            List<Choice> choices = new ArrayList<>();

            while (rs.next()) {
                if (resultStep == null) {
                    resultStep = new Step(
                            rs.getInt("id_step"),
                            rs.getString("title"),
                            rs.getString("text"),
                            rs.getObject("longitude", Double.class),
                            rs.getObject("latitude", Double.class),
                            choices
                    );
                }

                Integer choiceId = rs.getInt("id_choice");
                if (choiceId != null) {
                    Choice choice = new Choice(
                            choiceId,
                            rs.getString("choice_text"),
                            rs.getInt("id_next_step")
                    );
                    choices.add(choice);
                }
            }

            return resultStep;
        });

        return Optional.ofNullable(step);
    }

    // Method create - Creates a new step
    public void create(Step step) {
        String sql = "INSERT INTO public.\"Step\" (id_step, title, text, longitude, latitude) " +
                "VALUES (:id_step, :title, :text, :longitude, :latitude)";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id_step", step.getId())
                .addValue("title", step.getTitle())
                .addValue("text", step.getText())
                .addValue("longitude", step.getLongitude())
                .addValue("latitude", step.getLatitude());

        jdbcTemplate.update(sql, params);
    }

    // Method update - Updates step by id_step
    public void update(Step step, Integer id) {
        String sql = "UPDATE public.\"Step\" " +
                "SET title = :title, text = :text, longitude = :longitude, latitude = :latitude " +
                "WHERE id_step = :id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("title", step.getTitle())
                .addValue("text", step.getText())
                .addValue("longitude", step.getLongitude())
                .addValue("latitude", step.getLatitude())
                .addValue("id", id);

        int updated = jdbcTemplate.update(sql, params);
        Assert.state(updated == 1, "Step not found");
    }

    // Method delete - Deletes step by id_step
    public void delete(Integer id) {
        String sql = "DELETE FROM public.\"Step\" WHERE id_step = :id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);

        int deleted = jdbcTemplate.update(sql, params);
        Assert.state(deleted == 1, "Step not found");
    }
}