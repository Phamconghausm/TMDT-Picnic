package com.java.TMDTPicnic.repository;

import com.java.TMDTPicnic.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    // Tìm coupon theo mã (ví dụ: "DISCOUNT10")
    Optional<Coupon> findByCode(String code);

    // ===== COUPON STATS =====
    @Query(value = """
        SELECT COUNT(*)
        FROM coupons
        WHERE valid_from <= NOW() AND valid_to >= NOW()
    """, nativeQuery = true)
    Long countActiveCoupons();

    @Query(value = """
        SELECT COUNT(*)
        FROM coupons
        WHERE valid_to < NOW()
    """, nativeQuery = true)
    Long countExpiredCoupons();

    @Query("SELECT COUNT(c) FROM Coupon c WHERE c.usedCount >= c.usageLimit")
    Long countFullyUsedCoupons();

    // ===== TOP USED COUPONS =====
    @Query(value = """
        SELECT 
            id,
            code,
            used_count,
            usage_limit
        FROM coupons
        ORDER BY used_count DESC
        LIMIT 10
    """, nativeQuery = true)
    List<Object[]> getTopUsedCouponsRaw();

    // ===== COUPON USAGE RATE =====
    @Query(value = """
        SELECT 
            CASE 
                WHEN COUNT(*) = 0 THEN 0.0
                ELSE (COUNT(CASE WHEN coupon_id IS NOT NULL THEN 1 END) * 100.0 / COUNT(*))
            END
        FROM orders
    """, nativeQuery = true)
    Double getCouponUsageRate();
}
