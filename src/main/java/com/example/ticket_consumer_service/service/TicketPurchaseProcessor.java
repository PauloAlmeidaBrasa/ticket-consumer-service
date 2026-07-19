package com.example.ticket_consumer_service.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ticket_consumer_service.domain.TicketEntity;
import com.example.ticket_consumer_service.dto.TicketPurchaseMessage;
import com.example.ticket_consumer_service.repository.TicketRepository;

@Service
public class TicketPurchaseProcessor {

	private final TicketRepository ticketRepository;

	public TicketPurchaseProcessor(TicketRepository ticketRepository) {
		this.ticketRepository = ticketRepository;
	}

	@Transactional
	public TicketEntity process(TicketPurchaseMessage message) {
		int updatedRows = ticketRepository.markAsSold(
				message.ticketId(),
				message.userId(),
				TicketEntity.TicketStatus.SOLD.name());

		if (updatedRows == 0) {
			throw new IllegalArgumentException("Ticket %d was not found".formatted(message.ticketId()));
		}

		return ticketRepository.findById(message.ticketId())
				.orElseThrow(() -> new IllegalStateException(
						"Ticket %d was updated but could not be reloaded".formatted(message.ticketId())));
	}
}