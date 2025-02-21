package adventure.go.goadventure.step_choice;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/step_choice")
public class StepChoiceController {

    private final StepChoiceRepository stepChoiceRepository;

    public StepChoiceController(StepChoiceRepository stepChoiceRepository) {
        this.stepChoiceRepository = stepChoiceRepository;
    }

    // Metoda findAll - Zwraca wszystkie powiązania kroków i wyborów
    @GetMapping("/all")
    public List<StepChoice> findAll() {
        return stepChoiceRepository.findAll();
    }

    // Metoda findById - Zwraca powiązanie kroków i wyborów po id_step_choice
    @GetMapping("/{id}")
    public Optional<StepChoice> findById(@PathVariable Integer id) {
        return stepChoiceRepository.findById(id);
    }

    // Metoda create - Tworzy nowe powiązanie kroków i wyborów
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("")
    public void create(@RequestBody StepChoice stepChoice) {
        stepChoiceRepository.create(stepChoice);
    }

    // Metoda update - Aktualizuje powiązanie kroków i wyborów po id_step_choice
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/{id}")
    public void update(@RequestBody StepChoice stepChoice, @PathVariable Integer id) {
        stepChoiceRepository.update(stepChoice, id);
    }

    // Metoda delete - Usuwa powiązanie kroków i wyborów po id_step_choice
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        stepChoiceRepository.delete(id);
    }
}
