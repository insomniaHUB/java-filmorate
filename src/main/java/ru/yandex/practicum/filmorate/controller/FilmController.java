package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> findAll() {
        return filmService.findAll();
    }

    @GetMapping("/{id}")
    public Film findFilm(@PathVariable Long id) {
        return filmService.findFilm(id);
    }

    @GetMapping("/popular")
    public List<Film> getMostPopularFilms(@RequestParam(defaultValue = "10") int count,
                                          @RequestParam(required = false) Long genreId,
                                          @RequestParam(required = false) Long year) {
        return filmService.getMostPopularFilms(count, genreId, year);
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        return filmService.create(film);
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        return filmService.update(newFilm);
    }

    @PutMapping("/{filmId}/like/{id}")
    public void addLike(@PathVariable Long filmId,
                        @PathVariable Long id) {
        filmService.addLike(filmId, id);
    }

    @DeleteMapping("/{filmId}/like/{id}")
    public void deleteLike(@PathVariable Long filmId,
                           @PathVariable Long id) {
        filmService.deleteLike(filmId, id);
    }

}
