package com.java.TMDTPicnic.repository;

import com.java.TMDTPicnic.dto.response.UserRoleDistributionResponse;
import com.java.TMDTPicnic.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
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
        WHERE DATE(created_at) BETWEEN :fromDate AND :toDate
    """, nativeQuery = true)
    Long getTotalUsersWithDateRange(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    @Query(value = """
        SELECT COUNT(*)
        FROM users
        WHERE DATE(last_login) BETWEEN :fromDate AND :toDate
    """, nativeQuery = true)
    Long getTotalUsersActiveWithDateRange(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    @Query("""
        SELECT new com.java.TMDTPicnic.dto.response.UserRoleDistributionResponse(
            u.role,
            COUNT(u)
        )
        FROM User u
        GROUP BY u.role
    """)
    List<UserRoleDistributionResponse> getUserRoleDistribution();

    // ===== TOP ACTIVE USERS (by order count and review count) =====
    @Query(value = """
        SELECT 
            u.id as userId,
            u.username,
            u.email,
            COUNT(DISTINCT o.id) as orderCount,
            COUNT(DISTINCT r.id) as reviewCount
        FROM users u
        LEFT JOIN orders o ON o.user_id = u.id
        LEFT JOIN reviews r ON r.user_id = u.id
        GROUP BY u.id, u.username, u.email
        ORDER BY orderCount DESC, reviewCount DESC
        LIMIT 10
    """, nativeQuery = true)
    List<Object[]> getTopActiveUsersRaw();
    @Query("SELECT COUNT(u) FROM User u WHERE u.lastLogin >= :fromDate")
    Long countActiveUsers(@Param("fromDate") LocalDate fromDate);

}
