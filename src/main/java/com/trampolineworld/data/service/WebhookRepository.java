package com.trampolineworld.data.service;

import com.trampolineworld.data.entity.Webhook;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WebhookRepository extends JpaRepository<Webhook, UUID> {

	@Query(
	"select w from Webhook w " + 
	"where lower(w.webhookName) like lower(concat('%', :filterText, '%')) "
	+ "or lower(w.webhookUrl) like lower(concat('%', :filterText, '%'))"
	)
	List<Webhook> search(@Param("filterText") String filterText);
    Webhook findByWebhookName(String webhookName);
}