
package adventure.go.goadventure.session;

import adventure.go.goadventure.auth.AuthService;
import adventure.go.goadventure.step.Step;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class SessionRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final AuthService authService;
    private static final Logger log = LoggerFactory.getLogger(SessionRepository.class);


    public SessionRepository(NamedParameterJdbcTemplate jdbcTemplate, AuthService authService) {
        this.jdbcTemplate = jdbcTemplate;
        this.authService = authService;
    }

    // Method findAll - Retrieves all sessions
    public List<Session> findAll() {
        String sql = "SELECT * FROM public.\"Session\"";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Session session = new Session();
            session.setId_ses(rs.getInt("id_ses"));
            session.setId_user(rs.getInt("id_user"));
            session.setId_game(rs.getInt("id_game"));
            session.setCurrent_step(rs.getInt("current_step"));
            session.setStart_date(rs.getTimestamp("start_date"));
            session.setEnd_date(rs.getTimestamp("end_date"));
            return session;
        });
    }

    // Method findById - Retrieves a session by id_ses
    public Optional<Session> findById(Integer id) {
        String sql = "SELECT * FROM public.\"Session\" WHERE id_ses = :id";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("id", id);

        List<Session> sessions = jdbcTemplate.query(sql, params, (rs, rowNum) -> {
            Session session = new Session();
            session.setId_ses(rs.getInt("id_ses"));
            session.setId_user(rs.getInt("id_user"));
            session.setId_game(rs.getInt("id_game"));
            session.setCurrent_step(rs.getInt("current_step"));
            session.setStart_date(rs.getTimestamp("start_date"));
            session.setEnd_date(rs.getTimestamp("end_date"));
            return session;
        });
        return sessions.isEmpty() ? Optional.empty() : Optional.of(sessions.get(0));
    }

    // Method create - Creates a new session
    public void create(Session session) {
        String sql = "INSERT INTO public.\"Session\" (id_user, id_game, current_step, start_date) " +
                "VALUES (:id_user, :id_game, :current_step, :start_date)";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id_user", session.getId_user())
                .addValue("id_game", session.getId_game())
                .addValue("current_step", session.getCurrent_step())
                .addValue("start_date", session.getStart_date());

        jdbcTemplate.update(sql, params);
    }

    // Method update - Updates a session by id_ses
    public void update(Session session, Integer id) {
        String sql = "UPDATE public.\"Session\" " +
                "SET id_user = :id_user, id_game = :id_game, current_step = :current_step, start_date = :start_date, end_date = :end_date, id_choice = :id_choice " +
                "WHERE id_ses = :id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id_user", session.getId_user())
                .addValue("id_game", session.getId_game())
                .addValue("current_step", session.getCurrent_step())
                .addValue("start_date", session.getStart_date())
                .addValue("end_date", session.getEnd_date())
                .addValue("id_choice", session.getId_choice()) // Dodane pole
                .addValue("id", id);

        jdbcTemplate.update(sql, params);
    }

    // Method delete - Deletes a session by id_ses
    public void delete(Integer id) {
        String sql = "DELETE FROM public.\"Session\" WHERE id_ses = :id";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("id", id);
        jdbcTemplate.update(sql, params);
    }

    public Integer findLastInsertedId() {
        String sql = "SELECT currval(pg_get_serial_sequence('public.\"Session\"', 'id_ses'))";
        return jdbcTemplate.queryForObject(sql, new MapSqlParameterSource(), Integer.class);
    }

    public Optional<Object> findLatestByGameId(Integer gameId, Integer userId) {



        String sql = "SELECT * FROM public.\"Session\" WHERE id_game = :gameId AND id_user= :userId ORDER BY start_date DESC LIMIT 1";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("gameId", gameId)
                .addValue("userId", userId);

        List<Session> sessions = jdbcTemplate.query(sql, params, (rs, rowNum) -> {
            Session session = new Session();
            session.setId_ses(rs.getInt("id_ses"));
            session.setId_user(rs.getInt("id_user"));
            session.setId_game(rs.getInt("id_game"));
            session.setCurrent_step(rs.getInt("current_step"));
            session.setStart_date(rs.getTimestamp("start_date"));
            session.setEnd_date(rs.getTimestamp("end_date"));
            session.setId_choice(rs.getInt("id_choice"));
            return session;
        });

        if (sessions.isEmpty()) {
            return Optional.empty();
        }



        Session latestSession = sessions.get(0);
        if (latestSession.getEnd_date() != null) {
            // Fetch the step object
            String stepSql = "SELECT * FROM public.\"Step\" WHERE id_step = :currentStepId";
            MapSqlParameterSource stepParams = new MapSqlParameterSource().addValue("currentStepId", latestSession.getCurrent_step());
            Step step = jdbcTemplate.queryForObject(stepSql, stepParams, (rs, rowNum) -> {
                Step s = new Step();
                s.setId_step(rs.getInt("id_step"));
                s.setTitle(rs.getString("title"));
                s.setText(rs.getString("text"));
                s.setLongitude(rs.getDouble("longitude"));
                s.setLatitude(rs.getDouble("latitude"));
                s.setPhotoUrl(rs.getString("photo_url"));
                s.setChoices(Collections.emptyList());
                return s;
            });

            // Log the step object
            log.info("Step object with empty choices: {}", step);

            return Optional.of(step);
        }

        return Optional.of(latestSession);
    }

    public List<Session> findByGameId(Integer gameId) {
        String sql = "SELECT * FROM public.\"Session\" WHERE id_game = :gameId";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("gameId", gameId);

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> {
            Session session = new Session();
            session.setId_ses(rs.getInt("id_ses"));
            session.setId_user(rs.getInt("id_user"));
            session.setId_game(rs.getInt("id_game"));
            session.setCurrent_step(rs.getInt("current_step"));
            session.setStart_date(rs.getTimestamp("start_date"));
            session.setEnd_date(rs.getTimestamp("end_date"));
            return session;
        });
    }

    // SessionRepository.java
    public Optional<Integer> findPreviousChoiceId(Integer currentSessionId, Integer gameId, Integer userId) {
        String sql = "SELECT id_choice FROM public.\"Session\" " +
                "WHERE id_game = :gameId AND id_user = :userId AND id_ses < :currentSessionId " +
                "ORDER BY id_ses DESC LIMIT 1";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("gameId", gameId)
                .addValue("userId", userId)
                .addValue("currentSessionId", currentSessionId);

        List<Integer> result = jdbcTemplate.query(sql, params, (rs, rowNum) -> rs.getInt("id_choice"));
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }
    public Optional<Session> findLatestByGameIdAndUserId(Integer gameId, Integer userId) {
        String sql = "SELECT * FROM public.\"Session\" WHERE id_game = :gameId AND id_user = :userId ORDER BY id_ses DESC LIMIT 1";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("gameId", gameId)
                .addValue("userId", userId);

        List<Session> sessions = jdbcTemplate.query(sql, params, (rs, rowNum) -> {
            Session session = new Session();
            session.setId_ses(rs.getInt("id_ses"));
            session.setId_user(rs.getInt("id_user"));
            session.setId_game(rs.getInt("id_game"));
            session.setCurrent_step(rs.getInt("current_step"));
            session.setStart_date(rs.getTimestamp("start_date"));
            session.setEnd_date(rs.getTimestamp("end_date"));
            session.setId_choice(rs.getInt("id_choice"));
            return session;
        });

        if (sessions.isEmpty()) {
            return Optional.empty();
        }

        Session latestSession = sessions.get(0);
        if (latestSession.getEnd_date() != null) {
            // Fetch the step object
            String stepSql = "SELECT * FROM public.\"Step\" WHERE id_step = :currentStepId";
            MapSqlParameterSource stepParams = new MapSqlParameterSource().addValue("currentStepId", latestSession.getCurrent_step());
                Step step = jdbcTemplate.queryForObject(stepSql, stepParams, (rs, rowNum) -> {
                Step s = new Step();
                s.setId_step(rs.getInt("id_step"));
                s.setTitle(rs.getString("title"));
                s.setText(rs.getString("text"));
                s.setLongitude(rs.getDouble("longitude"));
                s.setLatitude(rs.getDouble("latitude"));
                s.setPhotoUrl(rs.getString("photo_url"));
                s.setChoices(Collections.emptyList());
                return s;
            });

            // Log the step object
            log.info("Step object with empty choices: {}", step);

            //return Optional.of(step);
        }

        return Optional.of(latestSession);
    }
}
