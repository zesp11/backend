package adventure.go.goadventure.user;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public UserRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    public List<User> findAll() {
        String sql = "SELECT użytkownik.* FROM public.\"User\" użytkownik";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new User(
                rs.getInt("id_user"),
                rs.getString("login"),
                rs.getString("email"),
                rs.getString("password")
        ));
    }

    public Optional<User> findById(Integer id) {
        String sql = "SELECT użytkownik.* FROM public.\"User\" użytkownik WHERE id_user = :id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);

        List<User> users = jdbcTemplate.query(sql, params, (rs, rowNum) -> new User(
                rs.getInt("id_user"),
                rs.getString("login"),
                rs.getString("email"),
                rs.getString("password")
        ));
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    public void create(User user) {
        String sql = "INSERT INTO public.\"User\" (login, email, password) " +
                "VALUES (:login, :email, :password)";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("login", user.getLogin())
                .addValue("email", user.getEmail())
                .addValue("password", user.getPassword());

        jdbcTemplate.update(sql, params);
    }

    public void update(User user, Integer id) {
        String sql = "UPDATE public.\"User\" użytkownik " +
                "SET login = :login, email = :email, password = :password " +
                "WHERE id_user = :id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("login", user.getLogin())
                .addValue("email", user.getEmail())
                .addValue("password", user.getPassword())
                .addValue("id", id);

        int updated = jdbcTemplate.update(sql, params);
        Assert.state(updated == 1, "User not found");
    }

    public void delete(Integer id) {
        String sql = "DELETE FROM public.\"User\" użytkownik WHERE id_user = :id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);

        int deleted = jdbcTemplate.update(sql, params);
        Assert.state(deleted == 1, "User not found");
    }

    public Optional<User> findByLogin(String login) {
        String sql = "SELECT użytkownik.* FROM public.\"User\" użytkownik WHERE użytkownik.login = :login";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("login", login);

        List<User> users = jdbcTemplate.query(sql, params, (rs, rowNum) -> new User(
                rs.getInt("id_user"),
                rs.getString("login"),
                rs.getString("email"),
                rs.getString("password")
        ));
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    public Optional<User> findByEmail(String email) {
        String sql = "SELECT użytkownik.* FROM public.\"User\" użytkownik WHERE użytkownik.email = :email";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("email", email);

        List<User> users = jdbcTemplate.query(sql, params, (rs, rowNum) -> new User(
                rs.getInt("id_user"),
                rs.getString("login"),
                rs.getString("email"),
                rs.getString("password")
        ));
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    public User save(User user) {
        String sql = "INSERT INTO public.\"User\" (login, email, password) " +
                "VALUES (:login, :email, :password) " +
                "RETURNING id_user";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("login", user.getLogin())
                .addValue("email", user.getEmail())
                .addValue("password", user.getPassword());

        Integer id = jdbcTemplate.queryForObject(sql, params, Integer.class);
        user.setId_user(id);
        return user;
    }

    public List<User> findAllWithPagination(int offset, int limit) {
        String sql = "SELECT użytkownik.* FROM public.\"User\" użytkownik " +
                "ORDER BY id_user " +
                "OFFSET :offset LIMIT :limit";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("offset", offset)
                .addValue("limit", limit);
        return jdbcTemplate.query(sql, params, (rs, rowNum) -> new User(
                rs.getInt("id_user"),
                rs.getString("login"),
                rs.getString("email"),
                rs.getString("password")
        ));
    }

    public long count() {
        String sql = "SELECT COUNT(*) FROM public.\"User\"";
        return jdbcTemplate.queryForObject(sql, new MapSqlParameterSource(), Long.class);
    }
}
