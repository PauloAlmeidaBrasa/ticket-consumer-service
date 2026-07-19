package com.example.ticket_consumer_service.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

import com.example.ticket_consumer_service.domain.EventEntity;
import com.example.ticket_consumer_service.domain.TicketEntity;
import com.example.ticket_consumer_service.domain.UserEntity;
import com.example.ticket_consumer_service.dto.TicketPurchaseMessage;
import com.example.ticket_consumer_service.repository.TicketRepository;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"app.ticket.queue.url="
})
@Transactional
class TicketPurchaseProcessorTest {

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private TicketRepository ticketRepository;

	@Autowired
	private TicketPurchaseProcessor processor;

	@Test
	void processMarksTicketAsSoldAndAssignsUser() {
		EventEntity event = new EventEntity(10L);
		UserEntity user = new UserEntity(2L);
		entityManager.persist(event);
		entityManager.persist(user);

		TicketEntity ticket = new TicketEntity(1L, event, null, TicketEntity.TicketStatus.AVAILABLE);
		entityManager.persist(ticket);
		entityManager.flush();

		TicketEntity processedTicket = processor.process(
				new TicketPurchaseMessage(1L, 2L, "dahjd@hotmail.com", "87312837613"));

		assertThat(processedTicket.getStatus()).isEqualTo(TicketEntity.TicketStatus.SOLD);
		assertThat(processedTicket.getUser()).isNotNull();
		assertThat(processedTicket.getUser().getId()).isEqualTo(2L);

		TicketEntity persistedTicket = ticketRepository.findById(1L).orElseThrow();
		assertThat(persistedTicket.getStatus()).isEqualTo(TicketEntity.TicketStatus.SOLD);
		assertThat(persistedTicket.getUser()).isNotNull();
		assertThat(persistedTicket.getUser().getId()).isEqualTo(2L);
	}
}