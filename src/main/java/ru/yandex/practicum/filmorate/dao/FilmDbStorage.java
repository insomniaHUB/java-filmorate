package ru.yandex.practicum.filmorate.dao;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MotionPicture;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbc;
    private final RowMapper<Film> mapper;
    private final RowMapper<Genre> genreMapper;
    private final RowMapper<MotionPicture> mpaMapper;

    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper,
                         RowMapper<MotionPicture> mpaMapper, RowMapper<Genre> genreMapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
        this.mpaMapper = mpaMapper;
        this.genreMapper = genreMapper;
    }

    @Override
    public List<Film> getAllFilms() {
        String query = "SELECT * FROM films";

        List<Film> films = jdbc.query(query, mapper);

        for (Film film : films) {
            film.setMpa(loadMotionPicture(film.getMpa().getId()));
            film.setLiked(loadLikes(film.getId()));
            film.setGenres(loadGenreObjects(film.getId()));
        }

        return films;
    }

    @Override
    public Film getFilmById(Long id) {
        String query = "SELECT * FROM films WHERE film_id = ?";

        try {
            Film film = jdbc.queryForObject(query, mapper, id);

            if (film != null) {
                film.setMpa(loadMotionPicture(film.getMpa().getId()));
                film.setLiked(loadLikes(film.getId()));
                film.setGenres(loadGenreObjects(film.getId()));
            }

            return film;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public Film createFilm(Film film) {
        if (mpaIdNotExist(film.getMpa().getId())) {
            throw new NotFoundException("Возрастного рейтинга с таким id не существует");
        }

        for (Genre genre : film.getGenres()) {
            if (genreIdNotExist(genre.getId())) {
                throw new NotFoundException("Жанра с таким id не существует");
            }
        }

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

    private MotionPicture loadMotionPicture(Long id) {
        String query = "SELECT * FROM motion_picture_association WHERE mpa_id = ?";

        return jdbc.queryForObject(query, mpaMapper, id);
    }

    private Set<Genre> loadGenreObjects(Long filmId) {
        String query = """
        SELECT g.genre_id, g.genre
        FROM genres g
        JOIN film_genres fg ON g.genre_id = fg.genre_id
        WHERE fg.film_id = ?
        """;

        return new HashSet<>(jdbc.query(query, genreMapper, filmId));
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

    private boolean mpaIdNotExist(Long mpaId) {
        if (mpaId == null) {
            return false;
        }
        String query = "SELECT * FROM motion_picture_association WHERE mpa_id = ?";
        try {
            jdbc.queryForObject(query, mpaMapper, mpaId);
            return false;
        } catch (EmptyResultDataAccessException e) {
            return true;
        }
    }

    private boolean genreIdNotExist(Long genreId) {
        if (genreId == null) {
            return false;
        }
        String query = "SELECT * FROM genres WHERE genre_id = ?";
        try {
            jdbc.queryForObject(query, genreMapper, genreId);
            return false;
        } catch (EmptyResultDataAccessException e) {
            return true;
        }
    }
}