package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

public interface UserStorage {

    void deleteUser(Long id);

    User createUser(User user);

    User updateUser(User newUser);
}
