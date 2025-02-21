package adventure.go.goadventure;

import adventure.go.goadventure.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class DBConnectionTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void testConnection() {
        Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        assertThat(result).isEqualTo(1);
        //System.out.println(result);

    }

    @Test
    public void testFetchUsers() {

        String sql = "SELECT użyszkodnik.* " +
                "FROM public.\"User\" użyszkodnik";


        List<User> users = jdbcTemplate.query(sql, (rs, rowNum) -> new User(
                rs.getInt("id_user"),
                rs.getString("login"),
                rs.getString("email"),
                rs.getString("password")
        ));


        assertThat(users).isNotEmpty();


        users.forEach(user -> System.out.println(
                "ID: " + user.getId_user() +
                        ", Login: " + user.getLogin() +
                        ", Email: " + user.getEmail()
        ));
    }
}