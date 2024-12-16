package adventure.go.goadventure.user;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {

    private final JdbcClient jdbcClient;

    public UserRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<User> findAll() {
        return jdbcClient.sql("SELECT użytkownik.* FROM public.\"User\" użytkownik")
                .query(User.class)
                .list();
    }

    public Optional<User> findById(Integer id) {
        return jdbcClient.sql("SELECT użytkownik.* FROM public.\"User\" użytkownik WHERE id_user = :id")
                .param(id)
                .query(User.class)
                .optional();
    }

    public void create(User user) {
        var added = jdbcClient.sql("INSERT INTO public.\"User\" użytkownik (id_user, login, email, password) values(:id_user, :login, :email, :password)")
                .param(user.getId_user())
                .param(user.getLogin())
                .param(user.getEmail())
                .param(user.getPassword())
                .update();
    }

    public void update(User user, Integer id) {
        var updated = jdbcClient.sql("UPDATE public.\"User\" użytkownik SET login = :login, email = :email, password = :password WHERE id_user = :id")
                .param(user.getLogin())
                .param(user.getEmail())
                .param(user.getPassword())
                .param(id)
                .update();

        Assert.state(updated == 1, "User not found");
    }

    public void delete(Integer id) {
        var deleted = jdbcClient.sql("DELETE FROM public.\"User\" użytkownik WHERE id_user = :id")
                .param(id)
                .update();

        Assert.state(deleted == 1, "User not found");
    }
}
