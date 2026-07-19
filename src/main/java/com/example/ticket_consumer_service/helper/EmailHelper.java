package com.example.ticket_consumer_service.helper;

import org.springframework.stereotype.Component;

import com.example.ticket_consumer_service.domain.TicketEntity;
import com.example.ticket_consumer_service.dto.NotificationResult;

@Component
public class EmailHelper {

	public NotificationResult sendTicketSoldEmail(String userEmail, TicketEntity ticket) {
		return new NotificationResult(
				"email",
				userEmail,
				"Ticket %d marked as %s".formatted(ticket.getId(), ticket.getStatus()));
	}
}