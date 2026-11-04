package com.example.cinema;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/seances")
public class SeanceController {

    private static final int HISTOGRAM_DAYS = 30; // вариант Н–Т

    private final SeanceRepository repo;

    public SeanceController(SeanceRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Seance> list(@RequestParam(required = false) String q) {
        if (q == null || q.isBlank()) {
            return repo.findAllByOrderBySessionDateTimeAscFilmTitleAsc();
        }
        return repo.search(q.trim());
    }

    @GetMapping("/count")
    public Map<String, Long> count() {
        return Map.of("count", repo.count());
    }

    @PostMapping
    public Seance create(@Valid @RequestBody Seance s) {
        checkTickets(s);
        s.setId(null);
        return repo.save(s);
    }

    @PutMapping("/{id}")
    public Seance update(@PathVariable Long id, @Valid @RequestBody Seance s) {
        checkTickets(s);
        if (!repo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Сеанс не найден");
        }
        s.setId(id);
        return repo.save(s);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Сеанс не найден");
        }
        repo.deleteById(id);
    }

    // Гистограмма: количество сеансов по дням за последние 30 дней (включая сегодня)
    @GetMapping("/histogram")
    public List<Map<String, Object>> histogram() {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(HISTOGRAM_DAYS - 1L);
        LocalDateTime from = start.atStartOfDay();
        LocalDateTime to = today.plusDays(1).atStartOfDay();

        Map<LocalDate, Integer> counts = new LinkedHashMap<>();
        for (int i = 0; i < HISTOGRAM_DAYS; i++) {
            counts.put(start.plusDays(i), 0);
        }
        for (Seance s : repo.findBySessionDateTimeBetween(from, to.minusNanos(1))) {
            counts.merge(s.getSessionDateTime().toLocalDate(), 1, Integer::sum);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        counts.forEach((d, c) -> result.add(Map.of("date", d.toString(), "count", c)));
        return result;
    }

    private void checkTickets(Seance s) {
        if (s.getTicketsSold() > s.getTicketsTotal()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Продано билетов больше, чем всего на сеанс");
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> onValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getDefaultMessage()).findFirst().orElse("Некорректные данные");
        return Map.of("message", msg);
    }
}
