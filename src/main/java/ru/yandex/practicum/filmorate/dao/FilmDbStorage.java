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
import ru.yandex.practicum.filmorate.model.Director;
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
            "LEFT JOIN motion_picture_association AS m ON f.mpa = m.mpa_id ";
    private static final String GET_FILM_BY_ID_QUERY = "SELECT f.*, m.rating FROM films AS f " +
            "LEFT JOIN motion_picture_association AS m ON f.mpa = m.mpa_id WHERE film_id = ?";
    private static final String GET_COMMON_FILMS_QUERY = "SELECT f.*, m.rating FROM films AS f LEFT JOIN " +
            "motion_picture_association AS m ON f.mpa = m.mpa_id WHERE f.film_id IN (SELECT l1.film_id FROM likes AS l1 " +
            " JOIN likes AS l2 ON l1.film_id = l2.film_id WHERE l1.user_id = ? AND l2.user_id = ?) ORDER BY (SELECT COUNT(*) " +
            " FROM likes AS l WHERE l.film_id = f.film_id) DESC;";
    private static final String GET_RECOMMENDATIONS_FILMS_QUERY = "SELECT f.*, m.rating FROM films AS f LEFT JOIN " +
            "motion_picture_association AS m ON f.mpa = m.mpa_id WHERE f.film_id IN (SELECT l2.film_id FROM likes l2 " +
            "WHERE l2.user_id = (SELECT l3.user_id FROM likes l3 WHERE l3.film_id IN (SELECT film_id FROM likes WHERE " +
            "user_id = ?) AND l3.user_id != ? GROUP BY l3.user_id ORDER BY COUNT(*) DESC, l3.user_id ASC LIMIT 1)) AND " +
            "f.film_id NOT IN (SELECT film_id FROM likes WHERE user_id = ?) ORDER BY f.film_id;";
    private static final String CREATE_FILM_QUERY = "INSERT INTO films (name, description, " +
            "duration, release_date, mpa) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_FILM_QUERY = "UPDATE films SET name = ?, description = ?, duration = ?, " +
            "release_date = ?, mpa = ? WHERE film_id = ?";
    private static final String DELETE_FILM_QUERY = "DELETE FROM films WHERE film_id = ?";
    private static final String ADD_LIKE_QUERY = "INSERT INTO likes (user_id, film_id) VALUES (?, ?)";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM likes WHERE user_id = ? AND film_id = ?";
    private static final String LOAD_LIKES_FOR_FILM_QUERY = "SELECT film_id, user_id FROM likes WHERE film_id IN (:ids)";
    private static final String SAVE_FILM_GENRES_QUERY = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
    private static final String SAVE_FILM_DIRECTORS_QUERY = "INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)";
    private static final String LOAD_LIKES_QUERY = "SELECT user_id FROM likes WHERE film_id = ?";
    private static final String LOAD_FILMS_FOR_DIRECTOR_QUERY = "SELECT f.*, m.rating FROM films AS f " +
            "JOIN film_directors AS fd ON f.film_id=fd.film_id " +
            "LEFT JOIN motion_picture_association AS m ON f.mpa = m.mpa_id WHERE fd.director_id = ?";
    private static final String DELETE_FILM_DIRECTORS_QUERY = "DELETE FROM film_directors WHERE film_id = ?";
    private static final String CHECK_LIKE_QUERY = "SELECT COUNT(*) FROM likes WHERE user_id = ? AND film_id = ?";
    private static final String DELETE_FILM_GENRES_QUERY = "DELETE FROM film_genres WHERE film_id = ?";

    private final JdbcTemplate jdbc;
    private final RowMapper<Film> mapper;

    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    @Override
    public List<Film> getAllFilms() {
        return jdbc.query(GET_ALL_FILMS_QUERY, mapper);
    }

    @Override
    public List<Film> getMostPopularFilms(int count, Long genreId, Long year) {
        StringBuilder sql = new StringBuilder(
                "SELECT f.*, m.rating " +
                        "FROM films AS f " +
                        "LEFT JOIN motion_picture_association AS m ON f.mpa = m.mpa_id " +
                        "LEFT JOIN likes AS l ON f.film_id = l.film_id "
        );
        List<Object> params = new ArrayList<>();

        if (genreId != null) {
            sql.append("JOIN film_genres fg ON f.film_id = fg.film_id ");
        }

        sql.append("WHERE 1=1 ");

        if (genreId != null) {
            sql.append("AND fg.genre_id = ? ");
            params.add(genreId);
        }

        if (year != null) {
            sql.append("AND EXTRACT(YEAR FROM f.release_date) = ? ");
            params.add(year);
        }

        sql.append("GROUP BY f.film_id, m.rating " +
                "ORDER BY COUNT(l.user_id) DESC " +
                "LIMIT ?");
        params.add(count);

        return jdbc.query(sql.toString(), mapper, params.toArray());
    }

    public List<Film> searchFilms(String query, Set<String> searchBy) {
        StringBuilder sql = new StringBuilder(
                "SELECT f.*, m.rating " +
                        "FROM films AS f " +
                        "LEFT JOIN motion_picture_association AS m ON f.mpa = m.mpa_id "
        );

        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        String likePattern = "%" + query.toLowerCase() + "%";

        if (searchBy.contains("director")) {
            sql.append("LEFT JOIN film_directors fd ON f.film_id = fd.film_id " +
                    "LEFT JOIN directors d ON fd.director_id = d.director_id");
            conditions.add("LOWER(d.director_name) LIKE ?");
            params.add(likePattern);
        }
        if (searchBy.contains("title")) {
            conditions.add("LOWER(f.name) LIKE ?");
            params.add(likePattern);
        }

        if (!conditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" OR ", conditions));
        }

        sql.append(" ORDER BY (SELECT COUNT(*) FROM likes l WHERE l.film_id = f.film_id) DESC");

        return jdbc.query(sql.toString(), mapper, params.toArray());
    }

    @Override
    public Film getFilmById(Long id) {
        try {
            return jdbc.queryForObject(GET_FILM_BY_ID_QUERY, mapper, id);
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
        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            saveFilmDirectors(id, film.getDirectors());
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

        jdbc.update(DELETE_FILM_DIRECTORS_QUERY, newFilm.getId());
        if (newFilm.getDirectors() != null && !newFilm.getDirectors().isEmpty()) {
            saveFilmDirectors(newFilm.getId(), newFilm.getDirectors());
        }
        jdbc.update(DELETE_FILM_GENRES_QUERY, newFilm.getId());
        if (newFilm.getGenres() != null && !newFilm.getGenres().isEmpty()) {
            saveFilmGenres(newFilm.getId(), newFilm.getGenres());
        }
        return newFilm;
    }

    @Override
    public void deleteFilm(Long id) {
        jdbc.update(DELETE_FILM_QUERY, id);
    }

    @Override
    public void addLike(Long id, Long idUser) {
        Integer count = jdbc.queryForObject(CHECK_LIKE_QUERY, Integer.class, idUser, id);

        if (count > 0) {
            return;
        }
        jdbc.update(ADD_LIKE_QUERY, idUser, id);
    }

    @Override
    public void deleteLike(Long id, Long idUser) {
        jdbc.update(DELETE_LIKE_QUERY, idUser, id);
    }

    @Override
    public List<Film> loadFilmsForDirector(Long id) {
        return new ArrayList<>(jdbc.query(LOAD_FILMS_FOR_DIRECTOR_QUERY, mapper, id));
    }

    public List<Film> commonFilmsByPopularity(Long userId, Long friendId) {
        return jdbc.query(GET_COMMON_FILMS_QUERY, mapper, userId, friendId);
    }

    @Override
    public List<Film> getFilmRecommendations(Long id) {
        return jdbc.query(GET_RECOMMENDATIONS_FILMS_QUERY, mapper, id, id, id);
    }

    @Override
    public Map<Long, Set<Long>> loadLikesForFilms(Set<Long> filmIds) {
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

    @Override
    public Set<Long> loadLikes(Long filmId) {
        return new HashSet<>(jdbc.queryForList(LOAD_LIKES_QUERY, Long.class, filmId));
    }

    private void saveFilmGenres(Long filmId, Set<Genre> genres) {
        Set<Long> genreIds = genres.stream().map(Genre::getId).collect(Collectors.toSet());

        for (Long genreId : genreIds) {
            jdbc.update(SAVE_FILM_GENRES_QUERY, filmId, genreId);
        }
    }

    private void saveFilmDirectors(Long filmId, Set<Director> directors) {
        Set<Long> directorsIds = directors.stream().map(Director::getId).collect(Collectors.toSet());

        for (Long directorId : directorsIds) {
            jdbc.update(SAVE_FILM_DIRECTORS_QUERY, filmId, directorId);
        }
    }
}