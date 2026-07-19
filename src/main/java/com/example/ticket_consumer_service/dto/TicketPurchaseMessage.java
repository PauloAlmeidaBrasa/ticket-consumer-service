package com.example.ticket_consumer_service.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public record TicketPurchaseMessage(
		Long ticketId,
		Long userId,
		String userEmail,
		@JsonAlias("userWhatapp") String userWhatsapp) {
}