package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {

    void deleteFilm(Long id);

    Film createFilm(Film film);

    Film updateFilm(Film newFilm);

    Collection<Film> getAllFilms();

    Film getFilmById(Long id);

    void addLike(Long id, Long idUser);

    void deleteLike(Long id, Long idUser);
}
