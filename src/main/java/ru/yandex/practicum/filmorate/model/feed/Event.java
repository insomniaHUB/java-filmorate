package ru.yandex.practicum.filmorate.model.feed;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Event {
    private Long eventId;
    private Long userId;
    private Long timestamp;
    private Long entityId;
    private EventType eventType;
    private OperationType operation;
}
