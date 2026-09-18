CREATE TABLE deal
(
    deal_id              bigint identity (1,1),
    customer_id          BIGINT       NOT NULL REFERENCES customer (customer_id) ON DELETE CASCADE,
    title                VARCHAR(255) NOT NULL,
    stage                VARCHAR(20)  NOT NULL DEFAULT 'PROSPECTING',
    amount               NUMERIC(14, 2)        DEFAULT 0,
    expected_close_date  DATE,
    owner_id             BIGINT REFERENCES users (user_id),
    notes                TEXT,
    last_updated_user_id bigint        not null default 1,
    last_updated_at      datetimeoffset      not null default current_timestamp,
    created_user_id      bigint        not null default 1,
    created_at           datetimeoffset      not null default current_timestamp,

    CONSTRAINT chk_deals_stage CHECK (
        stage IN (
                  'PROSPECTING',
                  'QUALIFICATION',
                  'PROPOSAL',
                  'NEGOTIATION',
                  'CLOSED_WON',
                  'CLOSED_LOST'
            )
        )
);

CREATE INDEX idx_deals_customer_id ON deal (customer_id);
CREATE INDEX idx_deals_owner_id ON deal (owner_id);
CREATE INDEX idx_deals_stage ON deal (stage);

alter table deal
    add constraint fk_deal_last_updated_user_id foreign key (last_updated_user_id) references users (user_id);
alter table deal
    add constraint fk_deal_created_user_id foreign key (created_user_id) references users (user_id);
alter table deal
    add constraint fk_deal_customer_id foreign key (customer_id) references customer (customer_id);
alter table deal
    add constraint fk_deal_user_id foreign key (owner_id) references users (user_id);
