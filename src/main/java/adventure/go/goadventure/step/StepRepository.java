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
                                rs.getString("photo_url"),
                                new ArrayList<Choice>()
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
        String sql = "SELECT st.*, c.id_choice, c.text AS choice_text, c.id_next_step, st.photo_url " +
                "FROM public.\"Step\" st " +
                "LEFT JOIN public.\"Step_Choices\" sc ON st.id_step = sc.id_step " +
                "LEFT JOIN public.\"Choice\" c ON sc.id_choice = c.id_choice " +
                "WHERE st.id_step = :id";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("id", id);

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
                            rs.getString("photo_url"),
                            choices
                    );
                }

                Integer choiceId = rs.getInt("id_choice");
                if (choiceId != null && choiceId != 0) {
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


    public void create(Step step) {
        String sql = "INSERT INTO public.\"Step\" (title, text, longitude, latitude, photo_url) " +
                "VALUES (:title, :text, :longitude, :latitude, :photo_url) RETURNING id_step";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("title", step.getTitle())
                .addValue("text", step.getText())
                .addValue("longitude", step.getLongitude())
                .addValue("latitude", step.getLatitude())
                .addValue("photo_url", step.getPhotoUrl());

        Integer generatedId = jdbcTemplate.queryForObject(sql, params, Integer.class);
        step.setId_step(generatedId);
    }

    // Method update - Updates step by id_step
    // StepRepository.java
    public void update(Step step, Integer id) {
        String sql = "UPDATE public.\"Step\" " +
                "SET title = :title, text = :text, longitude = :longitude, latitude = :latitude, photo_url = :photo_url " +
                "WHERE id_step = :id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("title", step.getTitle())
                .addValue("text", step.getText())
                .addValue("longitude", step.getLongitude())
                .addValue("latitude", step.getLatitude())
                .addValue("photo_url", step.getPhotoUrl())
                .addValue("id", id);

        int updated = jdbcTemplate.update(sql, params);
        Assert.state(updated == 1, "Step not found");
    }

    // StepRepository.java
    public void deleteAllRelatedEntities(Integer stepId) {
        // Update step_choices to remove references to the step
        String updateStepChoicesSql = "UPDATE public.\"Step_Choices\" SET id_step = -1 WHERE id_step = :stepId";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("stepId", stepId);
        jdbcTemplate.update(updateStepChoicesSql, params);

        // Update choices to set id_next_step to -1 where it references the step
        String updateChoicesSql = "UPDATE public.\"Choice\" SET id_next_step = -1 WHERE id_next_step = :stepId";
        jdbcTemplate.update(updateChoicesSql, params);

        // Delete the step
        String deleteStepSql = "DELETE FROM public.\"Step\" WHERE id_step = :stepId";
        jdbcTemplate.update(deleteStepSql, params);
    }

    public void delete(Integer id) {
        String sql = "DELETE FROM public.\"Step\" WHERE id_step = :id";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("id", id);

        int deleted = jdbcTemplate.update(sql, params);
        //Assert.state(deleted == 1, "Step not found");
    }

    public Integer findPreviousStepId(Integer currentStepId) {
        String sql = "SELECT sc.id_step " +
                "FROM public.\"Choice\" c " +
                "JOIN public.\"Step_Choices\" sc ON c.id_choice = sc.id_choice " +
                "WHERE c.id_next_step = :currentStepId";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("currentStepId", currentStepId);

        List<Integer> stepIds = jdbcTemplate.queryForList(sql, params, Integer.class);
        return stepIds.isEmpty() ? null : stepIds.get(0);
    }

    public String findStepTextById(Integer stepId) {
        String sql = "SELECT text FROM public.\"Step\" WHERE id_step = :stepId";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("stepId", stepId);

        return jdbcTemplate.queryForObject(sql, params, String.class);
    }
}