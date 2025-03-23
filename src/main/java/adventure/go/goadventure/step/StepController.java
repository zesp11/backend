package adventure.go.goadventure.step;
import adventure.go.goadventure.auth.AuthService;
import adventure.go.goadventure.image.ImageUploadService;
import adventure.go.goadventure.scenario.Scenario;
import adventure.go.goadventure.scenario.ScenarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import adventure.go.goadventure.jwt.JwtUtil;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/steps")
public class StepController {
    private final StepRepository stepRepository;
    private final ScenarioRepository scenarioRepository;
    private final AuthService authService;
    private static final Logger log = LoggerFactory.getLogger(StepController.class);
    private final ImageUploadService imageUploadService;
    private final JwtUtil jwtUtil;

    public StepController(StepRepository stepRepository, ScenarioRepository scenarioRepository, AuthService authService, ImageUploadService imageUploadService, JwtUtil jwtUtil) {
        this.stepRepository = stepRepository;
        this.scenarioRepository = scenarioRepository;
        this.authService = authService;
        this.imageUploadService = imageUploadService;
        this.jwtUtil = jwtUtil;
    }

    // Metoda findAll - Zwraca wszystkie kroki
    @GetMapping("/all")
    public List<Step> findAll() {
        return stepRepository.findAll();
    }

    // Metoda findById - Zwraca krok na podstawie id_step
    @GetMapping("/{id}")
    public Optional<Step> findById(@PathVariable Integer id) {
        return stepRepository.findById(id);
    }



    // StepController.java
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Step create(
            @RequestHeader("Authorization") String token,
            @RequestParam("title") String title,
            @RequestParam("text") String text,
            @RequestParam("id_scen") Integer id_scen,
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            @RequestParam(value = "longitude", required = false, defaultValue = "0.0") Double longitude,
            @RequestParam(value = "latitude", required = false, defaultValue = "0.0") Double latitude) {
        if (token == null || !token.startsWith("Bearer ") || !authService.isTokenValid(token.substring(7))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token.");
        }

        String jwtToken = token.substring(7);
        Integer userId = jwtUtil.getUserIdFromToken(jwtToken);

        // Log the start of the create method
        log.debug("Starting create method with title: {}, text: {}, scenario ID: {}", title, text, id_scen);


        // Check if the scenario has an empty id_first_step
        Optional<Scenario> scenarioOpt = scenarioRepository.findById(id_scen);
        if (scenarioOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Scenario not found");
        }

        Scenario scenario = scenarioOpt.get();

        if (!scenario.getAuthorId().equals(userId) || !authService.isAdmin(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to create a step for this scenario");
        }

        // Create the new step
        Step step = new Step();
        step.setTitle(title);
        step.setText(text);
        step.setLongitude(longitude);
        step.setLatitude(latitude);

        if (photo != null && !photo.isEmpty()) {
            try {
                String photoUrl = imageUploadService.uploadImage(photo);
                step.setPhotoUrl(photoUrl);
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload photo", e);
            }
        }

        stepRepository.create(step);

        // Retrieve the generated id_step
        Integer generatedId = step.getId_step();
        log.debug("Generated step ID: {}", generatedId);

        if (scenario.getFirstStep() == null) {
            log.debug("Scenario has no first step, updating with generated step ID: {}", generatedId);
            scenarioRepository.updateFirstStep(scenario.getId(), generatedId);
        } else {
            log.debug("Scenario already has a first step: {}", scenario.getFirstStep().getId_step());
        }

        // Return the created step
        return step;
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(
            @RequestHeader("Authorization") String token,
            @PathVariable Integer id,
            @RequestParam("id_scen") Integer id_scen,
            @RequestParam Map<String, Object> updateStepDTO,
            @RequestParam(value = "photo", required = false) MultipartFile photo) {
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
        if (!scenario.getAuthorId().equals(userId) || !authService.isAdmin(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to update this step");
        }

        Optional<Step> existingStep = stepRepository.findById(id);
        if (existingStep.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found");
        }

        Step updatedStep = existingStep.get();
        updatedStep.setTitle((String) updateStepDTO.get("title"));
        updatedStep.setText((String) updateStepDTO.get("text"));
        updatedStep.setLongitude(updateStepDTO.get("longitude") != null ? Double.parseDouble((String) updateStepDTO.get("longitude")) : null);
        updatedStep.setLatitude(updateStepDTO.get("latitude") != null ? Double.parseDouble((String) updateStepDTO.get("latitude")) : null);

        if (photo != null && !photo.isEmpty()) {
            try {
                String photoUrl = imageUploadService.uploadImage(photo);
                updatedStep.setPhotoUrl(photoUrl);
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload photo", e);
            }
        }

        stepRepository.update(updatedStep, id);
    }

    // Metoda delete - Usuwa krok po id_step
    // StepController.java
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
        if (!scenario.getAuthorId().equals(userId) || !authService.isAdmin(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to delete this step");
        }

        stepRepository.delete(id);
    }
}
