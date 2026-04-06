package ru.yandex.practicum.filmorate.dao;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbc;
    private final RowMapper<User> mapper;

    public UserDbStorage(JdbcTemplate jdbc, RowMapper<User> mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    @Override
    public List<User> getAllUsers() {
        String query = "SELECT * FROM users";
        List<User> users = jdbc.query(query, mapper);

        for (User user : users) {
            user.setFriends(loadFriends(user.getId()));
        }

        return users;
    }

    @Override
    public User getUserById(Long id) {
        String query = "SELECT * FROM users WHERE user_id = ?";
        try {
            User user = jdbc.queryForObject(query, mapper, id);
            if (user != null) {
                user.setFriends(loadFriends(id));
            }

            return user;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public User createUser(User user) {
        String query = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, user.getEmail());
            ps.setObject(2, user.getLogin());
            ps.setObject(3, user.getName());
            ps.setObject(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKeyAs(Long.class);
        if (id != null) {
            user.setId(id);
            return user;
        } else {
            throw new InternalServerException("Не удалось сохранить данные пользователя");
        }
    }

    @Override
    public User updateUser(User newUser) {
        String query = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";
        int rowsUpdated = jdbc.update(query,
                newUser.getEmail(),
                newUser.getLogin(),
                newUser.getName(),
                Date.valueOf(newUser.getBirthday()),
                newUser.getId()
        );
        if (rowsUpdated == 0) {
            throw new NotFoundException("Не удалось обновить данные пользователя");
        }

        return newUser;
    }

    @Override
    public void deleteUser(Long id) {
        String query = "DELETE FROM users WHERE user_id = ?";
        jdbc.update(query, id);
    }

    @Override
    public void addFriend(Long id, Long friendId) {
        String query = "INSERT INTO friendship (first_user_id, second_user_id, status) VALUES (?, ?, ?)";
        jdbc.update(query, id, friendId, "НЕПОДТВЕРЖДЕННАЯ");
    }

    @Override
    public void updateFriend(Long id, Long friendId) {
        String query = "UPDATE friendship SET status = ? WHERE first_user_id = ? AND second_user_id = ?";
        jdbc.update(query, "ПОДТВЕРЖДЕННАЯ", id, friendId);
    }

    @Override
    public void deleteFriend(Long id, Long friendId) {
        String query = "DELETE FROM friendship WHERE first_user_id = ? AND second_user_id = ?";
        jdbc.update(query, id, friendId);
    }

    private Set<Long> loadFriends(Long userId) {
        String query = "SELECT second_user_id FROM friendship WHERE first_user_id = ?";

        return new HashSet<>(jdbc.queryForList(query, Long.class, userId));
    }
}
