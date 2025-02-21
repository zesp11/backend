package adventure.go.goadventure.game;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.List;
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
}