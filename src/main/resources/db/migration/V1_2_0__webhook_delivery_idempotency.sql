CREATE TABLE IF NOT EXISTS webhook_delivery (
id BIGSERIAL PRIMARY KEY ,
delivery_id VARCHAR(128) NOT NULL UNIQUE ,
received_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_webhook_delivery_delivery_id ON webhook_delivery (delivery_id);
