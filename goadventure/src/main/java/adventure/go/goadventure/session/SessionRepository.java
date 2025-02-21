
package adventure.go.goadventure.session;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SessionRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public SessionRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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
            session.setStart_date(rs.getDate("start_date"));
            session.setEnd_date(rs.getDate("end_date"));
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
            session.setStart_date(rs.getDate("start_date"));
            session.setEnd_date(rs.getDate("end_date"));
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
}