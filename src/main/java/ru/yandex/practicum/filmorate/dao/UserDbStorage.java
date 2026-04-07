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
    private static final String GET_ALL_USERS_QUERY = "SELECT * FROM users";
    private static final String GET_USER_BY_ID_QUERY = "SELECT * FROM users WHERE user_id = ?";
    private static final String CREATE_USER_QUERY = "INSERT INTO users (email, login, name, birthday) " +
            "VALUES (?, ?, ?, ?)";
    private static final String UPDATE_USER_QUERY = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? " +
            "WHERE user_id = ?";
    private static final String DELETE_USER_QUERY = "DELETE FROM users WHERE user_id = ?";
    private static final String ADD_FRIEND_QUERY = "INSERT INTO friendship (first_user_id, second_user_id, status) " +
            "VALUES (?, ?, ?)";
    private static final String UPDATE_FRIEND_QUERY = "UPDATE friendship SET status = ? " +
            "WHERE first_user_id = ? AND second_user_id = ?";
    private static final String DELETE_FRIEND_QUERY = "DELETE FROM friendship " +
            "WHERE first_user_id = ? AND second_user_id = ?";
    private static final String LOAD_FRIENDS_QUERY = "SELECT second_user_id FROM friendship WHERE first_user_id = ?";
    private final JdbcTemplate jdbc;
    private final RowMapper<User> mapper;

    public UserDbStorage(JdbcTemplate jdbc, RowMapper<User> mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    @Override
    public List<User> getAllUsers() {
        List<User> users = jdbc.query(GET_ALL_USERS_QUERY, mapper);

        for (User user : users) {
            user.setFriends(loadFriends(user.getId()));
        }

        return users;
    }

    @Override
    public User getUserById(Long id) {
        try {
            User user = jdbc.queryForObject(GET_USER_BY_ID_QUERY, mapper, id);
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
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(CREATE_USER_QUERY, Statement.RETURN_GENERATED_KEYS);
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
        int rowsUpdated = jdbc.update(UPDATE_USER_QUERY,
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
        jdbc.update(DELETE_USER_QUERY, id);
    }

    @Override
    public void addFriend(Long id, Long friendId) {
        jdbc.update(ADD_FRIEND_QUERY, id, friendId, "НЕПОДТВЕРЖДЕННАЯ");
    }

    @Override
    public void updateFriend(Long id, Long friendId) {
        jdbc.update(UPDATE_FRIEND_QUERY, "ПОДТВЕРЖДЕННАЯ", id, friendId);
    }

    @Override
    public void deleteFriend(Long id, Long friendId) {
        jdbc.update(DELETE_FRIEND_QUERY, id, friendId);
    }

    private Set<Long> loadFriends(Long userId) {
        return new HashSet<>(jdbc.queryForList(LOAD_FRIENDS_QUERY, Long.class, userId));
    }
}