package adventure.go.goadventure.step;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/step")
public class StepController {
    private final StepRepository stepRepository;

    public StepController(StepRepository stepRepository) {
        this.stepRepository = stepRepository;
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



    // Metoda create - Tworzy nowy krok
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("")
    public void create(@RequestBody Step step) {
        stepRepository.create(step);
    }

    // Metoda update - Aktualizuje krok po id_step
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/{id}")
    public void update(@RequestBody Step step, @PathVariable Integer id) {
        stepRepository.update(step, id);
    }

    // Metoda delete - Usuwa krok po id_step
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        stepRepository.delete(id);
    }
}
