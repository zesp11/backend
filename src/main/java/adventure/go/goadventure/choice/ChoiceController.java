package adventure.go.goadventure.choice;

import adventure.go.goadventure.auth.AuthService;
import adventure.go.goadventure.jwt.JwtUtil;
import adventure.go.goadventure.scenario.Scenario;
import adventure.go.goadventure.scenario.ScenarioRepository;
import adventure.go.goadventure.step.StepRepository;
import adventure.go.goadventure.step_choice.StepChoice;
import adventure.go.goadventure.step_choice.StepChoiceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/choices")
public class ChoiceController {

    private final ChoiceRepository choiceRepository;
    private final StepChoiceRepository stepChoiceRepository;
    private final AuthService authService;
    private final ScenarioRepository scenarioRepository;
    private final JwtUtil jwtUtil;

    public ChoiceController(ChoiceRepository choiceRepository, StepChoiceRepository stepChoiceRepository, AuthService authService, ScenarioRepository scenarioRepository, JwtUtil jwtUtil) {
        this.choiceRepository = choiceRepository;
        this.stepChoiceRepository = stepChoiceRepository;
        this.authService = authService;
        this.scenarioRepository = scenarioRepository;
        this.jwtUtil = jwtUtil;
    }

    // Metoda findAll - Zwraca wszystkie opcje wyboru
    @GetMapping("/all")
    public List<Choice> findAll() {
        return choiceRepository.findAll();
    }

    // Metoda findById - Zwraca opcję wyboru na podstawie id_choice
    @GetMapping("/{id}")
    public Optional<Choice> findById(@PathVariable Integer id) {
        return choiceRepository.findById(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{id}")
    public Map<String, Integer> createChoice(
            @RequestHeader("Authorization") String token,
            @PathVariable Integer id,
            @RequestBody Map<String, Object> requestBody) {
        if (token == null || !token.startsWith("Bearer ") || !authService.isTokenValid(token.substring(7))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token.");
        }

        String jwtToken = token.substring(7);
        Integer userId = jwtUtil.getUserIdFromToken(jwtToken);

        Integer id_scen = (Integer) requestBody.get("id_scen");
        if (id_scen == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id_scen is required");
        }

        Optional<Scenario> scenarioOptional = scenarioRepository.findById(id_scen);
        if (scenarioOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Scenario not found");
        }

        Scenario scenario = scenarioOptional.get();
        if (!scenario.getAuthorId().equals(userId) && !authService.isAdmin(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to create a choice for this scenario");
        }

        Choice choice = new Choice();
        choice.setText((String) requestBody.get("text"));
        choice.setId_next_step((Integer) requestBody.get("id_next_step"));

        choiceRepository.create(choice);

        Map<String, Integer> response = new HashMap<>();
        response.put("id_choice", choice.getId_choice());
        return response;
    }


    // ChoiceController.java
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(
            @RequestHeader("Authorization") String token,
            @PathVariable Integer id,
            @RequestBody Map<String, Object> requestBody) {
        if (token == null || !token.startsWith("Bearer ") || !authService.isTokenValid(token.substring(7))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token.");
        }

        String jwtToken = token.substring(7);
        Integer userId = jwtUtil.getUserIdFromToken(jwtToken);

        Integer id_scen = (Integer) requestBody.get("id_scen");
        if (id_scen == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id_scen is required");
        }

        Optional<Scenario> scenarioOptional = scenarioRepository.findById(id_scen);
        if (scenarioOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Scenario not found");
        }

        Scenario scenario = scenarioOptional.get();
        if (!scenario.getAuthorId().equals(userId) && !authService.isAdmin(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to update this choice");
        }

        Optional<Choice> existingChoice = choiceRepository.findById(id);
        if (existingChoice.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Choice not found");
        }

        Choice choice = new Choice();
        choice.setText((String) requestBody.get("text"));
        choice.setId_next_step((Integer) requestBody.get("id_next_step"));

        choiceRepository.update(choice, id);
    }

    // ChoiceController.java
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestHeader("Authorization") String token, @PathVariable Integer id, @RequestParam Integer id_scen) {
        if (token == null || !token.startsWith("Bearer ") || !authService.isTokenValid(token.substring(7))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token.");
        }

        String jwtToken = token.substring(7);
        Integer userId = jwtUtil.getUserIdFromToken(jwtToken);

        Optional<Scenario> scenarioOptional = scenarioRepository.findById(id_scen);
        if (scenarioOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Scenario not found");
        }

        Scenario scenario = scenarioOptional.get();
        if (!scenario.getAuthorId().equals(userId) && !authService.isAdmin(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to delete this choice");
        }
        // Delete related entries in Step_Choices
        stepChoiceRepository.deleteByChoiceId(id);

        // Delete the Choice entity
        choiceRepository.delete(id);
    }
}
