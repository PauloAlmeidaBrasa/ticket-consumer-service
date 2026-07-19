package com.example.ticket_consumer_service.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.example.ticket_consumer_service.exception.MissingParameterException;
import com.example.ticket_consumer_service.exception.ResourceNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;

import software.amazon.awssdk.core.exception.SdkClientException;

@Component
public class ConsumerErrorHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerErrorHandler.class);

	public void handle(Throwable error, String messageId) {
		String context = messageId == null ? "" : " [messageId=%s]".formatted(messageId);

		if (error instanceof MissingParameterException || error instanceof ResourceNotFoundException) {
			LOGGER.warn("Business error%s: {}", context, error.getMessage());
			return;
		}

		if (error instanceof JsonProcessingException) {
			LOGGER.warn("Invalid SQS payload%s: message format is not supported.", context);
			LOGGER.debug("Payload parsing failure details", error);
			return;
		}

		if (error instanceof SdkClientException) {
			LOGGER.error("SQS communication failed. Check AWS credentials, region, and queue access policy.");
			LOGGER.debug("SQS SDK failure details", error);
			return;
		}

		LOGGER.error("Unexpected internal error while processing ticket purchase%s.", context);
		LOGGER.debug("Unexpected error details", error);
	}
}