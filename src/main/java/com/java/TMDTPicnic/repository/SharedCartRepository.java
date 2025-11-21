package com.java.TMDTPicnic.repository;

import com.java.TMDTPicnic.entity.SharedCart;
import com.java.TMDTPicnic.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SharedCartRepository extends JpaRepository<SharedCart, Long> {
    // Tìm các giỏ hàng mà user là owner
    List<SharedCart> findByOwner(User owner);

    // Tìm các giỏ hàng mà user tham gia (qua SharedCartParticipant)
    @Query("SELECT DISTINCT sc FROM SharedCart sc " +
            "JOIN SharedCartParticipant scp ON scp.sharedCart = sc " +
            "WHERE scp.user = :user")
    List<SharedCart> findByParticipant(@Param("user") User user);

    // Tìm tất cả giỏ hàng mà user tham gia (owner hoặc participant)
    @Query("SELECT DISTINCT sc FROM SharedCart sc " +
            "LEFT JOIN SharedCartParticipant scp ON scp.sharedCart = sc " +
            "WHERE sc.owner = :user OR scp.user = :user")
    List<SharedCart> findByOwnerOrParticipant(@Param("user") User user);

    // ===== SHARED CART STATS =====
    @Query("SELECT COUNT(sc) FROM SharedCart sc WHERE sc.status = 'OPEN'")
    Long countOpenSharedCarts();

    @Query("SELECT COUNT(sc) FROM SharedCart sc WHERE sc.status = 'COMPLETED' OR sc.status = 'CANCELLED'")
    Long countClosedSharedCarts();

    @Query(value = """
        SELECT COUNT(*)
        FROM shared_carts
        WHERE status = 'OPEN'
          AND expires_at BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL 24 HOUR)
    """, nativeQuery = true)
    Long countExpiringSoonSharedCarts();
}
