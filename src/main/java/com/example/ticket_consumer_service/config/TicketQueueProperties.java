package com.example.ticket_consumer_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ticket.queue")
public class TicketQueueProperties {

	private String url;
	private int maxMessages = 10;
	private int waitTimeSeconds = 1;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getMaxMessages() {
		return maxMessages;
	}

	public void setMaxMessages(int maxMessages) {
		this.maxMessages = maxMessages;
	}

	public int getWaitTimeSeconds() {
		return waitTimeSeconds;
	}

	public void setWaitTimeSeconds(int waitTimeSeconds) {
		this.waitTimeSeconds = waitTimeSeconds;
	}
}