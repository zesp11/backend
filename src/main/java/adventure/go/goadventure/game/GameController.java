package adventure.go.goadventure.game;

import adventure.go.goadventure.auth.AuthService;
import adventure.go.goadventure.choice.Choice;
import adventure.go.goadventure.choice.ChoiceRepository;
import adventure.go.goadventure.jwt.JwtUtil;
import adventure.go.goadventure.session.Session;
import adventure.go.goadventure.session.SessionRepository;
import adventure.go.goadventure.step.Step;
import adventure.go.goadventure.step.StepRepository;
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
    private final StepRepository stepRepository;
    private final ChoiceRepository choiceRepository;

    public GameController(GameRepository gameRepository, JwtUtil jwtUtil, AuthService authService, SessionRepository sessionRepository, NamedParameterJdbcTemplate jdbcTemplate, StepRepository stepRepository, ChoiceRepository choiceRepository) {
        this.gameRepository = gameRepository;
        this.jwtUtil = jwtUtil;
        this.authService = authService;
        this.sessionRepository = sessionRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.stepRepository = stepRepository;
        this.choiceRepository = choiceRepository;
    }

    @GetMapping("")
    public List<Map<String, Object>> findAll() {
        return gameRepository.findAllWithDetails();
    }

    @GetMapping("/{id}")
    public Map<String, Object> findById(@PathVariable Integer id) {
        return gameRepository.findByIdWithScenarioName(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found with id: " + id));
    }
    @GetMapping("/user")
    public List<Map<String, Object>> findGamesByUser(@RequestHeader("Authorization") String token, @RequestParam(required = false) Boolean includeFinished) {
        if (token == null || !token.startsWith("Bearer ") || !authService.isTokenValid(token.substring(7))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token.");
        }

        String jwtToken = token.substring(7);
        Integer userId = jwtUtil.getUserIdFromToken(jwtToken);

        if (includeFinished != null && includeFinished) {
            return gameRepository.findAllGamesWithDetailsByUserId(userId);
        } else {
            return gameRepository.findActiveGamesWithDetailsByUserId(userId);
        }
    }

    @GetMapping("/{id}/history")
    public List<Map<String, Object>> getGameHistory(@RequestHeader("Authorization") String token, @PathVariable Integer id) {
        if (token == null || !token.startsWith("Bearer ") || !authService.isTokenValid(token.substring(7))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token.");
        }

        String jwtToken = token.substring(7);
        Integer userId = jwtUtil.getUserIdFromToken(jwtToken);

        List<Session> sessions = sessionRepository.findByGameId(id);
        List<Map<String, Object>> response = new ArrayList<>();

        for (Session session : sessions) {
            Map<String, Object> sessionData = new HashMap<>();
            sessionData.put("id_ses", session.getId_ses());
            sessionData.put("id_user", session.getId_user());
            sessionData.put("id_game", session.getId_game());
            sessionData.put("current_step", session.getCurrent_step());
            sessionData.put("start_date", session.getStart_date());
            sessionData.put("end_date", session.getEnd_date());

            // Retrieve the previous step text
            Integer previousStepId = stepRepository.findPreviousStepId(session.getCurrent_step());
            String previousStepText = previousStepId != null ? stepRepository.findStepTextById(previousStepId) : null;
            sessionData.put("previous_step_text", previousStepText);


            // Retrieve the id_choice from the highest id_ses lower than the current one
            Optional<Integer> previousChoiceId = sessionRepository.findPreviousChoiceId(session.getId_ses(), session.getId_game(), session.getId_user());

            // Retrieve the choice text
            if (previousChoiceId.isPresent()) {
                String choiceText = choiceRepository.findChoiceTextById(previousChoiceId);
                sessionData.put("choice_text", choiceText);
            }

            response.add(sessionData);
        }

        return response;
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

    @GetMapping("/{id}/play")
    public Map<String, Object> getGameStepAndChoices(@RequestHeader("Authorization") String token, @PathVariable Integer id) {
        if (token == null || !token.startsWith("Bearer ") || !authService.isTokenValid(token.substring(7))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token.");
        }
        String jwtToken = token.substring(7);
        Integer userId = jwtUtil.getUserIdFromToken(jwtToken);

        // Find the latest session for the given game ID
        Optional<Object> latestSessionOpt = sessionRepository.findLatestByGameId(id, userId);
        if (latestSessionOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No active session found for the game.");
        }

        Object latestSessionOrStep = latestSessionOpt.get();
        Map<String, Object> response = new HashMap<>();

        if (latestSessionOrStep instanceof Session) {
            Session latestSession = (Session) latestSessionOrStep;
            Integer currentStepId = latestSession.getCurrent_step();

            // Find the step and its choices
            Optional<Step> stepOpt = stepRepository.findById(currentStepId);
            if (stepOpt.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found.");
            }
            Step step = stepOpt.get();

            // Fetch choices for the step
            List<Choice> choices = step.getChoices();

            response.put("step", step);
            //response.put("choices", choices);
        } else if (latestSessionOrStep instanceof Step) {
            Step step = (Step) latestSessionOrStep;

            // Fetch choices for the step
            List<Choice> choices = step.getChoices();

            response.put("step", step);
            //response.put("choices", choices);
        } else {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected object type.");
        }

        return response;
    }

    @PostMapping("/{id_game}/play")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createSessionChoice(
            @RequestHeader("Authorization") String token,
            @PathVariable Integer id_game,
            @RequestBody Map<String, Object> payload) {

        if (token == null || !token.startsWith("Bearer ") || !authService.isTokenValid(token.substring(7))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token.");
        }

        String jwtToken = token.substring(7);
        Integer userId = jwtUtil.getUserIdFromToken(jwtToken);

        Integer idChoice = (Integer) payload.get("id_choice");
        if (idChoice == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing choice ID");
        }

        // Find the latest session for the given game ID and user ID
        Optional<Session> latestSessionOpt = sessionRepository.findLatestByGameIdAndUserId(id_game, userId);
        if (latestSessionOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No active session found for the game.");
        }

        Session latestSession = (Session) latestSessionOpt.get();
        Integer currentStepId = latestSession.getCurrent_step();

        // Check if the id_choice is connected to the current_step
        String checkChoiceSql = "SELECT COUNT(*) FROM public.\"Step_Choices\" WHERE id_step = :currentStepId AND id_choice = :idChoice";
        MapSqlParameterSource checkChoiceParams = new MapSqlParameterSource()
                .addValue("currentStepId", currentStepId)
                .addValue("idChoice", idChoice);
        Integer count = jdbcTemplate.queryForObject(checkChoiceSql, checkChoiceParams, Integer.class);

        if (count == null || count == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid choice ID for the current step.");
        }
        java.sql.Timestamp currentDate = new java.sql.Timestamp(System.currentTimeMillis());

        // Update the existing session
        latestSession.setEnd_date(currentDate);
        latestSession.setId_choice(idChoice);
        sessionRepository.update(latestSession, latestSession.getId_ses());

        // Find the next step for the given choice
        String sql = "SELECT id_next_step FROM public.\"Choice\" WHERE id_choice = :id_choice";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("id_choice", idChoice);
        Integer nextStepId = jdbcTemplate.queryForObject(sql, params, Integer.class);

        // Create a new session entry
        Session newSession = new Session();
        newSession.setId_user(userId);
        newSession.setId_game(id_game);
        newSession.setCurrent_step(nextStepId);
        newSession.setStart_date(currentDate);
        newSession.setId_choice(idChoice);
        sessionRepository.create(newSession);

        // Retrieve the generated id_ses
        Integer newIdSes = sessionRepository.findLastInsertedId();

        // Fetch the new step and its choices
        String stepSql = "SELECT st.id_step, st.title, st.text, st.longitude, st.latitude, st.photo_url, c.id_choice, c.text AS choice_text " +
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
            step.put("photo_url", rs.getString("photo_url"));

            List<Map<String, Object>> choices = new ArrayList<>();
            do {
                Map<String, Object> choice = new HashMap<>();
                choice.put("id_choice", rs.getInt("id_choice"));
                choice.put("choice_text", rs.getString("choice_text"));
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
                step.put("choices", Collections.emptyList());

                // Update the newest session entry for the current game
                String updateSessionSql = "UPDATE public.\"Session\" SET end_date = :end_date, id_choice = 0 " +
                        "WHERE id_game = :id_game AND id_ses = (SELECT MAX(id_ses) FROM public.\"Session\" WHERE id_game = :id_game)";
                MapSqlParameterSource updateSessionParams = new MapSqlParameterSource()
                        .addValue("end_date", new java.sql.Date(System.currentTimeMillis()))
                        .addValue("id_game", id_game);
                jdbcTemplate.update(updateSessionSql, updateSessionParams);

                // Update the end_date in the Game table for the current game
                String updateGameSql = "UPDATE public.\"Game\" SET end_time = :end_time WHERE id_game = :id_game";
                MapSqlParameterSource updateGameParams = new MapSqlParameterSource()
                        .addValue("end_time", new java.sql.Timestamp(System.currentTimeMillis()))
                        .addValue("id_game", id_game);
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