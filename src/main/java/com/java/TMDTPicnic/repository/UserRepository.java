package com.java.TMDTPicnic.repository;

import com.java.TMDTPicnic.dto.response.UserStatsByDayResponse;
import com.java.TMDTPicnic.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    // Người dùng mới trong tháng
    @Query("""
            SELECT COUNT(u)
            FROM User u
            WHERE MONTH(u.createdAt) = MONTH(CURRENT_DATE)
              AND YEAR(u.createdAt) = YEAR(CURRENT_DATE)
            """)
    long countUsersCreatedThisMonth();

    // Người dùng mới trong 7 ngày gần nhất — dùng tham số ngày truyền vào
    @Query("""
            SELECT COUNT(u)
            FROM User u
            WHERE u.createdAt >= :fromDate
            """)
    long countUsersCreatedSince(@Param("fromDate") LocalDateTime fromDate);

    // Thống kê số user mới mỗi ngày (có thể tính returningUser nếu có dữ liệu)
    @Query("""
    SELECT new com.java.TMDTPicnic.dto.response.UserStatsByDayResponse(
        DATE(u.createdAt),
        COUNT(u),
        0L
    )
    FROM User u
    GROUP BY DATE(u.createdAt)
    ORDER BY DATE(u.createdAt)
    """)
    List<UserStatsByDayResponse> getUserStatsByDay();

}
