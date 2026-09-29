alter table users add role varchar(20) null;

ALTER TABLE users
    ADD CONSTRAINT chk_user_role
        CHECK (
            role IN (
                     'ADMIN',
                     'SALES',
                     'SUPPORT',
                     'MARKETING'
                )
            );
