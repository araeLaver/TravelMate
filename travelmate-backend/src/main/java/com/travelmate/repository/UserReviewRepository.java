package com.travelmate.repository;

import com.travelmate.entity.UserReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserReviewRepository extends JpaRepository<UserReview, Long> {
    
    List<UserReview> findByReviewedUserId(Long reviewedUserId);
    
    List<UserReview> findByReviewerId(Long reviewerId);
    
    boolean existsByReviewerIdAndReviewedUserId(Long reviewerId, Long reviewedUserId);
    
    @Query("SELECT AVG(r.rating) FROM UserReview r WHERE r.reviewedUser.id = :userId")
    Double getAverageRatingByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(r) FROM UserReview r WHERE r.reviewedUser.id = :userId")
    Integer getReviewCountByUserId(@Param("userId") Long userId);
    
    @Query("SELECT r FROM UserReview r WHERE r.reviewedUser.id = :userId ORDER BY r.createdAt DESC")
    List<UserReview> findByReviewedUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
}