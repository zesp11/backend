package adventure.go.goadventure.scenario;

import adventure.go.goadventure.choice.Choice;
import adventure.go.goadventure.dto.CreateScenarioDTO;
import adventure.go.goadventure.step.Step;
import adventure.go.goadventure.user.User;
import adventure.go.goadventure.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class ScenarioRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private static final Logger log = LoggerFactory.getLogger(ScenarioRepository.class);
    private final UserRepository userRepository;

    public ScenarioRepository(NamedParameterJdbcTemplate jdbcTemplate, UserRepository userRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.userRepository = userRepository;
    }

    public List<Scenario> findAll() {
        String sql = "SELECT s.id_scen AS id, s.name AS title, s.limit_players, s.id_author, s.creation_date, s.description, s.photo_url, st.id_step, st.title AS step_title, st.text AS step_text, st.longitude, st.latitude " +
                "FROM public.\"Scenario\" s " +
                "LEFT JOIN public.\"Step\" st ON s.id_first_step = st.id_step";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Step firstStep = new Step(
                    rs.getInt("id_step"),
                    rs.getString("step_title"),
                    rs.getString("step_text"),
                    rs.getObject("longitude", Double.class),
                    rs.getObject("latitude", Double.class),
                    rs.getString("photo_url"),
                    new ArrayList<>()
            );

            return new Scenario(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getInt("limit_players"),
                    rs.getInt("id_author"),
                    rs.getObject("creation_date", LocalDateTime.class),
                    rs.getString("description"),
                    rs.getString("photo_url"),
                    firstStep
            );
        });
    }

    // ScenarioRepository.java
    public Optional<Scenario> findById(Integer id) {
        String sql = "SELECT s.id_scen AS id, s.name AS title, s.limit_players, s.id_author, s.creation_date, s.description, s.photo_url, st.id_step, st.title AS step_title, st.text AS step_text, st.longitude, st.latitude " +
                "FROM public.\"Scenario\" s " +
                "LEFT JOIN public.\"Step\" st ON s.id_first_step = st.id_step " +
                "WHERE s.id_scen = :id";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("id", id);

        List<Scenario> scenarios = jdbcTemplate.query(sql, params, (rs, rowNum) -> {
            Step firstStep = null;
            if (rs.getInt("id_step") != 0) {
                firstStep = new Step(
                        rs.getInt("id_step"),
                        rs.getString("step_title"),
                        rs.getString("step_text"),
                        rs.getObject("longitude", Double.class),
                        rs.getObject("latitude", Double.class),
                        rs.getString("photo_url"),
                        new ArrayList<>()
                );
            }

            return new Scenario(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getInt("limit_players"),
                    rs.getInt("id_author"),
                    rs.getObject("creation_date", LocalDateTime.class),
                    rs.getString("description"),
                    rs.getString("photo_url"),
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
                    rs.getString("photo_url"),
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
        String sql = "SELECT s.id_scen AS id, s.name AS title, s.limit_players, s.id_author, s.creation_date, s.description, s.photo_url, st.id_step, st.title AS step_title, st.text AS step_text, st.longitude, st.latitude " +
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
                    rs.getString("photo_url"),
                    new ArrayList<>()
            );

            return new Scenario(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getInt("limit_players"),
                    rs.getInt("id_author"),
                    rs.getObject("creation_date", LocalDateTime.class),
                    rs.getString("description"),
                    rs.getString("photo_url"),
                    firstStep
            );
        });
    }

    // src/main/java/adventure/go/goadventure/scenario/ScenarioRepository.java
    public Optional<Map<String, Object>> findByIdWithFirstStep(Integer id) {
        String sql = "SELECT s.id_scen AS id, s.name AS title, s.limit_players, s.id_author, s.creation_date, s.description, s.photo_url, " +
                "st.id_step AS step_id, st.title AS step_title, st.text AS step_text, st.longitude, st.latitude, st.photo_url AS step_photo_url, " +
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
            response.put("creation_date", rs.getObject("creation_date", LocalDateTime.class));
            response.put("description", rs.getString("description"));
            response.put("photo_url", rs.getString("photo_url"));

            Map<String, Object> firstStep = new HashMap<>();
            firstStep.put("id_step", rs.getInt("step_id"));
            firstStep.put("title", rs.getString("step_title"));
            firstStep.put("text", rs.getString("step_text"));
            firstStep.put("longitude", rs.getObject("longitude", Double.class));
            firstStep.put("latitude", rs.getObject("latitude", Double.class));
            firstStep.put("photo_url", rs.getString("step_photo_url"));

            List<Map<String, Object>> choices = new ArrayList<>();
            do {
                Integer choiceId = rs.getInt("id_choice");
                if (choiceId != null && choiceId != 0) {
                    Map<String, Object> choice = new HashMap<>();
                    choice.put("id_choice", choiceId);
                    choice.put("choice_text", rs.getString("choice_text"));
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
        String sql = "INSERT INTO public.\"Scenario\" (name, limit_players, id_author, creation_date, description, photo_url) " +
                "VALUES (:name, :limit_players, :id_author, :creation_date, :description, :photo_url) RETURNING id_scen";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", scenario.getTitle())
                .addValue("limit_players", scenario.getLimitPlayers())
                .addValue("id_author", scenario.getAuthorId())
                .addValue("creation_date", scenario.getCreationDate())
                .addValue("description", scenario.getDescription())
                .addValue("photo_url", scenario.getPhotoUrl());

        Integer id = jdbcTemplate.queryForObject(sql, params, Integer.class);
        scenario.setId(id);
    }

    public void update(Scenario scenario) {
        String sql = "UPDATE public.\"Scenario\" " +
                "SET name = :name, limit_players = :limit_players, id_first_step = :id_first_step, creation_date = :creation_date, description = :description, photo_url = :photo_url " +
                "WHERE id_scen = :id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", scenario.getTitle())
                .addValue("limit_players", scenario.getLimitPlayers())
                .addValue("id_first_step", scenario.getFirstStep() != null ? scenario.getFirstStep().getId_step() : null)
                .addValue("creation_date", scenario.getCreationDate())
                .addValue("description", scenario.getDescription())
                .addValue("photo_url", scenario.getPhotoUrl())
                .addValue("id", scenario.getId());

        int updated = jdbcTemplate.update(sql, params);
        Assert.state(updated == 1, "Scenario not found");
    }

    public void updateFirstStep(Integer scenarioId, Integer firstStepId) {
        String sql = "UPDATE public.\"Scenario\" " +
                "SET id_first_step = :id_first_step " +
                "WHERE id_scen = :id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id_first_step", firstStepId)
                .addValue("id", scenarioId);

        // Log the parameters
        log.debug("Updating first step with params: scenarioId={}, firstStepId={}", scenarioId, firstStepId);

        int updated = jdbcTemplate.update(sql, params);
        Assert.state(updated == 1, "Scenario not found");
    }

    public List<Scenario> findByUserId(Integer userId) {
        String sql = "SELECT * FROM public.\"Scenario\" WHERE id_author = :userId";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("userId", userId);

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> new Scenario(
                rs.getInt("id_scen"),
                rs.getString("name"),
                rs.getInt("limit_players"),
                rs.getInt("id_author"),
                rs.getObject("creation_date", LocalDateTime.class),
                rs.getString("description"),
                rs.getString("photo_url"),
                null // First step is not needed here
        ));
    }

    public List<Scenario> findByNameContaining(String name) {
        String sql = "SELECT s.id_scen AS id, s.name AS title, st.id_step, st.title AS step_title, st.text AS step_text, st.longitude, st.latitude, st.photo_url, s.id_author, s.creation_date, s.description, s.limit_players " +
                "FROM public.\"Scenario\" s " +
                "LEFT JOIN public.\"Step\" st ON s.id_first_step = st.id_step " +
                "WHERE s.name LIKE :name";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("name", "%" + name + "%");

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> {
            Integer authorId = rs.getInt("id_author");
            log.debug("Author found: {}", authorId);
            Optional<User> author = userRepository.findById(authorId);
            if (author.isEmpty()) {
                return null; // Skip scenarios with non-existing authors
            }

            Step firstStep = new Step(
                    rs.getInt("id_step"),
                    rs.getString("step_title"),
                    rs.getString("step_text"),
                    rs.getObject("longitude", Double.class),
                    rs.getObject("latitude", Double.class),
                    rs.getString("photo_url"),
                    new ArrayList<>()
            );

            return new Scenario(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getInt("limit_players"),
                    authorId,
                    rs.getObject("creation_date", LocalDateTime.class),
                    rs.getString("description"),
                    rs.getString("photo_url"),
                    firstStep
            );

        }).stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    public void deleteAllRelatedEntities(Integer scenarioId) {
        // Retrieve the first step ID for the scenario
        String getFirstStepSql = "SELECT id_first_step FROM public.\"Scenario\" WHERE id_scen = :scenarioId";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("scenarioId", scenarioId);
        Integer firstStepId = jdbcTemplate.queryForObject(getFirstStepSql, params, Integer.class);

        if (firstStepId != null) {
            // Collect all step IDs starting from the first step
            Set<Integer> stepIds = new HashSet<>();
            collectStepIds(firstStepId, stepIds);

            // Update the scenario to remove the reference to the first step
            String updateScenarioSql = "UPDATE public.\"Scenario\" SET id_first_step = NULL WHERE id_scen = :scenarioId";
            jdbcTemplate.update(updateScenarioSql, params);

            // Delete all step_choices related to the collected steps
            String deleteStepChoicesSql = "DELETE FROM public.\"Step_Choices\" WHERE id_step IN (:stepIds)";
            params = new MapSqlParameterSource().addValue("stepIds", stepIds);
            jdbcTemplate.update(deleteStepChoicesSql, params);

            // Delete all choices related to the collected steps
            String deleteChoicesSql = "DELETE FROM public.\"Choice\" WHERE id_choice IN (" +
                    "SELECT sc.id_choice FROM public.\"Step_Choices\" sc " +
                    "WHERE sc.id_step IN (:stepIds))";
            jdbcTemplate.update(deleteChoicesSql, params);

            // Delete all collected steps
            String deleteStepsSql = "DELETE FROM public.\"Step\" WHERE id_step IN (:stepIds)";
            jdbcTemplate.update(deleteStepsSql, params);
        }
    }

    private void collectStepIds(Integer stepId, Set<Integer> stepIds) {
        if (stepIds.contains(stepId)) {
            return;
        }
        stepIds.add(stepId);

        // Retrieve the next step IDs from choices
        String getNextStepIdsSql = "SELECT id_next_step FROM public.\"Choice\" WHERE id_choice IN (" +
                "SELECT id_choice FROM public.\"Step_Choices\" WHERE id_step = :stepId)";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("stepId", stepId);
        Set<Integer> nextStepIds = new HashSet<>(jdbcTemplate.queryForList(getNextStepIdsSql, params, Integer.class));

        for (Integer nextStepId : nextStepIds) {
            if (nextStepId != null) {
                collectStepIds(nextStepId, stepIds);
            }
        }
    }
}