package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.feed.Event;

import java.util.List;

public interface FeedStorage {
    Event addEvent(Event event);
    List<Event> getFeed(Long userId);
}
