package com.java.TMDTPicnic.repository;

import com.java.TMDTPicnic.dto.response.UserStatsByDayResponse;
import com.java.TMDTPicnic.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findById(Long userId);
    Optional<User> findByUsername(String userName);
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // Tổng số người dùng
    @Query("SELECT COUNT(u) FROM User u")
    long countTotalUsers();
    // Thống kê số user mới mỗi ngày (có thể tính returningUser nếu có dữ liệu)
    @Query(value = """
        SELECT 
            DATE(created_at) as date,
            COUNT(*) as newUsers,
            0 as returningUsers
        FROM users
        GROUP BY DATE(created_at)
        ORDER BY DATE(created_at)
    """, nativeQuery = true)
    List<Object[]> getUserStatsByDayRaw();

    // Thống kê số user mới mỗi ngày với date range
    @Query(value = """
        SELECT 
            DATE(created_at) as date,
            COUNT(*) as newUsers,
            0 as returningUsers
        FROM users
        WHERE DATE(created_at) BETWEEN :fromDate AND :toDate
        GROUP BY DATE(created_at)
        ORDER BY DATE(created_at)
    """, nativeQuery = true)
    List<Object[]> getUserStatsByDayRawWithDateRange(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    // Tổng số user với date range
    @Query(value = """
        SELECT COUNT(*)
        FROM users
        WHERE DATE(created_at) <= :toDate
    """, nativeQuery = true)
    Long getTotalUsersWithDateRange(@Param("toDate") LocalDate toDate);

    // Số user mới trong khoảng thời gian
    @Query(value = """
        SELECT COUNT(*)
        FROM users
        WHERE DATE(created_at) BETWEEN :fromDate AND :toDate
    """, nativeQuery = true)
    Long getNewUsersWithDateRange(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

}
