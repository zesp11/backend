package adventure.go.goadventure.user;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public UserRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    public List<User> findAll() {
        String sql = "SELECT * FROM public.\"User\"";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new User(
                rs.getInt("id_user"),
                rs.getString("login"),
                rs.getString("email"),
                rs.getString("password"),
                rs.getString("bio"),
                rs.getTimestamp("creation_date").toLocalDateTime(),
                rs.getString("photo_url")
        ));
    }

    public Optional<User> findById(Integer id) {
        String sql = "SELECT * FROM public.\"User\" WHERE id_user = :id";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("id", id);

        List<User> users = jdbcTemplate.query(sql, params, (rs, rowNum) -> new User(
                rs.getInt("id_user"),
                rs.getString("login"),
                rs.getString("email"),
                rs.getString("password"),
                rs.getString("bio"),
                rs.getTimestamp("creation_date").toLocalDateTime(),
                rs.getString("photo_url")
        ));
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    public void create(User user) {
        String sql = "INSERT INTO public.\"User\" (login, email, password, bio, creation_date) " +
                "VALUES (:login, :email, :password, :bio, :creation_date)";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("login", user.getLogin())
                .addValue("email", user.getEmail())
                .addValue("password", user.getPassword())
                .addValue("bio", user.getBio())
                .addValue("creation_date", LocalDateTime.now());

        jdbcTemplate.update(sql, params);
    }

    public void update(User user, Integer id) {
        String sql = "UPDATE public.\"User\" SET login = :login, email = :email, password = :password, bio = :bio, photo_url = :photo_url " +
                "WHERE id_user = :id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("login", user.getLogin())
                .addValue("email", user.getEmail())
                .addValue("password", user.getPassword())
                .addValue("bio", user.getBio())
                .addValue("photo_url", user.getPhoto_url())
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
        String sql = "SELECT u.*, r.role_name AS role " +
                "FROM public.\"User\" u " +
                "LEFT JOIN public.\"User_Role\" ur ON u.id_user = ur.id_user " +
                "LEFT JOIN public.\"Role\" r ON ur.id_role = r.id_role " +
                "WHERE u.login = :login " +
                "ORDER BY ur.id_role ASC " +
                "LIMIT 1";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("login", login);

        List<User> users = jdbcTemplate.query(sql, params, (rs, rowNum) -> new User(
                rs.getInt("id_user"),
                rs.getString("login"),
                rs.getString("email"),
                rs.getString("password"),
                rs.getString("bio"),
                rs.getTimestamp("creation_date").toLocalDateTime(),
                rs.getString("photo_url"),
                rs.getString("role")
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
                rs.getString("password"),
                rs.getString("bio"),
                rs.getTimestamp("creation_date").toLocalDateTime(),
                rs.getString("photo_url")
        ));
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    public User save(User user) {
        String sql = "INSERT INTO public.\"User\" (login, email, password, bio, creation_date) " +
                "VALUES (:login, :email, :password, :bio, :creation_date) " +
                "RETURNING id_user";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("login", user.getLogin())
                .addValue("email", user.getEmail())
                .addValue("password", user.getPassword())
                .addValue("bio", user.getBio())
                .addValue("creation_date", LocalDateTime.now());

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
                rs.getString("password"),
                rs.getString("bio"),
                rs.getTimestamp("creation_date").toLocalDateTime(),
                rs.getString("photo_url")
        ));
    }
    public List<User> findByLoginOrEmailContaining(String search) {
        String sql = "SELECT użytkownik.* FROM public.\"User\" użytkownik WHERE użytkownik.login LIKE :search OR użytkownik.email LIKE :search";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("search", "%" + search + "%");

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> new User(
                rs.getInt("id_user"),
                rs.getString("login"),
                rs.getString("email"),
                rs.getString("password"),
                rs.getString("bio"),
                rs.getTimestamp("creation_date").toLocalDateTime(),
                rs.getString("photo_url")
        ));
    }

    public long count() {
        String sql = "SELECT COUNT(*) FROM public.\"User\"";
        return jdbcTemplate.queryForObject(sql, new MapSqlParameterSource(), Long.class);
    }

    public boolean hasRole(Integer userId, Integer roleId) {
        String sql = "SELECT COUNT(*) FROM public.\"User_Role\" WHERE id_user = :userId AND id_role = :roleId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("roleId", roleId);
        Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }
}
