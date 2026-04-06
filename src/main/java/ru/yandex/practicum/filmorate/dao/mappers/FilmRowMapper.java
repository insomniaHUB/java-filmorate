package ru.yandex.practicum.filmorate.dao.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MotionPicture;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FilmRowMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Film film = new Film();

        film.setId(resultSet.getLong("film_id"));
        film.setName(resultSet.getString("name"));
        film.setDuration(resultSet.getInt("duration"));
        film.setDescription(resultSet.getString("description"));
        film.setReleaseDate(resultSet.getDate("release_date").toLocalDate());

        MotionPicture mpa = new MotionPicture();
        mpa.setId(resultSet.getLong("mpa"));
        film.setMpa(mpa);

        return film;
    }
}
