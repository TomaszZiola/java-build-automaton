package io.github.tomaszziola.javabuildautomaton.webhook.entity;

import static jakarta.persistence.GenerationType.IDENTITY;
import static java.time.Instant.now;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
    name = "webhook_delivery",
    indexes = {
      @Index(name = "ux_webhook_delivery_delivery_id", columnList = "delivery_id", unique = true)
    })
public class WebhookDelivery {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  @Column(name = "delivery_id", nullable = false, unique = true, length = 128)
  private String deliveryId;

  @Column(name = "received_at", nullable = false)
  private Instant receivedAt;

  @PrePersist
  void prePersist() {
    if (receivedAt == null) {
      receivedAt = now();
    }
  }
}
