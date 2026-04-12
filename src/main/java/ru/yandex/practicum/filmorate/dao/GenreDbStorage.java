package ru.yandex.practicum.filmorate.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class GenreDbStorage implements GenreStorage {
    private static final String GET_ALL_GENRES_QUERY = "SELECT * FROM genres";
    private static final String GET_GENRE_BY_ID_QUERY = "SELECT * FROM genres WHERE genre_id = ?";
    private static final String LOAD_GENRES_FOR_FILMS_QUERY = "SELECT fg.film_id, g.genre_id, g.genre " +
            "FROM film_genres AS fg JOIN genres AS g ON fg.genre_id = g.genre_id WHERE fg.film_id IN (:ids)";
    private static final String LOAD_GENRE_QUERY = "SELECT g.genre_id, g.genre FROM genres AS g " +
            "JOIN film_genres AS fg ON g.genre_id = fg.genre_id WHERE fg.film_id = ?";
    private static final String GET_GENRES_ID_QUERY = "SELECT genre_id FROM genres";
    private final JdbcTemplate jdbc;
    private final RowMapper<Genre> mapper;

    public GenreDbStorage(JdbcTemplate jdbc, RowMapper<Genre> mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    @Override
    public Collection<Genre> getAllGenres() {
        return jdbc.query(GET_ALL_GENRES_QUERY, mapper);
    }

    @Override
    public Genre getGenreById(Long id) {
        List<Genre> results = jdbc.query(GET_GENRE_BY_ID_QUERY, mapper, id);

        if (results.isEmpty()) {
            throw new NotFoundException("Жанр с таким id не был найден");
        }

        return results.getFirst();
    }

    @Override
    public Map<Long, Set<Genre>> loadGenresForFilms(Set<Long> filmIds) {
        NamedParameterJdbcTemplate namedJdbc = new NamedParameterJdbcTemplate(jdbc);
        MapSqlParameterSource params = new MapSqlParameterSource("ids", filmIds);

        Map<Long, Set<Genre>> result = new HashMap<>();
        namedJdbc.query(LOAD_GENRES_FOR_FILMS_QUERY, params, rs -> {
            Long filmId = rs.getLong("film_id");
            Genre genre = new Genre();
            genre.setId(rs.getLong("genre_id"));
            genre.setName(rs.getString("genre"));
            result.computeIfAbsent(filmId, k -> new HashSet<>()).add(genre);
        });

        return result;
    }

    @Override
    public Set<Genre> loadGenreObjects(Long filmId) {
        return new HashSet<>(jdbc.query(LOAD_GENRE_QUERY, mapper, filmId));
    }

    @Override
    public void validateGenres(Set<Genre> genres) {
        Set<Long> requestedIds = genres.stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());

        if (requestedIds.isEmpty()) {
            return;
        }

        Set<Long> existingIds = getExistingGenreIds();

        for (Long id : requestedIds) {
            if (!existingIds.contains(id)) {
                throw new NotFoundException("Жанра с id=" + id + " не существует");
            }
        }
    }

    private Set<Long> getExistingGenreIds() {
        return new HashSet<>(jdbc.queryForList(GET_GENRES_ID_QUERY, Long.class));
    }
}
