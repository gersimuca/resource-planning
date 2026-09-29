alter table users add role varchar(20) null;

ALTER TABLE users
    ADD CONSTRAINT chk_user_role
        CHECK (
            type IN (
                     'ADMIN',
                     'SALES',
                     'SUPPORT',
                     'MARKETING'
                )
            );
