package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {

    void deleteFilm(Long id);

    Film createFilm(Film user);

    Film updateFilm(Film newUser);

    Collection<Film> getAllFilms();

    Film getFilm(Long id);
}
