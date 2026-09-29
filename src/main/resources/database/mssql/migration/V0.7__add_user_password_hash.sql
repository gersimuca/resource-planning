-- This service used to delegate authentication to Keycloak entirely and never stored a
-- credential of its own. Now that it issues and validates its own tokens, it needs somewhere to
-- keep a password hash. Nullable because existing rows have no password yet -- see UserEntity's
-- Javadoc on passwordHash for how that gets resolved.
alter table users add password_hash varchar(512) null;
