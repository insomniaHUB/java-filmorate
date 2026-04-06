package ru.yandex.practicum.filmorate.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.Collection;
import java.util.List;

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
}
