CREATE TABLE lead
(
    lead_id BIGINT IDENTITY(1,1) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name  VARCHAR(100) NOT NULL,
    email   VARCHAR(255) NULL,
    phone   VARCHAR(50) NULL,
    company VARCHAR(255) NULL,
    source VARCHAR(30) NOT NULL DEFAULT 'OTHER',
    status VARCHAR(20) NOT NULL DEFAULT 'NEW',
    estimated_value NUMERIC(14, 2) NULL,
    owner_id BIGINT NULL,
    converted_customer_id BIGINT NULL,
    notes VARCHAR(MAX) NULL,
    last_updated_user_id BIGINT NOT NULL DEFAULT 1,
    last_updated_at DATETIMEOFFSET NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_user_id BIGINT NOT NULL DEFAULT 1,
    created_at DATETIMEOFFSET NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_lead
        PRIMARY KEY (lead_id)
);

ALTER TABLE lead
    ADD CONSTRAINT fk_lead_owner
        FOREIGN KEY (owner_id)
            REFERENCES users (user_id);

ALTER TABLE lead
    ADD CONSTRAINT fk_lead_converted_customer
        FOREIGN KEY (converted_customer_id)
            REFERENCES customer (customer_id);

ALTER TABLE lead
    ADD CONSTRAINT fk_lead_last_updated_user
        FOREIGN KEY (last_updated_user_id)
            REFERENCES users (user_id);

ALTER TABLE lead
    ADD CONSTRAINT fk_lead_created_user
        FOREIGN KEY (created_user_id)
            REFERENCES users (user_id);

ALTER TABLE lead
    ADD CONSTRAINT chk_lead_source
        CHECK (
            source IN (
                       'WEBSITE',
                       'REFERRAL',
                       'COLD_CALL',
                       'EVENT',
                       'SOCIAL_MEDIA',
                       'ADVERTISEMENT',
                       'OTHER'
                )
            );

ALTER TABLE lead
    ADD CONSTRAINT chk_lead_status
        CHECK (
            status IN (
                       'NEW',
                       'CONTACTED',
                       'QUALIFIED',
                       'UNQUALIFIED',
                       'CONVERTED'
                )
            );


CREATE INDEX idx_lead_owner_id ON lead (owner_id);

CREATE INDEX idx_lead_status ON lead (status);

CREATE INDEX idx_lead_converted_customer_id ON lead (converted_customer_id);
