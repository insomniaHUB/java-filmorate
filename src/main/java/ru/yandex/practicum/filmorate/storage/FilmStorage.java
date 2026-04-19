package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface FilmStorage {

    void deleteFilm(Long id);

    Film createFilm(Film film);

    Film updateFilm(Film newFilm);

    Collection<Film> getAllFilms();

    List<Film> getMostPopularFilms(int count, Long genreId, Long year);

    Film getFilmById(Long id);

    void addLike(Long id, Long idUser);

    void deleteLike(Long id, Long idUser);

    List<Film> loadFilmsForDirector(Long id);

    Map<Long, Set<Long>> loadLikesForFilms(Set<Long> filmIds);

    List<Film> commonFilmsByPopularity(Long userId, Long friendId);

    List<Film> searchFilms(String query, Set<String> searchBy);
}
