package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {
    List<Review> getReviews(Long filmId, Integer count);

    Review getReviewById(Long id);

    Review addReview(Review review);

    Review updateReview(Review review);

    void deleteReviewById(Long id);

    void addReviewLike(Long id, Long userId);

    void deleteReviewLike(Long id, Long userId);

    void dislikeReview(Long id, Long userId);

    void deleteDislikeReview(Long id, Long userId);

    boolean checkReviewLike(Long reviewId, Long userId, boolean isPositive);
}
