package adventure.go.goadventure.image;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ImageRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ImageRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveImage(String url) {
        String sql = "INSERT INTO public.\"Image\" (url) VALUES (:url)";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("url", url);
        jdbcTemplate.update(sql, params);
    }

    public String findImageUrlById(Long id) {
        String sql = "SELECT url FROM public.\"Image\" WHERE id_image = :id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);
        return jdbcTemplate.queryForObject(sql, params, String.class);
    }
}