package com.example.userservice.repository;

import com.example.userservice.model.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    VerificationCode findByUserEmail(String email);
    VerificationCode findByValue(UUID uuid);
}
