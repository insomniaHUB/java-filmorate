package ru.yandex.practicum.filmorate.model.feed;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Event {
    private Long timestamp;
    private Long userId;
    private EventType eventType;
    private OperationType operation;
    private Long eventId;
    private Long entityId;
}
