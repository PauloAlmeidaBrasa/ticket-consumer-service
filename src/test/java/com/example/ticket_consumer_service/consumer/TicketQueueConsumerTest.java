package com.example.ticket_consumer_service.consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.ticket_consumer_service.config.TicketQueueProperties;
import com.example.ticket_consumer_service.domain.EventEntity;
import com.example.ticket_consumer_service.domain.TicketEntity;
import com.example.ticket_consumer_service.dto.TicketPurchaseMessage;
import com.example.ticket_consumer_service.helper.EmailHelper;
import com.example.ticket_consumer_service.helper.WhatAppHelper;
import com.example.ticket_consumer_service.service.TicketPurchaseProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;

import org.mockito.Mockito;

class TicketQueueConsumerTest {

	private SqsClient sqsClient;
	private TicketPurchaseProcessor processor;
	private EmailHelper emailHelper;
	private WhatAppHelper whatAppHelper;
	private TicketQueueConsumer consumer;
	private TicketQueueProperties properties;

	@BeforeEach
	void setUp() {
		sqsClient = Mockito.mock(SqsClient.class);
		processor = Mockito.mock(TicketPurchaseProcessor.class);
		emailHelper = Mockito.mock(EmailHelper.class);
		whatAppHelper = Mockito.mock(WhatAppHelper.class);
		consumer = new TicketQueueConsumer(sqsClient, new ObjectMapper(), processor);
		properties = new TicketQueueProperties();
		properties.setUrl("https://sqs.us-east-1.amazonaws.com/123456789012/tickets");
		consumer.setSupportComponents(emailHelper, whatAppHelper, properties);
	}

	@Test
	void processMessageUpdatesTicketAndSendsNotifications() {
		TicketEntity ticket = new TicketEntity(1L, new EventEntity(10L), null, TicketEntity.TicketStatus.SOLD);
		when(processor.process(any(TicketPurchaseMessage.class))).thenReturn(ticket);

		Message message = Message.builder()
				.body("""
						{"ticketId":1,"userId":2,"userEmail":"dahjd@hotmail.com","userWhatsapp":"87312837613"}
						""")
				.receiptHandle("receipt-1")
				.build();

		consumer.processMessage(message);

		verify(processor).process(new TicketPurchaseMessage(1L, 2L, "dahjd@hotmail.com", "87312837613"));
		verify(emailHelper).sendTicketSoldEmail("dahjd@hotmail.com", ticket);
		verify(whatAppHelper).sendTicketSoldMessage("87312837613", ticket);
		verify(sqsClient).deleteMessage(any(DeleteMessageRequest.class));
	}
}