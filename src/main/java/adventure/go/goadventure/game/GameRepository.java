package adventure.go.goadventure.game;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class GameRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public GameRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Game> findAll() {
        String sql = "SELECT g.id_game, g.id_scen, g.start_time, g.end_time " +
                "FROM public.\"Game\" g";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Game(
                rs.getInt("id_game"),
                rs.getInt("id_scen"),
                rs.getTimestamp("start_time"),
                rs.getTimestamp("end_time")
        ));
    }

    public Optional<Game> findById(Integer id) {
        String sql = "SELECT g.id_game, g.id_scen, g.start_time, g.end_time " +
                "FROM public.\"Game\" g " +
                "WHERE g.id_game = :id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);

        List<Game> games = jdbcTemplate.query(sql, params, (rs, rowNum) -> new Game(
                rs.getInt("id_game"),
                rs.getInt("id_scen"),
                rs.getTimestamp("start_time"),
                rs.getTimestamp("end_time")
        ));
        return games.isEmpty() ? Optional.empty() : Optional.of(games.get(0));
    }
    public List<Game> findActiveGamesByUserId(Integer userId) {
        String sql = "SELECT g.id_game, g.id_scen, g.start_time, g.end_time " +
                "FROM public.\"Game\" g " +
                "JOIN public.\"Session\" s ON g.id_game = s.id_game " +
                "WHERE s.id_user = :userId AND s.end_date IS NULL " +
                "AND s.id_ses = (SELECT MAX(id_ses) FROM public.\"Session\" WHERE id_game = g.id_game)";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("userId", userId);

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> new Game(
                rs.getInt("id_game"),
                rs.getInt("id_scen"),
                rs.getTimestamp("start_time"),
                rs.getTimestamp("end_time")
        ));
    }
    public void create(Game game) {
        String sql = "INSERT INTO public.\"Game\" (id_scen, start_time) VALUES (:id_scen, :start_time) RETURNING id_game";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id_scen", game.getId_scen())
                .addValue("start_time", game.getStartTime());

        Integer id = jdbcTemplate.queryForObject(sql, params, Integer.class);
        game.setId_game(id);
    }

    public void update(Game game, Integer id) {
        String sql = "UPDATE public.\"Game\" SET id_scen = :id_scen, start_time = :start_time, end_time = :end_time " +
                "WHERE id_game = :id AND end_time IS NULL";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id_scen", game.getId_scen())
                .addValue("start_time", game.getStartTime())
                .addValue("end_time", game.getEndTime())
                .addValue("id", id);

        int updated = jdbcTemplate.update(sql, params);
        Assert.state(updated == 1, "Game not found with id: " + id + " or end_time is not null");
    }

    public void delete(Integer id) {
        String sql = "DELETE FROM public.\"Game\" WHERE id_game = :id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);

        int deleted = jdbcTemplate.update(sql, params);
        Assert.state(deleted == 1, "Game not found with id: " + id);
    }

    public List<Game> findAllGamesByUserId(Integer userId) {
        String sql = "SELECT g.id_game, g.id_scen, g.start_time, g.end_time " +
                "FROM public.\"Game\" g " +
                "JOIN public.\"Session\" s ON g.id_game = s.id_game " +
                "WHERE s.id_user = :userId " +
                "AND s.id_ses = (SELECT MAX(id_ses) FROM public.\"Session\" WHERE id_game = g.id_game)";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("userId", userId);

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> new Game(
                rs.getInt("id_game"),
                rs.getInt("id_scen"),
                rs.getTimestamp("start_time"),
                rs.getTimestamp("end_time")
        ));
    }

    public List<Map<String, Object>> findActiveGamesWithDetailsByUserId(Integer userId) {
        String sql = "SELECT g.id_game, g.id_scen, g.start_time, g.end_time, s.name AS scenario_name, " +
                "(SELECT se.current_step FROM public.\"Session\" se WHERE se.id_game = g.id_game ORDER BY se.id_ses DESC LIMIT 1) AS current_step, " +
                "(SELECT st.text FROM public.\"Step\" st WHERE st.id_step = (SELECT se.current_step FROM public.\"Session\" se WHERE se.id_game = g.id_game ORDER BY se.id_ses DESC LIMIT 1)) AS current_step_text, " +
                "(SELECT MAX(se.start_date) FROM public.\"Session\" se WHERE se.id_game = g.id_game) AS last_change " +
                "FROM public.\"Game\" g " +
                "JOIN public.\"Scenario\" s ON g.id_scen = s.id_scen " +
                "JOIN public.\"Session\" se ON g.id_game = se.id_game " +
                "WHERE se.id_user = :userId AND se.end_date IS NULL " +
                "AND se.id_ses = (SELECT MAX(id_ses) FROM public.\"Session\" WHERE id_game = g.id_game) " +
                "ORDER BY g.start_time DESC";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("userId", userId);

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> {
            Map<String, Object> gameDetails = new HashMap<>();
            gameDetails.put("id_game", rs.getInt("id_game"));
            gameDetails.put("id_scen", rs.getInt("id_scen"));
            gameDetails.put("start_time", rs.getTimestamp("start_time"));
            gameDetails.put("end_time", rs.getTimestamp("end_time"));
            gameDetails.put("scenario_name", rs.getString("scenario_name"));
            gameDetails.put("current_step", rs.getInt("current_step"));
            gameDetails.put("current_step_text", rs.getString("current_step_text"));
            return gameDetails;
        });
    }

    public List<Map<String, Object>> findAllGamesWithDetailsByUserId(Integer userId) {
        String sql = "SELECT g.id_game, g.id_scen, g.start_time, g.end_time, s.name AS scenario_name, " +
                "(SELECT se.current_step FROM public.\"Session\" se WHERE se.id_game = g.id_game ORDER BY se.id_ses DESC LIMIT 1) AS current_step, " +
                "(SELECT st.text FROM public.\"Step\" st WHERE st.id_step = (SELECT se.current_step FROM public.\"Session\" se WHERE se.id_game = g.id_game ORDER BY se.id_ses DESC LIMIT 1)) AS current_step_text, " +
                "(SELECT MAX(se.start_date) FROM public.\"Session\" se WHERE se.id_game = g.id_game) AS last_change " +
                "FROM public.\"Game\" g " +
                "JOIN public.\"Scenario\" s ON g.id_scen = s.id_scen " +
                "JOIN public.\"Session\" se ON g.id_game = se.id_game " +
                "WHERE se.id_user = :userId " +
                "AND se.id_ses = (SELECT MAX(id_ses) FROM public.\"Session\" WHERE id_game = g.id_game) " +
                "ORDER BY g.start_time DESC";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("userId", userId);

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> {
            Map<String, Object> gameDetails = new HashMap<>();
            gameDetails.put("id_game", rs.getInt("id_game"));
            gameDetails.put("id_scen", rs.getInt("id_scen"));
            gameDetails.put("start_time", rs.getTimestamp("start_time"));
            gameDetails.put("end_time", rs.getTimestamp("end_time"));
            gameDetails.put("scenario_name", rs.getString("scenario_name"));
            gameDetails.put("current_step", rs.getInt("current_step"));
            gameDetails.put("current_step_text", rs.getString("current_step_text"));
            return gameDetails;
        });
    }

    public Optional<Map<String, Object>> findByIdWithScenarioName(Integer id) {
        String sql = "SELECT g.id_game, g.id_scen, g.start_time, g.end_time, s.name AS scenario_name, " +
                "(SELECT se.current_step FROM public.\"Session\" se WHERE se.id_game = g.id_game ORDER BY se.id_ses DESC LIMIT 1) AS current_step, " +
                "(SELECT st.text FROM public.\"Step\" st WHERE st.id_step = (SELECT se.current_step FROM public.\"Session\" se WHERE se.id_game = g.id_game ORDER BY se.id_ses DESC LIMIT 1)) AS current_step_text " +
                "FROM public.\"Game\" g " +
                "JOIN public.\"Scenario\" s ON g.id_scen = s.id_scen " +
                "WHERE g.id_game = :id";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("id", id);

        List<Map<String, Object>> games = jdbcTemplate.query(sql, params, (rs, rowNum) -> {
            Map<String, Object> gameData = new HashMap<>();
            gameData.put("id_game", rs.getInt("id_game"));
            gameData.put("id_scen", rs.getInt("id_scen"));
            gameData.put("start_time", rs.getTimestamp("start_time"));
            gameData.put("end_time", rs.getTimestamp("end_time"));
            gameData.put("scenario_name", rs.getString("scenario_name"));
            gameData.put("current_step", rs.getInt("current_step"));
            gameData.put("current_step_text", rs.getString("current_step_text"));
            return gameData;
        });

        return games.isEmpty() ? Optional.empty() : Optional.of(games.get(0));
    }

    public List<Map<String, Object>> findAllWithDetails() {
        String sql = "SELECT g.id_game, g.id_scen, g.start_time, g.end_time, s.name AS scenario_name, " +
                "(SELECT se.current_step FROM public.\"Session\" se WHERE se.id_game = g.id_game ORDER BY se.id_ses DESC LIMIT 1) AS current_step, " +
                "(SELECT st.text FROM public.\"Step\" st WHERE st.id_step = (SELECT se.current_step FROM public.\"Session\" se WHERE se.id_game = g.id_game ORDER BY se.id_ses DESC LIMIT 1)) AS current_step_text " +
                "FROM public.\"Game\" g " +
                "JOIN public.\"Scenario\" s ON g.id_scen = s.id_scen";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> gameData = new HashMap<>();
            gameData.put("id_game", rs.getInt("id_game"));
            gameData.put("id_scen", rs.getInt("id_scen"));
            gameData.put("start_time", rs.getTimestamp("start_time"));
            gameData.put("end_time", rs.getTimestamp("end_time"));
            gameData.put("scenario_name", rs.getString("scenario_name"));
            gameData.put("current_step", rs.getInt("current_step"));
            gameData.put("current_step_text", rs.getString("current_step_text"));
            return gameData;
        });
    }
}