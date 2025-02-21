package adventure.go.goadventure.scenario;

import adventure.go.goadventure.dto.CreateScenarioDTO;
import adventure.go.goadventure.dto.PaginatedResponse;
import adventure.go.goadventure.dto.ScenarioDTO;
import adventure.go.goadventure.game.Game;
import adventure.go.goadventure.game.GameRepository;
import adventure.go.goadventure.jwt.JwtUtil;
import adventure.go.goadventure.auth.AuthService;
import adventure.go.goadventure.session.Session;
import adventure.go.goadventure.session.SessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/scenarios")
public class ScenarioController {

    private static final Logger log = LoggerFactory.getLogger(ScenarioController.class);

    private final ScenarioRepository scenarioRepository;
    private final JwtUtil jwtUtil;
    private final AuthService authService;
    private final GameRepository gameRepository;
    private final SessionRepository sessionRepository;

    public ScenarioController(ScenarioRepository scenarioRepository, JwtUtil jwtUtil, AuthService authService, GameRepository gameRepository, SessionRepository sessionRepository) {
        this.scenarioRepository = scenarioRepository;
        this.jwtUtil = jwtUtil;
        this.authService = authService;
        this.gameRepository = gameRepository;
        this.sessionRepository = sessionRepository;
    }

    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    public PaginatedResponse<ScenarioDTO> findAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        if (page < 1 || limit < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid pagination parameters");
        }

        int offset = (page - 1) * limit;
        List<Scenario> scenarios = scenarioRepository.findAllWithPagination(offset, limit);
        long total = scenarioRepository.count();

        List<ScenarioDTO> scenarioDTOs = scenarios.stream()
                .map(scenario -> new ScenarioDTO(scenario.getId(), scenario.getTitle()))
                .collect(Collectors.toList());

        return new PaginatedResponse<>(page, limit, total, scenarioDTOs);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> getScenarioById(@RequestHeader("Authorization") String token, @PathVariable Integer id) {
        if (token == null || !token.startsWith("Bearer ") || !authService.isTokenValid(token.substring(7))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token.");
        }

        Optional<Map<String, Object>> scenarioWithFirstStep = scenarioRepository.findByIdWithFirstStep(id);
        if (scenarioWithFirstStep.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Scenario not found");
        }

        return scenarioWithFirstStep.get();
    }

    @GetMapping("createGame/{id}")
    public Map<String, Object> findById(@RequestHeader("Authorization") String token, @PathVariable Integer id) {
        if (token == null || !token.startsWith("Bearer ") || !authService.isTokenValid(token.substring(7))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token.");
        }

        String jwtToken = token.substring(7);
        Integer userId = jwtUtil.getUserIdFromToken(jwtToken);

        Optional<Map<String, Object>> scenarioWithFirstStep = scenarioRepository.findByIdWithFirstStep(id);
        if (scenarioWithFirstStep.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Scenario not found");
        }

        // Create a new game entry
        Game game = new Game();
        game.setId_scen(id);
        game.setStartTime(new Timestamp(System.currentTimeMillis()));
        gameRepository.create(game);

        // Get the first step ID for the scenario
        Map<String, Object> firstStep = (Map<String, Object>) scenarioWithFirstStep.get().get("first_step");
        Integer firstStepId = (Integer) firstStep.get("id_step");

        // Create a new session entry
        Session session = new Session();
        session.setId_user(userId);
        session.setId_game(game.getId_game());
        session.setCurrent_step(firstStepId);
        session.setStart_date(new java.sql.Date(System.currentTimeMillis()));
        sessionRepository.create(session);

        // Retrieve the generated id_ses
        Integer idSes = sessionRepository.findLastInsertedId();

        // Prepare the response in the desired order
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("user_id", userId);
        response.put("id_ses", idSes);
        response.put("id_author", scenarioWithFirstStep.get().get("id_author"));
        response.put("id_game", game.getId_game());
        response.put("name", scenarioWithFirstStep.get().get("name"));
        response.put("first_step", firstStep);

        return response;
    }

    @GetMapping("/name/{name}")
    public Optional<Scenario> findByName(@PathVariable String name) {
        return scenarioRepository.findByName(name);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, String> delete(@RequestHeader("Authorization") String token, @PathVariable Integer id) {
        if (id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ID format");
        }

        if (token == null || !token.startsWith("Bearer ") || !authService.isTokenValid(token.substring(7))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token.");
        }

        String jwtToken = token.substring(7);
        Integer userId = jwtUtil.getUserIdFromToken(jwtToken);

        Optional<Scenario> existingScenario = scenarioRepository.findById(id);
        if (existingScenario.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Scenario not found");
        }

        Scenario scenario = existingScenario.get();
        if (scenario.getAuthorId() == null || !scenario.getAuthorId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to delete this scenario");
        }

        scenarioRepository.delete(id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Scenario successfully deleted");
        return response;
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public Scenario create(@RequestHeader("Authorization") String token, @RequestBody CreateScenarioDTO createScenarioDTO) {
        if (createScenarioDTO.getName() == null || createScenarioDTO.getName().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required");
        }

        if (token == null || !token.startsWith("Bearer ") || !authService.isTokenValid(token.substring(7))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token.");
        }

        String jwtToken = token.substring(7);
        Integer userId = jwtUtil.getUserIdFromToken(jwtToken);

        Scenario scenario = new Scenario();
        scenario.setTitle(createScenarioDTO.getName());
        scenario.setLimitPlayers(createScenarioDTO.getLimitPlayers());
        scenario.setAuthorId(userId);
        scenarioRepository.create(scenario);
        return scenario;
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Scenario update(@RequestHeader("Authorization") String token, @PathVariable Integer id, @RequestBody CreateScenarioDTO updateScenarioDTO) {
        if (updateScenarioDTO.getName() == null || updateScenarioDTO.getName().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required");
        }

        if (token == null || !token.startsWith("Bearer ") || !authService.isTokenValid(token.substring(7))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token.");
        }

        String jwtToken = token.substring(7);
        Integer userId = jwtUtil.getUserIdFromToken(jwtToken);

        Optional<Scenario> existingScenario = scenarioRepository.findById(id);
        if (existingScenario.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Scenario not found");
        }

        Scenario scenario = existingScenario.get();
        if (scenario.getAuthorId() == null || !scenario.getAuthorId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to update this scenario");
        }

        scenario.setTitle(updateScenarioDTO.getName());
        scenario.setLimitPlayers(updateScenarioDTO.getLimitPlayers());
        scenarioRepository.update(scenario);

        return scenario;
    }
}