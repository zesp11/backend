package adventure.go.goadventure.choice;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/choice")
public class ChoiceController {

    private final ChoiceRepository choiceRepository;

    public ChoiceController(ChoiceRepository choiceRepository) {
        this.choiceRepository = choiceRepository;
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

    // Metoda create - Tworzy nową opcję wyboru
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("")
    public void create(@RequestBody Choice choice) {
        choiceRepository.create(choice);
    }

    // Metoda update - Aktualizuje opcję wyboru po id_choice
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/{id}")
    public void update(@RequestBody Choice choice, @PathVariable Integer id) {
        choiceRepository.update(choice, id);
    }

    // Metoda delete - Usuwa opcję wyboru po id_choice
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        choiceRepository.delete(id);
    }
}
