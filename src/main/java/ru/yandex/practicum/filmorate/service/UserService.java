package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> findAll() {
        return userStorage.getAllUsers();
    }

    public User findUser(Long id) {
        validateUser(id);
        return userStorage.getUserById(id);
    }

    public User create(User user) {
        validate(user);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("Имя пользователя взято из логина.");
        }
        log.info("Создан новый пользователь.");
        return userStorage.createUser(user);
    }

    public User update(User newUser) {
        if (newUser.getId() == null) {
            log.info("Не указан Id при обновлении данных пользователя.");
            throw new ConditionsNotMetException("Должен быть указан Id.");
        }
        if (userStorage.getUserById(newUser.getId()) == null) {
            log.info("Пользователь с Id = " + newUser.getId() + " не был найден!");
            throw new NotFoundException("Пользователь с Id = " + newUser.getId() + " не был найден!");
        }
        if (userStorage.getUserById(newUser.getId()) != null) {
            if (newUser.getEmail() == null || newUser.getName() == null || newUser.getLogin() == null || newUser.getBirthday() == null) {
                log.info("Не получилось изменить данные пользователя из-за нехватки данных.");
                return userStorage.getUserById(newUser.getId());
            }
            validate(newUser);

            log.info("Данные пользователя успешно обновлены.");
            return userStorage.updateUser(newUser);
        }
        log.info("Пользователь с Id = " + newUser.getId() + " не был найден!");
        throw new NotFoundException("Пользователь с Id = " + newUser.getId() + " не был найден!");
    }

    public void addFriend(Long id, Long friendId) {
        validateUser(id);
        validateUser(friendId);

        if (userStorage.getUserById(friendId).getFriends().contains(id)) {
            userStorage.updateFriend(id, friendId);
        } else {
            userStorage.addFriend(id, friendId);
        }
    }

    public Collection<User> getMutualFriends(Long id, Long friendId) {
        validateUser(id);
        validateUser(friendId);

        Set<Long> userFriends = userStorage.getUserById(id).getFriends();
        Set<Long> otherUserFriends = userStorage.getUserById(friendId).getFriends();

        return userFriends.stream()
                .filter(otherUserFriends::contains)
                .map(this::findUser)
                .collect(Collectors.toList());
    }

    public Collection<User> getAllFriends(Long id) {
        validateUser(id);
        return userStorage.getUserById(id).getFriends().stream()
                .map(this::findUser)
                .collect(Collectors.toList());
    }

    public void deleteFriend(Long id, Long friendId) {
        validateUser(id);
        validateUser(friendId);
        userStorage.deleteFriend(id, friendId);
        userStorage.getUserById(id).getFriends().remove(friendId);
        userStorage.getUserById(friendId).getFriends().remove(id);
    }

    private void validate(User user) {
        if (user.getEmail() == null || !user.getEmail().contains("@") || user.getEmail().isBlank()
                || user.getEmail().contains(" ") || user.getBirthday().isAfter(LocalDate.now())) {
            log.info("Произошла ошибка валидации при попытке создания нового пользователя.");
            throw new ValidationException("Ошибка валидации!");
        }
    }

    private void validateUser(Long id) {
        if (userStorage.getUserById(id) == null) {
            throw new NotFoundException("Пользователь не был найден");
        }
    }
}
