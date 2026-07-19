package com.example.ticket_consumer_service.helper;

import org.springframework.stereotype.Component;

import com.example.ticket_consumer_service.domain.TicketEntity;
import com.example.ticket_consumer_service.dto.NotificationResult;

@Component
public class WhatAppHelper {

	public NotificationResult sendTicketSoldMessage(String userWhatapp, TicketEntity ticket) {
		return new NotificationResult(
				"whatsapp",
				userWhatapp,
				"Ticket %d sold successfully".formatted(ticket.getId()));
	}
}