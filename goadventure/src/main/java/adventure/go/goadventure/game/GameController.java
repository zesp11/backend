package adventure.go.goadventure.game;

import adventure.go.goadventure.auth.AuthService;
import adventure.go.goadventure.jwt.JwtUtil;
import adventure.go.goadventure.session.Session;
import adventure.go.goadventure.session.SessionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameRepository gameRepository;
    private final JwtUtil jwtUtil;
    private final AuthService authService;
    private final SessionRepository sessionRepository;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public GameController(GameRepository gameRepository, JwtUtil jwtUtil, AuthService authService, SessionRepository sessionRepository, NamedParameterJdbcTemplate jdbcTemplate) {
        this.gameRepository = gameRepository;
        this.jwtUtil = jwtUtil;
        this.authService = authService;
        this.sessionRepository = sessionRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("")
    public List<Map<String, Object>> findAll() {
        List<Game> games = gameRepository.findAll();
        List<Map<String, Object>> response = new ArrayList<>();

        for (Game game : games) {
            Map<String, Object> gameData = new HashMap<>();
            gameData.put("gameId", game.getId_game());
            gameData.put("scenarioId", game.getId_scen());
            response.add(gameData);
        }

        return response;
    }

    @GetMapping("/{id}")
    public Optional<Game> findById(@PathVariable Integer id) {
        return gameRepository.findById(id);
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createGame(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> payload) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }

        String jwtToken = token.substring(7);
        if (!jwtUtil.validateToken(jwtToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }

        Integer userId = jwtUtil.getUserIdFromToken(jwtToken);

        Integer scenarioId = (Integer) payload.get("scenarioId");
        if (scenarioId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required data");
        }

        Game game = new Game();
        game.setId_scen(scenarioId);
        game.setStartTime(new java.sql.Timestamp(System.currentTimeMillis()));
        gameRepository.create(game);

        Map<String, Object> response = new HashMap<>();
        response.put("gameId", game.getId_game());
        response.put("userId", userId);
        response.put("scenarioId", scenarioId);
        response.put("status", "active");

        return response;
    }
    @PostMapping("/play")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createSessionChoice(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> payload) {

        if (token == null || !token.startsWith("Bearer ") || !authService.isTokenValid(token.substring(7))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token.");
        }

        String jwtToken = token.substring(7);
        Integer userId = jwtUtil.getUserIdFromToken(jwtToken);

        Integer idSes = (Integer) payload.get("id_ses");
        Integer idChoice = (Integer) payload.get("id_choice");
        Integer idGame = (Integer) payload.get("id_game");

        if (idSes == null || idChoice == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing session ID or choice ID");
        }

        Optional<Session> sessionOpt = sessionRepository.findById(idSes);
        if (sessionOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found");
        }

        Session session = sessionOpt.get();
        java.sql.Date currentDate = new java.sql.Date(System.currentTimeMillis());

        // Update the existing session
        session.setEnd_date(currentDate);
        session.setId_choice(idChoice);
        sessionRepository.update(session, idSes);


        // Find the next step for the given choice
        String sql = "SELECT id_next_step FROM public.\"Choice\" WHERE id_choice = :id_choice";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("id_choice", idChoice);
        Integer nextStepId = jdbcTemplate.queryForObject(sql, params, Integer.class);


        // Create a new session entry
        Session newSession = new Session();
        newSession.setId_user(userId);
        newSession.setId_game(session.getId_game());
        newSession.setCurrent_step(nextStepId);
        newSession.setStart_date(currentDate);
        newSession.setId_choice(idChoice);
        sessionRepository.create(newSession);

        // Retrieve the generated id_ses
        Integer newIdSes = sessionRepository.findLastInsertedId();

        // Fetch the new step and its choices
        String stepSql = "SELECT st.id_step, st.title, st.text, st.longitude, st.latitude, c.id_choice, c.text AS choice_text " +
                "FROM public.\"Step\" st " +
                "LEFT JOIN public.\"Step_Choices\" sc ON st.id_step = sc.id_step " +
                "LEFT JOIN public.\"Choice\" c ON sc.id_choice = c.id_choice " +
                "WHERE st.id_step = :id_step";
        MapSqlParameterSource stepParams = new MapSqlParameterSource().addValue("id_step", nextStepId);
        List<Map<String, Object>> stepData = jdbcTemplate.query(stepSql, stepParams, (rs, rowNum) -> {
            Map<String, Object> step = new HashMap<>();
            step.put("id_step", rs.getInt("id_step"));
            step.put("title", rs.getString("title"));
            step.put("text", rs.getString("text"));
            step.put("latitude", rs.getObject("latitude", Double.class));
            step.put("longitude", rs.getObject("longitude", Double.class));

            List<Map<String, Object>> choices = new ArrayList<>();
            do {
                Map<String, Object> choice = new HashMap<>();
                choice.put("id_choice", rs.getInt("id_choice"));
                choice.put("text", rs.getString("choice_text"));
                choices.add(choice);
            } while (rs.next());

            step.put("choices", choices);
            return step;
        });

        // Prepare the response
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> step = stepData.isEmpty() ? Collections.emptyMap() : stepData.get(0);
        if (step.containsKey("choices")) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) step.get("choices");
            if (choices.stream().anyMatch(choice -> choice.get("id_choice").equals(0))) {
                step.put("choices", "End of game");

                // Update the newest session entry for the current game
                String updateSessionSql = "UPDATE public.\"Session\" SET end_date = :end_date, id_choice = 0 " +
                        "WHERE id_game = :id_game AND id_ses = (SELECT MAX(id_ses) FROM public.\"Session\" WHERE id_game = :id_game)";
                MapSqlParameterSource updateSessionParams = new MapSqlParameterSource()
                        .addValue("end_date", new java.sql.Date(System.currentTimeMillis()))
                        .addValue("id_game", idGame);
                jdbcTemplate.update(updateSessionSql, updateSessionParams);

                // Update the end_date in the Game table for the current game
                String updateGameSql = "UPDATE public.\"Game\" SET end_time = :end_time WHERE id_game = :id_game";
                MapSqlParameterSource updateGameParams = new MapSqlParameterSource()
                        .addValue("end_time", new java.sql.Timestamp(System.currentTimeMillis()))
                        .addValue("id_game", idGame);
                jdbcTemplate.update(updateGameSql, updateGameParams);
            }
        }
        response.put("step", step);
        response.put("id_ses", newIdSes);

        return response;
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@RequestBody Game game, @PathVariable Integer id) {
        gameRepository.update(game, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        gameRepository.delete(id);
    }
}