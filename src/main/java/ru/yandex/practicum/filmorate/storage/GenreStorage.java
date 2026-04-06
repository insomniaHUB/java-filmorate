package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface GenreStorage {
    Collection<Genre> getAllGenres();

    Genre getGenreById(Long id);

    Set<Genre> loadGenreObjects(Long filmId);

    Map<Long, Set<Genre>> loadGenresForFilms(Set<Long> filmIds);

    void validateGenres(Set<Genre> genres);
}
