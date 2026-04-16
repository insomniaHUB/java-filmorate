package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class DirectorDbStorage implements DirectorStorage {
    private static final String GET_ALL_DIRECTORS_QUERY = "SELECT * FROM directors";
    private static final String GET_DIRECTOR_BY_ID_QUERY = "SELECT * FROM directors WHERE director_id = ?";
    private static final String CREATE_DIRECTOR_QUERY = "INSERT INTO directors (director_name) VALUES (?)";
    private static final String UPDATE_DIRECTOR_QUERY = "UPDATE directors SET director_name = ? WHERE director_id = ?";
    private static final String DELETE_DIRECTOR_QUERY = "DELETE FROM directors WHERE director_id = ?";
    private static final String LOAD_DIRECTORS_FOR_FILMS_QUERY = "SELECT fd.film_id, d.director_id, d.director_name " +
            "FROM film_directors AS fd JOIN directors AS d ON fd.director_id=d.director_id WHERE fd.film_id IN (:ids)";
    private static final String LOAD_DIRECTOR_QUERY = "SELECT fd.film_id, d.director_id, d.director_name " +
            "FROM directors AS d JOIN film_directors AS fd ON d.director_id=fd.director_id WHERE fd.film_id = ?";
    private static final String GET_DIRECTORS_ID_QUERY = "SELECT director_id FROM directors";
    private final JdbcTemplate jdbc;
    private final DirectorRowMapper mapper;

    @Override
    public List<Director> getAllDirectors() {
        return jdbc.query(GET_ALL_DIRECTORS_QUERY, mapper);
    }

    @Override
    public Director getDirectorById(Long id) {
        try {
            return jdbc.queryForObject(GET_DIRECTOR_BY_ID_QUERY, mapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Не удалось найти режиссера по указанному id");
        }
    }

    @Override
    public Director createDirector(Director director) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(CREATE_DIRECTOR_QUERY, Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, director.getName());
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKeyAs(Long.class);
        if (id != null) {
            director.setId(id);
            return director;
        } else {
            throw new InternalServerException("Не удалось сохранить данные режиссера");
        }
    }

    @Override
    public Director updateDirector(Director newDirector) {
        int rowsUpdated = jdbc.update(UPDATE_DIRECTOR_QUERY,
                newDirector.getName(),
                newDirector.getId()
        );
        if (rowsUpdated == 0) {
            throw new NotFoundException("Не удалось обновить данные режиссера");
        }

        return newDirector;
    }

    @Override
    public void deleteDirector(Long id) {
        jdbc.update(DELETE_DIRECTOR_QUERY, id);
    }

    @Override
    public Map<Long, Set<Director>> loadDirectorsForFilms(Set<Long> filmIds) {
        NamedParameterJdbcTemplate namedJdbc = new NamedParameterJdbcTemplate(jdbc);
        MapSqlParameterSource params = new MapSqlParameterSource("ids", filmIds);

        Map<Long, Set<Director>> result = new HashMap<>();
        namedJdbc.query(LOAD_DIRECTORS_FOR_FILMS_QUERY, params, rs -> {
            Long filmId = rs.getLong("film_id");
            Director director = new Director();
            director.setId(rs.getLong("director_id"));
            director.setName(rs.getString("director_name"));
            result.computeIfAbsent(filmId, k -> new HashSet<>()).add(director);
        });

        return result;
    }

    @Override
    public Set<Director> loadDirectorObjects(Long filmId) {
        return new HashSet<>(jdbc.query(LOAD_DIRECTOR_QUERY, mapper, filmId));
    }

    @Override
    public void validateDirectors(Set<Director> directors) {
        Set<Long> requestedIds = directors.stream()
                .map(Director::getId)
                .collect(Collectors.toSet());

        if (requestedIds.isEmpty()) {
            return;
        }

        Set<Long> existingIds = getExistingDirectorIds();

        for (Long id : requestedIds) {
            if (!existingIds.contains(id)) {
                throw new NotFoundException("Режиссера с id=" + id + " не существует");
            }
        }
    }

    private Set<Long> getExistingDirectorIds() {
        return new HashSet<>(jdbc.queryForList(GET_DIRECTORS_ID_QUERY, Long.class));
    }
}
