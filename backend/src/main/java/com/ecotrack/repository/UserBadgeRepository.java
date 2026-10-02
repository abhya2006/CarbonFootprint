package com.ecotrack.repository;

import com.ecotrack.entity.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {
    boolean existsByUserIdAndBadgeCode(Long userId, String code);
    List<UserBadge> findByUserIdOrderByAwardedAtDesc(Long userId);
    @Query("select ub from UserBadge ub join fetch ub.badge where ub.user.id in :userIds order by ub.awardedAt desc")
    List<UserBadge> findWithBadgesByUserIds(@Param("userIds") List<Long> userIds);
    long countByUserId(Long userId);
}