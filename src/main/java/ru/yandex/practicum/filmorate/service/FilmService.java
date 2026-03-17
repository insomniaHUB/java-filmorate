package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    private static final LocalDate THE_EARLIEST_DATA_RELEASE = LocalDate.of(1895, 12, 28);
    private static final int MAX_DESCRIPTION_LENGTH = 200;
    private long id = 1;
    private final InMemoryFilmStorage filmStorage;
    private final InMemoryUserStorage userStorage;

    @Autowired
    public FilmService(InMemoryFilmStorage filmStorage, InMemoryUserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Collection<Film> findAll() {
        return filmStorage.getAllFilms();
    }

    public Film findFilm(Long id) {
        if (!filmStorage.getFilmsMap().containsKey(id)) {
            throw new NotFoundException("Фильм не найден");
        }
        return filmStorage.getFilmsMap().get(id);
    }

    public Film create(Film film) {
        validate(film);

        film.setId(id);
        log.info("Создан новый фильм.");
        id++;
        return filmStorage.createFilm(film);
    }

    public Film update(Film newFilm) {
        if (newFilm.getId() == null) {
            log.info("Не указан Id при обновлении данных фильма.");
            throw new ConditionsNotMetException("Должен быть указан Id");
        }
        if (filmStorage.getFilmsMap().containsKey(newFilm.getId())) {
            if (newFilm.getDescription() == null || newFilm.getReleaseDate() == null || newFilm.getDuration() == null) {
                log.info("Не получилось изменить данные фильма из-за нехватки данных.");
                return filmStorage.getFilmsMap().get(newFilm.getId());
            }
            validate(newFilm);

            log.info("Данные фильма успешно обновлены.");
            return filmStorage.updateFilm(newFilm);
        }
        log.info("Пользователь с Id = " + newFilm.getId() + " не был найден!");
        throw new NotFoundException("Фильм с Id = " + newFilm.getId() + " не был найден!");
    }

    public void addLike(Long id, Long idUser) {
        if (!filmStorage.getFilmsMap().containsKey(id)) {
            throw new NotFoundException("Фильм не найден");
        }
        if (!userStorage.getUsersMap().containsKey(idUser)) {
            throw new NotFoundException("Пользователь не найден");
        }
        filmStorage.getFilmsMap().get(id).getLiked().add(idUser);
    }

    public void deleteLike(Long id, Long idUser) {
        if (!filmStorage.getFilmsMap().containsKey(id)) {
            throw new NotFoundException("Фильм не найден");
        }
        if (!userStorage.getUsersMap().containsKey(idUser)) {
            throw new NotFoundException("Пользователь не найден");
        }
        filmStorage.getFilmsMap().get(id).getLiked().remove(idUser);
    }

    public List<Film> getMostPopularFilms(int count) {
        return filmStorage.getFilmsMap().values().stream()
                .sorted(Comparator.comparingInt((Film film) -> film.getLiked().size()).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    private void validate(Film film) {
        if (film.getName() == null || film.getName().isBlank() || film.getDescription().length() > MAX_DESCRIPTION_LENGTH
                || film.getReleaseDate().isBefore(THE_EARLIEST_DATA_RELEASE)
                || film.getDuration() < 0) {
            log.info("Произошла ошибка валидации при попытке создания нового фильма.");
            throw new ValidationException("Ошибка валидации!");
        }
    }
}
