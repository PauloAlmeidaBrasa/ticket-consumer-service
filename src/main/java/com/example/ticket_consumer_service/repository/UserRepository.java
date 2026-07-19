package com.example.ticket_consumer_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ticket_consumer_service.domain.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
}