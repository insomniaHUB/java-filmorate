package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final Map<Long, User> users = new HashMap<>();
    private long id = 1;

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        validate(user);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("Имя пользователя взято из логина.");
        }
        user.setId(id);
        users.put(user.getId(), user);
        log.info("Создан новый пользователь.");
        id++;
        return user;
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        if (newUser.getId() == null) {
            log.info("Не указан Id при обновлении данных пользователя.");
            throw new ConditionsNotMetException("Должен быть указан Id.");
        }
        if (users.containsKey(newUser.getId())) {
            if (newUser.getEmail() == null || newUser.getName() == null || newUser.getLogin() == null || newUser.getBirthday() == null) {
                log.info("Не получилось изменить данные пользователя из-за нехватки данных.");
                return users.get(newUser.getId());
            }
            validate(newUser);

            User oldUser = users.get(newUser.getId());
            oldUser.setName(newUser.getName());
            oldUser.setLogin(newUser.getLogin());
            oldUser.setEmail(newUser.getEmail());
            oldUser.setBirthday(newUser.getBirthday());
            log.info("Данные пользователя успешно обновлены.");
            return oldUser;
        }
        log.info("Пользователь с Id = " + newUser.getId() + " не был найден!");
        throw new NotFoundException("Пользователь с Id = " + newUser.getId() + " не был найден!");
    }

    private void validate(User user) {
        if (user.getEmail() == null || !user.getEmail().contains("@") || user.getEmail().isBlank()
                || user.getEmail().contains(" ") || user.getBirthday().isAfter(LocalDate.now())) {
            log.info("Произошла ошибка валидации при попытке создания нового пользователя.");
            throw new ValidationException("Ошибка валидации!");
        }
    }
}
