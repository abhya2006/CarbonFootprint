package com.ecotrack.repository;

import com.ecotrack.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Collection;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findByUserIdAndActivityDateBetweenOrderByActivityDateDesc(Long userId, LocalDate from, LocalDate to);
    List<Activity> findByUserIdOrderByActivityDateDesc(Long userId);
    @Query("select coalesce(sum(a.co2e), 0) from Activity a where a.user.id = :userId and a.activityDate between :from and :to")
    double totalBetween(@Param("userId") Long userId, @Param("from") LocalDate from, @Param("to") LocalDate to);
    @Query("select a.category, sum(a.co2e) from Activity a where a.user.id = :userId and a.activityDate between :from and :to group by a.category")
    List<Object[]> categoryTotals(@Param("userId") Long userId, @Param("from") LocalDate from, @Param("to") LocalDate to);
    @Query("select a.activityDate, sum(a.co2e) from Activity a where a.user.id = :userId and a.activityDate between :from and :to group by a.activityDate order by a.activityDate")
    List<Object[]> dailyTotals(@Param("userId") Long userId, @Param("from") LocalDate from, @Param("to") LocalDate to);
    @Query(value = "select avg(user_total) from (select user_id, sum(co2e) as user_total from activities where activity_date between :from and :to group by user_id) user_totals", nativeQuery = true)
    Double platformAverage(@Param("from") LocalDate from, @Param("to") LocalDate to);
    @Query("select u.id, u.name, sum(a.co2e) from Activity a join a.user u where a.activityDate between :from and :to group by u.id, u.name order by sum(a.co2e)")
    List<Object[]> leaderboard(@Param("from") LocalDate from, @Param("to") LocalDate to);
    long countByUserId(Long userId);
    @Query("select u.id, a.activityDate, sum(a.co2e) from Activity a join a.user u where u.id in :userIds and a.activityDate between :from and :to group by u.id, a.activityDate")
    List<Object[]> memberDailyTotals(@Param("userIds") Collection<Long> userIds, @Param("from") LocalDate from, @Param("to") LocalDate to);
    @Query("select u.role, a.category, sum(a.co2e) from Activity a join a.user u where a.activityDate between :from and :to group by u.role, a.category")
    List<Object[]> organizationTotals(@Param("from") LocalDate from, @Param("to") LocalDate to);
    @Query("select u.id, u.name, sum(a.co2e) from Activity a join a.user u where u.id in :userIds and a.activityDate between :from and :to group by u.id, u.name order by sum(a.co2e)")
    List<Object[]> memberTotals(@Param("userIds") Collection<Long> userIds, @Param("from") LocalDate from, @Param("to") LocalDate to);
    @Query("select a.category, sum(a.co2e) from Activity a where a.user.id in :userIds and a.activityDate between :from and :to group by a.category")
    List<Object[]> memberCategoryTotals(@Param("userIds") Collection<Long> userIds, @Param("from") LocalDate from, @Param("to") LocalDate to);
}