package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.feed.Event;
import ru.yandex.practicum.filmorate.service.FeedService;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final FeedService feedService;
    private final FilmService filmService;

    @GetMapping
    public Collection<User> findAll() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public User findUser(@PathVariable Long id) {
        return userService.findUser(id);
    }

    @GetMapping("/{id}/feed")
    public List<Event> getFeeds(@PathVariable Long id) {
        return feedService.getFeed(id);
    }

    @GetMapping("/{id}/friends")
    public Collection<User> getAllFriends(@PathVariable Long id) {
        return userService.getAllFriends(id);
    }

    @GetMapping("/{id}/recommendations")
    public Collection<Film> getFilmRecommendations(@PathVariable Long id) {
        return filmService.getFilmRecommendations(id);
    }

    @GetMapping("/{id}/friends/common/{friendId}")
    public Collection<User> getMutualFriends(@PathVariable Long id,
                                             @PathVariable Long friendId) {
        return userService.getMutualFriends(id, friendId);
    }

    @PostMapping
    public User create(@RequestBody User user) {
        return userService.create(user);
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        return userService.update(newUser);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable Long id,
                          @PathVariable Long friendId) {
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable Long id,
                             @PathVariable Long friendId) {
        userService.deleteFriend(id, friendId);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
    }
}
