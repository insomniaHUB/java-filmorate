package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.feed.Event;
import ru.yandex.practicum.filmorate.storage.FeedStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedService {
    private final FeedStorage feedStorage;
    private final UserService userService;

    public Event addEvent(Event event) {
        return feedStorage.addEvent(event);
    }

    public List<Event> getFeed(Long userId) {
        userService.findUser(userId);
        return feedStorage.getFeed(userId);
    }
}
