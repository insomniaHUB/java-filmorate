package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {

    void deleteUser(Long id);

    User createUser(User user);

    User updateUser(User newUser);

    Collection<User> getAllUsers();

    User getUser(Long id);
}
