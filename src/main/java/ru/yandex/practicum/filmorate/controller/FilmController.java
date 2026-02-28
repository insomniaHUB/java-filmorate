package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        if (film.getName() == null || film.getName().isBlank() || film.getDescription().length() > 200
                || film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))
                || film.getDuration() < 0) {
            throw new ValidationException("Ошибка валидации!");
        }
        film.setId(getNextId());
        films.put(film.getId(), film);
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        if (newFilm.getId() == null) {
            throw new ConditionsNotMetException("Должен быть указан Id");
        }
        if (films.containsKey(newFilm.getId())) {
            if (newFilm.getDescription() == null || newFilm.getReleaseDate() == null || newFilm.getDuration() == null) {
                return films.get(newFilm.getId());
            }
            if (newFilm.getName() == null || newFilm.getName().isBlank() || newFilm.getDescription().length() > 200
                    || newFilm.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))
                    || newFilm.getDuration() < 0) {
                throw new ValidationException("Ошибка валидации!");
            }
            Film oldFilm = films.get(newFilm.getId());
            oldFilm.setName(newFilm.getName());
            oldFilm.setDescription(newFilm.getDescription());
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
            oldFilm.setDuration(newFilm.getDuration());
            return oldFilm;
        }
        throw new NotFoundException("Фильм с Id = " + newFilm.getId() + " не был найден!");
    }

    private long getNextId() {
        long currentMax = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMax;
    }

}
