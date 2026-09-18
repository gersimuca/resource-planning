CREATE TABLE customer
(
    customer_id      bigint identity (1,1) primary key,
    name             VARCHAR(255) NOT NULL,
    email            VARCHAR(255),
    phone            VARCHAR(50),
    company          VARCHAR(255),
    address_line     VARCHAR(255),
    city             VARCHAR(100),
    state            VARCHAR(100),
    postal_code      VARCHAR(20),
    country          VARCHAR(100),
    status           VARCHAR(20)  NOT NULL DEFAULT 'PROSPECT',
    owner_id         bigint REFERENCES users (user_id),
    notes            TEXT,
    last_updated_user_id bigint        not null default 1,
    last_updated_at      datetimeoffset      not null default current_timestamp,
    created_user_id      bigint        not null default 1,
    created_at           datetimeoffset      not null default current_timestamp,
    CONSTRAINT chk_customers_status CHECK (status IN ('PROSPECT', 'ACTIVE', 'INACTIVE', 'CHURNED'))
);

CREATE INDEX idx_customers_owner_id ON customer (owner_id);
CREATE INDEX idx_customers_status ON customer (status);

alter table customer
    add constraint fk_customer_last_updated_user_id foreign key (last_updated_user_id) references users (user_id);
alter table customer
    add constraint fk_customer_created_user_id foreign key (created_user_id) references users (user_id);
