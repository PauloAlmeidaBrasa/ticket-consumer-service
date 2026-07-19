package com.example.ticket_consumer_service.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.example.ticket_consumer_service.domain.TicketEntity;
import com.example.ticket_consumer_service.dto.TicketPurchaseMessage;
import com.example.ticket_consumer_service.exception.MissingParameterException;
import com.example.ticket_consumer_service.exception.ResourceNotFoundException;
import com.example.ticket_consumer_service.repository.TicketRepository;
import com.example.ticket_consumer_service.repository.UserRepository;

@Service
public class TicketPurchaseProcessor {

	private final TicketRepository ticketRepository;
	private final UserRepository userRepository;

	public TicketPurchaseProcessor(TicketRepository ticketRepository, UserRepository userRepository) {
		this.ticketRepository = ticketRepository;
		this.userRepository = userRepository;
	}

	@Transactional
	public TicketEntity process(TicketPurchaseMessage message) {
		validateRequiredParameters(message);

		if (!userRepository.existsById(message.userId())) {
			throw new ResourceNotFoundException("User with id %d was not found".formatted(message.userId()));
		}

		int updatedRows = ticketRepository.markAsSold(
				message.ticketId(),
				message.userId(),
				TicketEntity.TicketStatus.SOLD.name());

		if (updatedRows == 0) {
			throw new ResourceNotFoundException("Ticket with id %d was not found".formatted(message.ticketId()));
		}

		return ticketRepository.findById(message.ticketId())
				.orElseThrow(() -> new IllegalStateException(
						"Ticket %d was updated but could not be reloaded".formatted(message.ticketId())));
	}

	private void validateRequiredParameters(TicketPurchaseMessage message) {
		if (message == null) {
			throw new MissingParameterException("Message payload is required");
		}

		if (message.ticketId() == null) {
			throw new MissingParameterException("Missing required parameter: ticketId");
		}

		if (message.userId() == null) {
			throw new MissingParameterException("Missing required parameter: userId");
		}

		if (!StringUtils.hasText(message.userEmail())) {
			throw new MissingParameterException("Missing required parameter: userEmail");
		}

		if (!StringUtils.hasText(message.userWhatsapp())) {
			throw new MissingParameterException("Missing required parameter: userWhatsapp");
		}
	}
}