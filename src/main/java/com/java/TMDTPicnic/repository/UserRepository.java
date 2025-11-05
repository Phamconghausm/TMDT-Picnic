package com.java.TMDTPicnic.repository;

import com.java.TMDTPicnic.entity.User;
import com.java.TMDTPicnic.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Tìm theo username (dùng để đăng nhập)
    Optional<User> findById(Long userId);
    Optional<User> findByUsername(String userName);
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u " +
            "WHERE (:username IS NULL OR u.username LIKE %:username%) " +
            "AND (:fullName IS NULL OR u.fullName LIKE %:fullName%) " +
            "AND (:role IS NULL OR u.role = :role)")
    List<User> searchUsers(
            @Param("username") String username,
            @Param("fullName") String fullName,
            @Param("role") Role role
    );

}
