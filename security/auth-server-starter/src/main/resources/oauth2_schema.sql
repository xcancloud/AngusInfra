-- org/springframework/security/oauth2/server/authorization/client/oauth2-registered-client-schema.sql
-- CREATE TABLE oauth2_registered_client
-- (
--     id                            varchar(100)                            NOT NULL,
--     client_id                     varchar(100)                            NOT NULL,
--     client_id_issued_at           timestamp     DEFAULT CURRENT_TIMESTAMP NOT NULL,
--     client_secret                 varchar(200)  DEFAULT NULL,
--     client_secret_expires_at      timestamp     DEFAULT NULL,
--     client_name                   varchar(200)                            NOT NULL,
--     client_authentication_methods varchar(1000)                           NOT NULL,
--     authorization_grant_types     varchar(1000)                           NOT NULL,
--     redirect_uris                 varchar(1000) DEFAULT NULL,
--     post_logout_redirect_uris     varchar(1000) DEFAULT NULL,
--     scopes                        varchar(1000)                           NOT NULL,
--     client_settings               varchar(2000)                           NOT NULL,
--     token_settings                varchar(2000)                           NOT NULL,
--     PRIMARY KEY (id)
-- );
-- Customization definition
CREATE TABLE oauth2_registered_client
(
    id                            varchar(40)   NOT NULL,
    client_id                     varchar(100)  NOT NULL,
    client_id_issued_at           timestamp              DEFAULT CURRENT_TIMESTAMP NOT NULL,
    client_secret                 varchar(200)           DEFAULT NULL,
    client_secret_expires_at      timestamp NULL DEFAULT NULL,
    client_name                   varchar(200)  NOT NULL,
    client_authentication_methods varchar(200)  NOT NULL,
    authorization_grant_types     varchar(200)  NOT NULL,
    redirect_uris                 varchar(800)           DEFAULT NULL,
    post_logout_redirect_uris     varchar(800)           DEFAULT NULL,
    scopes                        varchar(2000) NOT NULL,
    client_settings               varchar(2000) NOT NULL,
    token_settings                varchar(2000) NOT NULL,
    description                   varchar(200)           DEFAULT NULL,
    enabled                       boolean       NOT NULL DEFAULT '1',
    platform                      varchar(40)            DEFAULT NULL,
    source                        varchar(40)            DEFAULT NULL,
    biz_tag                       varchar(100)           DEFAULT NULL,
    tenant_id                     varchar(32)            DEFAULT '-1',
    tenant_name                   varchar(100)           DEFAULT NULL,
    created_by                    varchar(32)            DEFAULT '-1',
    created_date                  timestamp NULL DEFAULT NULL,
    last_modified_by              varchar(32)            DEFAULT '-1',
    last_modified_date            timestamp NULL DEFAULT NULL,
    PRIMARY KEY (id)
);
CREATE INDEX idx_client_id ON oauth2_registered_client (client_id);
CREATE INDEX idx_biz_tag ON oauth2_registered_client (biz_tag);
CREATE INDEX idx_tenant_id ON oauth2_registered_client (tenant_id);
CREATE INDEX idx_created_date ON oauth2_registered_client (created_date);

-- org/springframework/security/core/userdetails/jdbc/users.ddl
-- create table users
-- (
--     username varchar_ignorecase(50) not null primary key,
--     password varchar_ignorecase(500) not null,
--     enabled  boolean not null
-- );
-- Customization definition
create table oauth2_user
(
    username                    varchar(100) NOT NULL,
    password                    varchar(500) DEFAULT NULL,
    enabled                     boolean      NOT NULL DEFAULT '1',
    account_non_expired         boolean      NOT NULL DEFAULT '1',
    account_non_locked          boolean      NOT NULL DEFAULT '1',
    credentials_non_expired     boolean      NOT NULL DEFAULT '1',
    id                          varchar(32)  NOT NULL primary key,
    first_name                  varchar(100) NOT NULL DEFAULT '',
    last_name                   varchar(100) NOT NULL DEFAULT '',
    full_name                   varchar(100) NOT NULL DEFAULT '',
    password_strength           varchar(20)  NOT NULL DEFAULT '',
    sys_admin                   boolean      NOT NULL DEFAULT '0',
    to_user                     boolean      NOT NULL DEFAULT '0',
    email                       varchar(100) NOT NULL DEFAULT '',
    mobile                      varchar(16)  NOT NULL DEFAULT '',
    main_dept_id                varchar(32)  NOT NULL DEFAULT '-1',
    password_expired_date       timestamp NULL DEFAULT NULL,
    last_modified_password_date timestamp NULL DEFAULT NULL,
    expired_date                timestamp NULL DEFAULT NULL,
    deleted                     boolean      NOT NULL DEFAULT '0',
    tenant_id                   varchar(32)  NOT NULL DEFAULT '-1',
    tenant_name                 varchar(100)          DEFAULT NULL,
    tenant_real_name_status     varchar(20)           DEFAULT NULL,
    directory_id                varchar(32)           DEFAULT NULL,
    default_language            varchar(20)           DEFAULT NULL,
    default_time_zone           varchar(20)           DEFAULT NULL
);
CREATE UNIQUE INDEX idx_username ON oauth2_user (username);
CREATE INDEX uidx_mobile_tenant_id ON oauth2_user (username, tenant_id);
CREATE INDEX uidx_email_tenant_id ON oauth2_user (email, tenant_id);
CREATE INDEX idx_tenant_id_2 ON oauth2_user (tenant_id);

-- create table authorities
-- (
--     username  varchar_ignorecase(50) not null,
--     authority varchar_ignorecase(50) not null,
--     constraint fk_authorities_users foreign key (username) references users (username)
-- );
-- Customization definition
create table oauth2_authorities
(
    username  varchar(100) not null,
    authority varchar(100) not null
);
CREATE UNIQUE INDEX idx_auth_username ON oauth2_authorities (username, authority);

-- org/springframework/security/oauth2/server/authorization/oauth2-authorization-consent-schema.sql
CREATE TABLE oauth2_authorization_consent
(
    registered_client_id varchar(100)  NOT NULL,
    principal_name       varchar(200)  NOT NULL,
    authorities          varchar(1000) NOT NULL,
    PRIMARY KEY (registered_client_id, principal_name)
);

-- org/springframework/security/oauth2/server/authorization/oauth2-authorization-schema.sql
/*
IMPORTANT:
    If using PostgreSQL, update ALL columns defined with 'blob' to 'text',
    as PostgreSQL does not support the 'blob' data type.
*/
CREATE TABLE oauth2_authorization
(
    id                            varchar(100) NOT NULL,
    registered_client_id          varchar(100) NOT NULL,
    principal_name                varchar(200) NOT NULL,
    authorization_grant_type      varchar(100) NOT NULL,
    authorized_scopes             varchar(1000) DEFAULT NULL,
    attributes                    blob          DEFAULT NULL,
    state                         varchar(500)  DEFAULT NULL,
    authorization_code_value      blob          DEFAULT NULL,
    authorization_code_issued_at  timestamp NULL DEFAULT NULL,
    authorization_code_expires_at timestamp NULL DEFAULT NULL,
    authorization_code_metadata   blob          DEFAULT NULL,
    access_token_value            blob          DEFAULT NULL,
    access_token_issued_at        timestamp NULL DEFAULT NULL,
    access_token_expires_at       timestamp NULL DEFAULT NULL,
    access_token_metadata         blob          DEFAULT NULL,
    access_token_type             varchar(100)  DEFAULT NULL,
    access_token_scopes           varchar(1000) DEFAULT NULL,
    oidc_id_token_value           blob          DEFAULT NULL,
    oidc_id_token_issued_at       timestamp NULL DEFAULT NULL,
    oidc_id_token_expires_at      timestamp NULL DEFAULT NULL,
    oidc_id_token_metadata        blob          DEFAULT NULL,
    refresh_token_value           blob          DEFAULT NULL,
    refresh_token_issued_at       timestamp NULL DEFAULT NULL,
    refresh_token_expires_at      timestamp NULL DEFAULT NULL,
    refresh_token_metadata        blob          DEFAULT NULL,
    user_code_value               blob          DEFAULT NULL,
    user_code_issued_at           timestamp NULL DEFAULT NULL,
    user_code_expires_at          timestamp NULL DEFAULT NULL,
    user_code_metadata            blob          DEFAULT NULL,
    device_code_value             blob          DEFAULT NULL,
    device_code_issued_at         timestamp NULL DEFAULT NULL,
    device_code_expires_at        timestamp NULL DEFAULT NULL,
    device_code_metadata          blob          DEFAULT NULL,
    PRIMARY KEY (id)
);

-- org/springframework/security/core/ott/jdbc/one-time-tokens-schema.sql
-- create table one_time_tokens
-- (
--     token_value varchar(36) not null primary key,
--     username    varchar_ignorecase(50) not null,
--     expires_at  timestamp   not null
-- );
