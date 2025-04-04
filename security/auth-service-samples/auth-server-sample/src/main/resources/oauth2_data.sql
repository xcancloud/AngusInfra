-- @formatter:off

INSERT INTO oauth2_user (username, password, enabled, id)
VALUES ('user1', '{noop}password1', true, '1'),
       ('user2', '{noop}password2', true, '2');

INSERT INTO oauth2_authorities (username, authority)
VALUES ('user1', 'client:list'),
       ('user2', 'ROLE_ADMIN');

-- Insert a client with password + refresh_token grant types
INSERT INTO oauth2_registered_client (id, client_id, client_secret, client_name,
                                      client_authentication_methods, authorization_grant_types,
                                      redirect_uris, scopes, client_settings, token_settings,
                                      enabled, platform, source)
VALUES ('password-client-id', -- id
        'password-client', -- client_id
        '{noop}secret', -- client_secret (plain text password)
        'Password Client', -- client_name
        'client_secret_post,client_secret_basic', -- client_authentication_methods
        'refresh_token,password', -- authorization_grant_types
        NULL, -- redirect_uris (not required for password grant)
        'read,write', -- scopes
        '{"@class":"java.util.Collections$UnmodifiableMap","settings.client.require-authorization-consent":true}', -- client_settings
        '{"@class":"java.util.Collections$UnmodifiableMap","settings.token.reuse-refresh-tokens":false,"settings.token.access-token-time-to-live":["java.time.Duration",3600.000000000],"settings.token.access-token-format":{"@class":"org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat","value":"reference"},"settings.token.refresh-token-time-to-live":["java.time.Duration",43200.000000000],"settings.token.authorization-code-time-to-live":["java.time.Duration",300.000000000],"settings.token.device-code-time-to-live":["java.time.Duration",300.000000000]}', -- token_settings
        true, 'XCAN_TP', 'XCAN_TP_SIGNIN'
       );

-- Insert a client with client_credentials grant type
INSERT INTO oauth2_registered_client (id, client_id, client_secret, client_name,
                                      client_authentication_methods, authorization_grant_types,
                                      redirect_uris, scopes, client_settings, token_settings,
                                      enabled, platform, source)
VALUES ('client-credentials-client-id', -- id
        'client-credentials-client', -- client_id
        '{noop}secret', -- client_secret (plain text password)
        'Client Credentials Client', -- client_name
        'client_secret_post,client_secret_basic', -- client_authentication_methods
        'client_credentials', -- authorization_grant_types
        NULL, -- redirect_uris (not required for client_credentials grant)
        'read', -- scopes
        '{"@class":"java.util.Collections$UnmodifiableMap","settings.client.require-authorization-consent":false}', -- client_settings
        '{"@class":"java.util.Collections$UnmodifiableMap","settings.token.access-token-time-to-live":["java.time.Duration",3600],"settings.token.access-token-format":{"@class":"org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat","value":"reference"}}', -- token_settings
        true, 'XCAN_TP', 'XCAN_SYS_TOKEN'
       );

-- Insert a client with client_credentials grant type, used by token validation in /oauth/introspect endpoint
INSERT INTO oauth2_registered_client (id, client_id, client_secret, client_name,
                                      client_authentication_methods, authorization_grant_types,
                                      redirect_uris, scopes, client_settings, token_settings)
VALUES ('client-credentials-introspect-id', -- id
        'client-credentials-introspect-client', -- client_id
        '{noop}secret', -- client_secret (plain text password)
        'Client Credentials Introspect Client', -- client_name
        'client_secret_post,client_secret_basic', -- client_authentication_methods
        'client_credentials', -- authorization_grant_types
        NULL, -- redirect_uris (not required for client_credentials grant)
        'read,write', -- scopes
        '{"@class":"java.util.Collections$UnmodifiableMap","settings.client.require-authorization-consent":false}', -- client_settings
        '{"@class":"java.util.Collections$UnmodifiableMap","settings.token.access-token-time-to-live":["java.time.Duration",360000],"settings.token.access-token-format":{"@class":"org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat","value":"reference"}}' -- token_settings
       );

-- @formatter:on
