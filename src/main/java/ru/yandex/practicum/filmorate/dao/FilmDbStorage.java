package ru.yandex.practicum.filmorate.dao;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbc;
    private final RowMapper<Film> mapper;

    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    @Override
    public List<Film> getAllFilms() {
        String query = "SELECT f.*, m.rating FROM films AS f " +
        "LEFT JOIN motion_picture_association AS m ON f.mpa = m.mpa_id";

        List<Film> films = jdbc.query(query, mapper);

        Set<Long> filmIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toSet());

        Map<Long, Set<Long>> likesMap = loadLikesForFilms(filmIds);

        for (Film film : films) {
            film.setLiked(likesMap.getOrDefault(film.getId(), Set.of()));
        }

        return films;
    }

    @Override
    public Film getFilmById(Long id) {
        String query = "SELECT f.*, m.rating FROM films AS f " +
                "LEFT JOIN motion_picture_association AS m ON f.mpa = m.mpa_id WHERE film_id = ?";

        try {
            Film film = jdbc.queryForObject(query, mapper, id);

            if (film != null) {
                film.setLiked(loadLikes(film.getId()));
            }

            return film;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public Film createFilm(Film film) {
        String query = "INSERT INTO films (name, description, duration, release_date, mpa) VALUES (?, ?, ?, ?, ?)";

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, film.getName());
            ps.setObject(2, film.getDescription());
            ps.setObject(3, film.getDuration());
            ps.setObject(4, Date.valueOf(film.getReleaseDate()));
            ps.setObject(5, film.getMpa().getId());
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKeyAs(Long.class);
        if (id != null) {
            film.setId(id);
        } else {
            throw new InternalServerException("Не удалось сохранить данные фильма");
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            saveFilmGenres(id, film.getGenres());
        }

        return film;
    }

    @Override
    public Film updateFilm(Film newFilm) {
        String query = "UPDATE films SET name = ?, description = ?, duration = ?, release_date = ?, mpa = ? WHERE film_id = ?";
        int rowsUpdated = jdbc.update(query,
                newFilm.getName(),
                newFilm.getDescription(),
                newFilm.getDuration(),
                Date.valueOf(newFilm.getReleaseDate()),
                newFilm.getMpa().getId(),
                newFilm.getId()
        );
        if (rowsUpdated == 0) {
            throw new NotFoundException("Не удалось обновить данные фильма");
        }

        return newFilm;
    }

    @Override
    public void deleteFilm(Long id) {
        String query = "DELETE FROM films WHERE film_id = ?";
        jdbc.update(query, id);
    }

    @Override
    public void addLike(Long id, Long idUser) {
        String query = "INSERT INTO likes (user_id, film_id) VALUES (?, ?)";
        jdbc.update(query, idUser, id);
    }

    @Override
    public void deleteLike(Long id, Long idUser) {
        String query = "DELETE FROM likes WHERE user_id = ? AND film_id = ?";
        jdbc.update(query, idUser, id);
    }

    private Map<Long, Set<Long>> loadLikesForFilms(Set<Long> filmIds) {
        String query = "SELECT film_id, user_id FROM likes WHERE film_id IN (:ids)";

        NamedParameterJdbcTemplate namedJdbc = new NamedParameterJdbcTemplate(jdbc);
        MapSqlParameterSource params = new MapSqlParameterSource("ids", filmIds);

        Map<Long, Set<Long>> result = new HashMap<>();
        namedJdbc.query(query, params, rs -> {
            Long filmId = rs.getLong("film_id");
            Long userId = rs.getLong("user_id");
            result.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
        });

        return result;
    }

    private void saveFilmGenres(Long filmId, Set<Genre> genres) {
        String query = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";

        Set<Long> genreIds = genres.stream().map(Genre::getId).collect(Collectors.toSet());

        for (Long genreId : genreIds) {
            jdbc.update(query, filmId, genreId);
        }
    }

    private Set<Long> loadLikes(Long filmId) {
        String query = "SELECT user_id FROM likes WHERE film_id = ?";

        return new HashSet<>(jdbc.queryForList(query, Long.class, filmId));
    }
}