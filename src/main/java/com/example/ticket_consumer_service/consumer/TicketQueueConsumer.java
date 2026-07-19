package com.example.ticket_consumer_service.consumer;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.example.ticket_consumer_service.config.TicketQueueProperties;
import com.example.ticket_consumer_service.domain.TicketEntity;
import com.example.ticket_consumer_service.dto.TicketPurchaseMessage;
import com.example.ticket_consumer_service.helper.EmailHelper;
import com.example.ticket_consumer_service.helper.WhatAppHelper;
import com.example.ticket_consumer_service.service.TicketPurchaseProcessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

@Component
public class TicketQueueConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(TicketQueueConsumer.class);

	private final SqsClient sqsClient;
	private final ObjectMapper objectMapper;
	private final TicketPurchaseProcessor processor;
	private EmailHelper emailHelper;
	private WhatAppHelper whatAppHelper;
	private TicketQueueProperties queueProperties;

	public TicketQueueConsumer(
			SqsClient sqsClient,
			ObjectMapper objectMapper,
			TicketPurchaseProcessor processor) {
		this.sqsClient = sqsClient;
		this.objectMapper = objectMapper;
		this.processor = processor;
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
        System.out.println("Consuming messages from SQS queue: " + queueProperties.getUrl());
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

			for (Message message : messages) {
				processMessage(message);
			}
		} catch (SdkClientException exception) {
			LOGGER.error("Failed to read from SQS. Check AWS credentials and region configuration.", exception);
		}
	}

	void processMessage(Message message) {
		try {
			TicketPurchaseMessage purchaseMessage = objectMapper.readValue(message.body(), TicketPurchaseMessage.class);
			TicketEntity ticket = processor.process(purchaseMessage);

			emailHelper.sendTicketSoldEmail(purchaseMessage.userEmail(), ticket);
			whatAppHelper.sendTicketSoldMessage(purchaseMessage.userWhatsapp(), ticket);

			sqsClient.deleteMessage(DeleteMessageRequest.builder()
					.queueUrl(queueProperties.getUrl())
					.receiptHandle(message.receiptHandle())
					.build());
		} catch (JsonProcessingException exception) {
			LOGGER.error("Failed to deserialize ticket purchase message", exception);
		} catch (RuntimeException exception) {
			LOGGER.error("Failed to process ticket purchase message", exception);
		}
	}
}