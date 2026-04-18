package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    @GetMapping
    public List<Review> getReviews(@RequestParam(required = false) Long filmId,
                                   @RequestParam(defaultValue = "10") Integer count) {
        return reviewService.getReviews(filmId, count);
    }

    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable Long id) {
        return reviewService.getReviewById(id);
    }

    @PostMapping
    public Review addReview(@Valid @RequestBody Review review) {
        return reviewService.addReview(review);
    }

    @PutMapping
    public Review updateReview(@Valid @RequestBody Review review) {
        return reviewService.updateReview(review);
    }

    @DeleteMapping("/{id}")
    public void deleteReviewById(@PathVariable Long id) {
        reviewService.deleteReviewById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addReviewLike(@PathVariable Long id,
                              @PathVariable Long userId) {
        reviewService.addReviewLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteReviewLike(@PathVariable Long id,
                                 @PathVariable Long userId) {
        reviewService.deleteReviewLike(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void dislikeReview(@PathVariable Long id,
                              @PathVariable Long userId) {
        reviewService.dislikeReview(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void deleteDislikeReview(@PathVariable Long id,
                                    @PathVariable Long userId) {
        reviewService.deleteDislikeReview(id, userId);
    }
}
