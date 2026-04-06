package ru.yandex.practicum.filmorate.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MotionPicture;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.Collection;
import java.util.List;

@Repository
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbc;
    private final RowMapper<MotionPicture> mapper;

    @Autowired
    public MpaDbStorage(JdbcTemplate jdbc, RowMapper<MotionPicture> mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    public Collection<MotionPicture> getAllMpa() {
        String query = "SELECT * FROM motion_picture_association";

        return jdbc.query(query, mapper);
    }

    public MotionPicture getMpaById(Long id) {
        String query = "SELECT * FROM motion_picture_association WHERE mpa_id = ?";

        List<MotionPicture> results = jdbc.query(query, mapper, id);

        if (results.isEmpty()) {
            throw new NotFoundException("Рейтинг с таким id не был найден");
        }

        return results.getFirst();
    }
}
