package com.example.ticket_consumer_service.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
public class TicketEntity {

	@Id
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = true)
	private UserEntity user;

	@ManyToOne
	@JoinColumn(name = "event_id", nullable = false)
	private EventEntity event;

	@Enumerated(EnumType.STRING)
	private TicketStatus status;

	@Column(name = "reserved_until")
	private LocalDateTime reservedUntil;

	protected TicketEntity() {
	}

	public TicketEntity(Long id, EventEntity event, UserEntity user, TicketStatus status) {
		this.id = id;
		this.event = event;
		this.user = user;
		this.status = status;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public UserEntity getUser() {
		return user;
	}

	public void setUser(UserEntity user) {
		this.user = user;
	}

	public EventEntity getEvent() {
		return event;
	}

	public void setEvent(EventEntity event) {
		this.event = event;
	}

	public TicketStatus getStatus() {
		return status;
	}

	public void setStatus(TicketStatus status) {
		this.status = status;
	}

	public LocalDateTime getReservedUntil() {
		return reservedUntil;
	}

	public void setReservedUntil(LocalDateTime reservedUntil) {
		this.reservedUntil = reservedUntil;
	}

	public enum TicketStatus {
		AVAILABLE,
		RESERVED,
		SOLD
	}
}