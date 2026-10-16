package com.farmerprocurement.backend.repository;

import com.farmerprocurement.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Find a farmer using their email address.
    Optional<User> findByEmail(String email);

    // Find a farmer using their phone number.
    Optional<User> findByPhoneNumber(String phoneNumber);

    // Check whether an email is already registered.
    boolean existsByEmail(String email);

    // Check whether a phone number is already registered.
    boolean existsByPhoneNumber(String phoneNumber);
}