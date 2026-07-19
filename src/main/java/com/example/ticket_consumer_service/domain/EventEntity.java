package com.example.ticket_consumer_service.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "event_catalog")
public class EventEntity {

	@Id
	private Long id;

	protected EventEntity() {
	}

	public EventEntity(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}