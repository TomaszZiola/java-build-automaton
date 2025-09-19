package io.github.tomaszziola.javabuildautomaton.webhook;

import io.github.tomaszziola.javabuildautomaton.webhook.entity.WebhookDelivery;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookDeliveryRepository extends JpaRepository<WebhookDelivery, Long> {}
