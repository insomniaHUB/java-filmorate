package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

public interface FilmStorage {
    void deleteFilm(Long id);

    Film createFilm(Film user);

    Film updateFilm(Film newUser);
}
