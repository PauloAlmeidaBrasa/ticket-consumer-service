package com.example.ticket_consumer_service.consumer;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.example.ticket_consumer_service.config.TicketQueueProperties;
import com.example.ticket_consumer_service.domain.TicketEntity;
import com.example.ticket_consumer_service.dto.TicketPurchaseMessage;
import com.example.ticket_consumer_service.handler.ConsumerErrorHandler;
import com.example.ticket_consumer_service.helper.EmailHelper;
import com.example.ticket_consumer_service.helper.WhatAppHelper;
import com.example.ticket_consumer_service.service.TicketPurchaseProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

@Component
public class TicketQueueConsumer {

	private final SqsClient sqsClient;
	private final ObjectMapper objectMapper;
	private final TicketPurchaseProcessor processor;
	private final ConsumerErrorHandler errorHandler;
	private EmailHelper emailHelper;
	private WhatAppHelper whatAppHelper;
	private TicketQueueProperties queueProperties;

	public TicketQueueConsumer(
			SqsClient sqsClient,
			ObjectMapper objectMapper,
			TicketPurchaseProcessor processor,
			ConsumerErrorHandler errorHandler) {
		this.sqsClient = sqsClient;
		this.objectMapper = objectMapper;
		this.processor = processor;
		this.errorHandler = errorHandler;
	}

	@Autowired
	void setSupportComponents(
			EmailHelper emailHelper,
			WhatAppHelper whatAppHelper,
			TicketQueueProperties queueProperties) {
		this.emailHelper = emailHelper;
		this.whatAppHelper = whatAppHelper;
		this.queueProperties = queueProperties;
	}

	@Scheduled(fixedDelay = 1000)
	public void consume() {
		if (queueProperties == null || !StringUtils.hasText(queueProperties.getUrl())) {
			return;
		}

		ReceiveMessageRequest request = ReceiveMessageRequest.builder()
				.queueUrl(queueProperties.getUrl())
				.maxNumberOfMessages(queueProperties.getMaxMessages())
				.waitTimeSeconds(queueProperties.getWaitTimeSeconds())
				.build();


		try {
			List<Message> messages = sqsClient.receiveMessage(request).messages();

			System.out.println();
			for (Message message : messages) {
				processMessage(message);
			}
		} catch (SdkClientException exception) {
			errorHandler.handle(exception, null);
		} catch (RuntimeException exception) {
			errorHandler.handle(exception, null);
		}
	}

	void processMessage(Message message) {
		try {
			System.out.println("Processing message: " + message.body());
			TicketPurchaseMessage purchaseMessage = objectMapper.readValue(message.body(), TicketPurchaseMessage.class);
			TicketEntity ticket = processor.process(purchaseMessage);

			emailHelper.sendTicketSoldEmail(purchaseMessage.userEmail(), ticket);
			whatAppHelper.sendTicketSoldMessage(purchaseMessage.userWhatsapp(), ticket);

			sqsClient.deleteMessage(DeleteMessageRequest.builder()
					.queueUrl(queueProperties.getUrl())
					.receiptHandle(message.receiptHandle())
					.build());
		} catch (Exception exception) {
			errorHandler.handle(exception, message.messageId());
		}
	}
}