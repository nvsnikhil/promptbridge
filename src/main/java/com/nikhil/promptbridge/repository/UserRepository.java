package com.nikhil.promptbridge.repository;

import com.nikhil.promptbridge.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional; // <-- ADD THIS IMPORT

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // v-- ADD THIS NEW METHOD --v
    Optional<User> findByEmail(String email);
}