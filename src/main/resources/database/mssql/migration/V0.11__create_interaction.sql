CREATE TABLE interaction
(
    interaction_id BIGINT IDENTITY(1,1) NOT NULL,
    customer_id BIGINT NULL,
    lead_id     BIGINT NULL,
    type        VARCHAR(20)  NOT NULL,
    subject     VARCHAR(255) NOT NULL,
    description VARCHAR(MAX) NULL,
    customerStatus      VARCHAR(20) NOT NULL CONSTRAINT df_interaction_status DEFAULT 'OPEN',
    occurred_at DATETIMEOFFSET NOT NULL CONSTRAINT df_interaction_occurred_at DEFAULT current_timestamp,
    owner_id BIGINT NULL,
    last_updated_user_id bigint        not null default 1,
    last_updated_at      datetimeoffset      not null default current_timestamp,
    created_user_id      bigint        not null default 1,
    created_at           datetimeoffset      not null default current_timestamp,
    CONSTRAINT pk_interaction PRIMARY KEY (interaction_id)
);

ALTER TABLE interaction
    ADD CONSTRAINT fk_interaction_customer
        FOREIGN KEY (customer_id)
            REFERENCES customers (customer_id)
            ON DELETE CASCADE;

ALTER TABLE interaction
    ADD CONSTRAINT fk_interaction_lead
        FOREIGN KEY (lead_id)
            REFERENCES leads (lead_id)
            ON DELETE CASCADE;

ALTER TABLE interaction
    ADD CONSTRAINT fk_interaction_owner
        FOREIGN KEY (owner_id)
            REFERENCES users (user_id);

ALTER TABLE interaction
    ADD CONSTRAINT fk_interaction_last_updated_user
        FOREIGN KEY (last_updated_user_id)
            REFERENCES users (user_id);

ALTER TABLE interaction
    ADD CONSTRAINT fk_interaction_created_user
        FOREIGN KEY (created_user_id)
            REFERENCES users (user_id);

ALTER TABLE interaction
    ADD CONSTRAINT chk_interaction_type
        CHECK (
            type IN (
                     'CALL',
                     'EMAIL',
                     'MEETING',
                     'NOTE',
                     'SUPPORT_REQUEST'
                )
            );

ALTER TABLE interaction
    ADD CONSTRAINT chk_interaction_status
        CHECK (
            customerStatus IN (
                       'OPEN',
                       'IN_PROGRESS',
                       'RESOLVED',
                       'CLOSED'
                )
            );

ALTER TABLE interaction
    ADD CONSTRAINT chk_interaction_related_entity
        CHECK (
            customer_id IS NOT NULL
                OR lead_id IS NOT NULL
            );

CREATE INDEX idx_interaction_customer_id ON interaction (customer_id);

CREATE INDEX idx_interaction_lead_id ON interaction (lead_id);

CREATE INDEX idx_interaction_owner_id ON interaction (owner_id);

CREATE INDEX idx_interaction_occurred_at ON interaction (occurred_at DESC);
