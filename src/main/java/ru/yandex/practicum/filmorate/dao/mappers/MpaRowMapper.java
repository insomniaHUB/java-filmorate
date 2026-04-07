package ru.yandex.practicum.filmorate.dao.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.MotionPicture;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class MpaRowMapper implements RowMapper<MotionPicture> {
    @Override
    public MotionPicture mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        MotionPicture mpa = new MotionPicture();
        mpa.setId(resultSet.getLong("mpa_id"));
        mpa.setName(resultSet.getString("rating"));

        return mpa;
    }
}
