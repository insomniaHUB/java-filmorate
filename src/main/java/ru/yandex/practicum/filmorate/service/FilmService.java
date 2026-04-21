package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.feed.Event;
import ru.yandex.practicum.filmorate.model.feed.EventType;
import ru.yandex.practicum.filmorate.model.feed.OperationType;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FilmService {
    private static final LocalDate THE_EARLIEST_DATA_RELEASE = LocalDate.of(1895, 12, 28);
    private static final int MAX_DESCRIPTION_LENGTH = 200;
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;
    private final FeedService feedService;
    private final DirectorStorage directorStorage;

    public Collection<Film> findAll() {
        Collection<Film> films = filmStorage.getAllFilms();

        Set<Long> filmIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toSet());

        Map<Long, Set<Genre>> genresMap = genreStorage.loadGenresForFilms(filmIds);
        Map<Long, Set<Director>> directorsMap = directorStorage.loadDirectorsForFilms(filmIds);

        for (Film film : films) {
            film.setGenres(genresMap.getOrDefault(film.getId(), Set.of()));
            film.setDirectors(directorsMap.getOrDefault(film.getId(), Set.of()));
        }

        return films;
    }

    public Film findFilm(Long id) {
        validateFilm(id);

        Film film = filmStorage.getFilmById(id);
        if (film != null) {
            film.setGenres(genreStorage.loadGenreObjects(film.getId()));
            film.setDirectors(directorStorage.loadDirectorObjects(film.getId()));
        }

        return film;
    }

    public Film create(Film film) {
        validate(film);
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            genreStorage.validateGenres(film.getGenres());
        }
        if (film.getMpa() != null && film.getMpa().getId() != null) {
            mpaStorage.mpaIdNotExist(film.getMpa().getId());
        }
        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            directorStorage.validateDirectors(film.getDirectors());
        }
        Film filmCreated = filmStorage.createFilm(film);
        if (filmCreated != null) {
            filmCreated.setGenres(genreStorage.loadGenreObjects(filmCreated.getId()));
            filmCreated.setDirectors(directorStorage.loadDirectorObjects(filmCreated.getId()));
        }
        log.info("Создан новый фильм.");
        return filmCreated;
    }

    public Film update(Film newFilm) {
        if (newFilm.getId() == null) {
            log.warn("Не указан Id при обновлении данных фильма.");
            throw new ConditionsNotMetException("Должен быть указан Id");
        }
        if (filmStorage.getFilmById(newFilm.getId()) == null) {
            log.warn("Фильм с Id = " + newFilm.getId() + " не был найден!");
            throw new NotFoundException("Фильм с Id = " + newFilm.getId() + " не был найден!");
        }
        if (filmStorage.getFilmById(newFilm.getId()) != null) {
            if (newFilm.getDescription() == null || newFilm.getReleaseDate() == null || newFilm.getDuration() == null) {
                log.warn("Не получилось изменить данные фильма из-за нехватки данных.");
                return filmStorage.getFilmById(newFilm.getId());
            }
            validate(newFilm);
            if (newFilm.getGenres() != null && !newFilm.getGenres().isEmpty()) {
                genreStorage.validateGenres(newFilm.getGenres());
            }
            if (newFilm.getMpa() != null && newFilm.getMpa().getId() != null) {
                mpaStorage.mpaIdNotExist(newFilm.getMpa().getId());
            }
            if (newFilm.getDirectors() != null && !newFilm.getDirectors().isEmpty()) {
                directorStorage.validateDirectors(newFilm.getDirectors());
            }

            log.info("Данные фильма успешно обновлены.");
            filmStorage.updateFilm(newFilm);

            return findFilm(newFilm.getId());
        }
        log.warn("Фильм с Id = " + newFilm.getId() + " не был найден!");
        throw new NotFoundException("Фильм с Id = " + newFilm.getId() + " не был найден!");
    }

    public Collection<Film> getFilmRecommendations(Long id) {
        validateUser(id);
        Collection<Film> films = filmStorage.getFilmRecommendations(id);

        Set<Long> filmIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toSet());

        Map<Long, Set<Genre>> genresMap = genreStorage.loadGenresForFilms(filmIds);
        Map<Long, Set<Director>> directorsMap = directorStorage.loadDirectorsForFilms(filmIds);

        for (Film film : films) {
            film.setGenres(genresMap.getOrDefault(film.getId(), Set.of()));
            film.setDirectors(directorsMap.getOrDefault(film.getId(), Set.of()));
        }

        return films;
    }

    public Collection<Film> commonFilmsByPopularity(Long userId, Long friendId) {
        validateUser(userId);
        validateUser(friendId);

        List<Film> films = filmStorage.commonFilmsByPopularity(userId, friendId);

        Set<Long> filmIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toSet());

        Map<Long, Set<Genre>> genresMap = genreStorage.loadGenresForFilms(filmIds);

        for (Film film : films) {
            film.setGenres(genresMap.getOrDefault(film.getId(), Set.of()));
        }

        return films;
    }

    public void addLike(Long id, Long idUser) {
        validateFilm(id);
        validateUser(idUser);

        filmStorage.addLike(id, idUser);
        filmStorage.getFilmById(id).getLiked().add(idUser);

        feedService.addEvent(Event.builder()
                .timestamp(Instant.now().toEpochMilli())
                .userId(idUser)
                .eventType(EventType.LIKE)
                .operation(OperationType.ADD)
                .entityId(id)
                .build());
    }

    public void deleteLike(Long id, Long idUser) {
        validateFilm(id);
        validateUser(idUser);

        filmStorage.deleteLike(id, idUser);
        filmStorage.getFilmById(id).getLiked().remove(idUser);

        feedService.addEvent(Event.builder()
                .timestamp(Instant.now().toEpochMilli())
                .userId(idUser)
                .eventType(EventType.LIKE)
                .operation(OperationType.REMOVE)
                .entityId(id)
                .build());
    }

    public List<Film> getMostPopularFilms(int count, Long genreId, Long year) {
        List<Film> films = filmStorage.getMostPopularFilms(count, genreId, year);

        if (films.isEmpty()) {
            return films;
        }

        Set<Long> filmIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toSet());

        Map<Long, Set<Genre>> genresMap = genreStorage.loadGenresForFilms(filmIds);
        Map<Long, Set<Director>> directorsMap = directorStorage.loadDirectorsForFilms(filmIds);

        for (Film film : films) {
            film.setGenres(genresMap.getOrDefault(film.getId(), new HashSet<>()));
            film.setDirectors(directorsMap.getOrDefault(film.getId(), new HashSet<>()));
        }

        return films;
    }

    public List<Film> getAllDirectorFilms(Long directorId, String sortBy) {
        directorStorage.getDirectorById(directorId);

        List<Film> films = filmStorage.loadFilmsForDirector(directorId);

        Set<Long> filmIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toSet());

        Map<Long, Set<Genre>> genresMap = genreStorage.loadGenresForFilms(filmIds);
        Map<Long, Set<Director>> directorsMap = directorStorage.loadDirectorsForFilms(filmIds);
        Map<Long, Set<Long>> likesMap = filmStorage.loadLikesForFilms(filmIds);

        for (Film film : films) {
            film.setGenres(genresMap.getOrDefault(film.getId(), new HashSet<>()));
            film.setDirectors(directorsMap.getOrDefault(film.getId(), new HashSet<>()));
            film.setLiked(likesMap.getOrDefault(film.getId(), new HashSet<>()));
        }

        if (sortBy.equals("year")) {
            films = films.stream()
                    .sorted(Comparator.comparing(Film::getReleaseDate))
                    .toList();
        } else if (sortBy.equals("likes")) {
            films = films.stream()
                    .sorted(Comparator.comparingInt((Film f) -> f.getLiked().size()).reversed())
                    .toList();
        }

        return films;
    }

    public List<Film> searchFilms(String query, String by) {
        Set<String> searchBy = Arrays.stream(by.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());

        List<Film> films = filmStorage.searchFilms(query, searchBy);

        if (!films.isEmpty()) {
            Set<Long> filmIds = films.stream()
                    .map(Film::getId)
                    .collect(Collectors.toSet());

            Map<Long, Set<Genre>> genresMap = genreStorage.loadGenresForFilms(filmIds);
            Map<Long, Set<Director>> directorsMap = directorStorage.loadDirectorsForFilms(filmIds);
            Map<Long, Set<Long>> likesMap = filmStorage.loadLikesForFilms(filmIds);

            for (Film film : films) {
                film.setGenres(genresMap.getOrDefault(film.getId(), Set.of()));
                film.setDirectors(directorsMap.getOrDefault(film.getId(), Set.of()));
                film.setLiked(likesMap.getOrDefault(film.getId(), Set.of()));
            }
        }

        return films;
    }

    public void deleteFilm(Long filmId) {
        validateFilm(filmId);
        filmStorage.deleteFilm(filmId);
    }

    private void validate(Film film) {
        if (film.getName() == null || film.getName().isBlank() || film.getDescription().length() > MAX_DESCRIPTION_LENGTH
                || film.getReleaseDate().isBefore(THE_EARLIEST_DATA_RELEASE)
                || film.getDuration() < 0) {
            log.warn("Произошла ошибка валидации при попытке создания нового фильма.");
            throw new ValidationException("Ошибка валидации!");
        }
    }

    private void validateFilm(Long id) {
        if (filmStorage.getFilmById(id) == null) {
            throw new NotFoundException("Фильм не найден");
        }
    }

    private void validateUser(Long id) {
        if (userStorage.getUserById(id) == null) {
            throw new NotFoundException("Пользователь не найден");
        }
    }
}