package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DirectorStorage {
    List<Director> getAllDirectors();

    Director getDirectorById(Long id);

    Director createDirector(Director director);

    Director updateDirector(Director newDirector);

    void deleteDirector(Long id);

    Map<Long, Set<Director>> loadDirectorsForFilms(Set<Long> filmIds);

    Set<Director> loadDirectorObjects(Long filmId);

    void validateDirectors(Set<Director> directors);
}
