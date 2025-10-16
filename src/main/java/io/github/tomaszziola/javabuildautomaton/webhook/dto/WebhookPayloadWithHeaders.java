package io.github.tomaszziola.javabuildautomaton.webhook.dto;

public record WebhookPayloadWithHeaders(WebhookPayload dto, String deliveryId, String eventType) {}
