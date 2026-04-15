package ru.yandex.practicum.filmorate.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {
    private final ReviewStorage reviewStorage;
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
        return reviewStorage.addReview(review);
    }

    public Review updateReview(Review review) {
        return reviewStorage.updateReview(review);
    }

    public void deleteReviewById(Long id) {
        reviewStorage.deleteReviewById(id);
    }

    @Transactional
    public void addReviewLike(Long id, Long userId) {
        getReviewById(id);
        userService.findUser(userId);
        if (!reviewStorage.checkReviewLike(id, userId)) {
            reviewStorage.addReviewLike(id, userId);
        } else {
            log.info("Пользователь не может поставить положительную оценку дважды. id - {}", userId);
        }
    }

    @Transactional
    public void deleteReviewLike(Long id, Long userId) {
        getReviewById(id);
        userService.findUser(userId);
        if (reviewStorage.checkReviewLike(id, userId)) {
            reviewStorage.deleteReviewLike(id, userId);
        } else {
            log.info("Оценка не найдена, удаление невозможно. userId - {}, reviewId - {}", userId, id);
        }
    }

    @Transactional
    public void dislikeReview(Long id, Long userId) {
        getReviewById(id);
        userService.findUser(userId);
        if (!reviewStorage.checkReviewLike(id, userId)) {
            reviewStorage.dislikeReview(id, userId);
        } else {
            log.info("Пользователь не может поставить отрицательную оценку дважды. id - {}", userId);
        }
    }

    @Transactional
    public void deleteDislikeReview(Long id, Long userId) {
        getReviewById(id);
        userService.findUser(userId);
        if (reviewStorage.checkReviewLike(id, userId)) {
            reviewStorage.deleteDislikeReview(id, userId);
        } else {
            log.info("Отрицательная оценка не найдена, удаление невозможно. userId - {}, reviewId - {}", userId, id);
        }
    }
}
