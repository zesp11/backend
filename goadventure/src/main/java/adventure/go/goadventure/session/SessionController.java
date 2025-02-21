package adventure.go.goadventure.session;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/session")
public class SessionController {

    private final SessionRepository sessionRepository;

    public SessionController(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    // Metoda findAll - Zwraca wszystkie sesje
    @GetMapping("/all")
    public List<Session> findAll() {
        return sessionRepository.findAll();
    }

    // Metoda findById - Zwraca sesję na podstawie id_ses
    @GetMapping("/{id}")
    public Optional<Session> findById(@PathVariable Integer id) {
        return sessionRepository.findById(id);
    }

    // Metoda create - Tworzy nową sesję
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("")
    public void create(@RequestBody Session session) {
        sessionRepository.create(session);
    }

    // Metoda update - Aktualizuje sesję po id_ses
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/{id}")
    public void update(@RequestBody Session session, @PathVariable Integer id) {
        sessionRepository.update(session, id);
    }

    // Metoda delete - Usuwa sesję po id_ses
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        sessionRepository.delete(id);
    }
}