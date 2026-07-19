package com.example.ticket_consumer_service.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.ticket_consumer_service.domain.TicketEntity;

public interface TicketRepository extends JpaRepository<TicketEntity, Long> {

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query(value = """
			update tickets
			set status = :status,
			    user_id = :userId
			where id = :ticketId
			""", nativeQuery = true)
	int markAsSold(@Param("ticketId") Long ticketId, @Param("userId") Long userId, @Param("status") String status);
}