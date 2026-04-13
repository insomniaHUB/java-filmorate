package ru.yandex.practicum.filmorate.dao.mappers;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.feed.Event;
import ru.yandex.practicum.filmorate.model.feed.EventType;
import ru.yandex.practicum.filmorate.model.feed.OperationType;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FeedRowMapper implements RowMapper<Event> {

    @Override
    public Event mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Event event = Event.builder()
                .eventId(resultSet.getLong("event_id"))
                .userId(resultSet.getLong("user_id"))
                .entityId(resultSet.getLong("entity_id"))
                .timestamp(resultSet.getLong("timestamp"))
                .eventType(EventType.valueOf(resultSet.getString("event_type")))
                .operation(OperationType.valueOf(resultSet.getString("operation")))
                .build();
        return event;
    }
}
