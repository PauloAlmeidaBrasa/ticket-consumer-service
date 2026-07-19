package com.example.ticket_consumer_service.dto;

public record NotificationResult(String channel, String recipient, String message) {
}