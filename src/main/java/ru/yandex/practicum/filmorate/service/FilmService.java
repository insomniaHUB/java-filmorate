package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

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
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Collection<Film> findAll() {
        return filmStorage.getAllFilms();
    }

    public Film findFilm(Long id) {
        validateFilm(id);

        return filmStorage.getFilm(id);
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
        if (filmStorage.getFilm(newFilm.getId()) != null) {
            if (newFilm.getDescription() == null || newFilm.getReleaseDate() == null || newFilm.getDuration() == null) {
                log.info("Не получилось изменить данные фильма из-за нехватки данных.");
                return filmStorage.getFilm(newFilm.getId());
            }
            validate(newFilm);

            log.info("Данные фильма успешно обновлены.");
            return filmStorage.updateFilm(newFilm);
        }
        log.info("Пользователь с Id = " + newFilm.getId() + " не был найден!");
        throw new NotFoundException("Фильм с Id = " + newFilm.getId() + " не был найден!");
    }

    public void addLike(Long id, Long idUser) {
        validateFilm(id);
        validateUser(idUser);

        filmStorage.getFilm(id).getLiked().add(idUser);
    }

    public void deleteLike(Long id, Long idUser) {
        validateFilm(id);
        validateUser(idUser);

        filmStorage.getFilm(id).getLiked().remove(idUser);
    }

    public List<Film> getMostPopularFilms(int count) {
        return filmStorage.getAllFilms().stream()
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

    private void validateFilm(Long id) {
        if (filmStorage.getFilm(id) == null) {
            throw new NotFoundException("Фильм не найден");
        }
    }

    private void validateUser(Long id) {
        if (userStorage.getUser(id) == null) {
            throw new NotFoundException("Пользователь не был найден");
        }
    }
}
