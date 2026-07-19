package com.example.ticket_consumer_service.exception;

public class MissingParameterException extends RuntimeException {

	public MissingParameterException(String message) {
		super(message);
	}
}