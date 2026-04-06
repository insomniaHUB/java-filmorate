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
    private final JdbcTemplate jdbc;
    private final RowMapper<Genre> mapper;

    public GenreDbStorage(JdbcTemplate jdbc, RowMapper<Genre> mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    @Override
    public Collection<Genre> getAllGenres() {
        String query = "SELECT * FROM genres";

        return jdbc.query(query, mapper);
    }

    @Override
    public Genre getGenreById(Long id) {
        String query = "SELECT * FROM genres WHERE genre_id = ?";

        List<Genre> results = jdbc.query(query, mapper, id);

        if (results.isEmpty()) {
            throw new NotFoundException("Жанр с таким id не был найден");
        }

        return results.getFirst();
    }

    @Override
    public Map<Long, Set<Genre>> loadGenresForFilms(Set<Long> filmIds) {
        String query = """
            SELECT fg.film_id, g.genre_id, g.genre
            FROM film_genres AS fg
            JOIN genres AS g ON fg.genre_id = g.genre_id
            WHERE fg.film_id IN (:ids)
            """;

        NamedParameterJdbcTemplate namedJdbc = new NamedParameterJdbcTemplate(jdbc);
        MapSqlParameterSource params = new MapSqlParameterSource("ids", filmIds);

        Map<Long, Set<Genre>> result = new HashMap<>();
        namedJdbc.query(query, params, rs -> {
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
        String query = """
        SELECT g.genre_id, g.genre
        FROM genres AS g
        JOIN film_genres AS fg ON g.genre_id = fg.genre_id
        WHERE fg.film_id = ?
        """;

        return new HashSet<>(jdbc.query(query, mapper, filmId));
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
        String query = "SELECT genre_id FROM genres";

        return new HashSet<>(jdbc.queryForList(query, Long.class));
    }
}
