package ru.yandex.practicum.filmorate.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.feed.Event;
import ru.yandex.practicum.filmorate.model.feed.EventType;
import ru.yandex.practicum.filmorate.model.feed.OperationType;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final FeedService feedService;
    private final UserService userService;
    private final FilmService filmService;

    public List<Review> getReviews(Long filmId, Integer count) {
        return reviewStorage.getReviews(filmId, count);
    }

    public Review getReviewById(Long id) {
        return reviewStorage.getReviewById(id);
    }

    public Review addReview(Review review) {
        userService.findUser(review.getUserId());
        filmService.findFilm(review.getFilmId());
        Review addedReview = reviewStorage.addReview(review);
        feedService.addEvent(Event.builder()
                .timestamp(System.currentTimeMillis())
                .userId(addedReview.getUserId())
                .eventType(EventType.REVIEW)
                .operation(OperationType.ADD)
                .entityId(addedReview.getReviewId())
                .build());
        return addedReview;
    }

    public Review updateReview(Review review) {
        Review updatedreview = reviewStorage.updateReview(review);
        feedService.addEvent(Event.builder()
                .timestamp(System.currentTimeMillis())
                .userId(updatedreview.getUserId())
                .eventType(EventType.REVIEW)
                .operation(OperationType.UPDATE)
                .entityId(updatedreview.getReviewId())
                .build());
        return updatedreview;
    }

    public void deleteReviewById(Long id) {
        Review review = getReviewById(id);
        reviewStorage.deleteReviewById(id);
        feedService.addEvent(Event.builder()
                .timestamp(System.currentTimeMillis())
                .userId(review.getUserId())
                .eventType(EventType.REVIEW)
                .operation(OperationType.REMOVE)
                .entityId(id)
                .build());
    }

    @Transactional
    public void addReviewLike(Long id, Long userId) {
        getReviewById(id);
        userService.findUser(userId);
        if (reviewStorage.checkReviewLike(id, userId, false)) {
            deleteDislikeReview(id, userId);
        }
        if (!reviewStorage.checkReviewLike(id, userId, true)) {
            reviewStorage.addReviewLike(id, userId);
        }
    }

    @Transactional
    public void deleteReviewLike(Long id, Long userId) {
        getReviewById(id);
        userService.findUser(userId);
        if (reviewStorage.checkReviewLike(id, userId, true)) {
            reviewStorage.deleteReviewLike(id, userId);
        } else {
            log.info("Оценка не найдена, удаление невозможно. userId - {}, reviewId - {}", userId, id);
        }
    }

    @Transactional
    public void dislikeReview(Long id, Long userId) {
        getReviewById(id);
        userService.findUser(userId);
        if (reviewStorage.checkReviewLike(id, userId, true)) {
            deleteReviewLike(id, userId);
        }
        if (!reviewStorage.checkReviewLike(id, userId, false)) {
            reviewStorage.dislikeReview(id, userId);
        } else {
            log.info("Пользователь не может поставить отрицательную оценку дважды. id - {}", userId);
        }
    }

    @Transactional
    public void deleteDislikeReview(Long id, Long userId) {
        getReviewById(id);
        userService.findUser(userId);
        if (reviewStorage.checkReviewLike(id, userId, false)) {
            reviewStorage.deleteDislikeReview(id, userId);
        } else {
            log.info("Отрицательная оценка не найдена, удаление невозможно. userId - {}, reviewId - {}", userId, id);
        }
    }
}