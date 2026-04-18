package ru.yandex.practicum.filmorate.model.feed;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonPropertyOrder({ "timestamp", "userId", "eventType", "operation", "eventId", "entityId" })
public class Event {
    private Long eventId;
    private Long userId;
    private Long timestamp;
    private Long entityId;
    private EventType eventType;
    private OperationType operation;
}
