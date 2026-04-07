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
    private static final String GET_ALL_MPA_QUERY = "SELECT * FROM motion_picture_association";
    private static final String GET_MPA_BY_ID_QUERY = "SELECT * FROM motion_picture_association WHERE mpa_id = ?";
    private final JdbcTemplate jdbc;
    private final RowMapper<MotionPicture> mapper;

    @Autowired
    public MpaDbStorage(JdbcTemplate jdbc, RowMapper<MotionPicture> mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    public Collection<MotionPicture> getAllMpa() {
        return jdbc.query(GET_ALL_MPA_QUERY, mapper);
    }

    public MotionPicture getMpaById(Long id) {
        List<MotionPicture> results = jdbc.query(GET_MPA_BY_ID_QUERY, mapper, id);

        if (results.isEmpty()) {
            throw new NotFoundException("Рейтинг с таким id не был найден");
        }

        return results.getFirst();
    }

    public void mpaIdNotExist(Long mpaId) {
        List<MotionPicture> mpa = jdbc.query(GET_MPA_BY_ID_QUERY, mapper, mpaId);
        if (mpa.isEmpty()) {
            throw new NotFoundException("Возрастного рейтинга с таким id не существует");
        }
    }
}
