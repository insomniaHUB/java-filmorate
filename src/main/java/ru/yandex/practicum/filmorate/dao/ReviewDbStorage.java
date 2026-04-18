package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.ReviewMapper;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ReviewDbStorage implements ReviewStorage {
    private final JdbcTemplate jdbcTemplate;
    private final ReviewMapper reviewMapper;


    public List<Review> getReviews(Long filmId, Integer count) {
        if (filmId == null) {
            String query = "SELECT * FROM reviews ORDER BY useful DESC LIMIT ?";
            return jdbcTemplate.query(query, reviewMapper, count);
        } else {
            String query = "SELECT * FROM reviews WHERE film_id = ? ORDER BY useful DESC LIMIT ?";
            return jdbcTemplate.query(query, reviewMapper, filmId, count);
        }
    }

    public Review getReviewById(Long id) {
        String query = "SELECT * FROM reviews WHERE review_id = ?";
        try {
            return jdbcTemplate.queryForObject(query, reviewMapper, id);
        } catch (EmptyResultDataAccessException exception) {
            log.error("Ошибка при получения отзыва. id - {} отсутствует!", id);
            throw new NotFoundException("Отзыва с таким id не существует");
        }
    }

    public Review addReview(Review review) {
        String query = "INSERT INTO reviews (content, is_positive, user_id, film_id) VALUES (?, ?, ?, ?)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.getIsPositive());
            ps.setLong(3, review.getUserId());
            ps.setLong(4, review.getFilmId());
            return ps;
        }, keyHolder);
        Long id = keyHolder.getKeyAs(Long.class);
        if (id != null) {
            review.setReviewId(id);
            return review;
        } else {
            throw new InternalServerException("Не удалось сохранить данные отзыва");
        }
    }

    public Review updateReview(Review review) {
        String query = "UPDATE reviews SET content = ?, is_positive = ? WHERE review_id = ?";
        int updatedRows = jdbcTemplate.update(query, review.getContent(), review.getIsPositive(), review.getReviewId());
        if (updatedRows == 0) {
            throw new NotFoundException("Не удалось обновить данные отзыва");
        }
        return getReviewById(review.getReviewId());
    }

    public void deleteReviewById(Long id) {
        String query = "DELETE FROM reviews WHERE review_id = ?";
        int editedRows = jdbcTemplate.update(query, id);
        if (editedRows == 0) {
            log.error("Ошибка при удалении отзыва. id - {} отсутствует!", id);
            throw new NotFoundException("Не удалось удалить данные отзыва");
        }
    }

    public void addReviewLike(Long id, Long userId) {
        String firstQuery = "INSERT INTO review_rating (review_id, user_id, is_positive) VALUES (?, ?, ?)";
        String secondQuery = "UPDATE reviews SET useful = useful + 1 WHERE review_id = ?";
        jdbcTemplate.update(firstQuery, id, userId, true);
        jdbcTemplate.update(secondQuery, id);
    }

    public void deleteReviewLike(Long id, Long userId) {
        String firstQuery = "DELETE FROM review_rating WHERE review_id = ? AND user_id = ?";
        String secondQuery = "UPDATE reviews SET useful = useful - 1 WHERE review_id = ?";
        jdbcTemplate.update(firstQuery, id, userId);
        jdbcTemplate.update(secondQuery, id);
    }

    public void dislikeReview(Long id, Long userId) {
        String firstQuery = "INSERT INTO review_rating (review_id, user_id, is_positive) VALUES (?, ?, ?)";
        String secondQuery = "UPDATE reviews SET useful = useful - 1 WHERE review_id = ?";
        jdbcTemplate.update(firstQuery, id, userId, false);
        jdbcTemplate.update(secondQuery, id);
    }

    public void deleteDislikeReview(Long id, Long userId) {
        String firstQuery = "DELETE FROM review_rating WHERE review_id = ? AND user_id = ?";
        String secondQuery = "UPDATE reviews SET useful = useful + 1 WHERE review_id = ?";
        jdbcTemplate.update(firstQuery, id, userId);
        jdbcTemplate.update(secondQuery, id);
    }

    public boolean checkReviewLike(Long reviewId, Long userId, boolean isPositive) {
        String query = "SELECT COUNT(*) FROM review_rating WHERE review_id = ? AND user_id = ? AND is_positive = ?";
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, reviewId, userId, isPositive);
        return count > 0;
    }
}