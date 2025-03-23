package adventure.go.goadventure.scenario;

import adventure.go.goadventure.choice.Choice;
import adventure.go.goadventure.choice.ChoiceRepository;
import adventure.go.goadventure.dto.CreateScenarioDTO;
import adventure.go.goadventure.dto.PaginatedResponse;
import adventure.go.goadventure.dto.ScenarioDTO;
import adventure.go.goadventure.dto.UserScenarioDTO;
import adventure.go.goadventure.game.Game;
import adventure.go.goadventure.game.GameRepository;
import adventure.go.goadventure.image.ImageUploadService;
import adventure.go.goadventure.jwt.JwtUtil;
import adventure.go.goadventure.auth.AuthService;
import adventure.go.goadventure.session.Session;
import adventure.go.goadventure.session.SessionRepository;
import adventure.go.goadventure.step.Step;
import adventure.go.goadventure.step.StepRepository;
import adventure.go.goadventure.step_choice.StepChoice;
import adventure.go.goadventure.step_choice.StepChoiceRepository;
import adventure.go.goadventure.user.User;
import adventure.go.goadventure.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
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
    private final StepChoiceRepository stepChoiceRepository;
    private final ChoiceRepository choiceRepository;
    private final StepRepository stepRepository;
    private final UserRepository userRepository;
    private final ImageUploadService imageUploadService;

    public ScenarioController(ScenarioRepository scenarioRepository, JwtUtil jwtUtil, AuthService authService, GameRepository gameRepository, SessionRepository sessionRepository, StepChoiceRepository stepChoiceRepository, ChoiceRepository choiceRepository, StepRepository stepRepository, UserRepository userRepository, ImageUploadService imageUploadService) {
        this.scenarioRepository = scenarioRepository;
        this.jwtUtil = jwtUtil;
        this.authService = authService;
        this.gameRepository = gameRepository;
        this.sessionRepository = sessionRepository;
        this.choiceRepository = choiceRepository;
        this.stepChoiceRepository = stepChoiceRepository;
        this.stepRepository = stepRepository;
        this.userRepository = userRepository;
        this.imageUploadService = imageUploadService;
    }

    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    public PaginatedResponse<ScenarioDTO> findAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String search) {
        int offset = (page - 1) * limit;
        List<Scenario> scenarios;
        long total;

        if (search == null || search.isEmpty()) {
            scenarios = scenarioRepository.findAllWithPagination(offset, limit);
            total = scenarioRepository.count();
        } else {
            scenarios = scenarioRepository.findByNameContaining(search);
            total = scenarios.size();
        }

        if (scenarios.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No scenarios found");
        }

        List<ScenarioDTO> scenarioDTOs = scenarios.stream()
                .map(scenario -> {
                    User author = userRepository.findById(scenario.getAuthorId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Author not found"));
                    ScenarioDTO.AuthorDTO authorDTO = new ScenarioDTO.AuthorDTO(
                            author.getId_user(),
                            author.getLogin(),
                            author.getEmail(),
                            (String) author.getBio(),
                            author.getCreation_date(),
                            author.getPhoto_url()
                    );
                    return new ScenarioDTO(
                            scenario.getId(),
                            scenario.getTitle(),
                            scenario.getLimitPlayers(),
                            authorDTO,
                            scenario.getCreationDate(),
                            scenario.getDescription(),
                            scenario.getPhotoUrl()
                    );
                })
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

        Map<String, Object> scenarioData = scenarioWithFirstStep.get();
        Integer authorId = (Integer) scenarioData.get("id_author");
        User author = userRepository.findById(authorId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Author not found"));
        ScenarioDTO.AuthorDTO authorDTO = new ScenarioDTO.AuthorDTO(
                author.getId_user(),
                author.getLogin(),
                author.getEmail(),
                (String) author.getBio(),
                author.getCreation_date(),
                author.getPhoto_url()
        );

        scenarioData.put("author", authorDTO);
        scenarioData.put("creation_date", scenarioData.get("creation_date"));
        scenarioData.put("description", scenarioData.get("description"));
        scenarioData.remove("id_author");
        scenarioData.remove("id");

        return scenarioData;
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
        session.setStart_date(new java.sql.Timestamp(System.currentTimeMillis()));
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
        response.put("photo_url", scenarioWithFirstStep.get().get("photo_url"));
        response.put("first_step", firstStep);

        return response;
    }

    @GetMapping("/user")
    @ResponseStatus(HttpStatus.OK)
    public List<UserScenarioDTO> findScenariosByUser(@RequestHeader("Authorization") String token) {
        if (token == null || !token.startsWith("Bearer ") || !authService.isTokenValid(token.substring(7))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token.");
        }

        String jwtToken = token.substring(7);
        Integer userId = jwtUtil.getUserIdFromToken(jwtToken);

        List<Scenario> scenarios = scenarioRepository.findByUserId(userId);
        return scenarios.stream()
                .map(scenario -> {
                    Optional<Map<String, Object>> scenarioWithFirstStep = scenarioRepository.findByIdWithFirstStep(scenario.getId());
                    if (scenarioWithFirstStep.isEmpty()) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Scenario not found");
                    }

                    Map<String, Object> scenarioData = scenarioWithFirstStep.get();
                    Map<String, Object> firstStep = (Map<String, Object>) scenarioData.get("first_step");

                    return new UserScenarioDTO(
                            scenario.getId(),
                            scenario.getTitle(),
                            scenario.getLimitPlayers(),
                            scenario.getCreationDate(),
                            scenario.getDescription(),
                            scenario.getPhotoUrl(),
                            firstStep
                    );
                })
                .collect(Collectors.toList());
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
        if (scenario.getAuthorId() == null || !scenario.getAuthorId().equals(userId) || !authService.isAdmin(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to delete this scenario");
        }

        scenarioRepository.deleteAllRelatedEntities(id);
        scenarioRepository.delete(id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Scenario and related entities successfully deleted");
        return response;
    }

    // ScenarioController.java
    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Integer> create(
            @RequestHeader("Authorization") String token,
            @RequestParam("name") String name,
            @RequestParam("limit_players") Integer limitPlayers,
            @RequestParam("description") String description,
            @RequestParam(value = "photo", required = false) MultipartFile photo){
        if (token == null || !token.startsWith("Bearer ") || !authService.isTokenValid(token.substring(7))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token.");
        }

        String jwtToken = token.substring(7);
        Integer userId = jwtUtil.getUserIdFromToken(jwtToken);

        // Create a new step
        Step firstStep = new Step();
        firstStep.setTitle("null");
        firstStep.setText("null");
        firstStep.setLongitude(0.0); // Default value
        firstStep.setLatitude(0.0); // Optional
        stepRepository.create(firstStep);

        // Create a new scenario
        Scenario scenario = new Scenario();
        scenario.setTitle(name);
        scenario.setLimitPlayers(limitPlayers);
        scenario.setAuthorId(userId);
        scenario.setCreationDate(LocalDateTime.now());
        scenario.setDescription(description);
        scenario.setFirstStep(firstStep);

        if (photo != null && !photo.isEmpty()) {
            try {
                String photoUrl = imageUploadService.uploadImage(photo);
                scenario.setPhotoUrl(photoUrl);
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload photo", e);
            }
        }

        scenarioRepository.create(scenario);

        // Update the scenario with the first step
        scenarioRepository.updateFirstStep(scenario.getId(), firstStep.getId_step());

        Map<String, Integer> response = new HashMap<>();
        response.put("id_scen", scenario.getId());
        return response;
    }

    // ScenarioController.java
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void update(@RequestHeader("Authorization") String token, @PathVariable Integer id, @RequestParam Map<String, Object> updateScenarioDTO, @RequestParam(value = "photo", required = false) MultipartFile photo) {
        if (updateScenarioDTO.get("name") == null || ((String) updateScenarioDTO.get("name")).isEmpty()) {
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
        if (scenario.getAuthorId() == null || !scenario.getAuthorId().equals(userId) || !authService.isAdmin(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to update this scenario");
        }

        scenario.setTitle((String) updateScenarioDTO.get("name"));
        scenario.setLimitPlayers(updateScenarioDTO.get("limitPlayers") != null ? Integer.parseInt((String) updateScenarioDTO.get("limitPlayers")) : null);
        scenario.setDescription((String) updateScenarioDTO.get("description"));

        if (photo != null && !photo.isEmpty()) {
            try {
                String photoUrl = imageUploadService.uploadImage(photo);
                scenario.setPhotoUrl(photoUrl);
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload photo", e);
            }
        }

        scenarioRepository.update(scenario);
    }
}