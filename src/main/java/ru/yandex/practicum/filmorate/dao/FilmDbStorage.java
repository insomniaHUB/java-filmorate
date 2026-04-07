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
    private static final String GET_ALL_FILMS_QUERY = "SELECT f.*, m.rating FROM films AS f " +
            "LEFT JOIN motion_picture_association AS m ON f.mpa = m.mpa_id";
    private static final String GET_FILM_BY_ID_QUERY = "SELECT f.*, m.rating FROM films AS f " +
            "LEFT JOIN motion_picture_association AS m ON f.mpa = m.mpa_id WHERE film_id = ?";
    private static final String CREATE_FILM_QUERY = "INSERT INTO films (name, description, " +
            "duration, release_date, mpa) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_FILM_QUERY = "UPDATE films SET name = ?, description = ?, duration = ?, " +
            "release_date = ?, mpa = ? WHERE film_id = ?";
    private static final String DELETE_FILM_QUERY = "DELETE FROM films WHERE film_id = ?";
    private static final String ADD_LIKE_QUERY = "INSERT INTO likes (user_id, film_id) VALUES (?, ?)";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM likes WHERE user_id = ? AND film_id = ?";
    private static final String LOAD_LIKES_FOR_FILM_QUERY = "SELECT film_id, user_id FROM likes WHERE film_id IN (:ids)";
    private static final String SAVE_FILM_GENRES_QUERY = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
    private static final String LOAD_LIKES_QUERY = "SELECT user_id FROM likes WHERE film_id = ?";
    private final JdbcTemplate jdbc;
    private final RowMapper<Film> mapper;

    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    @Override
    public List<Film> getAllFilms() {
        List<Film> films = jdbc.query(GET_ALL_FILMS_QUERY, mapper);

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
        try {
            Film film = jdbc.queryForObject(GET_FILM_BY_ID_QUERY, mapper, id);

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
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(CREATE_FILM_QUERY, Statement.RETURN_GENERATED_KEYS);
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
        int rowsUpdated = jdbc.update(UPDATE_FILM_QUERY,
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
        jdbc.update(DELETE_FILM_QUERY, id);
    }

    @Override
    public void addLike(Long id, Long idUser) {
        jdbc.update(ADD_LIKE_QUERY, idUser, id);
    }

    @Override
    public void deleteLike(Long id, Long idUser) {
        jdbc.update(DELETE_LIKE_QUERY, idUser, id);
    }

    private Map<Long, Set<Long>> loadLikesForFilms(Set<Long> filmIds) {
        NamedParameterJdbcTemplate namedJdbc = new NamedParameterJdbcTemplate(jdbc);
        MapSqlParameterSource params = new MapSqlParameterSource("ids", filmIds);

        Map<Long, Set<Long>> result = new HashMap<>();
        namedJdbc.query(LOAD_LIKES_FOR_FILM_QUERY, params, rs -> {
            Long filmId = rs.getLong("film_id");
            Long userId = rs.getLong("user_id");
            result.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
        });

        return result;
    }

    private void saveFilmGenres(Long filmId, Set<Genre> genres) {
        Set<Long> genreIds = genres.stream().map(Genre::getId).collect(Collectors.toSet());

        for (Long genreId : genreIds) {
            jdbc.update(SAVE_FILM_GENRES_QUERY, filmId, genreId);
        }
    }

    private Set<Long> loadLikes(Long filmId) {
        return new HashSet<>(jdbc.queryForList(LOAD_LIKES_QUERY, Long.class, filmId));
    }
}