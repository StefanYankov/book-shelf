CREATE TABLE account_status_events
(
    id UUID NOT NULL,
    version     BIGINT,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    user_id UUID NOT NULL,
    event_type  VARCHAR(255) NOT NULL,
    reason      VARCHAR(500),
    actor_id UUID,
    expiry_date TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_account_status_events PRIMARY KEY (id)
);

ALTER TABLE account_status_events
    ADD CONSTRAINT FK_ACCOUNT_STATUS_EVENTS_ON_ACTOR FOREIGN KEY (actor_id) REFERENCES users (id);

ALTER TABLE account_status_events
    ADD CONSTRAINT FK_ACCOUNT_STATUS_EVENTS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE application_users
    DROP COLUMN is_active;