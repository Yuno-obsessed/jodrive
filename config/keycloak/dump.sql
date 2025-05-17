SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;



CREATE SCHEMA if not exists keycloak;


SET default_tablespace = '';

SET default_table_access_method = heap;



CREATE TABLE keycloak.admin_event_entity
(
    id               character varying(36) NOT NULL,
    admin_event_time bigint,
    realm_id         character varying(255),
    operation_type   character varying(255),
    auth_realm_id    character varying(255),
    auth_client_id   character varying(255),
    auth_user_id     character varying(255),
    ip_address       character varying(255),
    resource_path    character varying(2550),
    representation   text,
    error            character varying(255),
    resource_type    character varying(64),
    details_json     text
);



CREATE TABLE keycloak.associated_policy
(
    policy_id            character varying(36) NOT NULL,
    associated_policy_id character varying(36) NOT NULL
);



CREATE TABLE keycloak.authentication_execution
(
    id                 character varying(36) NOT NULL,
    alias              character varying(255),
    authenticator      character varying(36),
    realm_id           character varying(36),
    flow_id            character varying(36),
    requirement        integer,
    priority           integer,
    authenticator_flow boolean DEFAULT false NOT NULL,
    auth_flow_id       character varying(36),
    auth_config        character varying(36)
);



CREATE TABLE keycloak.authentication_flow
(
    id          character varying(36)                                         NOT NULL,
    alias       character varying(255),
    description character varying(255),
    realm_id    character varying(36),
    provider_id character varying(36) DEFAULT 'basic-flow'::character varying NOT NULL,
    top_level   boolean               DEFAULT false                           NOT NULL,
    built_in    boolean               DEFAULT false                           NOT NULL
);



CREATE TABLE keycloak.authenticator_config
(
    id       character varying(36) NOT NULL,
    alias    character varying(255),
    realm_id character varying(36)
);



CREATE TABLE keycloak.authenticator_config_entry
(
    authenticator_id character varying(36)  NOT NULL,
    value            text,
    name             character varying(255) NOT NULL
);



CREATE TABLE keycloak.broker_link
(
    identity_provider   character varying(255) NOT NULL,
    storage_provider_id character varying(255),
    realm_id            character varying(36)  NOT NULL,
    broker_user_id      character varying(255),
    broker_username     character varying(255),
    token               text,
    user_id             character varying(255) NOT NULL
);



CREATE TABLE keycloak.client
(
    id                           character varying(36) NOT NULL,
    enabled                      boolean DEFAULT false NOT NULL,
    full_scope_allowed           boolean DEFAULT false NOT NULL,
    client_id                    character varying(255),
    not_before                   integer,
    public_client                boolean DEFAULT false NOT NULL,
    secret                       character varying(255),
    base_url                     character varying(255),
    bearer_only                  boolean DEFAULT false NOT NULL,
    management_url               character varying(255),
    surrogate_auth_required      boolean DEFAULT false NOT NULL,
    realm_id                     character varying(36),
    protocol                     character varying(255),
    node_rereg_timeout           integer DEFAULT 0,
    frontchannel_logout          boolean DEFAULT false NOT NULL,
    consent_required             boolean DEFAULT false NOT NULL,
    name                         character varying(255),
    service_accounts_enabled     boolean DEFAULT false NOT NULL,
    client_authenticator_type    character varying(255),
    root_url                     character varying(255),
    description                  character varying(255),
    registration_token           character varying(255),
    standard_flow_enabled        boolean DEFAULT true  NOT NULL,
    implicit_flow_enabled        boolean DEFAULT false NOT NULL,
    direct_access_grants_enabled boolean DEFAULT false NOT NULL,
    always_display_in_console    boolean DEFAULT false NOT NULL
);



CREATE TABLE keycloak.client_attributes
(
    client_id character varying(36)  NOT NULL,
    name      character varying(255) NOT NULL,
    value     text
);



CREATE TABLE keycloak.client_auth_flow_bindings
(
    client_id    character varying(36)  NOT NULL,
    flow_id      character varying(36),
    binding_name character varying(255) NOT NULL
);



CREATE TABLE keycloak.client_initial_access
(
    id              character varying(36) NOT NULL,
    realm_id        character varying(36) NOT NULL,
    "timestamp"     integer,
    expiration      integer,
    count           integer,
    remaining_count integer
);



CREATE TABLE keycloak.client_node_registrations
(
    client_id character varying(36)  NOT NULL,
    value     integer,
    name      character varying(255) NOT NULL
);



CREATE TABLE keycloak.client_scope
(
    id          character varying(36) NOT NULL,
    name        character varying(255),
    realm_id    character varying(36),
    description character varying(255),
    protocol    character varying(255)
);



CREATE TABLE keycloak.client_scope_attributes
(
    scope_id character varying(36)  NOT NULL,
    value    character varying(2048),
    name     character varying(255) NOT NULL
);



CREATE TABLE keycloak.client_scope_client
(
    client_id     character varying(255) NOT NULL,
    scope_id      character varying(255) NOT NULL,
    default_scope boolean DEFAULT false  NOT NULL
);



CREATE TABLE keycloak.client_scope_role_mapping
(
    scope_id character varying(36) NOT NULL,
    role_id  character varying(36) NOT NULL
);



CREATE TABLE keycloak.component
(
    id            character varying(36) NOT NULL,
    name          character varying(255),
    parent_id     character varying(36),
    provider_id   character varying(36),
    provider_type character varying(255),
    realm_id      character varying(36),
    sub_type      character varying(255)
);



CREATE TABLE keycloak.component_config
(
    id           character varying(36)  NOT NULL,
    component_id character varying(36)  NOT NULL,
    name         character varying(255) NOT NULL,
    value        text
);



CREATE TABLE keycloak.composite_role
(
    composite  character varying(36) NOT NULL,
    child_role character varying(36) NOT NULL
);



CREATE TABLE keycloak.credential
(
    id              character varying(36) NOT NULL,
    salt            bytea,
    type            character varying(255),
    user_id         character varying(36),
    created_date    bigint,
    user_label      character varying(255),
    secret_data     text,
    credential_data text,
    priority        integer,
    version         integer DEFAULT 0
);



CREATE TABLE keycloak.databasechangelog
(
    id            character varying(255)      NOT NULL,
    author        character varying(255)      NOT NULL,
    filename      character varying(255)      NOT NULL,
    dateexecuted  timestamp without time zone NOT NULL,
    orderexecuted integer                     NOT NULL,
    exectype      character varying(10)       NOT NULL,
    md5sum        character varying(35),
    description   character varying(255),
    comments      character varying(255),
    tag           character varying(255),
    liquibase     character varying(20),
    contexts      character varying(255),
    labels        character varying(255),
    deployment_id character varying(10)
);



CREATE TABLE keycloak.databasechangeloglock
(
    id          integer NOT NULL,
    locked      boolean NOT NULL,
    lockgranted timestamp without time zone,
    lockedby    character varying(255)
);



CREATE TABLE keycloak.default_client_scope
(
    realm_id      character varying(36) NOT NULL,
    scope_id      character varying(36) NOT NULL,
    default_scope boolean DEFAULT false NOT NULL
);



CREATE TABLE keycloak.event_entity
(
    id                      character varying(36) NOT NULL,
    client_id               character varying(255),
    details_json            character varying(2550),
    error                   character varying(255),
    ip_address              character varying(255),
    realm_id                character varying(255),
    session_id              character varying(255),
    event_time              bigint,
    type                    character varying(255),
    user_id                 character varying(255),
    details_json_long_value text
);



CREATE TABLE keycloak.fed_user_attribute
(
    id                         character varying(36)  NOT NULL,
    name                       character varying(255) NOT NULL,
    user_id                    character varying(255) NOT NULL,
    realm_id                   character varying(36)  NOT NULL,
    storage_provider_id        character varying(36),
    value                      character varying(2024),
    long_value_hash            bytea,
    long_value_hash_lower_case bytea,
    long_value                 text
);



CREATE TABLE keycloak.fed_user_consent
(
    id                      character varying(36)  NOT NULL,
    client_id               character varying(255),
    user_id                 character varying(255) NOT NULL,
    realm_id                character varying(36)  NOT NULL,
    storage_provider_id     character varying(36),
    created_date            bigint,
    last_updated_date       bigint,
    client_storage_provider character varying(36),
    external_client_id      character varying(255)
);



CREATE TABLE keycloak.fed_user_consent_cl_scope
(
    user_consent_id character varying(36) NOT NULL,
    scope_id        character varying(36) NOT NULL
);



CREATE TABLE keycloak.fed_user_credential
(
    id                  character varying(36)  NOT NULL,
    salt                bytea,
    type                character varying(255),
    created_date        bigint,
    user_id             character varying(255) NOT NULL,
    realm_id            character varying(36)  NOT NULL,
    storage_provider_id character varying(36),
    user_label          character varying(255),
    secret_data         text,
    credential_data     text,
    priority            integer
);



CREATE TABLE keycloak.fed_user_group_membership
(
    group_id            character varying(36)  NOT NULL,
    user_id             character varying(255) NOT NULL,
    realm_id            character varying(36)  NOT NULL,
    storage_provider_id character varying(36)
);



CREATE TABLE keycloak.fed_user_required_action
(
    required_action     character varying(255) DEFAULT ' '::character varying NOT NULL,
    user_id             character varying(255)                                NOT NULL,
    realm_id            character varying(36)                                 NOT NULL,
    storage_provider_id character varying(36)
);



CREATE TABLE keycloak.fed_user_role_mapping
(
    role_id             character varying(36)  NOT NULL,
    user_id             character varying(255) NOT NULL,
    realm_id            character varying(36)  NOT NULL,
    storage_provider_id character varying(36)
);



CREATE TABLE keycloak.federated_identity
(
    identity_provider  character varying(255) NOT NULL,
    realm_id           character varying(36),
    federated_user_id  character varying(255),
    federated_username character varying(255),
    token              text,
    user_id            character varying(36)  NOT NULL
);



CREATE TABLE keycloak.federated_user
(
    id                  character varying(255) NOT NULL,
    storage_provider_id character varying(255),
    realm_id            character varying(36)  NOT NULL
);



CREATE TABLE keycloak.group_attribute
(
    id       character varying(36) DEFAULT 'sybase-needs-something-here'::character varying NOT NULL,
    name     character varying(255)                                                         NOT NULL,
    value    character varying(255),
    group_id character varying(36)                                                          NOT NULL
);



CREATE TABLE keycloak.group_role_mapping
(
    role_id  character varying(36) NOT NULL,
    group_id character varying(36) NOT NULL
);



CREATE TABLE keycloak.identity_provider
(
    internal_id                character varying(36) NOT NULL,
    enabled                    boolean DEFAULT false NOT NULL,
    provider_alias             character varying(255),
    provider_id                character varying(255),
    store_token                boolean DEFAULT false NOT NULL,
    authenticate_by_default    boolean DEFAULT false NOT NULL,
    realm_id                   character varying(36),
    add_token_role             boolean DEFAULT true  NOT NULL,
    trust_email                boolean DEFAULT false NOT NULL,
    first_broker_login_flow_id character varying(36),
    post_broker_login_flow_id  character varying(36),
    provider_display_name      character varying(255),
    link_only                  boolean DEFAULT false NOT NULL,
    organization_id            character varying(255),
    hide_on_login              boolean DEFAULT false
);



CREATE TABLE keycloak.identity_provider_config
(
    identity_provider_id character varying(36)  NOT NULL,
    value                text,
    name                 character varying(255) NOT NULL
);



CREATE TABLE keycloak.identity_provider_mapper
(
    id              character varying(36)  NOT NULL,
    name            character varying(255) NOT NULL,
    idp_alias       character varying(255) NOT NULL,
    idp_mapper_name character varying(255) NOT NULL,
    realm_id        character varying(36)  NOT NULL
);



CREATE TABLE keycloak.idp_mapper_config
(
    idp_mapper_id character varying(36)  NOT NULL,
    value         text,
    name          character varying(255) NOT NULL
);



CREATE TABLE keycloak.jgroups_ping
(
    address      character varying(200) NOT NULL,
    name         character varying(200),
    cluster_name character varying(200) NOT NULL,
    ip           character varying(200) NOT NULL,
    coord        boolean
);



CREATE TABLE keycloak.keycloak_group
(
    id           character varying(36) NOT NULL,
    name         character varying(255),
    parent_group character varying(36) NOT NULL,
    realm_id     character varying(36),
    type         integer DEFAULT 0     NOT NULL
);



CREATE TABLE keycloak.keycloak_role
(
    id                      character varying(36) NOT NULL,
    client_realm_constraint character varying(255),
    client_role             boolean DEFAULT false NOT NULL,
    description             character varying(255),
    name                    character varying(255),
    realm_id                character varying(255),
    client                  character varying(36),
    realm                   character varying(36)
);



CREATE TABLE keycloak.migration_model
(
    id          character varying(36) NOT NULL,
    version     character varying(36),
    update_time bigint DEFAULT 0      NOT NULL
);



CREATE TABLE keycloak.offline_client_session
(
    user_session_id         character varying(36)                                     NOT NULL,
    client_id               character varying(255)                                    NOT NULL,
    offline_flag            character varying(4)                                      NOT NULL,
    "timestamp"             integer,
    data                    text,
    client_storage_provider character varying(36)  DEFAULT 'local'::character varying NOT NULL,
    external_client_id      character varying(255) DEFAULT 'local'::character varying NOT NULL,
    version                 integer                DEFAULT 0
);



CREATE TABLE keycloak.offline_user_session
(
    user_session_id      character varying(36)  NOT NULL,
    user_id              character varying(255) NOT NULL,
    realm_id             character varying(36)  NOT NULL,
    created_on           integer                NOT NULL,
    offline_flag         character varying(4)   NOT NULL,
    data                 text,
    last_session_refresh integer DEFAULT 0      NOT NULL,
    broker_session_id    character varying(1024),
    version              integer DEFAULT 0
);



CREATE TABLE keycloak.org
(
    id           character varying(255) NOT NULL,
    enabled      boolean                NOT NULL,
    realm_id     character varying(255) NOT NULL,
    group_id     character varying(255) NOT NULL,
    name         character varying(255) NOT NULL,
    description  character varying(4000),
    alias        character varying(255) NOT NULL,
    redirect_url character varying(2048)
);



CREATE TABLE keycloak.org_domain
(
    id       character varying(36)  NOT NULL,
    name     character varying(255) NOT NULL,
    verified boolean                NOT NULL,
    org_id   character varying(255) NOT NULL
);



CREATE TABLE keycloak.policy_config
(
    policy_id character varying(36)  NOT NULL,
    name      character varying(255) NOT NULL,
    value     text
);



CREATE TABLE keycloak.protocol_mapper
(
    id                   character varying(36)  NOT NULL,
    name                 character varying(255) NOT NULL,
    protocol             character varying(255) NOT NULL,
    protocol_mapper_name character varying(255) NOT NULL,
    client_id            character varying(36),
    client_scope_id      character varying(36)
);



CREATE TABLE keycloak.protocol_mapper_config
(
    protocol_mapper_id character varying(36)  NOT NULL,
    value              text,
    name               character varying(255) NOT NULL
);



CREATE TABLE keycloak.realm
(
    id                           character varying(36)               NOT NULL,
    access_code_lifespan         integer,
    user_action_lifespan         integer,
    access_token_lifespan        integer,
    account_theme                character varying(255),
    admin_theme                  character varying(255),
    email_theme                  character varying(255),
    enabled                      boolean               DEFAULT false NOT NULL,
    events_enabled               boolean               DEFAULT false NOT NULL,
    events_expiration            bigint,
    login_theme                  character varying(255),
    name                         character varying(255),
    not_before                   integer,
    password_policy              character varying(2550),
    registration_allowed         boolean               DEFAULT false NOT NULL,
    remember_me                  boolean               DEFAULT false NOT NULL,
    reset_password_allowed       boolean               DEFAULT false NOT NULL,
    social                       boolean               DEFAULT false NOT NULL,
    ssl_required                 character varying(255),
    sso_idle_timeout             integer,
    sso_max_lifespan             integer,
    update_profile_on_soc_login  boolean               DEFAULT false NOT NULL,
    verify_email                 boolean               DEFAULT false NOT NULL,
    master_admin_client          character varying(36),
    login_lifespan               integer,
    internationalization_enabled boolean               DEFAULT false NOT NULL,
    default_locale               character varying(255),
    reg_email_as_username        boolean               DEFAULT false NOT NULL,
    admin_events_enabled         boolean               DEFAULT false NOT NULL,
    admin_events_details_enabled boolean               DEFAULT false NOT NULL,
    edit_username_allowed        boolean               DEFAULT false NOT NULL,
    otp_policy_counter           integer               DEFAULT 0,
    otp_policy_window            integer               DEFAULT 1,
    otp_policy_period            integer               DEFAULT 30,
    otp_policy_digits            integer               DEFAULT 6,
    otp_policy_alg               character varying(36) DEFAULT 'HmacSHA1'::character varying,
    otp_policy_type              character varying(36) DEFAULT 'totp'::character varying,
    browser_flow                 character varying(36),
    registration_flow            character varying(36),
    direct_grant_flow            character varying(36),
    reset_credentials_flow       character varying(36),
    client_auth_flow             character varying(36),
    offline_session_idle_timeout integer               DEFAULT 0,
    revoke_refresh_token         boolean               DEFAULT false NOT NULL,
    access_token_life_implicit   integer               DEFAULT 0,
    login_with_email_allowed     boolean               DEFAULT true  NOT NULL,
    duplicate_emails_allowed     boolean               DEFAULT false NOT NULL,
    docker_auth_flow             character varying(36),
    refresh_token_max_reuse      integer               DEFAULT 0,
    allow_user_managed_access    boolean               DEFAULT false NOT NULL,
    sso_max_lifespan_remember_me integer               DEFAULT 0     NOT NULL,
    sso_idle_timeout_remember_me integer               DEFAULT 0     NOT NULL,
    default_role                 character varying(255)
);



CREATE TABLE keycloak.realm_attribute
(
    name     character varying(255) NOT NULL,
    realm_id character varying(36)  NOT NULL,
    value    text
);



CREATE TABLE keycloak.realm_default_groups
(
    realm_id character varying(36) NOT NULL,
    group_id character varying(36) NOT NULL
);



CREATE TABLE keycloak.realm_enabled_event_types
(
    realm_id character varying(36)  NOT NULL,
    value    character varying(255) NOT NULL
);



CREATE TABLE keycloak.realm_events_listeners
(
    realm_id character varying(36)  NOT NULL,
    value    character varying(255) NOT NULL
);



CREATE TABLE keycloak.realm_localizations
(
    realm_id character varying(255) NOT NULL,
    locale   character varying(255) NOT NULL,
    texts    text                   NOT NULL
);



CREATE TABLE keycloak.realm_required_credential
(
    type       character varying(255) NOT NULL,
    form_label character varying(255),
    input      boolean DEFAULT false  NOT NULL,
    secret     boolean DEFAULT false  NOT NULL,
    realm_id   character varying(36)  NOT NULL
);



CREATE TABLE keycloak.realm_smtp_config
(
    realm_id character varying(36)  NOT NULL,
    value    character varying(255),
    name     character varying(255) NOT NULL
);



CREATE TABLE keycloak.realm_supported_locales
(
    realm_id character varying(36)  NOT NULL,
    value    character varying(255) NOT NULL
);



CREATE TABLE keycloak.redirect_uris
(
    client_id character varying(36)  NOT NULL,
    value     character varying(255) NOT NULL
);



CREATE TABLE keycloak.required_action_config
(
    required_action_id character varying(36)  NOT NULL,
    value              text,
    name               character varying(255) NOT NULL
);



CREATE TABLE keycloak.required_action_provider
(
    id             character varying(36) NOT NULL,
    alias          character varying(255),
    name           character varying(255),
    realm_id       character varying(36),
    enabled        boolean DEFAULT false NOT NULL,
    default_action boolean DEFAULT false NOT NULL,
    provider_id    character varying(255),
    priority       integer
);



CREATE TABLE keycloak.resource_attribute
(
    id          character varying(36) DEFAULT 'sybase-needs-something-here'::character varying NOT NULL,
    name        character varying(255)                                                         NOT NULL,
    value       character varying(255),
    resource_id character varying(36)                                                          NOT NULL
);



CREATE TABLE keycloak.resource_policy
(
    resource_id character varying(36) NOT NULL,
    policy_id   character varying(36) NOT NULL
);



CREATE TABLE keycloak.resource_scope
(
    resource_id character varying(36) NOT NULL,
    scope_id    character varying(36) NOT NULL
);



CREATE TABLE keycloak.resource_server
(
    id                   character varying(36)  NOT NULL,
    allow_rs_remote_mgmt boolean  DEFAULT false NOT NULL,
    policy_enforce_mode  smallint               NOT NULL,
    decision_strategy    smallint DEFAULT 1     NOT NULL
);



CREATE TABLE keycloak.resource_server_perm_ticket
(
    id                 character varying(36)  NOT NULL,
    owner              character varying(255) NOT NULL,
    requester          character varying(255) NOT NULL,
    created_timestamp  bigint                 NOT NULL,
    granted_timestamp  bigint,
    resource_id        character varying(36)  NOT NULL,
    scope_id           character varying(36),
    resource_server_id character varying(36)  NOT NULL,
    policy_id          character varying(36)
);



CREATE TABLE keycloak.resource_server_policy
(
    id                 character varying(36)  NOT NULL,
    name               character varying(255) NOT NULL,
    description        character varying(255),
    type               character varying(255) NOT NULL,
    decision_strategy  smallint,
    logic              smallint,
    resource_server_id character varying(36)  NOT NULL,
    owner              character varying(255)
);



CREATE TABLE keycloak.resource_server_resource
(
    id                   character varying(36)  NOT NULL,
    name                 character varying(255) NOT NULL,
    type                 character varying(255),
    icon_uri             character varying(255),
    owner                character varying(255) NOT NULL,
    resource_server_id   character varying(36)  NOT NULL,
    owner_managed_access boolean DEFAULT false  NOT NULL,
    display_name         character varying(255)
);



CREATE TABLE keycloak.resource_server_scope
(
    id                 character varying(36)  NOT NULL,
    name               character varying(255) NOT NULL,
    icon_uri           character varying(255),
    resource_server_id character varying(36)  NOT NULL,
    display_name       character varying(255)
);



CREATE TABLE keycloak.resource_uris
(
    resource_id character varying(36)  NOT NULL,
    value       character varying(255) NOT NULL
);



CREATE TABLE keycloak.revoked_token
(
    id     character varying(255) NOT NULL,
    expire bigint                 NOT NULL
);



CREATE TABLE keycloak.role_attribute
(
    id      character varying(36)  NOT NULL,
    role_id character varying(36)  NOT NULL,
    name    character varying(255) NOT NULL,
    value   character varying(255)
);



CREATE TABLE keycloak.scope_mapping
(
    client_id character varying(36) NOT NULL,
    role_id   character varying(36) NOT NULL
);



CREATE TABLE keycloak.scope_policy
(
    scope_id  character varying(36) NOT NULL,
    policy_id character varying(36) NOT NULL
);



CREATE TABLE keycloak.server_config
(
    server_config_key character varying(255) NOT NULL,
    value             text                   NOT NULL,
    version           integer DEFAULT 0
);



CREATE TABLE keycloak.user_attribute
(
    name                       character varying(255)                                                         NOT NULL,
    value                      character varying(255),
    user_id                    character varying(36)                                                          NOT NULL,
    id                         character varying(36) DEFAULT 'sybase-needs-something-here'::character varying NOT NULL,
    long_value_hash            bytea,
    long_value_hash_lower_case bytea,
    long_value                 text
);



CREATE TABLE keycloak.user_consent
(
    id                      character varying(36) NOT NULL,
    client_id               character varying(255),
    user_id                 character varying(36) NOT NULL,
    created_date            bigint,
    last_updated_date       bigint,
    client_storage_provider character varying(36),
    external_client_id      character varying(255)
);



CREATE TABLE keycloak.user_consent_client_scope
(
    user_consent_id character varying(36) NOT NULL,
    scope_id        character varying(36) NOT NULL
);



CREATE TABLE keycloak.user_entity
(
    id                          character varying(36) NOT NULL,
    email                       character varying(255),
    email_constraint            character varying(255),
    email_verified              boolean DEFAULT false NOT NULL,
    enabled                     boolean DEFAULT false NOT NULL,
    federation_link             character varying(255),
    first_name                  character varying(255),
    last_name                   character varying(255),
    realm_id                    character varying(255),
    username                    character varying(255),
    created_timestamp           bigint,
    service_account_client_link character varying(255),
    not_before                  integer DEFAULT 0     NOT NULL
);



CREATE TABLE keycloak.user_federation_config
(
    user_federation_provider_id character varying(36)  NOT NULL,
    value                       character varying(255),
    name                        character varying(255) NOT NULL
);



CREATE TABLE keycloak.user_federation_mapper
(
    id                     character varying(36)  NOT NULL,
    name                   character varying(255) NOT NULL,
    federation_provider_id character varying(36)  NOT NULL,
    federation_mapper_type character varying(255) NOT NULL,
    realm_id               character varying(36)  NOT NULL
);



CREATE TABLE keycloak.user_federation_mapper_config
(
    user_federation_mapper_id character varying(36)  NOT NULL,
    value                     character varying(255),
    name                      character varying(255) NOT NULL
);



CREATE TABLE keycloak.user_federation_provider
(
    id                  character varying(36) NOT NULL,
    changed_sync_period integer,
    display_name        character varying(255),
    full_sync_period    integer,
    last_sync           integer,
    priority            integer,
    provider_name       character varying(255),
    realm_id            character varying(36)
);



CREATE TABLE keycloak.user_group_membership
(
    group_id        character varying(36)  NOT NULL,
    user_id         character varying(36)  NOT NULL,
    membership_type character varying(255) NOT NULL
);



CREATE TABLE keycloak.user_required_action
(
    user_id         character varying(36)                                 NOT NULL,
    required_action character varying(255) DEFAULT ' '::character varying NOT NULL
);



CREATE TABLE keycloak.user_role_mapping
(
    role_id character varying(255) NOT NULL,
    user_id character varying(36)  NOT NULL
);



CREATE TABLE keycloak.web_origins
(
    client_id character varying(36)  NOT NULL,
    value     character varying(255) NOT NULL
);



COPY keycloak.admin_event_entity (id, admin_event_time, realm_id, operation_type, auth_realm_id, auth_client_id,
                                  auth_user_id, ip_address, resource_path, representation, error, resource_type,
                                  details_json) FROM stdin;
\.


COPY keycloak.associated_policy (policy_id, associated_policy_id) FROM stdin;
\.


COPY keycloak.authentication_execution (id, alias, authenticator, realm_id, flow_id, requirement, priority,
                                        authenticator_flow, auth_flow_id, auth_config) FROM stdin;
8901d75b-a442-4141-8af2-784abc3318ba	\N	auth-cookie	fc4359ab-c586-4d33-a1e0-6382887b081c	ec8c7366-98cc-4c0a-88cf-c366091f9814	2	10	f	\N	\N
5df840b0-5724-49bf-873a-777ccc1dd0a8	\N	auth-spnego	fc4359ab-c586-4d33-a1e0-6382887b081c	ec8c7366-98cc-4c0a-88cf-c366091f9814	3	20	f	\N	\N
6b803ca3-7784-411d-9919-6d5fb089d917	\N	identity-provider-redirector	fc4359ab-c586-4d33-a1e0-6382887b081c	ec8c7366-98cc-4c0a-88cf-c366091f9814	2	25	f	\N	\N
6160e5c3-ca38-46d3-88a3-ff8f4cf21e50	\N	\N	fc4359ab-c586-4d33-a1e0-6382887b081c	ec8c7366-98cc-4c0a-88cf-c366091f9814	2	30	t	24a2ddab-5c4d-412e-a75c-6cad741e5090	\N
8b33a82a-6af4-4be6-ac3a-5b2db80764a1	\N	auth-username-password-form	fc4359ab-c586-4d33-a1e0-6382887b081c	24a2ddab-5c4d-412e-a75c-6cad741e5090	0	10	f	\N	\N
bb83bca4-79cf-4166-b0ae-de1e4073071f	\N	\N	fc4359ab-c586-4d33-a1e0-6382887b081c	24a2ddab-5c4d-412e-a75c-6cad741e5090	1	20	t	4467f962-a57d-48d3-bcfe-700926bf3e2c	\N
9d730a92-e97c-432d-8e6d-0b02364134b5	\N	conditional-user-configured	fc4359ab-c586-4d33-a1e0-6382887b081c	4467f962-a57d-48d3-bcfe-700926bf3e2c	0	10	f	\N	\N
1ae6dc72-34de-4377-9bab-69cf5b70dfbf	\N	auth-otp-form	fc4359ab-c586-4d33-a1e0-6382887b081c	4467f962-a57d-48d3-bcfe-700926bf3e2c	0	20	f	\N	\N
cf60d228-f0a5-42fb-a10f-0a0a6d8c2360	\N	direct-grant-validate-username	fc4359ab-c586-4d33-a1e0-6382887b081c	c76c5562-b5db-4f4b-9af0-62a9dee98b0a	0	10	f	\N	\N
5d5cffc1-46d2-4b7a-88c2-239e9021e376	\N	direct-grant-validate-password	fc4359ab-c586-4d33-a1e0-6382887b081c	c76c5562-b5db-4f4b-9af0-62a9dee98b0a	0	20	f	\N	\N
5b59dfd3-e8f4-4c3d-bcfd-7f7f9aca2ed5	\N	\N	fc4359ab-c586-4d33-a1e0-6382887b081c	c76c5562-b5db-4f4b-9af0-62a9dee98b0a	1	30	t	03058fb1-5f29-4cc3-a57b-5b0ce0b0b897	\N
fbcd7e66-e047-4601-954e-a9f43b3c29c2	\N	conditional-user-configured	fc4359ab-c586-4d33-a1e0-6382887b081c	03058fb1-5f29-4cc3-a57b-5b0ce0b0b897	0	10	f	\N	\N
f0332a8d-a110-49b5-ad50-ac110eb12609	\N	direct-grant-validate-otp	fc4359ab-c586-4d33-a1e0-6382887b081c	03058fb1-5f29-4cc3-a57b-5b0ce0b0b897	0	20	f	\N	\N
3896e598-03da-4c57-a28a-3d8eccf3afa4	\N	registration-page-form	fc4359ab-c586-4d33-a1e0-6382887b081c	60be8aa9-c1a4-4260-b9e2-b8f3e664626a	0	10	t	0bc65b2b-1430-4f16-ad0e-3768bb503b9d	\N
fa40fbcc-6acd-4f16-b83f-e126817badff	\N	registration-user-creation	fc4359ab-c586-4d33-a1e0-6382887b081c	0bc65b2b-1430-4f16-ad0e-3768bb503b9d	0	20	f	\N	\N
1a5ce090-4a39-41b7-803a-66c94db804f8	\N	registration-password-action	fc4359ab-c586-4d33-a1e0-6382887b081c	0bc65b2b-1430-4f16-ad0e-3768bb503b9d	0	50	f	\N	\N
9f8ba1d7-0b9e-48f0-9fdc-881d2fbee052	\N	registration-recaptcha-action	fc4359ab-c586-4d33-a1e0-6382887b081c	0bc65b2b-1430-4f16-ad0e-3768bb503b9d	3	60	f	\N	\N
da151d61-7d33-4ab0-8fc4-12f9634da3e5	\N	registration-terms-and-conditions	fc4359ab-c586-4d33-a1e0-6382887b081c	0bc65b2b-1430-4f16-ad0e-3768bb503b9d	3	70	f	\N	\N
6099027d-6dd7-45fb-9bc1-52d951f0178c	\N	reset-credentials-choose-user	fc4359ab-c586-4d33-a1e0-6382887b081c	77ad6b43-3234-4d5f-b305-5851d4a75e11	0	10	f	\N	\N
91f45e50-7678-4da7-8388-ef78f4fc7766	\N	reset-credential-email	fc4359ab-c586-4d33-a1e0-6382887b081c	77ad6b43-3234-4d5f-b305-5851d4a75e11	0	20	f	\N	\N
f3d6621e-96b6-488a-8e19-731d50862014	\N	reset-password	fc4359ab-c586-4d33-a1e0-6382887b081c	77ad6b43-3234-4d5f-b305-5851d4a75e11	0	30	f	\N	\N
b92f9038-564e-44ae-8b10-b5f960b6b0ae	\N	\N	fc4359ab-c586-4d33-a1e0-6382887b081c	77ad6b43-3234-4d5f-b305-5851d4a75e11	1	40	t	9505508b-d884-4b57-9989-5c3edd75342d	\N
1f7450af-feed-4ff8-8f53-36a2bf6b337f	\N	conditional-user-configured	fc4359ab-c586-4d33-a1e0-6382887b081c	9505508b-d884-4b57-9989-5c3edd75342d	0	10	f	\N	\N
6460c667-34ce-4a72-a34d-1d99ba1fbb9c	\N	reset-otp	fc4359ab-c586-4d33-a1e0-6382887b081c	9505508b-d884-4b57-9989-5c3edd75342d	0	20	f	\N	\N
2236d194-0e18-4dbd-82d4-ab68475c1742	\N	client-secret	fc4359ab-c586-4d33-a1e0-6382887b081c	d69a6bf8-de1a-4a74-b68d-d0b5f9439dd7	2	10	f	\N	\N
b45c2efc-07bc-43a2-b9b4-9afe81fcd0b1	\N	client-jwt	fc4359ab-c586-4d33-a1e0-6382887b081c	d69a6bf8-de1a-4a74-b68d-d0b5f9439dd7	2	20	f	\N	\N
21eaf63c-a866-47b4-bcb9-5960db20d5a7	\N	client-secret-jwt	fc4359ab-c586-4d33-a1e0-6382887b081c	d69a6bf8-de1a-4a74-b68d-d0b5f9439dd7	2	30	f	\N	\N
1e7f827b-51e0-4a87-801e-e26f0e48aad3	\N	client-x509	fc4359ab-c586-4d33-a1e0-6382887b081c	d69a6bf8-de1a-4a74-b68d-d0b5f9439dd7	2	40	f	\N	\N
6ec8ec2d-4fb9-4c01-841b-19eba8032048	\N	idp-review-profile	fc4359ab-c586-4d33-a1e0-6382887b081c	d7dff088-db5c-427a-bac1-8472d75ef7e6	0	10	f	\N	9625d798-2d6f-4e11-9205-1b472ca56b1e
53ad02d3-205c-44a9-8e5a-970c2127e41f	\N	\N	fc4359ab-c586-4d33-a1e0-6382887b081c	d7dff088-db5c-427a-bac1-8472d75ef7e6	0	20	t	f08a4ea3-150d-4979-95a0-29dc142c4e45	\N
c4b8d753-0831-44c6-ab32-69d3e302d3fa	\N	idp-create-user-if-unique	fc4359ab-c586-4d33-a1e0-6382887b081c	f08a4ea3-150d-4979-95a0-29dc142c4e45	2	10	f	\N	c35a5955-1841-45a1-a5f2-0e83ca8f6ba5
0f2ed1d1-7f2e-4f5a-867f-c3a0fa2fc1f2	\N	\N	fc4359ab-c586-4d33-a1e0-6382887b081c	f08a4ea3-150d-4979-95a0-29dc142c4e45	2	20	t	2159c908-2052-4133-a9e7-9cf90ee77266	\N
d7816bfe-c331-4605-87b7-891aacc5ce03	\N	idp-confirm-link	fc4359ab-c586-4d33-a1e0-6382887b081c	2159c908-2052-4133-a9e7-9cf90ee77266	0	10	f	\N	\N
526bbcf9-5a47-4390-9e23-6c04ad019bb5	\N	\N	fc4359ab-c586-4d33-a1e0-6382887b081c	2159c908-2052-4133-a9e7-9cf90ee77266	0	20	t	4848b97e-aa7b-4233-8c12-d1161d50d891	\N
3601efc5-30e4-4b02-bc63-2afeb0d0dabf	\N	idp-email-verification	fc4359ab-c586-4d33-a1e0-6382887b081c	4848b97e-aa7b-4233-8c12-d1161d50d891	2	10	f	\N	\N
f1f26123-a055-405b-ab40-3ab248910a14	\N	\N	fc4359ab-c586-4d33-a1e0-6382887b081c	4848b97e-aa7b-4233-8c12-d1161d50d891	2	20	t	1eae5226-c91b-4e85-87fe-92ae28b4ecd3	\N
bb8eb195-b714-4e11-9b85-5dde98501863	\N	idp-username-password-form	fc4359ab-c586-4d33-a1e0-6382887b081c	1eae5226-c91b-4e85-87fe-92ae28b4ecd3	0	10	f	\N	\N
86595d6d-a8f2-45ba-b6a5-f81337174cae	\N	\N	fc4359ab-c586-4d33-a1e0-6382887b081c	1eae5226-c91b-4e85-87fe-92ae28b4ecd3	1	20	t	22c095d0-851c-413c-9b10-52d64acd7e6d	\N
3ea81843-c13b-4fbc-88d8-dafda788aec3	\N	conditional-user-configured	fc4359ab-c586-4d33-a1e0-6382887b081c	22c095d0-851c-413c-9b10-52d64acd7e6d	0	10	f	\N	\N
661b42ea-5e7b-4517-a3ef-3ee345bd6ff4	\N	auth-otp-form	fc4359ab-c586-4d33-a1e0-6382887b081c	22c095d0-851c-413c-9b10-52d64acd7e6d	0	20	f	\N	\N
3e1b2e79-4ef6-4c5c-b2df-8723d2085391	\N	http-basic-authenticator	fc4359ab-c586-4d33-a1e0-6382887b081c	7d0068a6-69f5-4df2-8f30-d647efcac747	0	10	f	\N	\N
84fc8d92-ac98-4667-a81e-498fc2715ce4	\N	docker-http-basic-authenticator	fc4359ab-c586-4d33-a1e0-6382887b081c	63f29d08-cb16-43cf-bb50-a7e73f1227a8	0	10	f	\N	\N
55165a11-84dd-4b45-a3c2-bd9df6f97ec0	\N	auth-cookie	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	5eaaa984-74ce-4b09-a9e0-d827adbeb2ff	2	10	f	\N	\N
4ca3b14d-a96a-499c-bbdd-6062ae8c6c57	\N	auth-spnego	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	5eaaa984-74ce-4b09-a9e0-d827adbeb2ff	3	20	f	\N	\N
6a0dd3d9-ced3-4134-aa79-478a7fbbf26e	\N	identity-provider-redirector	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	5eaaa984-74ce-4b09-a9e0-d827adbeb2ff	2	25	f	\N	\N
be6ccb0f-42e4-4734-aa3a-03c85f5e0746	\N	\N	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	5eaaa984-74ce-4b09-a9e0-d827adbeb2ff	2	30	t	b8d39ffd-b9ea-43a4-a4e8-9bcb31d51c8f	\N
ab5b6c3a-2c92-4286-8650-553c9086d868	\N	auth-username-password-form	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	b8d39ffd-b9ea-43a4-a4e8-9bcb31d51c8f	0	10	f	\N	\N
a02d9c3a-c656-43ff-bdb7-e6350115f630	\N	\N	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	b8d39ffd-b9ea-43a4-a4e8-9bcb31d51c8f	1	20	t	1980edc7-611a-4324-b9d1-1ff6348af9c3	\N
096f928d-2907-4fe7-ad6d-92bd3f450c77	\N	conditional-user-configured	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	1980edc7-611a-4324-b9d1-1ff6348af9c3	0	10	f	\N	\N
23b7ecee-74f9-4e2d-a78c-768cfa4fb1d1	\N	auth-otp-form	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	1980edc7-611a-4324-b9d1-1ff6348af9c3	0	20	f	\N	\N
cf36b3dd-361f-4e8e-8c4b-ea841906a14e	\N	\N	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	5eaaa984-74ce-4b09-a9e0-d827adbeb2ff	2	26	t	8a09364d-e0a3-48fd-91b8-67cbc974892b	\N
41ce508d-7d0b-4b23-90b5-29784cc623af	\N	\N	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	8a09364d-e0a3-48fd-91b8-67cbc974892b	1	10	t	847fb9e5-8ded-4a16-9024-502bc99a6737	\N
8559e1e0-9981-4e3f-9aea-fdc3a091b50c	\N	conditional-user-configured	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	847fb9e5-8ded-4a16-9024-502bc99a6737	0	10	f	\N	\N
0acf00a8-9edb-4665-bf39-e01258212904	\N	organization	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	847fb9e5-8ded-4a16-9024-502bc99a6737	2	20	f	\N	\N
eb28126e-0f08-43ac-a7e6-71d72e4ad30c	\N	direct-grant-validate-username	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	28eb0bb5-8a43-493d-8fd2-2daf1e15d6ac	0	10	f	\N	\N
a7794968-6a3e-48e3-906e-4b75f577b702	\N	direct-grant-validate-password	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	28eb0bb5-8a43-493d-8fd2-2daf1e15d6ac	0	20	f	\N	\N
9a8c475c-80d0-4dee-be55-d23d2a592641	\N	\N	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	28eb0bb5-8a43-493d-8fd2-2daf1e15d6ac	1	30	t	672c2eea-11b7-45a1-b7a5-5e60c7772226	\N
6a27a6fc-3ce1-4d76-82f4-19435d915a6c	\N	conditional-user-configured	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	672c2eea-11b7-45a1-b7a5-5e60c7772226	0	10	f	\N	\N
7951926e-330f-4074-ad25-af161013995a	\N	direct-grant-validate-otp	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	672c2eea-11b7-45a1-b7a5-5e60c7772226	0	20	f	\N	\N
94dab4a7-1dcf-4749-a019-203e7d04a8db	\N	registration-page-form	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	534bfb6a-494c-43c6-a685-dc96179d6ad0	0	10	t	88ec0c70-edda-4450-8b4e-10bb06a0e687	\N
45633eec-90be-4457-bde6-016d522883ea	\N	registration-user-creation	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	88ec0c70-edda-4450-8b4e-10bb06a0e687	0	20	f	\N	\N
75d5bf9c-8103-4e2c-93d1-6532749f7ee1	\N	registration-password-action	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	88ec0c70-edda-4450-8b4e-10bb06a0e687	0	50	f	\N	\N
0516c692-a091-4c30-846a-f4e7e0dad6d7	\N	registration-recaptcha-action	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	88ec0c70-edda-4450-8b4e-10bb06a0e687	3	60	f	\N	\N
c11dc9d6-9664-44a9-9eb9-6dd470f37cc2	\N	registration-terms-and-conditions	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	88ec0c70-edda-4450-8b4e-10bb06a0e687	3	70	f	\N	\N
62c33101-01fa-46dd-af8f-18a7e48cc8a1	\N	reset-credentials-choose-user	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	773c9c90-190a-4366-b83e-3abec43ccb4b	0	10	f	\N	\N
d56b008c-2cbc-41ef-8298-973c7f522212	\N	reset-credential-email	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	773c9c90-190a-4366-b83e-3abec43ccb4b	0	20	f	\N	\N
3fc032ee-321d-42d9-8f6e-2b5da0dc584d	\N	reset-password	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	773c9c90-190a-4366-b83e-3abec43ccb4b	0	30	f	\N	\N
1f5ea373-3421-4f9b-bdc5-97670da6beea	\N	\N	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	773c9c90-190a-4366-b83e-3abec43ccb4b	1	40	t	0d87270a-5255-4f51-8b93-d1df446d6c67	\N
f0071ffd-46f2-4817-8df7-016a5b56f9eb	\N	conditional-user-configured	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	0d87270a-5255-4f51-8b93-d1df446d6c67	0	10	f	\N	\N
22cd2c3d-3688-4e5e-b37a-8a72a5ebfc62	\N	reset-otp	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	0d87270a-5255-4f51-8b93-d1df446d6c67	0	20	f	\N	\N
a17eb2b5-7c52-4a3a-b979-d607de92b35d	\N	client-secret	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	55c325b4-b949-4d1d-aa8e-3d89eae34f1d	2	10	f	\N	\N
4bf7652b-f570-4374-8713-4637989a5058	\N	client-jwt	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	55c325b4-b949-4d1d-aa8e-3d89eae34f1d	2	20	f	\N	\N
3ed954b0-4ce7-42e4-a94d-e6a521328d5b	\N	client-secret-jwt	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	55c325b4-b949-4d1d-aa8e-3d89eae34f1d	2	30	f	\N	\N
c4056508-eb23-46fa-b745-15529ab6e6d0	\N	client-x509	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	55c325b4-b949-4d1d-aa8e-3d89eae34f1d	2	40	f	\N	\N
a8051fac-7713-4bd7-9483-79c8f51a1c2c	\N	idp-review-profile	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	3b756761-9f06-41a6-bfea-c9f5c2eb8f84	0	10	f	\N	0cff28a3-952d-4e6b-a09b-70b4d9d3b99d
763ead8a-90e2-4656-b0f6-318161d040a4	\N	\N	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	3b756761-9f06-41a6-bfea-c9f5c2eb8f84	0	20	t	f96c8a0f-645f-4c99-b7d1-4e05a8f0a304	\N
534e8e33-61ae-4820-b027-49716e436b73	\N	idp-create-user-if-unique	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	f96c8a0f-645f-4c99-b7d1-4e05a8f0a304	2	10	f	\N	8e649d51-8f96-4d85-9df3-01e03d181dbe
e1d8dc01-0017-4859-9859-fb0c4b72de75	\N	\N	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	f96c8a0f-645f-4c99-b7d1-4e05a8f0a304	2	20	t	f579787b-8af4-4b8d-b055-3cd0636e6a63	\N
7bbf5ebc-747a-44ef-a091-919b270bc65f	\N	idp-confirm-link	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	f579787b-8af4-4b8d-b055-3cd0636e6a63	0	10	f	\N	\N
885f154c-2345-459a-9498-64614f3bdd63	\N	\N	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	f579787b-8af4-4b8d-b055-3cd0636e6a63	0	20	t	08eb4d24-da0b-4692-96d4-cbd56b98aa83	\N
e9b84438-6e41-4505-91f4-770fcdb7c7b3	\N	idp-email-verification	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	08eb4d24-da0b-4692-96d4-cbd56b98aa83	2	10	f	\N	\N
64084dcd-7241-4526-ba76-1b08d73e164c	\N	\N	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	08eb4d24-da0b-4692-96d4-cbd56b98aa83	2	20	t	4ecf3a61-53cb-4256-b3c3-041670af5405	\N
b98a4993-c233-48c6-b86a-f4f30731878d	\N	idp-username-password-form	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	4ecf3a61-53cb-4256-b3c3-041670af5405	0	10	f	\N	\N
4485c21a-ddd2-4507-bd31-f4321ba4984c	\N	\N	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	4ecf3a61-53cb-4256-b3c3-041670af5405	1	20	t	1d29132b-e1d3-46f0-b5de-9ae9ee31d584	\N
9cae2634-bf66-401a-88a2-4b5ea3e682eb	\N	conditional-user-configured	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	1d29132b-e1d3-46f0-b5de-9ae9ee31d584	0	10	f	\N	\N
a15277db-1e0c-42bf-a8cd-893566fbfa3d	\N	auth-otp-form	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	1d29132b-e1d3-46f0-b5de-9ae9ee31d584	0	20	f	\N	\N
b1780ed3-5580-43ea-937c-3ec10de5d641	\N	\N	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	3b756761-9f06-41a6-bfea-c9f5c2eb8f84	1	50	t	fd2ef5fe-12ca-43e4-8c45-a5e31072df17	\N
29131887-8efd-4a3d-82ed-abaa7b8d3e6c	\N	conditional-user-configured	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	fd2ef5fe-12ca-43e4-8c45-a5e31072df17	0	10	f	\N	\N
81019fa9-de44-49c4-8359-c054d5178e21	\N	idp-add-organization-member	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	fd2ef5fe-12ca-43e4-8c45-a5e31072df17	0	20	f	\N	\N
902b8600-7932-4b42-8992-0f58872ec70b	\N	http-basic-authenticator	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	abeb7c29-a68b-485d-97cf-25b8bf34cbc4	0	10	f	\N	\N
5c11b76f-9be8-4dde-b0e3-adf91811471a	\N	docker-http-basic-authenticator	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	3bfcbfd0-dbd8-40b3-8d07-f440ec385ea4	0	10	f	\N	\N
\.


COPY keycloak.authentication_flow (id, alias, description, realm_id, provider_id, top_level, built_in) FROM stdin;
ec8c7366-98cc-4c0a-88cf-c366091f9814	browser	Browser based authentication	fc4359ab-c586-4d33-a1e0-6382887b081c	basic-flow	t	t
24a2ddab-5c4d-412e-a75c-6cad741e5090	forms	Username, password, otp and other auth forms.	fc4359ab-c586-4d33-a1e0-6382887b081c	basic-flow	f	t
4467f962-a57d-48d3-bcfe-700926bf3e2c	Browser - Conditional OTP	Flow to determine if the OTP is required for the authentication	fc4359ab-c586-4d33-a1e0-6382887b081c	basic-flow	f	t
c76c5562-b5db-4f4b-9af0-62a9dee98b0a	direct grant	OpenID Connect Resource Owner Grant	fc4359ab-c586-4d33-a1e0-6382887b081c	basic-flow	t	t
03058fb1-5f29-4cc3-a57b-5b0ce0b0b897	Direct Grant - Conditional OTP	Flow to determine if the OTP is required for the authentication	fc4359ab-c586-4d33-a1e0-6382887b081c	basic-flow	f	t
60be8aa9-c1a4-4260-b9e2-b8f3e664626a	registration	Registration flow	fc4359ab-c586-4d33-a1e0-6382887b081c	basic-flow	t	t
0bc65b2b-1430-4f16-ad0e-3768bb503b9d	registration form	Registration form	fc4359ab-c586-4d33-a1e0-6382887b081c	form-flow	f	t
77ad6b43-3234-4d5f-b305-5851d4a75e11	reset credentials	Reset credentials for a user if they forgot their password or something	fc4359ab-c586-4d33-a1e0-6382887b081c	basic-flow	t	t
9505508b-d884-4b57-9989-5c3edd75342d	Reset - Conditional OTP	Flow to determine if the OTP should be reset or not. Set to REQUIRED to force.	fc4359ab-c586-4d33-a1e0-6382887b081c	basic-flow	f	t
d69a6bf8-de1a-4a74-b68d-d0b5f9439dd7	clients	Base authentication for clients	fc4359ab-c586-4d33-a1e0-6382887b081c	client-flow	t	t
d7dff088-db5c-427a-bac1-8472d75ef7e6	first broker login	Actions taken after first broker login with identity provider account, which is not yet linked to any Keycloak account	fc4359ab-c586-4d33-a1e0-6382887b081c	basic-flow	t	t
f08a4ea3-150d-4979-95a0-29dc142c4e45	User creation or linking	Flow for the existing/non-existing user alternatives	fc4359ab-c586-4d33-a1e0-6382887b081c	basic-flow	f	t
2159c908-2052-4133-a9e7-9cf90ee77266	Handle Existing Account	Handle what to do if there is existing account with same email/username like authenticated identity provider	fc4359ab-c586-4d33-a1e0-6382887b081c	basic-flow	f	t
4848b97e-aa7b-4233-8c12-d1161d50d891	Account verification options	Method with which to verity the existing account	fc4359ab-c586-4d33-a1e0-6382887b081c	basic-flow	f	t
1eae5226-c91b-4e85-87fe-92ae28b4ecd3	Verify Existing Account by Re-authentication	Reauthentication of existing account	fc4359ab-c586-4d33-a1e0-6382887b081c	basic-flow	f	t
22c095d0-851c-413c-9b10-52d64acd7e6d	First broker login - Conditional OTP	Flow to determine if the OTP is required for the authentication	fc4359ab-c586-4d33-a1e0-6382887b081c	basic-flow	f	t
7d0068a6-69f5-4df2-8f30-d647efcac747	saml ecp	SAML ECP Profile Authentication Flow	fc4359ab-c586-4d33-a1e0-6382887b081c	basic-flow	t	t
63f29d08-cb16-43cf-bb50-a7e73f1227a8	docker auth	Used by Docker clients to authenticate against the IDP	fc4359ab-c586-4d33-a1e0-6382887b081c	basic-flow	t	t
5eaaa984-74ce-4b09-a9e0-d827adbeb2ff	browser	Browser based authentication	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	basic-flow	t	t
b8d39ffd-b9ea-43a4-a4e8-9bcb31d51c8f	forms	Username, password, otp and other auth forms.	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	basic-flow	f	t
1980edc7-611a-4324-b9d1-1ff6348af9c3	Browser - Conditional OTP	Flow to determine if the OTP is required for the authentication	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	basic-flow	f	t
8a09364d-e0a3-48fd-91b8-67cbc974892b	Organization	\N	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	basic-flow	f	t
847fb9e5-8ded-4a16-9024-502bc99a6737	Browser - Conditional Organization	Flow to determine if the organization identity-first login is to be used	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	basic-flow	f	t
28eb0bb5-8a43-493d-8fd2-2daf1e15d6ac	direct grant	OpenID Connect Resource Owner Grant	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	basic-flow	t	t
672c2eea-11b7-45a1-b7a5-5e60c7772226	Direct Grant - Conditional OTP	Flow to determine if the OTP is required for the authentication	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	basic-flow	f	t
534bfb6a-494c-43c6-a685-dc96179d6ad0	registration	Registration flow	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	basic-flow	t	t
88ec0c70-edda-4450-8b4e-10bb06a0e687	registration form	Registration form	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	form-flow	f	t
773c9c90-190a-4366-b83e-3abec43ccb4b	reset credentials	Reset credentials for a user if they forgot their password or something	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	basic-flow	t	t
0d87270a-5255-4f51-8b93-d1df446d6c67	Reset - Conditional OTP	Flow to determine if the OTP should be reset or not. Set to REQUIRED to force.	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	basic-flow	f	t
55c325b4-b949-4d1d-aa8e-3d89eae34f1d	clients	Base authentication for clients	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	client-flow	t	t
3b756761-9f06-41a6-bfea-c9f5c2eb8f84	first broker login	Actions taken after first broker login with identity provider account, which is not yet linked to any Keycloak account	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	basic-flow	t	t
f96c8a0f-645f-4c99-b7d1-4e05a8f0a304	User creation or linking	Flow for the existing/non-existing user alternatives	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	basic-flow	f	t
f579787b-8af4-4b8d-b055-3cd0636e6a63	Handle Existing Account	Handle what to do if there is existing account with same email/username like authenticated identity provider	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	basic-flow	f	t
08eb4d24-da0b-4692-96d4-cbd56b98aa83	Account verification options	Method with which to verity the existing account	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	basic-flow	f	t
4ecf3a61-53cb-4256-b3c3-041670af5405	Verify Existing Account by Re-authentication	Reauthentication of existing account	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	basic-flow	f	t
1d29132b-e1d3-46f0-b5de-9ae9ee31d584	First broker login - Conditional OTP	Flow to determine if the OTP is required for the authentication	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	basic-flow	f	t
fd2ef5fe-12ca-43e4-8c45-a5e31072df17	First Broker Login - Conditional Organization	Flow to determine if the authenticator that adds organization members is to be used	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	basic-flow	f	t
abeb7c29-a68b-485d-97cf-25b8bf34cbc4	saml ecp	SAML ECP Profile Authentication Flow	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	basic-flow	t	t
3bfcbfd0-dbd8-40b3-8d07-f440ec385ea4	docker auth	Used by Docker clients to authenticate against the IDP	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	basic-flow	t	t
\.


COPY keycloak.authenticator_config (id, alias, realm_id) FROM stdin;
9625d798-2d6f-4e11-9205-1b472ca56b1e	review profile config	fc4359ab-c586-4d33-a1e0-6382887b081c
c35a5955-1841-45a1-a5f2-0e83ca8f6ba5	create unique user config	fc4359ab-c586-4d33-a1e0-6382887b081c
0cff28a3-952d-4e6b-a09b-70b4d9d3b99d	review profile config	bde3ba76-fd10-4c67-8d50-9435f4f48ccc
8e649d51-8f96-4d85-9df3-01e03d181dbe	create unique user config	bde3ba76-fd10-4c67-8d50-9435f4f48ccc
\.


COPY keycloak.authenticator_config_entry (authenticator_id, value, name) FROM stdin;
9625d798-2d6f-4e11-9205-1b472ca56b1e	missing	update.profile.on.first.login
c35a5955-1841-45a1-a5f2-0e83ca8f6ba5	false	require.password.update.after.registration
0cff28a3-952d-4e6b-a09b-70b4d9d3b99d	missing	update.profile.on.first.login
8e649d51-8f96-4d85-9df3-01e03d181dbe	false	require.password.update.after.registration
\.


COPY keycloak.broker_link (identity_provider, storage_provider_id, realm_id, broker_user_id, broker_username, token,
                           user_id) FROM stdin;
\.


COPY keycloak.client (id, enabled, full_scope_allowed, client_id, not_before, public_client, secret, base_url,
                      bearer_only, management_url, surrogate_auth_required, realm_id, protocol, node_rereg_timeout,
                      frontchannel_logout, consent_required, name, service_accounts_enabled, client_authenticator_type,
                      root_url, description, registration_token, standard_flow_enabled, implicit_flow_enabled,
                      direct_access_grants_enabled, always_display_in_console) FROM stdin;
ae3ea88f-aecf-4fec-b290-bb7594719e44	t	f	master-realm	0	f	\N	\N	t	\N	f	fc4359ab-c586-4d33-a1e0-6382887b081c	\N	0	f	f	master Realm	f	client-secret	\N	\N	\N	t	f	f	f
b0de488c-4330-4955-91e2-f8dfb13e054b	t	f	account	0	t	\N	/realms/master/account/	f	\N	f	fc4359ab-c586-4d33-a1e0-6382887b081c	openid-connect	0	f	f	${client_account}	f	client-secret	${authBaseUrl}	\N	\N	t	f	f	f
ee76a050-87a8-4470-82e6-1a09a0e47223	t	f	account-console	0	t	\N	/realms/master/account/	f	\N	f	fc4359ab-c586-4d33-a1e0-6382887b081c	openid-connect	0	f	f	${client_account-console}	f	client-secret	${authBaseUrl}	\N	\N	t	f	f	f
ebe6655e-eccd-4d44-a08c-d4b523c6ce96	t	f	broker	0	f	\N	\N	t	\N	f	fc4359ab-c586-4d33-a1e0-6382887b081c	openid-connect	0	f	f	${client_broker}	f	client-secret	\N	\N	\N	t	f	f	f
108d6f49-22ba-43f2-84e8-105fbc9e9cdd	t	t	security-admin-console	0	t	\N	/admin/master/console/	f	\N	f	fc4359ab-c586-4d33-a1e0-6382887b081c	openid-connect	0	f	f	${client_security-admin-console}	f	client-secret	${authAdminUrl}	\N	\N	t	f	f	f
41cd2bbf-9898-4522-b144-ec933da2a045	t	t	admin-cli	0	t	\N	\N	f	\N	f	fc4359ab-c586-4d33-a1e0-6382887b081c	openid-connect	0	f	f	${client_admin-cli}	f	client-secret	\N	\N	\N	f	f	t	f
0936c0bc-f90c-4bd0-8208-1d3a997b8eca	t	f	jodrive-realm-realm	0	f	\N	\N	t	\N	f	fc4359ab-c586-4d33-a1e0-6382887b081c	\N	0	f	f	jodrive-realm Realm	f	client-secret	\N	\N	\N	t	f	f	f
f8e3dca3-1b19-48ea-a95d-b7e8cfa335c3	t	f	realm-management	0	f	\N	\N	t	\N	f	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	openid-connect	0	f	f	${client_realm-management}	f	client-secret	\N	\N	\N	t	f	f	f
da0a22ac-4993-40b2-a719-2a2f01790525	t	f	account	0	t	\N	/realms/jodrive-realm/account/	f	\N	f	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	openid-connect	0	f	f	${client_account}	f	client-secret	${authBaseUrl}	\N	\N	t	f	f	f
c7633729-6ca9-4cc4-9153-86f6d352230d	t	f	account-console	0	t	\N	/realms/jodrive-realm/account/	f	\N	f	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	openid-connect	0	f	f	${client_account-console}	f	client-secret	${authBaseUrl}	\N	\N	t	f	f	f
62f26ff7-f9ed-4f44-84d7-4aa189b52d2c	t	f	broker	0	f	\N	\N	t	\N	f	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	openid-connect	0	f	f	${client_broker}	f	client-secret	\N	\N	\N	t	f	f	f
495ee007-a202-41b2-b78c-3ce14d1da8ce	t	t	security-admin-console	0	t	\N	/admin/jodrive-realm/console/	f	\N	f	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	openid-connect	0	f	f	${client_security-admin-console}	f	client-secret	${authAdminUrl}	\N	\N	t	f	f	f
065f09ca-5467-4cea-b320-8800d6de5a46	t	t	admin-cli	0	t	\N	\N	f	\N	f	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	openid-connect	0	f	f	${client_admin-cli}	f	client-secret	\N	\N	\N	f	f	t	f
05b20b1d-d90a-4d28-b31f-73b413c99bf1	t	t	jodrive	0	t	\N	http://localhost:5173	f		f	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	openid-connect	-1	t	f	client_jodrive	f	client-jwt	http://localhost:5173		\N	t	f	t	f
\.


COPY keycloak.client_attributes (client_id, name, value) FROM stdin;
b0de488c-4330-4955-91e2-f8dfb13e054b	post.logout.redirect.uris	+
ee76a050-87a8-4470-82e6-1a09a0e47223	post.logout.redirect.uris	+
ee76a050-87a8-4470-82e6-1a09a0e47223	pkce.code.challenge.method	S256
108d6f49-22ba-43f2-84e8-105fbc9e9cdd	post.logout.redirect.uris	+
108d6f49-22ba-43f2-84e8-105fbc9e9cdd	pkce.code.challenge.method	S256
108d6f49-22ba-43f2-84e8-105fbc9e9cdd	client.use.lightweight.access.token.enabled	true
41cd2bbf-9898-4522-b144-ec933da2a045	client.use.lightweight.access.token.enabled	true
da0a22ac-4993-40b2-a719-2a2f01790525	post.logout.redirect.uris	+
c7633729-6ca9-4cc4-9153-86f6d352230d	post.logout.redirect.uris	+
c7633729-6ca9-4cc4-9153-86f6d352230d	pkce.code.challenge.method	S256
495ee007-a202-41b2-b78c-3ce14d1da8ce	post.logout.redirect.uris	+
495ee007-a202-41b2-b78c-3ce14d1da8ce	pkce.code.challenge.method	S256
495ee007-a202-41b2-b78c-3ce14d1da8ce	client.use.lightweight.access.token.enabled	true
065f09ca-5467-4cea-b320-8800d6de5a46	client.use.lightweight.access.token.enabled	true
05b20b1d-d90a-4d28-b31f-73b413c99bf1	oauth2.device.authorization.grant.enabled	false
05b20b1d-d90a-4d28-b31f-73b413c99bf1	oidc.ciba.grant.enabled	false
05b20b1d-d90a-4d28-b31f-73b413c99bf1	post.logout.redirect.uris	http://localhost:5173/*
05b20b1d-d90a-4d28-b31f-73b413c99bf1	backchannel.logout.session.required	true
05b20b1d-d90a-4d28-b31f-73b413c99bf1	backchannel.logout.revoke.offline.tokens	false
05b20b1d-d90a-4d28-b31f-73b413c99bf1	realm_client	false
05b20b1d-d90a-4d28-b31f-73b413c99bf1	display.on.consent.screen	false
05b20b1d-d90a-4d28-b31f-73b413c99bf1	frontchannel.logout.session.required	true
05b20b1d-d90a-4d28-b31f-73b413c99bf1	client.secret.creation.time	1745446836
05b20b1d-d90a-4d28-b31f-73b413c99bf1	use.jwks.url	false
05b20b1d-d90a-4d28-b31f-73b413c99bf1	token.endpoint.auth.signing.alg	RS256
05b20b1d-d90a-4d28-b31f-73b413c99bf1	token.endpoint.auth.signing.max.exp	600
05b20b1d-d90a-4d28-b31f-73b413c99bf1	access.token.header.type.rfc9068	false
05b20b1d-d90a-4d28-b31f-73b413c99bf1	request.object.signature.alg	any
05b20b1d-d90a-4d28-b31f-73b413c99bf1	request.object.encryption.alg	any
05b20b1d-d90a-4d28-b31f-73b413c99bf1	request.object.encryption.enc	any
05b20b1d-d90a-4d28-b31f-73b413c99bf1	request.object.required	not required
05b20b1d-d90a-4d28-b31f-73b413c99bf1	use.refresh.tokens	true
05b20b1d-d90a-4d28-b31f-73b413c99bf1	client_credentials.use_refresh_token	false
05b20b1d-d90a-4d28-b31f-73b413c99bf1	token.response.type.bearer.lower-case	false
05b20b1d-d90a-4d28-b31f-73b413c99bf1	tls.client.certificate.bound.access.tokens	false
05b20b1d-d90a-4d28-b31f-73b413c99bf1	require.pushed.authorization.requests	false
05b20b1d-d90a-4d28-b31f-73b413c99bf1	client.use.lightweight.access.token.enabled	false
05b20b1d-d90a-4d28-b31f-73b413c99bf1	client.introspection.response.allow.jwt.claim.enabled	false
05b20b1d-d90a-4d28-b31f-73b413c99bf1	jwt.credential.certificate	MIICnTCCAYUCBgGWcZFJEjANBgkqhkiG9w0BAQsFADASMRAwDgYDVQQDDAdqb2RyaXZlMB4XDTI1MDQyNjEwMDYwOVoXDTM1MDQyNjEwMDc0OVowEjEQMA4GA1UEAwwHam9kcml2ZTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAMrhirxcYpROlKkQfHyayqfW3q+p8zEvZcOBV+iBnLg4dQi1JZk5brQALb99FWYZVzsY5VmNWPvHBTmD/JiPkTXBKcmmOxxjCsPEIdo5FaBOZETicldgpw7gINq8qSJwnwoCaU3XhdNtkaFeBBgXpWvCAWpSqeGi8Px4tsOF6rCqBXPQOZMtE1On5hN2ptuyJzCrp95lyGF54AZHidM8NXncENsgPjfbzUdtmsZKxR1YzsRUbKHMmjJubx1FvjVb2D7dPTYi9/tL/z9kSmjvc5e1+7mdA3LuVTgMTg5ij6AfgVz2BmuTCRUcjGxE43HWpO8wCV57ondQvsIkZsc7g7kCAwEAATANBgkqhkiG9w0BAQsFAAOCAQEAdUJ21jUEC/slrfICMJo0zEuMVDLPWpE/7uU42w1ZVR8k6tRcmuOo1+zx00bQPTVQAi0s8DT3YKZLNFNwQLwrotzKdchPWG5zDyo98cMP3pMAxqtyoZEbuKWN/Lu+7DcsY3TiTT1SKIJVlW4A+UkVg7L9j2dnH9GnfSlUEKCYGliwgXq/LmRasY/HtWXimeCqPifBTiQzV9ObDk3/RNSBUdtB4okWR59j7Be2dvKfyzPqeN9qYAdru/fAmX70fAAmGRFfT/ytww5ocGCwlMPoufw4Y/hnCCOGIlBUJPfZsBKF762SG21uHQwL/HZiz2aHgJXu3oOi5Htglk6vOPJRVQ==
05b20b1d-d90a-4d28-b31f-73b413c99bf1	standard.token.exchange.enabled	false
\.


COPY keycloak.client_auth_flow_bindings (client_id, flow_id, binding_name) FROM stdin;
\.


COPY keycloak.client_initial_access (id, realm_id, "timestamp", expiration, count, remaining_count) FROM stdin;
\.


COPY keycloak.client_node_registrations (client_id, value, name) FROM stdin;
\.


COPY keycloak.client_scope (id, name, realm_id, description, protocol) FROM stdin;
9f35e4f4-171b-4e4c-bd41-80956867a296	offline_access	fc4359ab-c586-4d33-a1e0-6382887b081c	OpenID Connect built-in scope: offline_access	openid-connect
1ae2dcaa-0525-4bf7-bfc4-0abf95a42d77	role_list	fc4359ab-c586-4d33-a1e0-6382887b081c	SAML role list	saml
e8d54d8c-62f6-4798-afa7-464b8c3584e0	saml_organization	fc4359ab-c586-4d33-a1e0-6382887b081c	Organization Membership	saml
8a546be3-675d-42c6-a00f-021792f5948b	profile	fc4359ab-c586-4d33-a1e0-6382887b081c	OpenID Connect built-in scope: profile	openid-connect
9e96fd62-765d-4a19-be1a-153aa28fa60f	email	fc4359ab-c586-4d33-a1e0-6382887b081c	OpenID Connect built-in scope: email	openid-connect
9c8d4a40-e6f2-43a1-a3d8-3225bf6962a2	address	fc4359ab-c586-4d33-a1e0-6382887b081c	OpenID Connect built-in scope: address	openid-connect
5cdaa0cd-4746-4007-831b-addde8e7ea8a	phone	fc4359ab-c586-4d33-a1e0-6382887b081c	OpenID Connect built-in scope: phone	openid-connect
1de55914-4287-4f90-a756-98935801a54a	roles	fc4359ab-c586-4d33-a1e0-6382887b081c	OpenID Connect scope for add user roles to the access token	openid-connect
6371eda8-48d2-49aa-8d78-d7494faa18a8	web-origins	fc4359ab-c586-4d33-a1e0-6382887b081c	OpenID Connect scope for add allowed web origins to the access token	openid-connect
a30af88e-ea48-4f49-93dd-5b12e663c661	microprofile-jwt	fc4359ab-c586-4d33-a1e0-6382887b081c	Microprofile - JWT built-in scope	openid-connect
f324045a-4192-4f9f-a91a-0c13207ca40b	acr	fc4359ab-c586-4d33-a1e0-6382887b081c	OpenID Connect scope for add acr (authentication context class reference) to the token	openid-connect
f5b0a239-7f75-4370-ba8b-2e297524a542	basic	fc4359ab-c586-4d33-a1e0-6382887b081c	OpenID Connect scope for add all basic claims to the token	openid-connect
dfcaecf4-fb06-49fc-bd13-7d7f1bd656e0	service_account	fc4359ab-c586-4d33-a1e0-6382887b081c	Specific scope for a client enabled for service accounts	openid-connect
dc87757c-d255-495f-b67b-e56125c73634	organization	fc4359ab-c586-4d33-a1e0-6382887b081c	Additional claims about the organization a subject belongs to	openid-connect
6eca9a41-8a64-412f-a81a-1002d28db0a2	offline_access	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	OpenID Connect built-in scope: offline_access	openid-connect
edc041b2-9228-48c8-95d6-ce1c8cfa0d1c	role_list	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	SAML role list	saml
ee6c4196-cb97-4d15-9398-329614005ffd	saml_organization	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	Organization Membership	saml
ac6628bd-6dbc-4b80-814e-520ed71001c5	profile	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	OpenID Connect built-in scope: profile	openid-connect
9a14df16-95c2-4978-a6f8-eaf87357be72	email	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	OpenID Connect built-in scope: email	openid-connect
124a2c54-450c-41e5-9b9f-259ce8dd8e58	address	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	OpenID Connect built-in scope: address	openid-connect
ea3bcee8-a9b8-49e5-b903-3ea8a125a1b8	phone	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	OpenID Connect built-in scope: phone	openid-connect
299a79ee-e783-468b-8e75-27838b370ef8	roles	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	OpenID Connect scope for add user roles to the access token	openid-connect
a3c705a8-6890-452a-abf2-ef79dfa0f6cd	web-origins	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	OpenID Connect scope for add allowed web origins to the access token	openid-connect
58f9639a-c0ed-4002-9d6e-f84547b6b2e3	microprofile-jwt	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	Microprofile - JWT built-in scope	openid-connect
a683d956-1aa5-4087-941d-40624fbaa93f	acr	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	OpenID Connect scope for add acr (authentication context class reference) to the token	openid-connect
3f50493d-06bd-4e56-84c1-46186100d0fd	basic	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	OpenID Connect scope for add all basic claims to the token	openid-connect
677437b7-e6dd-4725-9cc4-3bb21ed07d75	service_account	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	Specific scope for a client enabled for service accounts	openid-connect
c75ba36c-1229-4b13-8537-017e7a1a0300	organization	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	Additional claims about the organization a subject belongs to	openid-connect
\.


COPY keycloak.client_scope_attributes (scope_id, value, name) FROM stdin;
9f35e4f4-171b-4e4c-bd41-80956867a296	true	display.on.consent.screen
9f35e4f4-171b-4e4c-bd41-80956867a296	${offlineAccessScopeConsentText}	consent.screen.text
1ae2dcaa-0525-4bf7-bfc4-0abf95a42d77	true	display.on.consent.screen
1ae2dcaa-0525-4bf7-bfc4-0abf95a42d77	${samlRoleListScopeConsentText}	consent.screen.text
e8d54d8c-62f6-4798-afa7-464b8c3584e0	false	display.on.consent.screen
8a546be3-675d-42c6-a00f-021792f5948b	true	display.on.consent.screen
8a546be3-675d-42c6-a00f-021792f5948b	${profileScopeConsentText}	consent.screen.text
8a546be3-675d-42c6-a00f-021792f5948b	true	include.in.token.scope
9e96fd62-765d-4a19-be1a-153aa28fa60f	true	display.on.consent.screen
9e96fd62-765d-4a19-be1a-153aa28fa60f	${emailScopeConsentText}	consent.screen.text
9e96fd62-765d-4a19-be1a-153aa28fa60f	true	include.in.token.scope
9c8d4a40-e6f2-43a1-a3d8-3225bf6962a2	true	display.on.consent.screen
9c8d4a40-e6f2-43a1-a3d8-3225bf6962a2	${addressScopeConsentText}	consent.screen.text
9c8d4a40-e6f2-43a1-a3d8-3225bf6962a2	true	include.in.token.scope
5cdaa0cd-4746-4007-831b-addde8e7ea8a	true	display.on.consent.screen
5cdaa0cd-4746-4007-831b-addde8e7ea8a	${phoneScopeConsentText}	consent.screen.text
5cdaa0cd-4746-4007-831b-addde8e7ea8a	true	include.in.token.scope
1de55914-4287-4f90-a756-98935801a54a	true	display.on.consent.screen
1de55914-4287-4f90-a756-98935801a54a	${rolesScopeConsentText}	consent.screen.text
1de55914-4287-4f90-a756-98935801a54a	false	include.in.token.scope
6371eda8-48d2-49aa-8d78-d7494faa18a8	false	display.on.consent.screen
6371eda8-48d2-49aa-8d78-d7494faa18a8		consent.screen.text
6371eda8-48d2-49aa-8d78-d7494faa18a8	false	include.in.token.scope
a30af88e-ea48-4f49-93dd-5b12e663c661	false	display.on.consent.screen
a30af88e-ea48-4f49-93dd-5b12e663c661	true	include.in.token.scope
f324045a-4192-4f9f-a91a-0c13207ca40b	false	display.on.consent.screen
f324045a-4192-4f9f-a91a-0c13207ca40b	false	include.in.token.scope
f5b0a239-7f75-4370-ba8b-2e297524a542	false	display.on.consent.screen
f5b0a239-7f75-4370-ba8b-2e297524a542	false	include.in.token.scope
dfcaecf4-fb06-49fc-bd13-7d7f1bd656e0	false	display.on.consent.screen
dfcaecf4-fb06-49fc-bd13-7d7f1bd656e0	false	include.in.token.scope
dc87757c-d255-495f-b67b-e56125c73634	true	display.on.consent.screen
dc87757c-d255-495f-b67b-e56125c73634	${organizationScopeConsentText}	consent.screen.text
dc87757c-d255-495f-b67b-e56125c73634	true	include.in.token.scope
6eca9a41-8a64-412f-a81a-1002d28db0a2	true	display.on.consent.screen
6eca9a41-8a64-412f-a81a-1002d28db0a2	${offlineAccessScopeConsentText}	consent.screen.text
edc041b2-9228-48c8-95d6-ce1c8cfa0d1c	true	display.on.consent.screen
edc041b2-9228-48c8-95d6-ce1c8cfa0d1c	${samlRoleListScopeConsentText}	consent.screen.text
ee6c4196-cb97-4d15-9398-329614005ffd	false	display.on.consent.screen
ac6628bd-6dbc-4b80-814e-520ed71001c5	true	display.on.consent.screen
ac6628bd-6dbc-4b80-814e-520ed71001c5	${profileScopeConsentText}	consent.screen.text
ac6628bd-6dbc-4b80-814e-520ed71001c5	true	include.in.token.scope
9a14df16-95c2-4978-a6f8-eaf87357be72	true	display.on.consent.screen
9a14df16-95c2-4978-a6f8-eaf87357be72	${emailScopeConsentText}	consent.screen.text
9a14df16-95c2-4978-a6f8-eaf87357be72	true	include.in.token.scope
124a2c54-450c-41e5-9b9f-259ce8dd8e58	true	display.on.consent.screen
124a2c54-450c-41e5-9b9f-259ce8dd8e58	${addressScopeConsentText}	consent.screen.text
124a2c54-450c-41e5-9b9f-259ce8dd8e58	true	include.in.token.scope
ea3bcee8-a9b8-49e5-b903-3ea8a125a1b8	true	display.on.consent.screen
ea3bcee8-a9b8-49e5-b903-3ea8a125a1b8	${phoneScopeConsentText}	consent.screen.text
ea3bcee8-a9b8-49e5-b903-3ea8a125a1b8	true	include.in.token.scope
299a79ee-e783-468b-8e75-27838b370ef8	true	display.on.consent.screen
299a79ee-e783-468b-8e75-27838b370ef8	${rolesScopeConsentText}	consent.screen.text
299a79ee-e783-468b-8e75-27838b370ef8	false	include.in.token.scope
a3c705a8-6890-452a-abf2-ef79dfa0f6cd	false	display.on.consent.screen
a3c705a8-6890-452a-abf2-ef79dfa0f6cd		consent.screen.text
a3c705a8-6890-452a-abf2-ef79dfa0f6cd	false	include.in.token.scope
58f9639a-c0ed-4002-9d6e-f84547b6b2e3	false	display.on.consent.screen
58f9639a-c0ed-4002-9d6e-f84547b6b2e3	true	include.in.token.scope
a683d956-1aa5-4087-941d-40624fbaa93f	false	display.on.consent.screen
a683d956-1aa5-4087-941d-40624fbaa93f	false	include.in.token.scope
3f50493d-06bd-4e56-84c1-46186100d0fd	false	display.on.consent.screen
3f50493d-06bd-4e56-84c1-46186100d0fd	false	include.in.token.scope
677437b7-e6dd-4725-9cc4-3bb21ed07d75	false	display.on.consent.screen
677437b7-e6dd-4725-9cc4-3bb21ed07d75	false	include.in.token.scope
c75ba36c-1229-4b13-8537-017e7a1a0300	true	display.on.consent.screen
c75ba36c-1229-4b13-8537-017e7a1a0300	${organizationScopeConsentText}	consent.screen.text
c75ba36c-1229-4b13-8537-017e7a1a0300	true	include.in.token.scope
\.


COPY keycloak.client_scope_client (client_id, scope_id, default_scope) FROM stdin;
b0de488c-4330-4955-91e2-f8dfb13e054b	1de55914-4287-4f90-a756-98935801a54a	t
b0de488c-4330-4955-91e2-f8dfb13e054b	6371eda8-48d2-49aa-8d78-d7494faa18a8	t
b0de488c-4330-4955-91e2-f8dfb13e054b	f5b0a239-7f75-4370-ba8b-2e297524a542	t
b0de488c-4330-4955-91e2-f8dfb13e054b	f324045a-4192-4f9f-a91a-0c13207ca40b	t
b0de488c-4330-4955-91e2-f8dfb13e054b	8a546be3-675d-42c6-a00f-021792f5948b	t
b0de488c-4330-4955-91e2-f8dfb13e054b	9e96fd62-765d-4a19-be1a-153aa28fa60f	t
b0de488c-4330-4955-91e2-f8dfb13e054b	9f35e4f4-171b-4e4c-bd41-80956867a296	f
b0de488c-4330-4955-91e2-f8dfb13e054b	a30af88e-ea48-4f49-93dd-5b12e663c661	f
b0de488c-4330-4955-91e2-f8dfb13e054b	dc87757c-d255-495f-b67b-e56125c73634	f
b0de488c-4330-4955-91e2-f8dfb13e054b	9c8d4a40-e6f2-43a1-a3d8-3225bf6962a2	f
b0de488c-4330-4955-91e2-f8dfb13e054b	5cdaa0cd-4746-4007-831b-addde8e7ea8a	f
ee76a050-87a8-4470-82e6-1a09a0e47223	1de55914-4287-4f90-a756-98935801a54a	t
ee76a050-87a8-4470-82e6-1a09a0e47223	6371eda8-48d2-49aa-8d78-d7494faa18a8	t
ee76a050-87a8-4470-82e6-1a09a0e47223	f5b0a239-7f75-4370-ba8b-2e297524a542	t
ee76a050-87a8-4470-82e6-1a09a0e47223	f324045a-4192-4f9f-a91a-0c13207ca40b	t
ee76a050-87a8-4470-82e6-1a09a0e47223	8a546be3-675d-42c6-a00f-021792f5948b	t
ee76a050-87a8-4470-82e6-1a09a0e47223	9e96fd62-765d-4a19-be1a-153aa28fa60f	t
ee76a050-87a8-4470-82e6-1a09a0e47223	9f35e4f4-171b-4e4c-bd41-80956867a296	f
ee76a050-87a8-4470-82e6-1a09a0e47223	a30af88e-ea48-4f49-93dd-5b12e663c661	f
ee76a050-87a8-4470-82e6-1a09a0e47223	dc87757c-d255-495f-b67b-e56125c73634	f
ee76a050-87a8-4470-82e6-1a09a0e47223	9c8d4a40-e6f2-43a1-a3d8-3225bf6962a2	f
ee76a050-87a8-4470-82e6-1a09a0e47223	5cdaa0cd-4746-4007-831b-addde8e7ea8a	f
41cd2bbf-9898-4522-b144-ec933da2a045	1de55914-4287-4f90-a756-98935801a54a	t
41cd2bbf-9898-4522-b144-ec933da2a045	6371eda8-48d2-49aa-8d78-d7494faa18a8	t
41cd2bbf-9898-4522-b144-ec933da2a045	f5b0a239-7f75-4370-ba8b-2e297524a542	t
41cd2bbf-9898-4522-b144-ec933da2a045	f324045a-4192-4f9f-a91a-0c13207ca40b	t
41cd2bbf-9898-4522-b144-ec933da2a045	8a546be3-675d-42c6-a00f-021792f5948b	t
41cd2bbf-9898-4522-b144-ec933da2a045	9e96fd62-765d-4a19-be1a-153aa28fa60f	t
41cd2bbf-9898-4522-b144-ec933da2a045	9f35e4f4-171b-4e4c-bd41-80956867a296	f
41cd2bbf-9898-4522-b144-ec933da2a045	a30af88e-ea48-4f49-93dd-5b12e663c661	f
41cd2bbf-9898-4522-b144-ec933da2a045	dc87757c-d255-495f-b67b-e56125c73634	f
41cd2bbf-9898-4522-b144-ec933da2a045	9c8d4a40-e6f2-43a1-a3d8-3225bf6962a2	f
41cd2bbf-9898-4522-b144-ec933da2a045	5cdaa0cd-4746-4007-831b-addde8e7ea8a	f
ebe6655e-eccd-4d44-a08c-d4b523c6ce96	1de55914-4287-4f90-a756-98935801a54a	t
ebe6655e-eccd-4d44-a08c-d4b523c6ce96	6371eda8-48d2-49aa-8d78-d7494faa18a8	t
ebe6655e-eccd-4d44-a08c-d4b523c6ce96	f5b0a239-7f75-4370-ba8b-2e297524a542	t
ebe6655e-eccd-4d44-a08c-d4b523c6ce96	f324045a-4192-4f9f-a91a-0c13207ca40b	t
ebe6655e-eccd-4d44-a08c-d4b523c6ce96	8a546be3-675d-42c6-a00f-021792f5948b	t
ebe6655e-eccd-4d44-a08c-d4b523c6ce96	9e96fd62-765d-4a19-be1a-153aa28fa60f	t
ebe6655e-eccd-4d44-a08c-d4b523c6ce96	9f35e4f4-171b-4e4c-bd41-80956867a296	f
ebe6655e-eccd-4d44-a08c-d4b523c6ce96	a30af88e-ea48-4f49-93dd-5b12e663c661	f
ebe6655e-eccd-4d44-a08c-d4b523c6ce96	dc87757c-d255-495f-b67b-e56125c73634	f
ebe6655e-eccd-4d44-a08c-d4b523c6ce96	9c8d4a40-e6f2-43a1-a3d8-3225bf6962a2	f
ebe6655e-eccd-4d44-a08c-d4b523c6ce96	5cdaa0cd-4746-4007-831b-addde8e7ea8a	f
ae3ea88f-aecf-4fec-b290-bb7594719e44	1de55914-4287-4f90-a756-98935801a54a	t
ae3ea88f-aecf-4fec-b290-bb7594719e44	6371eda8-48d2-49aa-8d78-d7494faa18a8	t
ae3ea88f-aecf-4fec-b290-bb7594719e44	f5b0a239-7f75-4370-ba8b-2e297524a542	t
ae3ea88f-aecf-4fec-b290-bb7594719e44	f324045a-4192-4f9f-a91a-0c13207ca40b	t
ae3ea88f-aecf-4fec-b290-bb7594719e44	8a546be3-675d-42c6-a00f-021792f5948b	t
ae3ea88f-aecf-4fec-b290-bb7594719e44	9e96fd62-765d-4a19-be1a-153aa28fa60f	t
ae3ea88f-aecf-4fec-b290-bb7594719e44	9f35e4f4-171b-4e4c-bd41-80956867a296	f
ae3ea88f-aecf-4fec-b290-bb7594719e44	a30af88e-ea48-4f49-93dd-5b12e663c661	f
ae3ea88f-aecf-4fec-b290-bb7594719e44	dc87757c-d255-495f-b67b-e56125c73634	f
ae3ea88f-aecf-4fec-b290-bb7594719e44	9c8d4a40-e6f2-43a1-a3d8-3225bf6962a2	f
ae3ea88f-aecf-4fec-b290-bb7594719e44	5cdaa0cd-4746-4007-831b-addde8e7ea8a	f
108d6f49-22ba-43f2-84e8-105fbc9e9cdd	1de55914-4287-4f90-a756-98935801a54a	t
108d6f49-22ba-43f2-84e8-105fbc9e9cdd	6371eda8-48d2-49aa-8d78-d7494faa18a8	t
108d6f49-22ba-43f2-84e8-105fbc9e9cdd	f5b0a239-7f75-4370-ba8b-2e297524a542	t
108d6f49-22ba-43f2-84e8-105fbc9e9cdd	f324045a-4192-4f9f-a91a-0c13207ca40b	t
108d6f49-22ba-43f2-84e8-105fbc9e9cdd	8a546be3-675d-42c6-a00f-021792f5948b	t
108d6f49-22ba-43f2-84e8-105fbc9e9cdd	9e96fd62-765d-4a19-be1a-153aa28fa60f	t
108d6f49-22ba-43f2-84e8-105fbc9e9cdd	9f35e4f4-171b-4e4c-bd41-80956867a296	f
108d6f49-22ba-43f2-84e8-105fbc9e9cdd	a30af88e-ea48-4f49-93dd-5b12e663c661	f
108d6f49-22ba-43f2-84e8-105fbc9e9cdd	dc87757c-d255-495f-b67b-e56125c73634	f
108d6f49-22ba-43f2-84e8-105fbc9e9cdd	9c8d4a40-e6f2-43a1-a3d8-3225bf6962a2	f
108d6f49-22ba-43f2-84e8-105fbc9e9cdd	5cdaa0cd-4746-4007-831b-addde8e7ea8a	f
da0a22ac-4993-40b2-a719-2a2f01790525	ac6628bd-6dbc-4b80-814e-520ed71001c5	t
da0a22ac-4993-40b2-a719-2a2f01790525	a3c705a8-6890-452a-abf2-ef79dfa0f6cd	t
da0a22ac-4993-40b2-a719-2a2f01790525	3f50493d-06bd-4e56-84c1-46186100d0fd	t
da0a22ac-4993-40b2-a719-2a2f01790525	9a14df16-95c2-4978-a6f8-eaf87357be72	t
da0a22ac-4993-40b2-a719-2a2f01790525	a683d956-1aa5-4087-941d-40624fbaa93f	t
da0a22ac-4993-40b2-a719-2a2f01790525	299a79ee-e783-468b-8e75-27838b370ef8	t
da0a22ac-4993-40b2-a719-2a2f01790525	124a2c54-450c-41e5-9b9f-259ce8dd8e58	f
da0a22ac-4993-40b2-a719-2a2f01790525	ea3bcee8-a9b8-49e5-b903-3ea8a125a1b8	f
da0a22ac-4993-40b2-a719-2a2f01790525	6eca9a41-8a64-412f-a81a-1002d28db0a2	f
da0a22ac-4993-40b2-a719-2a2f01790525	c75ba36c-1229-4b13-8537-017e7a1a0300	f
da0a22ac-4993-40b2-a719-2a2f01790525	58f9639a-c0ed-4002-9d6e-f84547b6b2e3	f
c7633729-6ca9-4cc4-9153-86f6d352230d	ac6628bd-6dbc-4b80-814e-520ed71001c5	t
c7633729-6ca9-4cc4-9153-86f6d352230d	a3c705a8-6890-452a-abf2-ef79dfa0f6cd	t
c7633729-6ca9-4cc4-9153-86f6d352230d	3f50493d-06bd-4e56-84c1-46186100d0fd	t
c7633729-6ca9-4cc4-9153-86f6d352230d	9a14df16-95c2-4978-a6f8-eaf87357be72	t
c7633729-6ca9-4cc4-9153-86f6d352230d	a683d956-1aa5-4087-941d-40624fbaa93f	t
c7633729-6ca9-4cc4-9153-86f6d352230d	299a79ee-e783-468b-8e75-27838b370ef8	t
c7633729-6ca9-4cc4-9153-86f6d352230d	124a2c54-450c-41e5-9b9f-259ce8dd8e58	f
c7633729-6ca9-4cc4-9153-86f6d352230d	ea3bcee8-a9b8-49e5-b903-3ea8a125a1b8	f
c7633729-6ca9-4cc4-9153-86f6d352230d	6eca9a41-8a64-412f-a81a-1002d28db0a2	f
c7633729-6ca9-4cc4-9153-86f6d352230d	c75ba36c-1229-4b13-8537-017e7a1a0300	f
c7633729-6ca9-4cc4-9153-86f6d352230d	58f9639a-c0ed-4002-9d6e-f84547b6b2e3	f
065f09ca-5467-4cea-b320-8800d6de5a46	ac6628bd-6dbc-4b80-814e-520ed71001c5	t
065f09ca-5467-4cea-b320-8800d6de5a46	a3c705a8-6890-452a-abf2-ef79dfa0f6cd	t
065f09ca-5467-4cea-b320-8800d6de5a46	3f50493d-06bd-4e56-84c1-46186100d0fd	t
065f09ca-5467-4cea-b320-8800d6de5a46	9a14df16-95c2-4978-a6f8-eaf87357be72	t
065f09ca-5467-4cea-b320-8800d6de5a46	a683d956-1aa5-4087-941d-40624fbaa93f	t
065f09ca-5467-4cea-b320-8800d6de5a46	299a79ee-e783-468b-8e75-27838b370ef8	t
065f09ca-5467-4cea-b320-8800d6de5a46	124a2c54-450c-41e5-9b9f-259ce8dd8e58	f
065f09ca-5467-4cea-b320-8800d6de5a46	ea3bcee8-a9b8-49e5-b903-3ea8a125a1b8	f
065f09ca-5467-4cea-b320-8800d6de5a46	6eca9a41-8a64-412f-a81a-1002d28db0a2	f
065f09ca-5467-4cea-b320-8800d6de5a46	c75ba36c-1229-4b13-8537-017e7a1a0300	f
065f09ca-5467-4cea-b320-8800d6de5a46	58f9639a-c0ed-4002-9d6e-f84547b6b2e3	f
62f26ff7-f9ed-4f44-84d7-4aa189b52d2c	ac6628bd-6dbc-4b80-814e-520ed71001c5	t
62f26ff7-f9ed-4f44-84d7-4aa189b52d2c	a3c705a8-6890-452a-abf2-ef79dfa0f6cd	t
62f26ff7-f9ed-4f44-84d7-4aa189b52d2c	3f50493d-06bd-4e56-84c1-46186100d0fd	t
62f26ff7-f9ed-4f44-84d7-4aa189b52d2c	9a14df16-95c2-4978-a6f8-eaf87357be72	t
62f26ff7-f9ed-4f44-84d7-4aa189b52d2c	a683d956-1aa5-4087-941d-40624fbaa93f	t
62f26ff7-f9ed-4f44-84d7-4aa189b52d2c	299a79ee-e783-468b-8e75-27838b370ef8	t
62f26ff7-f9ed-4f44-84d7-4aa189b52d2c	124a2c54-450c-41e5-9b9f-259ce8dd8e58	f
62f26ff7-f9ed-4f44-84d7-4aa189b52d2c	ea3bcee8-a9b8-49e5-b903-3ea8a125a1b8	f
62f26ff7-f9ed-4f44-84d7-4aa189b52d2c	6eca9a41-8a64-412f-a81a-1002d28db0a2	f
62f26ff7-f9ed-4f44-84d7-4aa189b52d2c	c75ba36c-1229-4b13-8537-017e7a1a0300	f
62f26ff7-f9ed-4f44-84d7-4aa189b52d2c	58f9639a-c0ed-4002-9d6e-f84547b6b2e3	f
f8e3dca3-1b19-48ea-a95d-b7e8cfa335c3	ac6628bd-6dbc-4b80-814e-520ed71001c5	t
f8e3dca3-1b19-48ea-a95d-b7e8cfa335c3	a3c705a8-6890-452a-abf2-ef79dfa0f6cd	t
f8e3dca3-1b19-48ea-a95d-b7e8cfa335c3	3f50493d-06bd-4e56-84c1-46186100d0fd	t
f8e3dca3-1b19-48ea-a95d-b7e8cfa335c3	9a14df16-95c2-4978-a6f8-eaf87357be72	t
f8e3dca3-1b19-48ea-a95d-b7e8cfa335c3	a683d956-1aa5-4087-941d-40624fbaa93f	t
f8e3dca3-1b19-48ea-a95d-b7e8cfa335c3	299a79ee-e783-468b-8e75-27838b370ef8	t
f8e3dca3-1b19-48ea-a95d-b7e8cfa335c3	124a2c54-450c-41e5-9b9f-259ce8dd8e58	f
f8e3dca3-1b19-48ea-a95d-b7e8cfa335c3	ea3bcee8-a9b8-49e5-b903-3ea8a125a1b8	f
f8e3dca3-1b19-48ea-a95d-b7e8cfa335c3	6eca9a41-8a64-412f-a81a-1002d28db0a2	f
f8e3dca3-1b19-48ea-a95d-b7e8cfa335c3	c75ba36c-1229-4b13-8537-017e7a1a0300	f
f8e3dca3-1b19-48ea-a95d-b7e8cfa335c3	58f9639a-c0ed-4002-9d6e-f84547b6b2e3	f
495ee007-a202-41b2-b78c-3ce14d1da8ce	ac6628bd-6dbc-4b80-814e-520ed71001c5	t
495ee007-a202-41b2-b78c-3ce14d1da8ce	a3c705a8-6890-452a-abf2-ef79dfa0f6cd	t
495ee007-a202-41b2-b78c-3ce14d1da8ce	3f50493d-06bd-4e56-84c1-46186100d0fd	t
495ee007-a202-41b2-b78c-3ce14d1da8ce	9a14df16-95c2-4978-a6f8-eaf87357be72	t
495ee007-a202-41b2-b78c-3ce14d1da8ce	a683d956-1aa5-4087-941d-40624fbaa93f	t
495ee007-a202-41b2-b78c-3ce14d1da8ce	299a79ee-e783-468b-8e75-27838b370ef8	t
495ee007-a202-41b2-b78c-3ce14d1da8ce	124a2c54-450c-41e5-9b9f-259ce8dd8e58	f
495ee007-a202-41b2-b78c-3ce14d1da8ce	ea3bcee8-a9b8-49e5-b903-3ea8a125a1b8	f
495ee007-a202-41b2-b78c-3ce14d1da8ce	6eca9a41-8a64-412f-a81a-1002d28db0a2	f
495ee007-a202-41b2-b78c-3ce14d1da8ce	c75ba36c-1229-4b13-8537-017e7a1a0300	f
495ee007-a202-41b2-b78c-3ce14d1da8ce	58f9639a-c0ed-4002-9d6e-f84547b6b2e3	f
05b20b1d-d90a-4d28-b31f-73b413c99bf1	ac6628bd-6dbc-4b80-814e-520ed71001c5	t
05b20b1d-d90a-4d28-b31f-73b413c99bf1	a3c705a8-6890-452a-abf2-ef79dfa0f6cd	t
05b20b1d-d90a-4d28-b31f-73b413c99bf1	3f50493d-06bd-4e56-84c1-46186100d0fd	t
05b20b1d-d90a-4d28-b31f-73b413c99bf1	9a14df16-95c2-4978-a6f8-eaf87357be72	t
05b20b1d-d90a-4d28-b31f-73b413c99bf1	a683d956-1aa5-4087-941d-40624fbaa93f	t
05b20b1d-d90a-4d28-b31f-73b413c99bf1	299a79ee-e783-468b-8e75-27838b370ef8	t
05b20b1d-d90a-4d28-b31f-73b413c99bf1	124a2c54-450c-41e5-9b9f-259ce8dd8e58	f
05b20b1d-d90a-4d28-b31f-73b413c99bf1	ea3bcee8-a9b8-49e5-b903-3ea8a125a1b8	f
05b20b1d-d90a-4d28-b31f-73b413c99bf1	6eca9a41-8a64-412f-a81a-1002d28db0a2	f
05b20b1d-d90a-4d28-b31f-73b413c99bf1	c75ba36c-1229-4b13-8537-017e7a1a0300	f
05b20b1d-d90a-4d28-b31f-73b413c99bf1	58f9639a-c0ed-4002-9d6e-f84547b6b2e3	f
\.


COPY keycloak.client_scope_role_mapping (scope_id, role_id) FROM stdin;
9f35e4f4-171b-4e4c-bd41-80956867a296	a7b80c13-90d7-476a-acb9-55eed1cb5cb2
6eca9a41-8a64-412f-a81a-1002d28db0a2	c6ff9adb-e6c5-450c-af4d-c5eb488114ae
\.


COPY keycloak.component (id, name, parent_id, provider_id, provider_type, realm_id, sub_type) FROM stdin;
5206d988-e54a-4b62-8c9e-92068f020ae0	Trusted Hosts	fc4359ab-c586-4d33-a1e0-6382887b081c	trusted-hosts	org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy	fc4359ab-c586-4d33-a1e0-6382887b081c	anonymous
917be88c-b5f7-4c6d-b4e8-cab122029880	Consent Required	fc4359ab-c586-4d33-a1e0-6382887b081c	consent-required	org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy	fc4359ab-c586-4d33-a1e0-6382887b081c	anonymous
7b39c75f-1f87-4963-b543-27faa4fc6784	Full Scope Disabled	fc4359ab-c586-4d33-a1e0-6382887b081c	scope	org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy	fc4359ab-c586-4d33-a1e0-6382887b081c	anonymous
ea4c065a-9e25-4970-842a-a05bf8d1142e	Max Clients Limit	fc4359ab-c586-4d33-a1e0-6382887b081c	max-clients	org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy	fc4359ab-c586-4d33-a1e0-6382887b081c	anonymous
34ba7b9c-0bc4-4f0a-b7f4-70cf391cfff1	Allowed Protocol Mapper Types	fc4359ab-c586-4d33-a1e0-6382887b081c	allowed-protocol-mappers	org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy	fc4359ab-c586-4d33-a1e0-6382887b081c	anonymous
7d269974-d70c-410a-9911-1379b7d2fb22	Allowed Client Scopes	fc4359ab-c586-4d33-a1e0-6382887b081c	allowed-client-templates	org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy	fc4359ab-c586-4d33-a1e0-6382887b081c	anonymous
644751d6-d7d3-4f9e-934e-7bc67352139c	Allowed Protocol Mapper Types	fc4359ab-c586-4d33-a1e0-6382887b081c	allowed-protocol-mappers	org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy	fc4359ab-c586-4d33-a1e0-6382887b081c	authenticated
7835e275-c0a0-4086-9c8b-0d8d516ea2b9	Allowed Client Scopes	fc4359ab-c586-4d33-a1e0-6382887b081c	allowed-client-templates	org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy	fc4359ab-c586-4d33-a1e0-6382887b081c	authenticated
1e8e0437-6e58-4e41-b4fa-d3f760ba2458	rsa-generated	fc4359ab-c586-4d33-a1e0-6382887b081c	rsa-generated	org.keycloak.keys.KeyProvider	fc4359ab-c586-4d33-a1e0-6382887b081c	\N
dc82f638-4de3-46be-8205-fb018fb58813	rsa-enc-generated	fc4359ab-c586-4d33-a1e0-6382887b081c	rsa-enc-generated	org.keycloak.keys.KeyProvider	fc4359ab-c586-4d33-a1e0-6382887b081c	\N
664de519-2838-4744-8250-c32c3f653e8f	hmac-generated-hs512	fc4359ab-c586-4d33-a1e0-6382887b081c	hmac-generated	org.keycloak.keys.KeyProvider	fc4359ab-c586-4d33-a1e0-6382887b081c	\N
a55438b5-63d6-49a1-b7e5-0b92a902ebac	aes-generated	fc4359ab-c586-4d33-a1e0-6382887b081c	aes-generated	org.keycloak.keys.KeyProvider	fc4359ab-c586-4d33-a1e0-6382887b081c	\N
82049e98-c30c-4eab-bd6b-227c601500e8	\N	fc4359ab-c586-4d33-a1e0-6382887b081c	declarative-user-profile	org.keycloak.userprofile.UserProfileProvider	fc4359ab-c586-4d33-a1e0-6382887b081c	\N
693db534-8c74-45da-977c-7e75b0d67205	rsa-generated	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	rsa-generated	org.keycloak.keys.KeyProvider	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	\N
bd0818c6-225c-4d21-ad3a-a267e57c0f2e	rsa-enc-generated	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	rsa-enc-generated	org.keycloak.keys.KeyProvider	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	\N
a7555771-ecee-46bf-8b6c-d3650aa99a28	hmac-generated-hs512	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	hmac-generated	org.keycloak.keys.KeyProvider	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	\N
45190737-c32b-4720-adf7-290fa187382e	aes-generated	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	aes-generated	org.keycloak.keys.KeyProvider	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	\N
bcc2ac9a-b7bc-487a-b146-4b11bfca636c	Trusted Hosts	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	trusted-hosts	org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	anonymous
15664216-d703-45aa-a7e7-a9a07a77c324	Consent Required	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	consent-required	org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	anonymous
90ffd4ba-35a9-41e3-80f9-114343cbf52d	Full Scope Disabled	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	scope	org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	anonymous
a2977679-fa92-45cd-9f17-419dad8181a1	Max Clients Limit	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	max-clients	org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	anonymous
4e2a261b-21d6-43b4-ad2e-5b369c8caa04	Allowed Protocol Mapper Types	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	allowed-protocol-mappers	org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	anonymous
9da37549-03ce-4453-831a-6cbd340ba269	Allowed Client Scopes	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	allowed-client-templates	org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	anonymous
105d953d-1399-43b1-9551-9d0f47ce466d	Allowed Protocol Mapper Types	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	allowed-protocol-mappers	org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	authenticated
74c6dbee-68d4-4105-878c-b1e03d76f883	Allowed Client Scopes	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	allowed-client-templates	org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	authenticated
\.


COPY keycloak.component_config (id, component_id, name, value) FROM stdin;
223604bc-2589-4cba-9b56-f204c06453be	644751d6-d7d3-4f9e-934e-7bc67352139c	allowed-protocol-mapper-types	oidc-address-mapper
5ca80901-740f-4478-906a-00b5786379b9	644751d6-d7d3-4f9e-934e-7bc67352139c	allowed-protocol-mapper-types	oidc-usermodel-property-mapper
54d877c7-420c-4994-ae58-231abc151829	644751d6-d7d3-4f9e-934e-7bc67352139c	allowed-protocol-mapper-types	oidc-usermodel-attribute-mapper
a64d40ff-a15a-4482-86af-53f34ed8c5c5	644751d6-d7d3-4f9e-934e-7bc67352139c	allowed-protocol-mapper-types	saml-user-property-mapper
7e3b1be1-4bc9-4b63-b909-27f7c648ab6f	644751d6-d7d3-4f9e-934e-7bc67352139c	allowed-protocol-mapper-types	oidc-full-name-mapper
2752b201-9a6b-477f-9fa6-cadb1b350c76	644751d6-d7d3-4f9e-934e-7bc67352139c	allowed-protocol-mapper-types	saml-user-attribute-mapper
75080162-98de-4a11-87b5-a13b3b730b9b	644751d6-d7d3-4f9e-934e-7bc67352139c	allowed-protocol-mapper-types	saml-role-list-mapper
812b6033-c8c8-4c2d-a3ae-61ec568cd896	644751d6-d7d3-4f9e-934e-7bc67352139c	allowed-protocol-mapper-types	oidc-sha256-pairwise-sub-mapper
52eb4886-1dbd-4c57-aec3-f13955501d3f	7835e275-c0a0-4086-9c8b-0d8d516ea2b9	allow-default-scopes	true
8c157bb3-0205-44c8-bf29-5fabdd5898e5	5206d988-e54a-4b62-8c9e-92068f020ae0	client-uris-must-match	true
aa4093b7-8a31-4b98-b341-03cc584199b8	5206d988-e54a-4b62-8c9e-92068f020ae0	host-sending-registration-request-must-match	true
6b86df40-d5d9-427d-886f-a4ef15d4ae7f	ea4c065a-9e25-4970-842a-a05bf8d1142e	max-clients	200
b56b5c9d-4910-4b89-981f-abd5e160c633	7d269974-d70c-410a-9911-1379b7d2fb22	allow-default-scopes	true
18bcf22a-4e64-4752-93fb-e7eae3b94cb7	34ba7b9c-0bc4-4f0a-b7f4-70cf391cfff1	allowed-protocol-mapper-types	oidc-usermodel-property-mapper
329b7525-6d20-4712-8e1c-58d5e1711591	34ba7b9c-0bc4-4f0a-b7f4-70cf391cfff1	allowed-protocol-mapper-types	oidc-full-name-mapper
32655b6a-3c54-43ba-94e4-3c1d09327f89	34ba7b9c-0bc4-4f0a-b7f4-70cf391cfff1	allowed-protocol-mapper-types	saml-role-list-mapper
0a4c7c99-19d2-4f48-ab1c-00077d4d275a	34ba7b9c-0bc4-4f0a-b7f4-70cf391cfff1	allowed-protocol-mapper-types	saml-user-attribute-mapper
dbf79b65-b831-48bc-85d9-1074bd20a6c4	34ba7b9c-0bc4-4f0a-b7f4-70cf391cfff1	allowed-protocol-mapper-types	oidc-sha256-pairwise-sub-mapper
98042dd6-c626-429a-ad97-a018f12c11dc	34ba7b9c-0bc4-4f0a-b7f4-70cf391cfff1	allowed-protocol-mapper-types	oidc-address-mapper
7b52651e-64e8-4ec5-9aa0-ded76c394858	34ba7b9c-0bc4-4f0a-b7f4-70cf391cfff1	allowed-protocol-mapper-types	saml-user-property-mapper
9d74232e-18cf-48cc-8df4-1adb201563ef	34ba7b9c-0bc4-4f0a-b7f4-70cf391cfff1	allowed-protocol-mapper-types	oidc-usermodel-attribute-mapper
7d9da45b-341f-43ef-9c1b-f011a6c2b9eb	82049e98-c30c-4eab-bd6b-227c601500e8	kc.user.profile.config	{"attributes":[{"name":"username","displayName":"${username}","validations":{"length":{"min":3,"max":255},"username-prohibited-characters":{},"up-username-not-idn-homograph":{}},"permissions":{"view":["admin","user"],"edit":["admin","user"]},"multivalued":false},{"name":"email","displayName":"${email}","validations":{"email":{},"length":{"max":255}},"permissions":{"view":["admin","user"],"edit":["admin","user"]},"multivalued":false},{"name":"firstName","displayName":"${firstName}","validations":{"length":{"max":255},"person-name-prohibited-characters":{}},"permissions":{"view":["admin","user"],"edit":["admin","user"]},"multivalued":false},{"name":"lastName","displayName":"${lastName}","validations":{"length":{"max":255},"person-name-prohibited-characters":{}},"permissions":{"view":["admin","user"],"edit":["admin","user"]},"multivalued":false}],"groups":[{"name":"user-metadata","displayHeader":"User metadata","displayDescription":"Attributes, which refer to user metadata"}]}
69c326b5-56fd-42c5-b011-423574b26b84	1e8e0437-6e58-4e41-b4fa-d3f760ba2458	privateKey	MIIEowIBAAKCAQEAuwLil5FLRTB8PZ+zSTYToNmIOetCRf8EYkfvkuhnLbhOwgo8fCLHuHQ5PJ2vuR9RHDhnZqOcvA0Y/YwtOBE/huacLOuaC/KQ2x9PVlZR9KhPOaUDZtOwhCt4dFBzT+WHqan9vbpgFMnv2pQQcHJ4FA6sNb2cQozev2Znfeg1Oys51Q1Gls/L3EPqFraArFdzK3jAz20zMmgyoGOG8GTOev+bGMbIEYznmXS7SFmICLEYgk3jABjwgE3h7wetAQAE9/NyRDk0Yhup8sy56eQgjmIr6V/6/TbrQvfMEtPuR8BFMxMmQOnKppQ3Xg1MNJvY6r02aFDyc22EbPa3gWK0SQIDAQABAoIBABqFpbomLn0hC7W3pPcglrcMss+4CD2iFa6UkisND7kBtxW42uwmCJolzYpFuMqx4NB2RV/em1dmKiGnVDBr7c0Ffqe2QDIqlBWngpp1gokGp7m+p9VY92M/QQWJ9Vii7lMHG0fhm0fHfd9LeWthKJt7mzvB6NvdW6fcb5gBKy95SyI0r2bz4d1/tzZOH4+DKGaF7VBNiudGtWCrzfuGi85JA0rnvoBtxqsoyX/M5ciAM+igrYeVDWWR2iyKW/y5qT8xN9KD5e5w5sbdIuh5f6QYNEyt6YPIQUe6Znq2kwsjKHDf28dxHdLHA+Kee3qSwJqj2IcSs4OeYBowbH8iavsCgYEA7ecFoiykXsymB05FFktAZZDyKNocKcFa2ugsMl+zTsWtIwDR3qGfc3KiU4GKfmUWxDF7pnpeRToWr9Aphs7kUGf10PVIRA9ieE3nk5s36qp/cx5I0NPzpckVDjxNtVIfZ3NP7eb5E8EVjHULZS+wm7z29cHUHCsGMktehZ/MEaMCgYEAyTzLZwDgxngrG5AKBa19IWp7QBNQpUWnnxndjJQ0TleAVxJStnM2GJ0bdZLEzZWASi863lvgBKYEhf4ToySbpOT55cm8f385WKbW3GtxsR2iV7T0JufeLCaQCZekSuXlS4kGq0ti+FB9ndLPsEMJQZMtDYTIQvkfpbybVGQlOSMCgYALRIXfyH2Af/DdJ7fd9nJ2XQcbKDltQNsswxJU0HONp48ruT43bTBAJ43IproIgoExiaZxnBoa7UVlzlYpLGNj64iY/QUPNCtVx1rQgumDE9A1g5mvSlqf34Y/xltn2xvhqnR+sBHb/aknneOMR1X2Cl0oBqYlL2R7vVYcvhTMQQKBgFgQitLgx30TSYoeVsR1T7U37Af2a+xntxshLps+oEVIdZmlIx1E/f93gXTKbeWp7yKH2LNGcZqXLJOWxPE4LATVT6cHyb3h5y6slIqRWoev+EN5pP8BqXCYz5/jBLriMJ4FsS11KDKSxuYyQ0FXlM1loumvmhP1Aax+BVvBF6kRAoGBANjK/qybcCXtvpaSdpfy9lrSN+oj7LQ8cdLkLoCn2f8eF5Zbg2BA1r6u1S7pvfmjm8DyvcDmICBnVJbw97F09zH4O5pB9fD8fCwvM0HA80DiSC/6Cmug0QP9kFvZXIwxcPYXUIIZwN3pXskFB+ZwEqIamtlVOIa2L6bsets4U7AW
9fed5894-9409-40ec-acb6-b1ef9b3ebba3	1e8e0437-6e58-4e41-b4fa-d3f760ba2458	keyUse	SIG
adb66122-de0a-4861-be51-8c64db6f8c71	1e8e0437-6e58-4e41-b4fa-d3f760ba2458	priority	100
3fb23166-0568-4c49-bc0f-f2f58b907f47	1e8e0437-6e58-4e41-b4fa-d3f760ba2458	certificate	MIICmzCCAYMCBgGWZIlSLzANBgkqhkiG9w0BAQsFADARMQ8wDQYDVQQDDAZtYXN0ZXIwHhcNMjUwNDIzMjEyMjIzWhcNMzUwNDIzMjEyNDAzWjARMQ8wDQYDVQQDDAZtYXN0ZXIwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQC7AuKXkUtFMHw9n7NJNhOg2Yg560JF/wRiR++S6GctuE7CCjx8Ise4dDk8na+5H1EcOGdmo5y8DRj9jC04ET+G5pws65oL8pDbH09WVlH0qE85pQNm07CEK3h0UHNP5Yepqf29umAUye/alBBwcngUDqw1vZxCjN6/Zmd96DU7KznVDUaWz8vcQ+oWtoCsV3MreMDPbTMyaDKgY4bwZM56/5sYxsgRjOeZdLtIWYgIsRiCTeMAGPCATeHvB60BAAT383JEOTRiG6nyzLnp5CCOYivpX/r9NutC98wS0+5HwEUzEyZA6cqmlDdeDUw0m9jqvTZoUPJzbYRs9reBYrRJAgMBAAEwDQYJKoZIhvcNAQELBQADggEBAGZy514oVr0kVdCjBncVLSsDDp6w1NCewzu61tRUz9VikpeopgW22/lNH531zibLBBQIL50ydLhPSOZClvfGmKKHuJ7xisxw32iphIgGvaTLAzI7okF4JsvSiFgYsONr5rRtu17frzoOoTSZr/XBIL4AUMfWSRwOWKJfJMoQ4lPpgpEXfUSiJHToDJ4Y8XGsUdWkzr5rn7TRRk9Vl0bIvf0Ur+ZsaYbS9jL+nEV3gtyTUlNXoCmTm0gM5uFca/l3ktYCS/4qYrFFUUL+3EcgAry/11dFhSmpgM2+VY8zCVVJ4f5XqIPFxaFhGQ5XSvpw+LVsPnptKsjz4rKavRHI//E=
2887b1c4-f3cc-4822-ba00-994fc752019c	664de519-2838-4744-8250-c32c3f653e8f	priority	100
92af5751-146e-4f11-af95-a66ac64a0228	664de519-2838-4744-8250-c32c3f653e8f	algorithm	HS512
c5c901d9-9742-46d0-a98b-351d0e9b25e0	664de519-2838-4744-8250-c32c3f653e8f	secret	pZ9f0oRWNy0c1vV6HOlvzk_kDottZcTgZ7d7SxUhfuEalXhSpQ_DhFrA6wwg6GtK8_LPoo22vJbA61VCQ_zh4uAldomt6dgsgbWO8v-XydMlnZZll1GodVuRl8WUGtBRZK4ypZW57CNxVFvGt1JMZRWY4oxMZ5l3Zy0k7fyJv9E
c9e14fb5-f51d-4419-8943-eab2a6a81f9d	664de519-2838-4744-8250-c32c3f653e8f	kid	37a17843-f58c-44ec-a4c9-54793c60ea9d
f950440f-1452-4c2d-aad0-e2cc506d367d	a55438b5-63d6-49a1-b7e5-0b92a902ebac	priority	100
3e700965-adbc-4441-9b49-ec0603ce1528	a55438b5-63d6-49a1-b7e5-0b92a902ebac	secret	tYIrLNkZkxPPRqqF8HErhA
cb3ff008-2786-4a0b-874d-3a3fb71b48ca	a55438b5-63d6-49a1-b7e5-0b92a902ebac	kid	578daf3c-ea76-40fe-83df-54fc1e1820a5
7da40241-1122-46a7-9b8a-eebc4cd1c61b	dc82f638-4de3-46be-8205-fb018fb58813	certificate	MIICmzCCAYMCBgGWZIlTJTANBgkqhkiG9w0BAQsFADARMQ8wDQYDVQQDDAZtYXN0ZXIwHhcNMjUwNDIzMjEyMjIzWhcNMzUwNDIzMjEyNDAzWjARMQ8wDQYDVQQDDAZtYXN0ZXIwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCUC506QjHvwovnGv2YkWL930lSJBupG5Hggn2ftDnD9znYKdOfL1A51z8kxZHEn9i2jdWCfhmIE3LHA9HLmm4z9A5B4EK//5Z/DjR7+1d6TDV60exQ5vXo8ekG9S/A6UFhTRty4HZv1JvrdBl0seaTW6yyPExq9UB8MOjn09JEBw3W3JULjfJai+TxEgJ/xfdPyfFly+rzpUUJt9GTAtGbTf5y2O+m499adpziIRCRUyEtwAVleuCoYUH0mN7AiY0J025aS45+0lbEd/Z+yYRaG4wOxcQCWgdiOouYTaiYzeZzeTh3UFz0WMWyVs9VuBkAt/AxbnAMx48T89AZj+Q1AgMBAAEwDQYJKoZIhvcNAQELBQADggEBAFUySCSp7VaNK8hgY42zUlM5tXMiWBsxrWlDOHUh0BiSlEaVMXsUEhkHuXW9jB68LfClvWQtsa+luImq9gjqhX8zMGY0StiqsxZTW6HUO4xaVfKXn8Y2KwoeEWZF88UsauiYTCj03wQr38S2UYgc/fPN4l7OKqR5F14hJtTWHudo+1j4vZpTM326itBs/1rsVPL7zRxHWphD8u+u9c8db35mhTON5ZYAdCEPngQHl/pBGOZSbIP9/OTgmchzN5Pw0+8nXIgK/8zcbGaygKhI14GbNQpke1Hp7sbKX/H6hCSqi1wVDM/g2kXmBJmpMtNxcxOZkgLSQQcyL7wIoCBQ8YY=
206a5d33-2fad-483d-b614-01639d2fd61b	dc82f638-4de3-46be-8205-fb018fb58813	privateKey	MIIEpAIBAAKCAQEAlAudOkIx78KL5xr9mJFi/d9JUiQbqRuR4IJ9n7Q5w/c52CnTny9QOdc/JMWRxJ/Yto3Vgn4ZiBNyxwPRy5puM/QOQeBCv/+Wfw40e/tXekw1etHsUOb16PHpBvUvwOlBYU0bcuB2b9Sb63QZdLHmk1ussjxMavVAfDDo59PSRAcN1tyVC43yWovk8RICf8X3T8nxZcvq86VFCbfRkwLRm03+ctjvpuPfWnac4iEQkVMhLcAFZXrgqGFB9JjewImNCdNuWkuOftJWxHf2fsmEWhuMDsXEAloHYjqLmE2omM3mc3k4d1Bc9FjFslbPVbgZALfwMW5wDMePE/PQGY/kNQIDAQABAoIBAC/M+jSr1+TSu4pguB4RX1WaSVnIx9eXy8eiSn6M2hPwmNA3XRlqVOyAF+a/FfxXIkl7TUOHhKXfpbElhhFH6SpENHBHwTWuCEE47HBVrJ6TybbA3h0XHbJbnYJ+Gy+eANqyY1j5qbQdAUUKZQe9QzY0JPQdrQzh6DLRNfpYde05niHU0Pp/uuyPeGZjyOi+91UhB6RD9fFdAt0hDS1hguwQ0ZZwgqU6ows3XE8kprFSjphyRPBQusrBW0eP/zn9FSK2ed+2sDQnfzN20xb3Nd7NwiYR6E9GbB3ErGafQttxrDAMFjpueH64ckd3BVft7VS2GwDY92u1crc0O0E1ZrcCgYEAw73Bx4BEbZzWUOA3KGNe3kHj3qev4aQMn5k/oY09ZlJ0aC+XicHl6+p9QMof5ebsd4zaZrO0aQkhG8hRzsvNhc2XQFFhoP9y3KVtMiwZJcfbpn/en8Fj5Y3wcGpoWO0SoC7HY7/i71KphN2aMFAqZpjlmG21qUwG5nHE4qBYV88CgYEAwZ7503ZWQjdz718JO27btayMNLXTwqR7h5X2Vf4zmqdFo3Vh5GsuA1YBv9kPH6s6CkLnMB+luo+A0x1yuHadcWdktQWrtvYsSsayLbSNXBjZvFFzSuFoljKexzKFcmIzD7WuQ8UeuC7q4NiwcNgcLyPz25dSi4A0iM7QQtysQLsCgYEArGTb3lFh8whG2UQgUDn0u9tcTiVyClDprUbTe/1hLlVAoOIJnRJg5b7IQd/C6EDR0xkC4aE0K64Wstli87cbc9T2eXczfRB+oUYxPhcIrvWuxp2n/RveV7cDJH8TF/70gI+ynG/L9uve0V47BoIUVfgjfnRhopGLIjrhJJgENpECgYBirRq1YoHjjU9QlyalW4ItvJpX4rLhZ+Js30iD2uGB1+3tuE0DlHwGxzZtB7plcWZLBH2+UOKtiaUl8sQcrpbezMrOVu2EeUFpeookMPg6D5AoIXqYlvguU+4Hpv/Lo7BSOU1uzG8zfvXFUanaWMlEXRZSu3QeFGCxuHT7HmvLMQKBgQCA+xwX1G6JGBXr8LXtbGKHu5MN/CtxfV6fQApReQz8GpQzJlZ1cv0iBAAYuvmD0pB2YKnnZ3Vw9GgCDftuLoWYKWHofhwFf98VklEW14JoPMVVtDh/xW3UWvzgvEcQRaXozjyukRts/L+Ka3v+tfV5QofidBQOt9VsZV2byu8ptA==
8b8f1561-23fb-42c9-a678-c80cda6fe7f5	dc82f638-4de3-46be-8205-fb018fb58813	priority	100
04381b84-3564-4211-98cb-598d04ae80de	dc82f638-4de3-46be-8205-fb018fb58813	algorithm	RSA-OAEP
20a3f85b-5a4c-47ae-985e-7ae519998409	dc82f638-4de3-46be-8205-fb018fb58813	keyUse	ENC
a9f1d2f9-9d26-4242-ae33-2e11c0777e60	693db534-8c74-45da-977c-7e75b0d67205	privateKey	MIIEowIBAAKCAQEAjY3lN27V/9QpDXxe344ow/9pki+Er1DotJ4v4hSMda3aTfO1W3vhCQwT78rRwPKxKqYjUMHe1U6VNSbdd2D+eBbVtDGYhRoNnSOo0w4iK0uYwsYprqP5ekHiLwID0RUr/ldyZ87Fd0CNYnSM5abHtrLaFwxBDZ4mTzxJXJQPZyN9eiuWPGZ/QxGQDuv1o4cJPECCTztCALvPtJJPCTHhyM7nj1XZOfWGq4ESLvmg8GgQRq8g8VGLV2u3OcoQdrILv1otyue+N6d/nar3RFONXIqOx5WT0Xv2bsP3IaBziFWTJ+BWFtHFrFkf7zgSaNKI7ORoMFQHFQYfFzwk0eLWtwIDAQABAoIBACecVE79ue57zP0UF2xRWID4IO+iUmvIpUWdmmKWQ3POsW7syowODzu74HxScf2fLAp2MR2xEoz4D3KvLHvybWLnaOeilT5xuuuFBgIBS3h4EAg48YMIKZCXjRaAZtcEKKpkiAQabVFgg+/QE3lUAUDTrYmTiUSpQ6kagqDgYasHbdRzkn62HZisD3DwbCqROZJq8XzMxZu5OgA7xO9ihvrA5vF6BB7UbFqcVe26J2jkzV9OiiG1g3ooIfdl8GNTRkVRK431hlQa6cTa1SlAojf/NqLMXEMZA4NMcnveeOKu+fe4lnJoOLfJIeoA7/PMhaMT64pjGxmzbPiAaeziDtkCgYEAvmxSPJa17w2tZdBQUz7ObuANuYPQy9rsP5jF3bocv6MzxMPwBBUEeMSJjbOo1NSLUVUQ8s67gmRBKdIUkJ0tnPprl9ic4YKFxdYbfsMPpfc06EEunQTWqchC4KlRa9hSvKxajseIEzdriOL24eT+kHpMnERr1r9XLElEnOJcK/sCgYEAvk1Nb8hiUShCpaivzdlU8geZAoGKHZU1X0ItDX9jqddvebMxYQv5ifx0YEjIHqK7+skxY5wbXy5jGZAxPOIUP0yf184C0/ljsv+gasggKVSz9vyDF1wvgIv7apWcJQ5HwoZgC04+3Ogtp07/CXJNbKohGxSeB7OqBoCOnuiWp3UCgYAhbcCuFEMSGVOUaRKA2qjAvCKMq8BO2TraG0a5GwALAGqJI2CSRuJHFTGUp7J+0mU2vPlBttsiE9mArvmfPYiRY0DaIlPAHvq+9UxUX4fyucgW8cJuHDhXC+n5qJ+7j/lOV+952ztfLD57hGSU2W97C41BbZ2E0IhMJPyfkfleWwKBgQCbBAtFOFzmMT6BBTUYIKYydX/ta6ofSPDuBNEAQrnjmwtCfnaF0CRYg54k5ISnd/ZaZlNmCb6+POIa67tAFgVaPseiGj1xgJ/3KoX5FcYlBMuIMM90UvKa6uxFOVO1V7mauq036GgKuNKfjBjyD/sDvl4zWgsUyQSCQWxsA5jdFQKBgAGs3iprb4ZC8h0TzX7rnMGulqhCKLkGqFERO1bqmyU4N9s/uxloV8SrOACYoKB1nz4bZH/O7Bg65rvZSwCHR+gRev8IMK8TeYcy68tTQAVnKETGLGFMzQChuYumsQjSo+pQXUyawoZl0EsoCAVR8QSeffOUfy5wk8C/d1jtyd3H
de49b5ab-41b7-40b7-b16e-e963f4308fe6	693db534-8c74-45da-977c-7e75b0d67205	certificate	MIICqTCCAZECBgGWZK9lNjANBgkqhkiG9w0BAQsFADAYMRYwFAYDVQQDDA1qb2RyaXZlLXJlYWxtMB4XDTI1MDQyMzIyMDM1OFoXDTM1MDQyMzIyMDUzOFowGDEWMBQGA1UEAwwNam9kcml2ZS1yZWFsbTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAI2N5Tdu1f/UKQ18Xt+OKMP/aZIvhK9Q6LSeL+IUjHWt2k3ztVt74QkME+/K0cDysSqmI1DB3tVOlTUm3Xdg/ngW1bQxmIUaDZ0jqNMOIitLmMLGKa6j+XpB4i8CA9EVK/5XcmfOxXdAjWJ0jOWmx7ay2hcMQQ2eJk88SVyUD2cjfXorljxmf0MRkA7r9aOHCTxAgk87QgC7z7SSTwkx4cjO549V2Tn1hquBEi75oPBoEEavIPFRi1drtznKEHayC79aLcrnvjenf52q90RTjVyKjseVk9F79m7D9yGgc4hVkyfgVhbRxaxZH+84EmjSiOzkaDBUBxUGHxc8JNHi1rcCAwEAATANBgkqhkiG9w0BAQsFAAOCAQEARoGlcPCcjZhpXsRiiR77swkvG362DWJOHny8cwgnCKYy0av+hu0QtwgnpASThxUsJlNXn5jWExV5VcZGQ1r6RA9G5iUFU9Uq9RB+HtYKHuYQFPDIFcp7sFIy6WDsRwjDX6BWFwCBBzTUJRw2wFgMVs0G3kWXQO+SVzovrr4XBLc7vlj/jr+YSzGidktLly4XvnzHJiqZQ9hs6OIjNSQTkwxevyk6QANWRdIRCiw24LI/emi64AOSR7KLHxxoLy8YRhWvfUxdiUaL/dueK9KKDX4at6iJItmM9pOfqqg9+QJ2Zql0OqDry6E+3QC5alCUXC7jvmMJsXZZ7jOKqaTI5A==
7296cb82-409d-45c6-a1b9-c47f8aa29ec5	693db534-8c74-45da-977c-7e75b0d67205	priority	100
15cc3544-2988-4f32-a17f-d921cd4c0146	693db534-8c74-45da-977c-7e75b0d67205	keyUse	SIG
b3000e5a-e6be-4a6b-9447-1cf044f7ff68	bd0818c6-225c-4d21-ad3a-a267e57c0f2e	priority	100
583c3eec-302d-4ae5-97f9-d5fe4525fa1a	bd0818c6-225c-4d21-ad3a-a267e57c0f2e	certificate	MIICqTCCAZECBgGWZK9mhzANBgkqhkiG9w0BAQsFADAYMRYwFAYDVQQDDA1qb2RyaXZlLXJlYWxtMB4XDTI1MDQyMzIyMDM1OFoXDTM1MDQyMzIyMDUzOFowGDEWMBQGA1UEAwwNam9kcml2ZS1yZWFsbTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAONHrOk5MjkDN/rch32EOpn02zdXxkuAqi276ZUxwZuQTvpfzJFQBdwgKvHT52dyl+s8wbbazbRUEdJ45PyfHRx+o7Acuz0qtTYImMC35hpBGAPBcHdwhGCeKQg/VwhSQogp2Jw7KtmdmLH8HOD9lK/17ie1MP30X+tFceRNKK+MbA8gdqcj/3ND9x8yEhRPoa8DerD68cWpKQpnDBilCAQ/qYjigL/vNiYaC8ssT54d6nr7STFBgVfOnOemtrt20IQ1O+HqQUGL4b8Yv3oDL6HyOsFtu9Pyw6NzLr9WfpsOjcjTJhMFE9h7OsnNeR/zeNczefWCaSkji5tPNfmd+GkCAwEAATANBgkqhkiG9w0BAQsFAAOCAQEAkl7omc+/RdxNdkAMgFCA4qHAtcB4SwyNmZKEareC0Gltuno9ivJ2MgB/fjwOwybPACgyZaKVt7Et4TEYPLadfS0gLHzKHFDRVqUIDxWzpJNotnLWLhyx5J/IvZ9mowbY3D/OFPXgbFMSs+KjfXT/x9hyFOVBjIV83qa5aFR2Tq6+8+m93hyekPkknVqrC1cr+jgHaeDPKkGZI8xhiVTAO9qxoe/8rQlJBDNlrmfA0md8M3aOZqqgfPCvl5Jt8wJ7Vy/L8NPwP5vCNaCBP1vXG9CFGchfctU8CPbWl791hLJbVkObZdB3rv6Qau5h5irnsmuJR7/EUdXZTWvgp3aXdQ==
afb6a1ea-b11d-43e4-a737-3712a5b71976	bd0818c6-225c-4d21-ad3a-a267e57c0f2e	algorithm	RSA-OAEP
b24d65a5-9fc4-45ab-9aa7-8d55e479f289	bd0818c6-225c-4d21-ad3a-a267e57c0f2e	privateKey	MIIEpAIBAAKCAQEA40es6TkyOQM3+tyHfYQ6mfTbN1fGS4CqLbvplTHBm5BO+l/MkVAF3CAq8dPnZ3KX6zzBttrNtFQR0njk/J8dHH6jsBy7PSq1NgiYwLfmGkEYA8Fwd3CEYJ4pCD9XCFJCiCnYnDsq2Z2Ysfwc4P2Ur/XuJ7Uw/fRf60Vx5E0or4xsDyB2pyP/c0P3HzISFE+hrwN6sPrxxakpCmcMGKUIBD+piOKAv+82JhoLyyxPnh3qevtJMUGBV86c56a2u3bQhDU74epBQYvhvxi/egMvofI6wW270/LDo3Muv1Z+mw6NyNMmEwUT2Hs6yc15H/N41zN59YJpKSOLm081+Z34aQIDAQABAoIBAA+YUhMlNW7fy4orMmFQjshUWqvhGhZBb3vooFIYSkt7Mw4fP21Ln0FP1D6IbqEEpguyPS0HtbRtTdVCLh0u6kuTnXX2vLrJWMqKu7eCjteHlqcgUXAioHS0OYbLONS1d0L6UxaCQVISDCGPVehywe5yeqtPFZvhVikRuGtEx5lG8Vnp0ds18HmRwVa3Lpe3bSLKAAe3viD0D1+o889koAKKKaGC0xE/gfuPMFOE4VPGnIWXG7RFxeZAuIY7W0lmkWg6KPZsGsKrYVu1D5/sROGpzZu3xtX3Ev2HNvQVIeAA0AzKiBNDMW1hJUh/f7WO2nygZDT7G7ip2ndfa7TEJ2ECgYEA/PA4QUwzGf5ifjpfMWxAZ6VnRKu6DAirYgIh+Cg5qpx3HWFFSngkHS1nJPEBjLcuZCtV5Hx3eHKEmQ7FUIItK4D6cb6FY9j7yWmyYwxmlNnuJi+IWZSMR0JK1Qvf8pA7aqifTEUqxecJMgO1h396qrtoH5H/CpYiF+qUMH9p5MkCgYEA5gfytBckzfHTVn8bDzrOPoP4WIG56FMO4Uyi9F3sxptbmvBq3Ir6lYUaYC0ba7bmDr4qSk4pq/ysON1Oaor1s0jrolG62+1MNqSbKiPTBmwuDLLHW6xCfP/561BqvnHXHK/dKAroTh/AqEsVZ+ZG3LKXJu7FRh9LXbejud91ZqECgYBf80yT0LEt8icw1TnZSMRhzQgQr8hC2mMRKdfzCGdfxj0VYxni9xJUEy4mgFpjI/YwP5Q0udakVWThnw3QzaEO+L2IuoplZyGUtUjbdA/R9Z321F84BDb2rTov+XI51OcFPfQ3/aENM+h0FZ4eSYiiDbehCkQiovNJ+U2/3AWyqQKBgQCSIDptbMZUjWpTaCsoYZLi47K/hqkyjJDHToe19HSVNdvbcR5xO1gz43n93qmso2NBukvjKAQQ3VIKTYui4uNyIMCPPJwXWYcwryBihJ9pVQ9rwaYi9irGfVWlxlAI6whbkNxMs6Ee/8zDG+9dMUuSp2CdutBW20plHmNDkFBkAQKBgQDH8QYlnMFitwDWi99q6ovjMBodxAyMLME0A5GXgnv4Y9SHEbFCcuBlMnf76x7gYSwPHYPa9LcQuZrTCuJOn6s8wN7+k8bq46BhYxQCnkFiept5G8YTqjSYR7Jxg8aqN61ZlwEpL+0+aEtKoAxseAPe0VT/uSTlPYa2PmjV28NWlA==
79138697-d152-4a94-8618-8354a73d9eac	bd0818c6-225c-4d21-ad3a-a267e57c0f2e	keyUse	ENC
e1aff188-a54a-4d49-99d5-a196203858ec	45190737-c32b-4720-adf7-290fa187382e	kid	5a54571e-e37e-49fe-90ab-632e290e157b
fd50a374-c245-42e7-9d39-b61cc7ad7a9e	45190737-c32b-4720-adf7-290fa187382e	priority	100
3c96f2d5-7aa9-48ac-9a55-31992a9b9497	45190737-c32b-4720-adf7-290fa187382e	secret	WSLeBdejaBn2cwoRaQHs2g
0874c1d1-4f4c-42b5-820d-eb86f116e8e2	a7555771-ecee-46bf-8b6c-d3650aa99a28	secret	zA4SEOSDbO1TRZ_TuJ5Rdu0GQnetYwiDREIL0-ObS_14WVjgovvuRSDYq73oi-8yhd7n84kTvZiCMkETHtEShox0sNIEy-Xdj8Ty33hj1P_JhN1DcuaLAIWKffwHQk-bbZcdEo1aEaxI0j8xG9B_d-Oetb01yf0aSwecu0E4Yio
09d1107d-ceb6-4e32-8f4b-88021430c298	a7555771-ecee-46bf-8b6c-d3650aa99a28	priority	100
c37ea299-3ae3-42d9-8b2f-ebe25cc9f80e	a7555771-ecee-46bf-8b6c-d3650aa99a28	kid	2a5e368c-e285-4e30-9332-368466ec889f
4d233dbe-0c57-4776-8ad4-795b9096dbac	a7555771-ecee-46bf-8b6c-d3650aa99a28	algorithm	HS512
f1e3177d-1b68-4f10-b097-7202dc118a6e	9da37549-03ce-4453-831a-6cbd340ba269	allow-default-scopes	true
7286a485-c5da-4a95-b0de-3f089ebc0345	105d953d-1399-43b1-9551-9d0f47ce466d	allowed-protocol-mapper-types	saml-user-attribute-mapper
d917571e-cb3d-490e-8ca9-4e7d7ea86389	105d953d-1399-43b1-9551-9d0f47ce466d	allowed-protocol-mapper-types	oidc-sha256-pairwise-sub-mapper
1d60b722-34e2-45d5-9832-4aaf98f3199b	105d953d-1399-43b1-9551-9d0f47ce466d	allowed-protocol-mapper-types	oidc-full-name-mapper
87c3fdd4-91f5-49fb-85f2-9c439148d902	105d953d-1399-43b1-9551-9d0f47ce466d	allowed-protocol-mapper-types	oidc-usermodel-attribute-mapper
149c0568-c468-4277-9c71-60b69a547269	105d953d-1399-43b1-9551-9d0f47ce466d	allowed-protocol-mapper-types	saml-role-list-mapper
cbd9af3f-288b-4ed6-b31e-841d6fb1643c	105d953d-1399-43b1-9551-9d0f47ce466d	allowed-protocol-mapper-types	saml-user-property-mapper
2cdc8720-5ace-4d55-b80a-95989202d6e8	105d953d-1399-43b1-9551-9d0f47ce466d	allowed-protocol-mapper-types	oidc-address-mapper
3d830fb3-3f43-4926-9197-aa1e23f43886	105d953d-1399-43b1-9551-9d0f47ce466d	allowed-protocol-mapper-types	oidc-usermodel-property-mapper
e78dc5a3-566a-4f78-bff6-40d02c163fa7	74c6dbee-68d4-4105-878c-b1e03d76f883	allow-default-scopes	true
876835b0-700e-4b7b-bad1-a39d55586cef	bcc2ac9a-b7bc-487a-b146-4b11bfca636c	client-uris-must-match	true
537c23eb-b3b7-450e-ab14-0e001eb847b7	bcc2ac9a-b7bc-487a-b146-4b11bfca636c	host-sending-registration-request-must-match	true
d86f76a1-6c01-4f54-9008-1c9b947ab298	4e2a261b-21d6-43b4-ad2e-5b369c8caa04	allowed-protocol-mapper-types	oidc-usermodel-property-mapper
13d21723-3758-4968-b5f1-569fbc076451	4e2a261b-21d6-43b4-ad2e-5b369c8caa04	allowed-protocol-mapper-types	saml-role-list-mapper
0ceb81ae-9ee9-4bee-b107-18279e47cffd	4e2a261b-21d6-43b4-ad2e-5b369c8caa04	allowed-protocol-mapper-types	oidc-address-mapper
0adc6327-f0d2-47db-94ca-6bf26a24cc67	4e2a261b-21d6-43b4-ad2e-5b369c8caa04	allowed-protocol-mapper-types	oidc-sha256-pairwise-sub-mapper
a30bfdbf-cf74-49f8-bfe3-52107d392079	4e2a261b-21d6-43b4-ad2e-5b369c8caa04	allowed-protocol-mapper-types	oidc-usermodel-attribute-mapper
85e78f37-1060-4ba4-ab0d-93cff086a303	4e2a261b-21d6-43b4-ad2e-5b369c8caa04	allowed-protocol-mapper-types	saml-user-property-mapper
32de292d-712d-44c9-9f7f-fa1859d417c7	4e2a261b-21d6-43b4-ad2e-5b369c8caa04	allowed-protocol-mapper-types	saml-user-attribute-mapper
5484895f-4727-4e6b-8f82-f4ff455868b9	4e2a261b-21d6-43b4-ad2e-5b369c8caa04	allowed-protocol-mapper-types	oidc-full-name-mapper
ef5483b7-0548-4968-be1a-b82c25f50db0	a2977679-fa92-45cd-9f17-419dad8181a1	max-clients	200
\.


COPY keycloak.composite_role (composite, child_role) FROM stdin;
6af59e01-d323-4062-93d1-bb37cc8b53e9	737455a9-1291-4832-b01b-78f0cf479900
6af59e01-d323-4062-93d1-bb37cc8b53e9	3245476b-5de1-4649-a959-8a5c4a33dc63
6af59e01-d323-4062-93d1-bb37cc8b53e9	b04a3ad5-3997-4382-b47a-c47f4ece8d90
6af59e01-d323-4062-93d1-bb37cc8b53e9	eec006c0-25ef-40bf-b3fb-b278367415a2
6af59e01-d323-4062-93d1-bb37cc8b53e9	8e4732cf-a386-4586-92c7-39b18892491e
6af59e01-d323-4062-93d1-bb37cc8b53e9	b7a0f0f1-d3e5-4feb-8d0c-c394e36d2121
6af59e01-d323-4062-93d1-bb37cc8b53e9	2cae5fa0-f061-4cd1-a145-45f41a6aaefd
6af59e01-d323-4062-93d1-bb37cc8b53e9	dbf4f987-8c14-4dc2-8a55-c6f25c3d6fb0
6af59e01-d323-4062-93d1-bb37cc8b53e9	51d60a47-e5c6-4176-8822-b21c8b8f1209
6af59e01-d323-4062-93d1-bb37cc8b53e9	20e54757-3b48-4c5b-81ed-772ef520935a
6af59e01-d323-4062-93d1-bb37cc8b53e9	29a68cbc-b626-4da2-a99e-65e1218445c9
6af59e01-d323-4062-93d1-bb37cc8b53e9	94fc47ab-d824-4c1b-adf2-b3fce7d0c0d5
6af59e01-d323-4062-93d1-bb37cc8b53e9	d9db935b-547c-4dc7-ac7e-8edd8f4c2924
6af59e01-d323-4062-93d1-bb37cc8b53e9	45a5b1a1-1b5c-4c66-b314-f6f8673416ec
6af59e01-d323-4062-93d1-bb37cc8b53e9	70b597fc-aa49-4f51-bd6e-232557c2276a
6af59e01-d323-4062-93d1-bb37cc8b53e9	15cbc476-ac24-419e-b19e-be7a69e098d7
6af59e01-d323-4062-93d1-bb37cc8b53e9	c7884384-d02b-4e5f-b6b8-7ceefd9fd9b3
6af59e01-d323-4062-93d1-bb37cc8b53e9	e5431fa5-1b7d-4a8e-90d2-db427cfd4e46
8e4732cf-a386-4586-92c7-39b18892491e	15cbc476-ac24-419e-b19e-be7a69e098d7
ee605334-ad03-4dd9-b1e1-abdda1eec33a	c43e62d2-1528-4b99-b247-7074e483ad3d
eec006c0-25ef-40bf-b3fb-b278367415a2	70b597fc-aa49-4f51-bd6e-232557c2276a
eec006c0-25ef-40bf-b3fb-b278367415a2	e5431fa5-1b7d-4a8e-90d2-db427cfd4e46
ee605334-ad03-4dd9-b1e1-abdda1eec33a	5c738e1f-c579-4144-a96c-18a066451b74
5c738e1f-c579-4144-a96c-18a066451b74	84f36e7a-9548-4cd3-b960-c191230b6910
09ea5cda-6c63-4317-adb5-834ae9627be9	02698cb9-1489-4b6e-883c-2b8a5c088b19
6af59e01-d323-4062-93d1-bb37cc8b53e9	dc70d154-f36e-407a-8fdc-4e30d8fbc9eb
ee605334-ad03-4dd9-b1e1-abdda1eec33a	a7b80c13-90d7-476a-acb9-55eed1cb5cb2
ee605334-ad03-4dd9-b1e1-abdda1eec33a	8ce73c59-de91-407f-83a8-9e2d01772cb5
6af59e01-d323-4062-93d1-bb37cc8b53e9	0e692e74-336d-4401-b1ef-ac68fd98df20
6af59e01-d323-4062-93d1-bb37cc8b53e9	e46f8a39-b526-4473-9ad1-a4f7bb61b7c9
6af59e01-d323-4062-93d1-bb37cc8b53e9	ffeb13e4-61f3-476c-998d-1563f4933cca
6af59e01-d323-4062-93d1-bb37cc8b53e9	d6ab2d37-8ba7-48c8-93e2-5c15ed1ddbe9
6af59e01-d323-4062-93d1-bb37cc8b53e9	759c4746-5a2d-4edb-a9bf-02dfbe154e3e
6af59e01-d323-4062-93d1-bb37cc8b53e9	e4b5d537-5e21-40cc-9e38-ceb8c4749264
6af59e01-d323-4062-93d1-bb37cc8b53e9	e7863c94-48b0-427a-8ec7-1ed566228dd9
6af59e01-d323-4062-93d1-bb37cc8b53e9	786f78d0-b138-4186-86c3-a837190d25ab
6af59e01-d323-4062-93d1-bb37cc8b53e9	ca4961fc-eb26-45a5-8c72-4fdfa138a4e8
6af59e01-d323-4062-93d1-bb37cc8b53e9	3742aff0-647b-4d84-89b0-db0896817781
6af59e01-d323-4062-93d1-bb37cc8b53e9	035ca821-ee04-4fee-9b3d-e699e4daddd5
6af59e01-d323-4062-93d1-bb37cc8b53e9	9c690433-c067-40ef-b853-3edf1728ee05
6af59e01-d323-4062-93d1-bb37cc8b53e9	e06657c4-7e07-4b01-9e6b-28a41f2e53d8
6af59e01-d323-4062-93d1-bb37cc8b53e9	e15c445a-3e00-4b1b-ae3b-377a965e7373
6af59e01-d323-4062-93d1-bb37cc8b53e9	600ae185-6918-4da3-af48-866c6f64c90c
6af59e01-d323-4062-93d1-bb37cc8b53e9	7b3034e5-5498-46f0-ab68-94619e4252c2
6af59e01-d323-4062-93d1-bb37cc8b53e9	088d3afe-494d-47ae-b9e4-974c2a18e050
d6ab2d37-8ba7-48c8-93e2-5c15ed1ddbe9	600ae185-6918-4da3-af48-866c6f64c90c
ffeb13e4-61f3-476c-998d-1563f4933cca	088d3afe-494d-47ae-b9e4-974c2a18e050
ffeb13e4-61f3-476c-998d-1563f4933cca	e15c445a-3e00-4b1b-ae3b-377a965e7373
8a073218-8363-478d-9c03-4535467cca5e	94f54247-a73c-4fb0-af7f-caf7e4460796
8a073218-8363-478d-9c03-4535467cca5e	129a3654-da4b-4d7b-baea-a683e22b7a0f
8a073218-8363-478d-9c03-4535467cca5e	ec9abeba-95ff-4b3c-9e4c-2780ec242b36
8a073218-8363-478d-9c03-4535467cca5e	19e98e43-c1c5-426b-ac5b-9f6852de48ef
8a073218-8363-478d-9c03-4535467cca5e	4f262492-395c-4983-a1e0-874abc3f5fdf
8a073218-8363-478d-9c03-4535467cca5e	1f1e3936-e96d-4a0b-8229-fee110cf39ad
8a073218-8363-478d-9c03-4535467cca5e	10d8aa8c-7242-4a21-b77f-6451c64d1bac
8a073218-8363-478d-9c03-4535467cca5e	34d123d7-f9e0-44ad-82a1-1c49f6388a73
8a073218-8363-478d-9c03-4535467cca5e	0b811a26-9290-408f-9f89-63ab359b23e1
8a073218-8363-478d-9c03-4535467cca5e	d70fbcf3-ecb4-4470-922d-8e18b43bb89d
8a073218-8363-478d-9c03-4535467cca5e	56f75a23-f0eb-4ace-9b8d-42064995acdb
8a073218-8363-478d-9c03-4535467cca5e	a225830e-d025-4c64-9e7c-0afbaa0faaff
8a073218-8363-478d-9c03-4535467cca5e	56f3ca35-2166-4228-b126-49c6563a6587
8a073218-8363-478d-9c03-4535467cca5e	e1a087af-fc00-492e-8651-209db2ec15cc
8a073218-8363-478d-9c03-4535467cca5e	fcc9e5d3-9143-4df0-a031-0d178db71a7b
8a073218-8363-478d-9c03-4535467cca5e	94b50168-6623-4f36-9ed9-4af07b8d2547
8a073218-8363-478d-9c03-4535467cca5e	3d68b3e6-2827-42c2-878e-0dbafad5f9f4
19e98e43-c1c5-426b-ac5b-9f6852de48ef	fcc9e5d3-9143-4df0-a031-0d178db71a7b
da241a9c-0230-44b2-859b-d198a2b0863a	3c69c637-c3a8-4624-ad3b-413be787e258
ec9abeba-95ff-4b3c-9e4c-2780ec242b36	3d68b3e6-2827-42c2-878e-0dbafad5f9f4
ec9abeba-95ff-4b3c-9e4c-2780ec242b36	e1a087af-fc00-492e-8651-209db2ec15cc
da241a9c-0230-44b2-859b-d198a2b0863a	2248cd8f-a233-4393-9257-f2e4b3953667
2248cd8f-a233-4393-9257-f2e4b3953667	0e61f418-c7b2-4d9d-8cc1-d0ca664f76ff
6b62356e-e96d-4605-9bf5-2483707decc8	e4c96ba4-201d-47ac-bf65-33d480b2f1c4
6af59e01-d323-4062-93d1-bb37cc8b53e9	0cdcfbbe-8283-4af3-ae24-b5c0f8d11a1f
8a073218-8363-478d-9c03-4535467cca5e	115cfc4e-fb1a-4168-b34e-701ccaeb3d37
da241a9c-0230-44b2-859b-d198a2b0863a	c6ff9adb-e6c5-450c-af4d-c5eb488114ae
da241a9c-0230-44b2-859b-d198a2b0863a	c62f516b-16f5-4ed8-9f22-eb47d2d5fbdd
da241a9c-0230-44b2-859b-d198a2b0863a	553f1779-7b3c-4b71-87b9-230f6ac03d5d
\.


COPY keycloak.credential (id, salt, type, user_id, created_date, user_label, secret_data, credential_data, priority,
                          version) FROM stdin;
147100d5-c00f-44c4-b1bb-a1360c162cda	\N	password	1d071bc2-3f12-462a-b686-079304d649a0	1745443444145	\N	{"value":"wfUUyigX+U/yRsC5RN3cNJUejdZY5FKwur/ry+nrbnE=","salt":"H1XqSZvU/kTs5I4pS8hilg==","additionalParameters":{}}	{"hashIterations":5,"algorithm":"argon2","additionalParameters":{"hashLength":["32"],"memory":["7168"],"type":["id"],"version":["1.3"],"parallelism":["1"]}}	10	0
57ca41e0-8660-4700-9382-9c6b35579482	\N	password	29849880-ddd4-4000-b100-460f4c505045	1745447362666	\N	{"value":"xeA0M+DvMLel/S84VylOF9/nzcxaP7O4jkMFaRD9zYs=","salt":"3P4BeJs+MwVf28tbhhHHnA==","additionalParameters":{}}	{"hashIterations":5,"algorithm":"argon2","additionalParameters":{"hashLength":["32"],"memory":["7168"],"type":["id"],"version":["1.3"],"parallelism":["1"]}}	10	0
\.


COPY keycloak.default_client_scope (realm_id, scope_id, default_scope) FROM stdin;
fc4359ab-c586-4d33-a1e0-6382887b081c	9f35e4f4-171b-4e4c-bd41-80956867a296	f
fc4359ab-c586-4d33-a1e0-6382887b081c	1ae2dcaa-0525-4bf7-bfc4-0abf95a42d77	t
fc4359ab-c586-4d33-a1e0-6382887b081c	e8d54d8c-62f6-4798-afa7-464b8c3584e0	t
fc4359ab-c586-4d33-a1e0-6382887b081c	8a546be3-675d-42c6-a00f-021792f5948b	t
fc4359ab-c586-4d33-a1e0-6382887b081c	9e96fd62-765d-4a19-be1a-153aa28fa60f	t
fc4359ab-c586-4d33-a1e0-6382887b081c	9c8d4a40-e6f2-43a1-a3d8-3225bf6962a2	f
fc4359ab-c586-4d33-a1e0-6382887b081c	5cdaa0cd-4746-4007-831b-addde8e7ea8a	f
fc4359ab-c586-4d33-a1e0-6382887b081c	1de55914-4287-4f90-a756-98935801a54a	t
fc4359ab-c586-4d33-a1e0-6382887b081c	6371eda8-48d2-49aa-8d78-d7494faa18a8	t
fc4359ab-c586-4d33-a1e0-6382887b081c	a30af88e-ea48-4f49-93dd-5b12e663c661	f
fc4359ab-c586-4d33-a1e0-6382887b081c	f324045a-4192-4f9f-a91a-0c13207ca40b	t
fc4359ab-c586-4d33-a1e0-6382887b081c	f5b0a239-7f75-4370-ba8b-2e297524a542	t
fc4359ab-c586-4d33-a1e0-6382887b081c	dc87757c-d255-495f-b67b-e56125c73634	f
bde3ba76-fd10-4c67-8d50-9435f4f48ccc	6eca9a41-8a64-412f-a81a-1002d28db0a2	f
bde3ba76-fd10-4c67-8d50-9435f4f48ccc	edc041b2-9228-48c8-95d6-ce1c8cfa0d1c	t
bde3ba76-fd10-4c67-8d50-9435f4f48ccc	ee6c4196-cb97-4d15-9398-329614005ffd	t
bde3ba76-fd10-4c67-8d50-9435f4f48ccc	ac6628bd-6dbc-4b80-814e-520ed71001c5	t
bde3ba76-fd10-4c67-8d50-9435f4f48ccc	9a14df16-95c2-4978-a6f8-eaf87357be72	t
bde3ba76-fd10-4c67-8d50-9435f4f48ccc	124a2c54-450c-41e5-9b9f-259ce8dd8e58	f
bde3ba76-fd10-4c67-8d50-9435f4f48ccc	ea3bcee8-a9b8-49e5-b903-3ea8a125a1b8	f
bde3ba76-fd10-4c67-8d50-9435f4f48ccc	299a79ee-e783-468b-8e75-27838b370ef8	t
bde3ba76-fd10-4c67-8d50-9435f4f48ccc	a3c705a8-6890-452a-abf2-ef79dfa0f6cd	t
bde3ba76-fd10-4c67-8d50-9435f4f48ccc	58f9639a-c0ed-4002-9d6e-f84547b6b2e3	f
bde3ba76-fd10-4c67-8d50-9435f4f48ccc	a683d956-1aa5-4087-941d-40624fbaa93f	t
bde3ba76-fd10-4c67-8d50-9435f4f48ccc	3f50493d-06bd-4e56-84c1-46186100d0fd	t
bde3ba76-fd10-4c67-8d50-9435f4f48ccc	c75ba36c-1229-4b13-8537-017e7a1a0300	f
\.


COPY keycloak.event_entity (id, client_id, details_json, error, ip_address, realm_id, session_id, event_time, type,
                            user_id, details_json_long_value) FROM stdin;
\.


COPY keycloak.fed_user_attribute (id, name, user_id, realm_id, storage_provider_id, value, long_value_hash,
                                  long_value_hash_lower_case, long_value) FROM stdin;
\.


COPY keycloak.fed_user_consent (id, client_id, user_id, realm_id, storage_provider_id, created_date, last_updated_date,
                                client_storage_provider, external_client_id) FROM stdin;
\.


COPY keycloak.fed_user_consent_cl_scope (user_consent_id, scope_id) FROM stdin;
\.


COPY keycloak.fed_user_credential (id, salt, type, created_date, user_id, realm_id, storage_provider_id, user_label,
                                   secret_data, credential_data, priority) FROM stdin;
\.


COPY keycloak.fed_user_group_membership (group_id, user_id, realm_id, storage_provider_id) FROM stdin;
\.


COPY keycloak.fed_user_required_action (required_action, user_id, realm_id, storage_provider_id) FROM stdin;
\.


COPY keycloak.fed_user_role_mapping (role_id, user_id, realm_id, storage_provider_id) FROM stdin;
\.


COPY keycloak.federated_identity (identity_provider, realm_id, federated_user_id, federated_username, token,
                                  user_id) FROM stdin;
\.


COPY keycloak.federated_user (id, storage_provider_id, realm_id) FROM stdin;
\.


COPY keycloak.group_attribute (id, name, value, group_id) FROM stdin;
\.


COPY keycloak.group_role_mapping (role_id, group_id) FROM stdin;
\.


COPY keycloak.identity_provider_mapper (id, name, idp_alias, idp_mapper_name, realm_id) FROM stdin;
\.


COPY keycloak.idp_mapper_config (idp_mapper_id, value, name) FROM stdin;
\.


COPY keycloak.jgroups_ping (address, name, cluster_name, ip, coord) FROM stdin;
\.


COPY keycloak.keycloak_group (id, name, parent_group, realm_id, type) FROM stdin;
\.


COPY keycloak.keycloak_role (id, client_realm_constraint, client_role, description, name, realm_id, client,
                             realm) FROM stdin;
ee605334-ad03-4dd9-b1e1-abdda1eec33a	fc4359ab-c586-4d33-a1e0-6382887b081c	f	${role_default-roles}	default-roles-master	fc4359ab-c586-4d33-a1e0-6382887b081c	\N	\N
6af59e01-d323-4062-93d1-bb37cc8b53e9	fc4359ab-c586-4d33-a1e0-6382887b081c	f	${role_admin}	admin	fc4359ab-c586-4d33-a1e0-6382887b081c	\N	\N
737455a9-1291-4832-b01b-78f0cf479900	fc4359ab-c586-4d33-a1e0-6382887b081c	f	${role_create-realm}	create-realm	fc4359ab-c586-4d33-a1e0-6382887b081c	\N	\N
3245476b-5de1-4649-a959-8a5c4a33dc63	ae3ea88f-aecf-4fec-b290-bb7594719e44	t	${role_create-client}	create-client	fc4359ab-c586-4d33-a1e0-6382887b081c	ae3ea88f-aecf-4fec-b290-bb7594719e44	\N
b04a3ad5-3997-4382-b47a-c47f4ece8d90	ae3ea88f-aecf-4fec-b290-bb7594719e44	t	${role_view-realm}	view-realm	fc4359ab-c586-4d33-a1e0-6382887b081c	ae3ea88f-aecf-4fec-b290-bb7594719e44	\N
eec006c0-25ef-40bf-b3fb-b278367415a2	ae3ea88f-aecf-4fec-b290-bb7594719e44	t	${role_view-users}	view-users	fc4359ab-c586-4d33-a1e0-6382887b081c	ae3ea88f-aecf-4fec-b290-bb7594719e44	\N
8e4732cf-a386-4586-92c7-39b18892491e	ae3ea88f-aecf-4fec-b290-bb7594719e44	t	${role_view-clients}	view-clients	fc4359ab-c586-4d33-a1e0-6382887b081c	ae3ea88f-aecf-4fec-b290-bb7594719e44	\N
b7a0f0f1-d3e5-4feb-8d0c-c394e36d2121	ae3ea88f-aecf-4fec-b290-bb7594719e44	t	${role_view-events}	view-events	fc4359ab-c586-4d33-a1e0-6382887b081c	ae3ea88f-aecf-4fec-b290-bb7594719e44	\N
2cae5fa0-f061-4cd1-a145-45f41a6aaefd	ae3ea88f-aecf-4fec-b290-bb7594719e44	t	${role_view-identity-providers}	view-identity-providers	fc4359ab-c586-4d33-a1e0-6382887b081c	ae3ea88f-aecf-4fec-b290-bb7594719e44	\N
dbf4f987-8c14-4dc2-8a55-c6f25c3d6fb0	ae3ea88f-aecf-4fec-b290-bb7594719e44	t	${role_view-authorization}	view-authorization	fc4359ab-c586-4d33-a1e0-6382887b081c	ae3ea88f-aecf-4fec-b290-bb7594719e44	\N
51d60a47-e5c6-4176-8822-b21c8b8f1209	ae3ea88f-aecf-4fec-b290-bb7594719e44	t	${role_manage-realm}	manage-realm	fc4359ab-c586-4d33-a1e0-6382887b081c	ae3ea88f-aecf-4fec-b290-bb7594719e44	\N
20e54757-3b48-4c5b-81ed-772ef520935a	ae3ea88f-aecf-4fec-b290-bb7594719e44	t	${role_manage-users}	manage-users	fc4359ab-c586-4d33-a1e0-6382887b081c	ae3ea88f-aecf-4fec-b290-bb7594719e44	\N
29a68cbc-b626-4da2-a99e-65e1218445c9	ae3ea88f-aecf-4fec-b290-bb7594719e44	t	${role_manage-clients}	manage-clients	fc4359ab-c586-4d33-a1e0-6382887b081c	ae3ea88f-aecf-4fec-b290-bb7594719e44	\N
94fc47ab-d824-4c1b-adf2-b3fce7d0c0d5	ae3ea88f-aecf-4fec-b290-bb7594719e44	t	${role_manage-events}	manage-events	fc4359ab-c586-4d33-a1e0-6382887b081c	ae3ea88f-aecf-4fec-b290-bb7594719e44	\N
d9db935b-547c-4dc7-ac7e-8edd8f4c2924	ae3ea88f-aecf-4fec-b290-bb7594719e44	t	${role_manage-identity-providers}	manage-identity-providers	fc4359ab-c586-4d33-a1e0-6382887b081c	ae3ea88f-aecf-4fec-b290-bb7594719e44	\N
45a5b1a1-1b5c-4c66-b314-f6f8673416ec	ae3ea88f-aecf-4fec-b290-bb7594719e44	t	${role_manage-authorization}	manage-authorization	fc4359ab-c586-4d33-a1e0-6382887b081c	ae3ea88f-aecf-4fec-b290-bb7594719e44	\N
70b597fc-aa49-4f51-bd6e-232557c2276a	ae3ea88f-aecf-4fec-b290-bb7594719e44	t	${role_query-users}	query-users	fc4359ab-c586-4d33-a1e0-6382887b081c	ae3ea88f-aecf-4fec-b290-bb7594719e44	\N
15cbc476-ac24-419e-b19e-be7a69e098d7	ae3ea88f-aecf-4fec-b290-bb7594719e44	t	${role_query-clients}	query-clients	fc4359ab-c586-4d33-a1e0-6382887b081c	ae3ea88f-aecf-4fec-b290-bb7594719e44	\N
c7884384-d02b-4e5f-b6b8-7ceefd9fd9b3	ae3ea88f-aecf-4fec-b290-bb7594719e44	t	${role_query-realms}	query-realms	fc4359ab-c586-4d33-a1e0-6382887b081c	ae3ea88f-aecf-4fec-b290-bb7594719e44	\N
e5431fa5-1b7d-4a8e-90d2-db427cfd4e46	ae3ea88f-aecf-4fec-b290-bb7594719e44	t	${role_query-groups}	query-groups	fc4359ab-c586-4d33-a1e0-6382887b081c	ae3ea88f-aecf-4fec-b290-bb7594719e44	\N
c43e62d2-1528-4b99-b247-7074e483ad3d	b0de488c-4330-4955-91e2-f8dfb13e054b	t	${role_view-profile}	view-profile	fc4359ab-c586-4d33-a1e0-6382887b081c	b0de488c-4330-4955-91e2-f8dfb13e054b	\N
5c738e1f-c579-4144-a96c-18a066451b74	b0de488c-4330-4955-91e2-f8dfb13e054b	t	${role_manage-account}	manage-account	fc4359ab-c586-4d33-a1e0-6382887b081c	b0de488c-4330-4955-91e2-f8dfb13e054b	\N
84f36e7a-9548-4cd3-b960-c191230b6910	b0de488c-4330-4955-91e2-f8dfb13e054b	t	${role_manage-account-links}	manage-account-links	fc4359ab-c586-4d33-a1e0-6382887b081c	b0de488c-4330-4955-91e2-f8dfb13e054b	\N
add12d6c-6ac9-4f6f-925f-a9873f53aabb	b0de488c-4330-4955-91e2-f8dfb13e054b	t	${role_view-applications}	view-applications	fc4359ab-c586-4d33-a1e0-6382887b081c	b0de488c-4330-4955-91e2-f8dfb13e054b	\N
02698cb9-1489-4b6e-883c-2b8a5c088b19	b0de488c-4330-4955-91e2-f8dfb13e054b	t	${role_view-consent}	view-consent	fc4359ab-c586-4d33-a1e0-6382887b081c	b0de488c-4330-4955-91e2-f8dfb13e054b	\N
09ea5cda-6c63-4317-adb5-834ae9627be9	b0de488c-4330-4955-91e2-f8dfb13e054b	t	${role_manage-consent}	manage-consent	fc4359ab-c586-4d33-a1e0-6382887b081c	b0de488c-4330-4955-91e2-f8dfb13e054b	\N
1630d110-0b4e-4099-bdc0-38a6651e3550	b0de488c-4330-4955-91e2-f8dfb13e054b	t	${role_view-groups}	view-groups	fc4359ab-c586-4d33-a1e0-6382887b081c	b0de488c-4330-4955-91e2-f8dfb13e054b	\N
db0c4796-4e3e-4797-b8dd-771d68ef7f3e	b0de488c-4330-4955-91e2-f8dfb13e054b	t	${role_delete-account}	delete-account	fc4359ab-c586-4d33-a1e0-6382887b081c	b0de488c-4330-4955-91e2-f8dfb13e054b	\N
fba31a42-dae7-436e-a1a6-5aed16c2c53f	ebe6655e-eccd-4d44-a08c-d4b523c6ce96	t	${role_read-token}	read-token	fc4359ab-c586-4d33-a1e0-6382887b081c	ebe6655e-eccd-4d44-a08c-d4b523c6ce96	\N
dc70d154-f36e-407a-8fdc-4e30d8fbc9eb	ae3ea88f-aecf-4fec-b290-bb7594719e44	t	${role_impersonation}	impersonation	fc4359ab-c586-4d33-a1e0-6382887b081c	ae3ea88f-aecf-4fec-b290-bb7594719e44	\N
a7b80c13-90d7-476a-acb9-55eed1cb5cb2	fc4359ab-c586-4d33-a1e0-6382887b081c	f	${role_offline-access}	offline_access	fc4359ab-c586-4d33-a1e0-6382887b081c	\N	\N
8ce73c59-de91-407f-83a8-9e2d01772cb5	fc4359ab-c586-4d33-a1e0-6382887b081c	f	${role_uma_authorization}	uma_authorization	fc4359ab-c586-4d33-a1e0-6382887b081c	\N	\N
da241a9c-0230-44b2-859b-d198a2b0863a	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	f	${role_default-roles}	default-roles-jodrive-realm	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	\N	\N
0e692e74-336d-4401-b1ef-ac68fd98df20	0936c0bc-f90c-4bd0-8208-1d3a997b8eca	t	${role_create-client}	create-client	fc4359ab-c586-4d33-a1e0-6382887b081c	0936c0bc-f90c-4bd0-8208-1d3a997b8eca	\N
e46f8a39-b526-4473-9ad1-a4f7bb61b7c9	0936c0bc-f90c-4bd0-8208-1d3a997b8eca	t	${role_view-realm}	view-realm	fc4359ab-c586-4d33-a1e0-6382887b081c	0936c0bc-f90c-4bd0-8208-1d3a997b8eca	\N
ffeb13e4-61f3-476c-998d-1563f4933cca	0936c0bc-f90c-4bd0-8208-1d3a997b8eca	t	${role_view-users}	view-users	fc4359ab-c586-4d33-a1e0-6382887b081c	0936c0bc-f90c-4bd0-8208-1d3a997b8eca	\N
d6ab2d37-8ba7-48c8-93e2-5c15ed1ddbe9	0936c0bc-f90c-4bd0-8208-1d3a997b8eca	t	${role_view-clients}	view-clients	fc4359ab-c586-4d33-a1e0-6382887b081c	0936c0bc-f90c-4bd0-8208-1d3a997b8eca	\N
759c4746-5a2d-4edb-a9bf-02dfbe154e3e	0936c0bc-f90c-4bd0-8208-1d3a997b8eca	t	${role_view-events}	view-events	fc4359ab-c586-4d33-a1e0-6382887b081c	0936c0bc-f90c-4bd0-8208-1d3a997b8eca	\N
e4b5d537-5e21-40cc-9e38-ceb8c4749264	0936c0bc-f90c-4bd0-8208-1d3a997b8eca	t	${role_view-identity-providers}	view-identity-providers	fc4359ab-c586-4d33-a1e0-6382887b081c	0936c0bc-f90c-4bd0-8208-1d3a997b8eca	\N
e7863c94-48b0-427a-8ec7-1ed566228dd9	0936c0bc-f90c-4bd0-8208-1d3a997b8eca	t	${role_view-authorization}	view-authorization	fc4359ab-c586-4d33-a1e0-6382887b081c	0936c0bc-f90c-4bd0-8208-1d3a997b8eca	\N
786f78d0-b138-4186-86c3-a837190d25ab	0936c0bc-f90c-4bd0-8208-1d3a997b8eca	t	${role_manage-realm}	manage-realm	fc4359ab-c586-4d33-a1e0-6382887b081c	0936c0bc-f90c-4bd0-8208-1d3a997b8eca	\N
ca4961fc-eb26-45a5-8c72-4fdfa138a4e8	0936c0bc-f90c-4bd0-8208-1d3a997b8eca	t	${role_manage-users}	manage-users	fc4359ab-c586-4d33-a1e0-6382887b081c	0936c0bc-f90c-4bd0-8208-1d3a997b8eca	\N
3742aff0-647b-4d84-89b0-db0896817781	0936c0bc-f90c-4bd0-8208-1d3a997b8eca	t	${role_manage-clients}	manage-clients	fc4359ab-c586-4d33-a1e0-6382887b081c	0936c0bc-f90c-4bd0-8208-1d3a997b8eca	\N
035ca821-ee04-4fee-9b3d-e699e4daddd5	0936c0bc-f90c-4bd0-8208-1d3a997b8eca	t	${role_manage-events}	manage-events	fc4359ab-c586-4d33-a1e0-6382887b081c	0936c0bc-f90c-4bd0-8208-1d3a997b8eca	\N
9c690433-c067-40ef-b853-3edf1728ee05	0936c0bc-f90c-4bd0-8208-1d3a997b8eca	t	${role_manage-identity-providers}	manage-identity-providers	fc4359ab-c586-4d33-a1e0-6382887b081c	0936c0bc-f90c-4bd0-8208-1d3a997b8eca	\N
e06657c4-7e07-4b01-9e6b-28a41f2e53d8	0936c0bc-f90c-4bd0-8208-1d3a997b8eca	t	${role_manage-authorization}	manage-authorization	fc4359ab-c586-4d33-a1e0-6382887b081c	0936c0bc-f90c-4bd0-8208-1d3a997b8eca	\N
e15c445a-3e00-4b1b-ae3b-377a965e7373	0936c0bc-f90c-4bd0-8208-1d3a997b8eca	t	${role_query-users}	query-users	fc4359ab-c586-4d33-a1e0-6382887b081c	0936c0bc-f90c-4bd0-8208-1d3a997b8eca	\N
600ae185-6918-4da3-af48-866c6f64c90c	0936c0bc-f90c-4bd0-8208-1d3a997b8eca	t	${role_query-clients}	query-clients	fc4359ab-c586-4d33-a1e0-6382887b081c	0936c0bc-f90c-4bd0-8208-1d3a997b8eca	\N
7b3034e5-5498-46f0-ab68-94619e4252c2	0936c0bc-f90c-4bd0-8208-1d3a997b8eca	t	${role_query-realms}	query-realms	fc4359ab-c586-4d33-a1e0-6382887b081c	0936c0bc-f90c-4bd0-8208-1d3a997b8eca	\N
088d3afe-494d-47ae-b9e4-974c2a18e050	0936c0bc-f90c-4bd0-8208-1d3a997b8eca	t	${role_query-groups}	query-groups	fc4359ab-c586-4d33-a1e0-6382887b081c	0936c0bc-f90c-4bd0-8208-1d3a997b8eca	\N
8a073218-8363-478d-9c03-4535467cca5e	f8e3dca3-1b19-48ea-a95d-b7e8cfa335c3	t	${role_realm-admin}	realm-admin	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	f8e3dca3-1b19-48ea-a95d-b7e8cfa335c3	\N
94f54247-a73c-4fb0-af7f-caf7e4460796	f8e3dca3-1b19-48ea-a95d-b7e8cfa335c3	t	${role_create-client}	create-client	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	f8e3dca3-1b19-48ea-a95d-b7e8cfa335c3	\N
129a3654-da4b-4d7b-baea-a683e22b7a0f	f8e3dca3-1b19-48ea-a95d-b7e8cfa335c3	t	${role_view-realm}	view-realm	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	f8e3dca3-1b19-48ea-a95d-b7e8cfa335c3	\N
ec9abeba-95ff-4b3c-9e4c-2780ec242b36	f8e3dca3-1b19-48ea-a95d-b7e8cfa335c3	t	${role_view-users}	view-users	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	f8e3dca3-1b19-48ea-a95d-b7e8cfa335c3	\N
19e98e43-c1c5-426b-ac5b-9f6852de48ef	f8e3dca3-1b19-48ea-a95d-b7e8cfa335c3	t	${role_view-clients}	view-clients	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	f8e3dca3-1b19-48ea-a95d-b7e8cfa335c3	\N
4f262492-395c-4983-a1e0-874abc3f5fdf	f8e3dca3-1b19-48ea-a95d-b7e8cfa335c3	t	${role_view-events}	view-events	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	f8e3dca3-1b19-48ea-a95d-b7e8cfa335c3	\N
1f1e3936-e96d-4a0b-8229-fee110cf39ad	f8e3dca3-1b19-48ea-a95d-b7e8cfa335c3	t	${role_view-identity-providers}	view-identity-providers	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	f8e3dca3-1b19-48ea-a95d-b7e8cfa335c3	\N
10d8aa8c-7242-4a21-b77f-6451c64d1bac	f8e3dca3-1b19-48ea-a95d-b7e8cfa335c3	t	${role_view-authorization}	view-authorization	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	f8e3dca3-1b19-48ea-a95d-b7e8cfa335c3	\N
34d123d7-f9e0-44ad-82a1-1c49f6388a73	f8e3dca3-1b19-48ea-a95d-b7e8cfa335c3	t	${role_manage-realm}	manage-realm	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	f8e3dca3-1b19-48ea-a95d-b7e8cfa335c3	\N
0b811a26-9290-408f-9f89-63ab359b23e1	f8e3dca3-1b19-48ea-a95d-b7e8cfa335c3	t	${role_manage-users}	manage-users	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	f8e3dca3-1b19-48ea-a95d-b7e8cfa335c3	\N
d70fbcf3-ecb4-4470-922d-8e18b43bb89d	f8e3dca3-1b19-48ea-a95d-b7e8cfa335c3	t	${role_manage-clients}	manage-clients	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	f8e3dca3-1b19-48ea-a95d-b7e8cfa335c3	\N
56f75a23-f0eb-4ace-9b8d-42064995acdb	f8e3dca3-1b19-48ea-a95d-b7e8cfa335c3	t	${role_manage-events}	manage-events	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	f8e3dca3-1b19-48ea-a95d-b7e8cfa335c3	\N
a225830e-d025-4c64-9e7c-0afbaa0faaff	f8e3dca3-1b19-48ea-a95d-b7e8cfa335c3	t	${role_manage-identity-providers}	manage-identity-providers	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	f8e3dca3-1b19-48ea-a95d-b7e8cfa335c3	\N
56f3ca35-2166-4228-b126-49c6563a6587	f8e3dca3-1b19-48ea-a95d-b7e8cfa335c3	t	${role_manage-authorization}	manage-authorization	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	f8e3dca3-1b19-48ea-a95d-b7e8cfa335c3	\N
e1a087af-fc00-492e-8651-209db2ec15cc	f8e3dca3-1b19-48ea-a95d-b7e8cfa335c3	t	${role_query-users}	query-users	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	f8e3dca3-1b19-48ea-a95d-b7e8cfa335c3	\N
fcc9e5d3-9143-4df0-a031-0d178db71a7b	f8e3dca3-1b19-48ea-a95d-b7e8cfa335c3	t	${role_query-clients}	query-clients	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	f8e3dca3-1b19-48ea-a95d-b7e8cfa335c3	\N
94b50168-6623-4f36-9ed9-4af07b8d2547	f8e3dca3-1b19-48ea-a95d-b7e8cfa335c3	t	${role_query-realms}	query-realms	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	f8e3dca3-1b19-48ea-a95d-b7e8cfa335c3	\N
3d68b3e6-2827-42c2-878e-0dbafad5f9f4	f8e3dca3-1b19-48ea-a95d-b7e8cfa335c3	t	${role_query-groups}	query-groups	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	f8e3dca3-1b19-48ea-a95d-b7e8cfa335c3	\N
3c69c637-c3a8-4624-ad3b-413be787e258	da0a22ac-4993-40b2-a719-2a2f01790525	t	${role_view-profile}	view-profile	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	da0a22ac-4993-40b2-a719-2a2f01790525	\N
2248cd8f-a233-4393-9257-f2e4b3953667	da0a22ac-4993-40b2-a719-2a2f01790525	t	${role_manage-account}	manage-account	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	da0a22ac-4993-40b2-a719-2a2f01790525	\N
0e61f418-c7b2-4d9d-8cc1-d0ca664f76ff	da0a22ac-4993-40b2-a719-2a2f01790525	t	${role_manage-account-links}	manage-account-links	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	da0a22ac-4993-40b2-a719-2a2f01790525	\N
2e6ffe59-90e6-4f47-9ada-3a17fcd9dea0	da0a22ac-4993-40b2-a719-2a2f01790525	t	${role_view-applications}	view-applications	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	da0a22ac-4993-40b2-a719-2a2f01790525	\N
e4c96ba4-201d-47ac-bf65-33d480b2f1c4	da0a22ac-4993-40b2-a719-2a2f01790525	t	${role_view-consent}	view-consent	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	da0a22ac-4993-40b2-a719-2a2f01790525	\N
6b62356e-e96d-4605-9bf5-2483707decc8	da0a22ac-4993-40b2-a719-2a2f01790525	t	${role_manage-consent}	manage-consent	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	da0a22ac-4993-40b2-a719-2a2f01790525	\N
fffbb75c-d70b-4b6c-9048-158d57e5ee99	da0a22ac-4993-40b2-a719-2a2f01790525	t	${role_view-groups}	view-groups	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	da0a22ac-4993-40b2-a719-2a2f01790525	\N
cc8067a1-f927-412f-94e0-cb160ffc3e6a	da0a22ac-4993-40b2-a719-2a2f01790525	t	${role_delete-account}	delete-account	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	da0a22ac-4993-40b2-a719-2a2f01790525	\N
0cdcfbbe-8283-4af3-ae24-b5c0f8d11a1f	0936c0bc-f90c-4bd0-8208-1d3a997b8eca	t	${role_impersonation}	impersonation	fc4359ab-c586-4d33-a1e0-6382887b081c	0936c0bc-f90c-4bd0-8208-1d3a997b8eca	\N
115cfc4e-fb1a-4168-b34e-701ccaeb3d37	f8e3dca3-1b19-48ea-a95d-b7e8cfa335c3	t	${role_impersonation}	impersonation	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	f8e3dca3-1b19-48ea-a95d-b7e8cfa335c3	\N
3b92910c-2c03-400d-901e-7a711a9dfb53	62f26ff7-f9ed-4f44-84d7-4aa189b52d2c	t	${role_read-token}	read-token	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	62f26ff7-f9ed-4f44-84d7-4aa189b52d2c	\N
c6ff9adb-e6c5-450c-af4d-c5eb488114ae	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	f	${role_offline-access}	offline_access	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	\N	\N
c62f516b-16f5-4ed8-9f22-eb47d2d5fbdd	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	f	${role_uma_authorization}	uma_authorization	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	\N	\N
553f1779-7b3c-4b71-87b9-230f6ac03d5d	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	f	role_user	user	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	\N	\N
\.


COPY keycloak.migration_model (id, version, update_time) FROM stdin;
9g959	26.2.0	1745443441
\.


COPY keycloak.offline_client_session (user_session_id, client_id, offline_flag, "timestamp", data,
                                      client_storage_provider, external_client_id, version) FROM stdin;
93e407dd-3eb1-4b18-a77e-084b321d9ebe	05b20b1d-d90a-4d28-b31f-73b413c99bf1	0	1746569983	{"authMethod":"openid-connect","notes":{"clientId":"05b20b1d-d90a-4d28-b31f-73b413c99bf1","scope":"openid","userSessionStartedAt":"1746569983","iss":"http://localhost:7080/realms/jodrive-realm","startedAt":"1746569983","level-of-authentication":"-1"}}	local	local	0
ea91f117-449a-409a-8fa9-d4e4aaa91e9d	108d6f49-22ba-43f2-84e8-105fbc9e9cdd	0	1746571081	{"authMethod":"openid-connect","redirectUri":"http://localhost:7080/admin/master/console/","notes":{"clientId":"108d6f49-22ba-43f2-84e8-105fbc9e9cdd","iss":"http://localhost:7080/realms/master","startedAt":"1746570959","response_type":"code","level-of-authentication":"-1","code_challenge_method":"S256","nonce":"7b21199e-d931-4931-a943-2dd94b93d644","response_mode":"query","scope":"openid","userSessionStartedAt":"1746570959","redirect_uri":"http://localhost:7080/admin/master/console/","state":"bf58cbba-aa80-4bfe-929e-6e7ce4ec6f87","code_challenge":"TwN5_wt4yF-gUMUVu85XPD3sbIe-s13Wfz7vzRHRr_A"}}	local	local	2
\.


COPY keycloak.offline_user_session (user_session_id, user_id, realm_id, created_on, offline_flag, data,
                                    last_session_refresh, broker_session_id, version) FROM stdin;
93e407dd-3eb1-4b18-a77e-084b321d9ebe	29849880-ddd4-4000-b100-460f4c505045	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	1746569983	0	{"ipAddress":"172.18.0.1","authMethod":"openid-connect","rememberMe":false,"started":0,"notes":{"KC_DEVICE_NOTE":"eyJpcEFkZHJlc3MiOiIxNzIuMTguMC4xIiwib3MiOiJPdGhlciIsIm9zVmVyc2lvbiI6IlVua25vd24iLCJicm93c2VyIjoiUHl0aG9uIFJlcXVlc3RzLzIuMzIiLCJkZXZpY2UiOiJPdGhlciIsImxhc3RBY2Nlc3MiOjAsIm1vYmlsZSI6ZmFsc2V9","authenticators-completed":"{\\"eb28126e-0f08-43ac-a7e6-71d72e4ad30c\\":1746569983,\\"a7794968-6a3e-48e3-906e-4b75f577b702\\":1746569983}"},"state":"LOGGED_IN"}	1746569983	\N	0
ea91f117-449a-409a-8fa9-d4e4aaa91e9d	1d071bc2-3f12-462a-b686-079304d649a0	fc4359ab-c586-4d33-a1e0-6382887b081c	1746570959	0	{"ipAddress":"172.18.0.1","authMethod":"openid-connect","rememberMe":false,"started":0,"notes":{"KC_DEVICE_NOTE":"eyJpcEFkZHJlc3MiOiIxNzIuMTguMC4xIiwib3MiOiJXaW5kb3dzIiwib3NWZXJzaW9uIjoiMTAiLCJicm93c2VyIjoiQ2hyb21lLzEzNS4wLjAiLCJkZXZpY2UiOiJPdGhlciIsImxhc3RBY2Nlc3MiOjAsIm1vYmlsZSI6ZmFsc2V9","AUTH_TIME":"1746570959","authenticators-completed":"{\\"8b33a82a-6af4-4be6-ac3a-5b2db80764a1\\":1746570959}"},"state":"LOGGED_IN"}	1746571081	\N	2
\.


COPY keycloak.org (id, enabled, realm_id, group_id, name, description, alias, redirect_url) FROM stdin;
\.


COPY keycloak.org_domain (id, name, verified, org_id) FROM stdin;
\.


COPY keycloak.policy_config (policy_id, name, value) FROM stdin;
\.


COPY keycloak.protocol_mapper (id, name, protocol, protocol_mapper_name, client_id, client_scope_id) FROM stdin;
f86167ca-5d06-414e-a5fd-738259702626	audience resolve	openid-connect	oidc-audience-resolve-mapper	ee76a050-87a8-4470-82e6-1a09a0e47223	\N
02cb42a2-1320-4d3b-92a4-d1451ae9e1ca	locale	openid-connect	oidc-usermodel-attribute-mapper	108d6f49-22ba-43f2-84e8-105fbc9e9cdd	\N
864883a8-77ae-4460-929c-405a7bfcf9fc	role list	saml	saml-role-list-mapper	\N	1ae2dcaa-0525-4bf7-bfc4-0abf95a42d77
a54f3b80-e16a-442f-8aed-445dae79dc46	organization	saml	saml-organization-membership-mapper	\N	e8d54d8c-62f6-4798-afa7-464b8c3584e0
12c85cae-57b7-4e2b-9bc5-5c67641f4a93	full name	openid-connect	oidc-full-name-mapper	\N	8a546be3-675d-42c6-a00f-021792f5948b
914762e1-29dd-4bf5-bfb6-d985a6c1626a	family name	openid-connect	oidc-usermodel-attribute-mapper	\N	8a546be3-675d-42c6-a00f-021792f5948b
40598d34-4c2d-4dd9-b137-58471dc05c15	given name	openid-connect	oidc-usermodel-attribute-mapper	\N	8a546be3-675d-42c6-a00f-021792f5948b
151727d7-66cf-42fc-ad50-ae75e4161770	middle name	openid-connect	oidc-usermodel-attribute-mapper	\N	8a546be3-675d-42c6-a00f-021792f5948b
8bf6f1db-a568-4910-8f23-5f4ea64560f3	nickname	openid-connect	oidc-usermodel-attribute-mapper	\N	8a546be3-675d-42c6-a00f-021792f5948b
66dae11b-5efd-4548-aff5-9a5fac70b8a1	username	openid-connect	oidc-usermodel-attribute-mapper	\N	8a546be3-675d-42c6-a00f-021792f5948b
5bd9367e-31a8-4bd0-a80b-e7f68fe6f232	profile	openid-connect	oidc-usermodel-attribute-mapper	\N	8a546be3-675d-42c6-a00f-021792f5948b
21512dab-d146-409b-8ed2-ebce11dd8219	picture	openid-connect	oidc-usermodel-attribute-mapper	\N	8a546be3-675d-42c6-a00f-021792f5948b
ab524fd9-212b-41d7-9286-0f0855388dbd	website	openid-connect	oidc-usermodel-attribute-mapper	\N	8a546be3-675d-42c6-a00f-021792f5948b
3241dd12-e453-4907-acd8-e9ce41cb26c1	gender	openid-connect	oidc-usermodel-attribute-mapper	\N	8a546be3-675d-42c6-a00f-021792f5948b
bb2e821a-d4d5-46fc-996b-62dac70ea5d7	birthdate	openid-connect	oidc-usermodel-attribute-mapper	\N	8a546be3-675d-42c6-a00f-021792f5948b
e8abd4d9-ab56-4255-9e07-f3fd31448301	zoneinfo	openid-connect	oidc-usermodel-attribute-mapper	\N	8a546be3-675d-42c6-a00f-021792f5948b
13e0dc34-59f0-48e6-82f5-0e7341b91d6f	locale	openid-connect	oidc-usermodel-attribute-mapper	\N	8a546be3-675d-42c6-a00f-021792f5948b
fa9bb112-a644-48c1-8527-6de0adf8ad7b	updated at	openid-connect	oidc-usermodel-attribute-mapper	\N	8a546be3-675d-42c6-a00f-021792f5948b
8764d608-fae5-4d29-9464-1b9e34a45a89	email	openid-connect	oidc-usermodel-attribute-mapper	\N	9e96fd62-765d-4a19-be1a-153aa28fa60f
0d451588-2f5b-4933-8acb-7755fb471a63	email verified	openid-connect	oidc-usermodel-property-mapper	\N	9e96fd62-765d-4a19-be1a-153aa28fa60f
d6049b4b-fb68-4b28-a11a-78dcb2f8949f	address	openid-connect	oidc-address-mapper	\N	9c8d4a40-e6f2-43a1-a3d8-3225bf6962a2
ca970342-6549-4eaa-82bf-08f9f3e184ba	phone number	openid-connect	oidc-usermodel-attribute-mapper	\N	5cdaa0cd-4746-4007-831b-addde8e7ea8a
9ae02f12-2570-4e12-bb20-df9e43d84a8b	phone number verified	openid-connect	oidc-usermodel-attribute-mapper	\N	5cdaa0cd-4746-4007-831b-addde8e7ea8a
06391a1a-ad0c-4c84-adec-5b7f639e2ee7	realm roles	openid-connect	oidc-usermodel-realm-role-mapper	\N	1de55914-4287-4f90-a756-98935801a54a
c796b891-270a-4617-9f6f-c48fb238848b	client roles	openid-connect	oidc-usermodel-client-role-mapper	\N	1de55914-4287-4f90-a756-98935801a54a
56f1af0c-1a93-46ec-a513-e74d20373cb1	audience resolve	openid-connect	oidc-audience-resolve-mapper	\N	1de55914-4287-4f90-a756-98935801a54a
1679c109-9590-4673-b1a0-7d5ecaba8cb8	allowed web origins	openid-connect	oidc-allowed-origins-mapper	\N	6371eda8-48d2-49aa-8d78-d7494faa18a8
567e2c1d-556e-47d9-91fb-ea0dd97125e3	upn	openid-connect	oidc-usermodel-attribute-mapper	\N	a30af88e-ea48-4f49-93dd-5b12e663c661
19bae4df-df7a-4ba6-ab3f-5ed511261bbf	groups	openid-connect	oidc-usermodel-realm-role-mapper	\N	a30af88e-ea48-4f49-93dd-5b12e663c661
9b2b8857-462d-4da7-93f2-0488b1fdacf3	acr loa level	openid-connect	oidc-acr-mapper	\N	f324045a-4192-4f9f-a91a-0c13207ca40b
97ed5f8d-726c-43a8-b772-40ced76fbdf0	auth_time	openid-connect	oidc-usersessionmodel-note-mapper	\N	f5b0a239-7f75-4370-ba8b-2e297524a542
5b90801b-e8d1-4d70-a1a1-bc448c9e3e52	sub	openid-connect	oidc-sub-mapper	\N	f5b0a239-7f75-4370-ba8b-2e297524a542
02158905-8f5a-44d7-9d72-4ca2ec1b7ec9	Client ID	openid-connect	oidc-usersessionmodel-note-mapper	\N	dfcaecf4-fb06-49fc-bd13-7d7f1bd656e0
8bcec692-6c95-48cc-9153-82e8017da960	Client Host	openid-connect	oidc-usersessionmodel-note-mapper	\N	dfcaecf4-fb06-49fc-bd13-7d7f1bd656e0
8db0f190-4100-4fd7-ace3-67878105b761	Client IP Address	openid-connect	oidc-usersessionmodel-note-mapper	\N	dfcaecf4-fb06-49fc-bd13-7d7f1bd656e0
3cd01669-15d8-4e5c-ab01-399f11971857	organization	openid-connect	oidc-organization-membership-mapper	\N	dc87757c-d255-495f-b67b-e56125c73634
979d6062-f5dd-4e8d-8384-0a88fa5e8376	audience resolve	openid-connect	oidc-audience-resolve-mapper	c7633729-6ca9-4cc4-9153-86f6d352230d	\N
d7e49f3d-73ca-4279-b1b8-40a32c3d0627	role list	saml	saml-role-list-mapper	\N	edc041b2-9228-48c8-95d6-ce1c8cfa0d1c
08ec0dbe-3327-4bfa-ae4b-d70f7043f75b	organization	saml	saml-organization-membership-mapper	\N	ee6c4196-cb97-4d15-9398-329614005ffd
49434dc5-6631-4610-88ad-7e181d30810a	full name	openid-connect	oidc-full-name-mapper	\N	ac6628bd-6dbc-4b80-814e-520ed71001c5
6a3ef67b-f7d7-4cc1-a1a3-d5b72b786314	family name	openid-connect	oidc-usermodel-attribute-mapper	\N	ac6628bd-6dbc-4b80-814e-520ed71001c5
df368ebf-a356-4612-81cc-3c60463583aa	given name	openid-connect	oidc-usermodel-attribute-mapper	\N	ac6628bd-6dbc-4b80-814e-520ed71001c5
14eaac5b-969a-4528-bd40-19d087e17df3	middle name	openid-connect	oidc-usermodel-attribute-mapper	\N	ac6628bd-6dbc-4b80-814e-520ed71001c5
100921bf-3266-48bc-9a38-3cd3cdb29550	nickname	openid-connect	oidc-usermodel-attribute-mapper	\N	ac6628bd-6dbc-4b80-814e-520ed71001c5
e0a557db-acdc-4049-b1ec-100a951ffcfc	username	openid-connect	oidc-usermodel-attribute-mapper	\N	ac6628bd-6dbc-4b80-814e-520ed71001c5
e04b00d5-fcdc-4259-8e92-d237e27c9768	profile	openid-connect	oidc-usermodel-attribute-mapper	\N	ac6628bd-6dbc-4b80-814e-520ed71001c5
95034058-17e7-4201-9e20-4c9091db7ed8	picture	openid-connect	oidc-usermodel-attribute-mapper	\N	ac6628bd-6dbc-4b80-814e-520ed71001c5
64465c28-6f96-4882-85f5-e6bcd4ef7334	website	openid-connect	oidc-usermodel-attribute-mapper	\N	ac6628bd-6dbc-4b80-814e-520ed71001c5
be562e27-0970-47b5-94ca-2d41fa53d179	gender	openid-connect	oidc-usermodel-attribute-mapper	\N	ac6628bd-6dbc-4b80-814e-520ed71001c5
89e39e7c-d00a-4136-86b8-7f8cc963dc08	birthdate	openid-connect	oidc-usermodel-attribute-mapper	\N	ac6628bd-6dbc-4b80-814e-520ed71001c5
619d7fe3-2b54-44d5-8d4f-f4970e4fffa3	zoneinfo	openid-connect	oidc-usermodel-attribute-mapper	\N	ac6628bd-6dbc-4b80-814e-520ed71001c5
9ddc9190-b3fc-4eb3-9fcf-b4e283f6e006	locale	openid-connect	oidc-usermodel-attribute-mapper	\N	ac6628bd-6dbc-4b80-814e-520ed71001c5
5b63e600-3753-468a-851e-5b9ceed71c55	updated at	openid-connect	oidc-usermodel-attribute-mapper	\N	ac6628bd-6dbc-4b80-814e-520ed71001c5
5e1d1af0-d110-4303-ae5e-648ab140200a	email	openid-connect	oidc-usermodel-attribute-mapper	\N	9a14df16-95c2-4978-a6f8-eaf87357be72
b884e5ac-a304-4cb6-a03f-12d913998f12	email verified	openid-connect	oidc-usermodel-property-mapper	\N	9a14df16-95c2-4978-a6f8-eaf87357be72
9c090eb2-2f90-4e43-9a0c-e923dcf35968	address	openid-connect	oidc-address-mapper	\N	124a2c54-450c-41e5-9b9f-259ce8dd8e58
8c1c571f-8558-43cb-8e26-5286cc2db738	phone number	openid-connect	oidc-usermodel-attribute-mapper	\N	ea3bcee8-a9b8-49e5-b903-3ea8a125a1b8
138c726e-466b-4d3a-a2a5-cab0d8ab2f6e	phone number verified	openid-connect	oidc-usermodel-attribute-mapper	\N	ea3bcee8-a9b8-49e5-b903-3ea8a125a1b8
46543c8e-f4af-4ed8-8801-571c576d6c29	realm roles	openid-connect	oidc-usermodel-realm-role-mapper	\N	299a79ee-e783-468b-8e75-27838b370ef8
83b779f4-3041-488d-913c-3fc65abd2530	client roles	openid-connect	oidc-usermodel-client-role-mapper	\N	299a79ee-e783-468b-8e75-27838b370ef8
2547f839-155c-42f4-8fa6-748ceaabb2be	audience resolve	openid-connect	oidc-audience-resolve-mapper	\N	299a79ee-e783-468b-8e75-27838b370ef8
9d262a96-86fd-42d0-bfd1-abbbf759266c	allowed web origins	openid-connect	oidc-allowed-origins-mapper	\N	a3c705a8-6890-452a-abf2-ef79dfa0f6cd
53bcfb45-4b1d-4672-8df3-45f2b69231dc	upn	openid-connect	oidc-usermodel-attribute-mapper	\N	58f9639a-c0ed-4002-9d6e-f84547b6b2e3
5d9b06b8-55af-46bf-927d-aa8d5e7d010e	groups	openid-connect	oidc-usermodel-realm-role-mapper	\N	58f9639a-c0ed-4002-9d6e-f84547b6b2e3
d80c2cb9-edff-4bb9-bc91-f15c6c711f02	acr loa level	openid-connect	oidc-acr-mapper	\N	a683d956-1aa5-4087-941d-40624fbaa93f
6e1fa6f4-0fa9-40d6-a121-c7e254d6f014	auth_time	openid-connect	oidc-usersessionmodel-note-mapper	\N	3f50493d-06bd-4e56-84c1-46186100d0fd
8b280690-ea19-4c52-8ac5-6a8452d6a795	sub	openid-connect	oidc-sub-mapper	\N	3f50493d-06bd-4e56-84c1-46186100d0fd
e5f4e98f-5f89-4300-ba55-e33dd5bcf279	Client ID	openid-connect	oidc-usersessionmodel-note-mapper	\N	677437b7-e6dd-4725-9cc4-3bb21ed07d75
da32b4fb-2f90-4c00-9bcb-a79ffd466565	Client Host	openid-connect	oidc-usersessionmodel-note-mapper	\N	677437b7-e6dd-4725-9cc4-3bb21ed07d75
b72e6702-0a7d-4091-b1ff-66738a0b6bbe	Client IP Address	openid-connect	oidc-usersessionmodel-note-mapper	\N	677437b7-e6dd-4725-9cc4-3bb21ed07d75
15629814-535c-4864-b36f-5ce4b32d4b19	organization	openid-connect	oidc-organization-membership-mapper	\N	c75ba36c-1229-4b13-8537-017e7a1a0300
590b29ec-499e-4d89-9fc1-cf9d709b1e5b	locale	openid-connect	oidc-usermodel-attribute-mapper	495ee007-a202-41b2-b78c-3ce14d1da8ce	\N
\.


COPY keycloak.protocol_mapper_config (protocol_mapper_id, value, name) FROM stdin;
02cb42a2-1320-4d3b-92a4-d1451ae9e1ca	true	introspection.token.claim
02cb42a2-1320-4d3b-92a4-d1451ae9e1ca	true	userinfo.token.claim
02cb42a2-1320-4d3b-92a4-d1451ae9e1ca	locale	user.attribute
02cb42a2-1320-4d3b-92a4-d1451ae9e1ca	true	id.token.claim
02cb42a2-1320-4d3b-92a4-d1451ae9e1ca	true	access.token.claim
02cb42a2-1320-4d3b-92a4-d1451ae9e1ca	locale	claim.name
02cb42a2-1320-4d3b-92a4-d1451ae9e1ca	String	jsonType.label
864883a8-77ae-4460-929c-405a7bfcf9fc	false	single
864883a8-77ae-4460-929c-405a7bfcf9fc	Basic	attribute.nameformat
864883a8-77ae-4460-929c-405a7bfcf9fc	Role	attribute.name
12c85cae-57b7-4e2b-9bc5-5c67641f4a93	true	introspection.token.claim
12c85cae-57b7-4e2b-9bc5-5c67641f4a93	true	userinfo.token.claim
12c85cae-57b7-4e2b-9bc5-5c67641f4a93	true	id.token.claim
12c85cae-57b7-4e2b-9bc5-5c67641f4a93	true	access.token.claim
13e0dc34-59f0-48e6-82f5-0e7341b91d6f	true	introspection.token.claim
13e0dc34-59f0-48e6-82f5-0e7341b91d6f	true	userinfo.token.claim
13e0dc34-59f0-48e6-82f5-0e7341b91d6f	locale	user.attribute
13e0dc34-59f0-48e6-82f5-0e7341b91d6f	true	id.token.claim
13e0dc34-59f0-48e6-82f5-0e7341b91d6f	true	access.token.claim
13e0dc34-59f0-48e6-82f5-0e7341b91d6f	locale	claim.name
13e0dc34-59f0-48e6-82f5-0e7341b91d6f	String	jsonType.label
151727d7-66cf-42fc-ad50-ae75e4161770	true	introspection.token.claim
151727d7-66cf-42fc-ad50-ae75e4161770	true	userinfo.token.claim
151727d7-66cf-42fc-ad50-ae75e4161770	middleName	user.attribute
151727d7-66cf-42fc-ad50-ae75e4161770	true	id.token.claim
151727d7-66cf-42fc-ad50-ae75e4161770	true	access.token.claim
151727d7-66cf-42fc-ad50-ae75e4161770	middle_name	claim.name
151727d7-66cf-42fc-ad50-ae75e4161770	String	jsonType.label
21512dab-d146-409b-8ed2-ebce11dd8219	true	introspection.token.claim
21512dab-d146-409b-8ed2-ebce11dd8219	true	userinfo.token.claim
21512dab-d146-409b-8ed2-ebce11dd8219	picture	user.attribute
21512dab-d146-409b-8ed2-ebce11dd8219	true	id.token.claim
21512dab-d146-409b-8ed2-ebce11dd8219	true	access.token.claim
21512dab-d146-409b-8ed2-ebce11dd8219	picture	claim.name
21512dab-d146-409b-8ed2-ebce11dd8219	String	jsonType.label
3241dd12-e453-4907-acd8-e9ce41cb26c1	true	introspection.token.claim
3241dd12-e453-4907-acd8-e9ce41cb26c1	true	userinfo.token.claim
3241dd12-e453-4907-acd8-e9ce41cb26c1	gender	user.attribute
3241dd12-e453-4907-acd8-e9ce41cb26c1	true	id.token.claim
3241dd12-e453-4907-acd8-e9ce41cb26c1	true	access.token.claim
3241dd12-e453-4907-acd8-e9ce41cb26c1	gender	claim.name
3241dd12-e453-4907-acd8-e9ce41cb26c1	String	jsonType.label
40598d34-4c2d-4dd9-b137-58471dc05c15	true	introspection.token.claim
40598d34-4c2d-4dd9-b137-58471dc05c15	true	userinfo.token.claim
40598d34-4c2d-4dd9-b137-58471dc05c15	firstName	user.attribute
40598d34-4c2d-4dd9-b137-58471dc05c15	true	id.token.claim
40598d34-4c2d-4dd9-b137-58471dc05c15	true	access.token.claim
40598d34-4c2d-4dd9-b137-58471dc05c15	given_name	claim.name
40598d34-4c2d-4dd9-b137-58471dc05c15	String	jsonType.label
5bd9367e-31a8-4bd0-a80b-e7f68fe6f232	true	introspection.token.claim
5bd9367e-31a8-4bd0-a80b-e7f68fe6f232	true	userinfo.token.claim
5bd9367e-31a8-4bd0-a80b-e7f68fe6f232	profile	user.attribute
5bd9367e-31a8-4bd0-a80b-e7f68fe6f232	true	id.token.claim
5bd9367e-31a8-4bd0-a80b-e7f68fe6f232	true	access.token.claim
5bd9367e-31a8-4bd0-a80b-e7f68fe6f232	profile	claim.name
5bd9367e-31a8-4bd0-a80b-e7f68fe6f232	String	jsonType.label
66dae11b-5efd-4548-aff5-9a5fac70b8a1	true	introspection.token.claim
66dae11b-5efd-4548-aff5-9a5fac70b8a1	true	userinfo.token.claim
66dae11b-5efd-4548-aff5-9a5fac70b8a1	username	user.attribute
66dae11b-5efd-4548-aff5-9a5fac70b8a1	true	id.token.claim
66dae11b-5efd-4548-aff5-9a5fac70b8a1	true	access.token.claim
66dae11b-5efd-4548-aff5-9a5fac70b8a1	preferred_username	claim.name
66dae11b-5efd-4548-aff5-9a5fac70b8a1	String	jsonType.label
8bf6f1db-a568-4910-8f23-5f4ea64560f3	true	introspection.token.claim
8bf6f1db-a568-4910-8f23-5f4ea64560f3	true	userinfo.token.claim
8bf6f1db-a568-4910-8f23-5f4ea64560f3	nickname	user.attribute
8bf6f1db-a568-4910-8f23-5f4ea64560f3	true	id.token.claim
8bf6f1db-a568-4910-8f23-5f4ea64560f3	true	access.token.claim
8bf6f1db-a568-4910-8f23-5f4ea64560f3	nickname	claim.name
8bf6f1db-a568-4910-8f23-5f4ea64560f3	String	jsonType.label
914762e1-29dd-4bf5-bfb6-d985a6c1626a	true	introspection.token.claim
914762e1-29dd-4bf5-bfb6-d985a6c1626a	true	userinfo.token.claim
914762e1-29dd-4bf5-bfb6-d985a6c1626a	lastName	user.attribute
914762e1-29dd-4bf5-bfb6-d985a6c1626a	true	id.token.claim
914762e1-29dd-4bf5-bfb6-d985a6c1626a	true	access.token.claim
914762e1-29dd-4bf5-bfb6-d985a6c1626a	family_name	claim.name
914762e1-29dd-4bf5-bfb6-d985a6c1626a	String	jsonType.label
ab524fd9-212b-41d7-9286-0f0855388dbd	true	introspection.token.claim
ab524fd9-212b-41d7-9286-0f0855388dbd	true	userinfo.token.claim
ab524fd9-212b-41d7-9286-0f0855388dbd	website	user.attribute
ab524fd9-212b-41d7-9286-0f0855388dbd	true	id.token.claim
ab524fd9-212b-41d7-9286-0f0855388dbd	true	access.token.claim
ab524fd9-212b-41d7-9286-0f0855388dbd	website	claim.name
ab524fd9-212b-41d7-9286-0f0855388dbd	String	jsonType.label
bb2e821a-d4d5-46fc-996b-62dac70ea5d7	true	introspection.token.claim
bb2e821a-d4d5-46fc-996b-62dac70ea5d7	true	userinfo.token.claim
bb2e821a-d4d5-46fc-996b-62dac70ea5d7	birthdate	user.attribute
bb2e821a-d4d5-46fc-996b-62dac70ea5d7	true	id.token.claim
bb2e821a-d4d5-46fc-996b-62dac70ea5d7	true	access.token.claim
bb2e821a-d4d5-46fc-996b-62dac70ea5d7	birthdate	claim.name
bb2e821a-d4d5-46fc-996b-62dac70ea5d7	String	jsonType.label
e8abd4d9-ab56-4255-9e07-f3fd31448301	true	introspection.token.claim
e8abd4d9-ab56-4255-9e07-f3fd31448301	true	userinfo.token.claim
e8abd4d9-ab56-4255-9e07-f3fd31448301	zoneinfo	user.attribute
e8abd4d9-ab56-4255-9e07-f3fd31448301	true	id.token.claim
e8abd4d9-ab56-4255-9e07-f3fd31448301	true	access.token.claim
e8abd4d9-ab56-4255-9e07-f3fd31448301	zoneinfo	claim.name
e8abd4d9-ab56-4255-9e07-f3fd31448301	String	jsonType.label
fa9bb112-a644-48c1-8527-6de0adf8ad7b	true	introspection.token.claim
fa9bb112-a644-48c1-8527-6de0adf8ad7b	true	userinfo.token.claim
fa9bb112-a644-48c1-8527-6de0adf8ad7b	updatedAt	user.attribute
fa9bb112-a644-48c1-8527-6de0adf8ad7b	true	id.token.claim
fa9bb112-a644-48c1-8527-6de0adf8ad7b	true	access.token.claim
fa9bb112-a644-48c1-8527-6de0adf8ad7b	updated_at	claim.name
fa9bb112-a644-48c1-8527-6de0adf8ad7b	long	jsonType.label
0d451588-2f5b-4933-8acb-7755fb471a63	true	introspection.token.claim
0d451588-2f5b-4933-8acb-7755fb471a63	true	userinfo.token.claim
0d451588-2f5b-4933-8acb-7755fb471a63	emailVerified	user.attribute
0d451588-2f5b-4933-8acb-7755fb471a63	true	id.token.claim
0d451588-2f5b-4933-8acb-7755fb471a63	true	access.token.claim
0d451588-2f5b-4933-8acb-7755fb471a63	email_verified	claim.name
0d451588-2f5b-4933-8acb-7755fb471a63	boolean	jsonType.label
8764d608-fae5-4d29-9464-1b9e34a45a89	true	introspection.token.claim
8764d608-fae5-4d29-9464-1b9e34a45a89	true	userinfo.token.claim
8764d608-fae5-4d29-9464-1b9e34a45a89	email	user.attribute
8764d608-fae5-4d29-9464-1b9e34a45a89	true	id.token.claim
8764d608-fae5-4d29-9464-1b9e34a45a89	true	access.token.claim
8764d608-fae5-4d29-9464-1b9e34a45a89	email	claim.name
8764d608-fae5-4d29-9464-1b9e34a45a89	String	jsonType.label
d6049b4b-fb68-4b28-a11a-78dcb2f8949f	formatted	user.attribute.formatted
d6049b4b-fb68-4b28-a11a-78dcb2f8949f	country	user.attribute.country
d6049b4b-fb68-4b28-a11a-78dcb2f8949f	true	introspection.token.claim
d6049b4b-fb68-4b28-a11a-78dcb2f8949f	postal_code	user.attribute.postal_code
d6049b4b-fb68-4b28-a11a-78dcb2f8949f	true	userinfo.token.claim
d6049b4b-fb68-4b28-a11a-78dcb2f8949f	street	user.attribute.street
d6049b4b-fb68-4b28-a11a-78dcb2f8949f	true	id.token.claim
d6049b4b-fb68-4b28-a11a-78dcb2f8949f	region	user.attribute.region
d6049b4b-fb68-4b28-a11a-78dcb2f8949f	true	access.token.claim
d6049b4b-fb68-4b28-a11a-78dcb2f8949f	locality	user.attribute.locality
9ae02f12-2570-4e12-bb20-df9e43d84a8b	true	introspection.token.claim
9ae02f12-2570-4e12-bb20-df9e43d84a8b	true	userinfo.token.claim
9ae02f12-2570-4e12-bb20-df9e43d84a8b	phoneNumberVerified	user.attribute
9ae02f12-2570-4e12-bb20-df9e43d84a8b	true	id.token.claim
9ae02f12-2570-4e12-bb20-df9e43d84a8b	true	access.token.claim
9ae02f12-2570-4e12-bb20-df9e43d84a8b	phone_number_verified	claim.name
9ae02f12-2570-4e12-bb20-df9e43d84a8b	boolean	jsonType.label
ca970342-6549-4eaa-82bf-08f9f3e184ba	true	introspection.token.claim
ca970342-6549-4eaa-82bf-08f9f3e184ba	true	userinfo.token.claim
ca970342-6549-4eaa-82bf-08f9f3e184ba	phoneNumber	user.attribute
ca970342-6549-4eaa-82bf-08f9f3e184ba	true	id.token.claim
ca970342-6549-4eaa-82bf-08f9f3e184ba	true	access.token.claim
ca970342-6549-4eaa-82bf-08f9f3e184ba	phone_number	claim.name
ca970342-6549-4eaa-82bf-08f9f3e184ba	String	jsonType.label
06391a1a-ad0c-4c84-adec-5b7f639e2ee7	true	introspection.token.claim
06391a1a-ad0c-4c84-adec-5b7f639e2ee7	true	multivalued
06391a1a-ad0c-4c84-adec-5b7f639e2ee7	foo	user.attribute
06391a1a-ad0c-4c84-adec-5b7f639e2ee7	true	access.token.claim
06391a1a-ad0c-4c84-adec-5b7f639e2ee7	realm_access.roles	claim.name
06391a1a-ad0c-4c84-adec-5b7f639e2ee7	String	jsonType.label
56f1af0c-1a93-46ec-a513-e74d20373cb1	true	introspection.token.claim
56f1af0c-1a93-46ec-a513-e74d20373cb1	true	access.token.claim
c796b891-270a-4617-9f6f-c48fb238848b	true	introspection.token.claim
c796b891-270a-4617-9f6f-c48fb238848b	true	multivalued
c796b891-270a-4617-9f6f-c48fb238848b	foo	user.attribute
c796b891-270a-4617-9f6f-c48fb238848b	true	access.token.claim
c796b891-270a-4617-9f6f-c48fb238848b	resource_access.${client_id}.roles	claim.name
c796b891-270a-4617-9f6f-c48fb238848b	String	jsonType.label
1679c109-9590-4673-b1a0-7d5ecaba8cb8	true	introspection.token.claim
1679c109-9590-4673-b1a0-7d5ecaba8cb8	true	access.token.claim
19bae4df-df7a-4ba6-ab3f-5ed511261bbf	true	introspection.token.claim
19bae4df-df7a-4ba6-ab3f-5ed511261bbf	true	multivalued
19bae4df-df7a-4ba6-ab3f-5ed511261bbf	foo	user.attribute
19bae4df-df7a-4ba6-ab3f-5ed511261bbf	true	id.token.claim
19bae4df-df7a-4ba6-ab3f-5ed511261bbf	true	access.token.claim
19bae4df-df7a-4ba6-ab3f-5ed511261bbf	groups	claim.name
19bae4df-df7a-4ba6-ab3f-5ed511261bbf	String	jsonType.label
567e2c1d-556e-47d9-91fb-ea0dd97125e3	true	introspection.token.claim
567e2c1d-556e-47d9-91fb-ea0dd97125e3	true	userinfo.token.claim
567e2c1d-556e-47d9-91fb-ea0dd97125e3	username	user.attribute
567e2c1d-556e-47d9-91fb-ea0dd97125e3	true	id.token.claim
567e2c1d-556e-47d9-91fb-ea0dd97125e3	true	access.token.claim
567e2c1d-556e-47d9-91fb-ea0dd97125e3	upn	claim.name
567e2c1d-556e-47d9-91fb-ea0dd97125e3	String	jsonType.label
9b2b8857-462d-4da7-93f2-0488b1fdacf3	true	introspection.token.claim
9b2b8857-462d-4da7-93f2-0488b1fdacf3	true	id.token.claim
9b2b8857-462d-4da7-93f2-0488b1fdacf3	true	access.token.claim
5b90801b-e8d1-4d70-a1a1-bc448c9e3e52	true	introspection.token.claim
5b90801b-e8d1-4d70-a1a1-bc448c9e3e52	true	access.token.claim
97ed5f8d-726c-43a8-b772-40ced76fbdf0	AUTH_TIME	user.session.note
97ed5f8d-726c-43a8-b772-40ced76fbdf0	true	introspection.token.claim
97ed5f8d-726c-43a8-b772-40ced76fbdf0	true	id.token.claim
97ed5f8d-726c-43a8-b772-40ced76fbdf0	true	access.token.claim
97ed5f8d-726c-43a8-b772-40ced76fbdf0	auth_time	claim.name
97ed5f8d-726c-43a8-b772-40ced76fbdf0	long	jsonType.label
02158905-8f5a-44d7-9d72-4ca2ec1b7ec9	client_id	user.session.note
02158905-8f5a-44d7-9d72-4ca2ec1b7ec9	true	introspection.token.claim
02158905-8f5a-44d7-9d72-4ca2ec1b7ec9	true	id.token.claim
02158905-8f5a-44d7-9d72-4ca2ec1b7ec9	true	access.token.claim
02158905-8f5a-44d7-9d72-4ca2ec1b7ec9	client_id	claim.name
02158905-8f5a-44d7-9d72-4ca2ec1b7ec9	String	jsonType.label
8bcec692-6c95-48cc-9153-82e8017da960	clientHost	user.session.note
8bcec692-6c95-48cc-9153-82e8017da960	true	introspection.token.claim
8bcec692-6c95-48cc-9153-82e8017da960	true	id.token.claim
8bcec692-6c95-48cc-9153-82e8017da960	true	access.token.claim
8bcec692-6c95-48cc-9153-82e8017da960	clientHost	claim.name
8bcec692-6c95-48cc-9153-82e8017da960	String	jsonType.label
8db0f190-4100-4fd7-ace3-67878105b761	clientAddress	user.session.note
8db0f190-4100-4fd7-ace3-67878105b761	true	introspection.token.claim
8db0f190-4100-4fd7-ace3-67878105b761	true	id.token.claim
8db0f190-4100-4fd7-ace3-67878105b761	true	access.token.claim
8db0f190-4100-4fd7-ace3-67878105b761	clientAddress	claim.name
8db0f190-4100-4fd7-ace3-67878105b761	String	jsonType.label
3cd01669-15d8-4e5c-ab01-399f11971857	true	introspection.token.claim
3cd01669-15d8-4e5c-ab01-399f11971857	true	multivalued
3cd01669-15d8-4e5c-ab01-399f11971857	true	id.token.claim
3cd01669-15d8-4e5c-ab01-399f11971857	true	access.token.claim
3cd01669-15d8-4e5c-ab01-399f11971857	organization	claim.name
3cd01669-15d8-4e5c-ab01-399f11971857	String	jsonType.label
d7e49f3d-73ca-4279-b1b8-40a32c3d0627	false	single
d7e49f3d-73ca-4279-b1b8-40a32c3d0627	Basic	attribute.nameformat
d7e49f3d-73ca-4279-b1b8-40a32c3d0627	Role	attribute.name
100921bf-3266-48bc-9a38-3cd3cdb29550	true	introspection.token.claim
100921bf-3266-48bc-9a38-3cd3cdb29550	true	userinfo.token.claim
100921bf-3266-48bc-9a38-3cd3cdb29550	nickname	user.attribute
100921bf-3266-48bc-9a38-3cd3cdb29550	true	id.token.claim
100921bf-3266-48bc-9a38-3cd3cdb29550	true	access.token.claim
100921bf-3266-48bc-9a38-3cd3cdb29550	nickname	claim.name
100921bf-3266-48bc-9a38-3cd3cdb29550	String	jsonType.label
14eaac5b-969a-4528-bd40-19d087e17df3	true	introspection.token.claim
14eaac5b-969a-4528-bd40-19d087e17df3	true	userinfo.token.claim
14eaac5b-969a-4528-bd40-19d087e17df3	middleName	user.attribute
14eaac5b-969a-4528-bd40-19d087e17df3	true	id.token.claim
14eaac5b-969a-4528-bd40-19d087e17df3	true	access.token.claim
14eaac5b-969a-4528-bd40-19d087e17df3	middle_name	claim.name
14eaac5b-969a-4528-bd40-19d087e17df3	String	jsonType.label
49434dc5-6631-4610-88ad-7e181d30810a	true	introspection.token.claim
49434dc5-6631-4610-88ad-7e181d30810a	true	userinfo.token.claim
49434dc5-6631-4610-88ad-7e181d30810a	true	id.token.claim
49434dc5-6631-4610-88ad-7e181d30810a	true	access.token.claim
5b63e600-3753-468a-851e-5b9ceed71c55	true	introspection.token.claim
5b63e600-3753-468a-851e-5b9ceed71c55	true	userinfo.token.claim
5b63e600-3753-468a-851e-5b9ceed71c55	updatedAt	user.attribute
5b63e600-3753-468a-851e-5b9ceed71c55	true	id.token.claim
5b63e600-3753-468a-851e-5b9ceed71c55	true	access.token.claim
5b63e600-3753-468a-851e-5b9ceed71c55	updated_at	claim.name
5b63e600-3753-468a-851e-5b9ceed71c55	long	jsonType.label
619d7fe3-2b54-44d5-8d4f-f4970e4fffa3	true	introspection.token.claim
619d7fe3-2b54-44d5-8d4f-f4970e4fffa3	true	userinfo.token.claim
619d7fe3-2b54-44d5-8d4f-f4970e4fffa3	zoneinfo	user.attribute
619d7fe3-2b54-44d5-8d4f-f4970e4fffa3	true	id.token.claim
619d7fe3-2b54-44d5-8d4f-f4970e4fffa3	true	access.token.claim
619d7fe3-2b54-44d5-8d4f-f4970e4fffa3	zoneinfo	claim.name
619d7fe3-2b54-44d5-8d4f-f4970e4fffa3	String	jsonType.label
64465c28-6f96-4882-85f5-e6bcd4ef7334	true	introspection.token.claim
64465c28-6f96-4882-85f5-e6bcd4ef7334	true	userinfo.token.claim
64465c28-6f96-4882-85f5-e6bcd4ef7334	website	user.attribute
64465c28-6f96-4882-85f5-e6bcd4ef7334	true	id.token.claim
64465c28-6f96-4882-85f5-e6bcd4ef7334	true	access.token.claim
64465c28-6f96-4882-85f5-e6bcd4ef7334	website	claim.name
64465c28-6f96-4882-85f5-e6bcd4ef7334	String	jsonType.label
6a3ef67b-f7d7-4cc1-a1a3-d5b72b786314	true	introspection.token.claim
6a3ef67b-f7d7-4cc1-a1a3-d5b72b786314	true	userinfo.token.claim
6a3ef67b-f7d7-4cc1-a1a3-d5b72b786314	lastName	user.attribute
6a3ef67b-f7d7-4cc1-a1a3-d5b72b786314	true	id.token.claim
6a3ef67b-f7d7-4cc1-a1a3-d5b72b786314	true	access.token.claim
6a3ef67b-f7d7-4cc1-a1a3-d5b72b786314	family_name	claim.name
6a3ef67b-f7d7-4cc1-a1a3-d5b72b786314	String	jsonType.label
89e39e7c-d00a-4136-86b8-7f8cc963dc08	true	introspection.token.claim
89e39e7c-d00a-4136-86b8-7f8cc963dc08	true	userinfo.token.claim
89e39e7c-d00a-4136-86b8-7f8cc963dc08	birthdate	user.attribute
89e39e7c-d00a-4136-86b8-7f8cc963dc08	true	id.token.claim
89e39e7c-d00a-4136-86b8-7f8cc963dc08	true	access.token.claim
89e39e7c-d00a-4136-86b8-7f8cc963dc08	birthdate	claim.name
89e39e7c-d00a-4136-86b8-7f8cc963dc08	String	jsonType.label
95034058-17e7-4201-9e20-4c9091db7ed8	true	introspection.token.claim
95034058-17e7-4201-9e20-4c9091db7ed8	true	userinfo.token.claim
95034058-17e7-4201-9e20-4c9091db7ed8	picture	user.attribute
95034058-17e7-4201-9e20-4c9091db7ed8	true	id.token.claim
95034058-17e7-4201-9e20-4c9091db7ed8	true	access.token.claim
95034058-17e7-4201-9e20-4c9091db7ed8	picture	claim.name
95034058-17e7-4201-9e20-4c9091db7ed8	String	jsonType.label
9ddc9190-b3fc-4eb3-9fcf-b4e283f6e006	true	introspection.token.claim
9ddc9190-b3fc-4eb3-9fcf-b4e283f6e006	true	userinfo.token.claim
9ddc9190-b3fc-4eb3-9fcf-b4e283f6e006	locale	user.attribute
9ddc9190-b3fc-4eb3-9fcf-b4e283f6e006	true	id.token.claim
9ddc9190-b3fc-4eb3-9fcf-b4e283f6e006	true	access.token.claim
9ddc9190-b3fc-4eb3-9fcf-b4e283f6e006	locale	claim.name
9ddc9190-b3fc-4eb3-9fcf-b4e283f6e006	String	jsonType.label
be562e27-0970-47b5-94ca-2d41fa53d179	true	introspection.token.claim
be562e27-0970-47b5-94ca-2d41fa53d179	true	userinfo.token.claim
be562e27-0970-47b5-94ca-2d41fa53d179	gender	user.attribute
be562e27-0970-47b5-94ca-2d41fa53d179	true	id.token.claim
be562e27-0970-47b5-94ca-2d41fa53d179	true	access.token.claim
be562e27-0970-47b5-94ca-2d41fa53d179	gender	claim.name
be562e27-0970-47b5-94ca-2d41fa53d179	String	jsonType.label
df368ebf-a356-4612-81cc-3c60463583aa	true	introspection.token.claim
df368ebf-a356-4612-81cc-3c60463583aa	true	userinfo.token.claim
df368ebf-a356-4612-81cc-3c60463583aa	firstName	user.attribute
df368ebf-a356-4612-81cc-3c60463583aa	true	id.token.claim
df368ebf-a356-4612-81cc-3c60463583aa	true	access.token.claim
df368ebf-a356-4612-81cc-3c60463583aa	given_name	claim.name
df368ebf-a356-4612-81cc-3c60463583aa	String	jsonType.label
e04b00d5-fcdc-4259-8e92-d237e27c9768	true	introspection.token.claim
e04b00d5-fcdc-4259-8e92-d237e27c9768	true	userinfo.token.claim
e04b00d5-fcdc-4259-8e92-d237e27c9768	profile	user.attribute
e04b00d5-fcdc-4259-8e92-d237e27c9768	true	id.token.claim
e04b00d5-fcdc-4259-8e92-d237e27c9768	true	access.token.claim
e04b00d5-fcdc-4259-8e92-d237e27c9768	profile	claim.name
e04b00d5-fcdc-4259-8e92-d237e27c9768	String	jsonType.label
e0a557db-acdc-4049-b1ec-100a951ffcfc	true	introspection.token.claim
e0a557db-acdc-4049-b1ec-100a951ffcfc	true	userinfo.token.claim
e0a557db-acdc-4049-b1ec-100a951ffcfc	username	user.attribute
e0a557db-acdc-4049-b1ec-100a951ffcfc	true	id.token.claim
e0a557db-acdc-4049-b1ec-100a951ffcfc	true	access.token.claim
e0a557db-acdc-4049-b1ec-100a951ffcfc	preferred_username	claim.name
e0a557db-acdc-4049-b1ec-100a951ffcfc	String	jsonType.label
5e1d1af0-d110-4303-ae5e-648ab140200a	true	introspection.token.claim
5e1d1af0-d110-4303-ae5e-648ab140200a	true	userinfo.token.claim
5e1d1af0-d110-4303-ae5e-648ab140200a	email	user.attribute
5e1d1af0-d110-4303-ae5e-648ab140200a	true	id.token.claim
5e1d1af0-d110-4303-ae5e-648ab140200a	true	access.token.claim
5e1d1af0-d110-4303-ae5e-648ab140200a	email	claim.name
5e1d1af0-d110-4303-ae5e-648ab140200a	String	jsonType.label
b884e5ac-a304-4cb6-a03f-12d913998f12	true	introspection.token.claim
b884e5ac-a304-4cb6-a03f-12d913998f12	true	userinfo.token.claim
b884e5ac-a304-4cb6-a03f-12d913998f12	emailVerified	user.attribute
b884e5ac-a304-4cb6-a03f-12d913998f12	true	id.token.claim
b884e5ac-a304-4cb6-a03f-12d913998f12	true	access.token.claim
b884e5ac-a304-4cb6-a03f-12d913998f12	email_verified	claim.name
b884e5ac-a304-4cb6-a03f-12d913998f12	boolean	jsonType.label
9c090eb2-2f90-4e43-9a0c-e923dcf35968	formatted	user.attribute.formatted
9c090eb2-2f90-4e43-9a0c-e923dcf35968	country	user.attribute.country
9c090eb2-2f90-4e43-9a0c-e923dcf35968	true	introspection.token.claim
9c090eb2-2f90-4e43-9a0c-e923dcf35968	postal_code	user.attribute.postal_code
9c090eb2-2f90-4e43-9a0c-e923dcf35968	true	userinfo.token.claim
9c090eb2-2f90-4e43-9a0c-e923dcf35968	street	user.attribute.street
9c090eb2-2f90-4e43-9a0c-e923dcf35968	true	id.token.claim
9c090eb2-2f90-4e43-9a0c-e923dcf35968	region	user.attribute.region
9c090eb2-2f90-4e43-9a0c-e923dcf35968	true	access.token.claim
9c090eb2-2f90-4e43-9a0c-e923dcf35968	locality	user.attribute.locality
138c726e-466b-4d3a-a2a5-cab0d8ab2f6e	true	introspection.token.claim
138c726e-466b-4d3a-a2a5-cab0d8ab2f6e	true	userinfo.token.claim
138c726e-466b-4d3a-a2a5-cab0d8ab2f6e	phoneNumberVerified	user.attribute
138c726e-466b-4d3a-a2a5-cab0d8ab2f6e	true	id.token.claim
138c726e-466b-4d3a-a2a5-cab0d8ab2f6e	true	access.token.claim
138c726e-466b-4d3a-a2a5-cab0d8ab2f6e	phone_number_verified	claim.name
138c726e-466b-4d3a-a2a5-cab0d8ab2f6e	boolean	jsonType.label
8c1c571f-8558-43cb-8e26-5286cc2db738	true	introspection.token.claim
8c1c571f-8558-43cb-8e26-5286cc2db738	true	userinfo.token.claim
8c1c571f-8558-43cb-8e26-5286cc2db738	phoneNumber	user.attribute
8c1c571f-8558-43cb-8e26-5286cc2db738	true	id.token.claim
8c1c571f-8558-43cb-8e26-5286cc2db738	true	access.token.claim
8c1c571f-8558-43cb-8e26-5286cc2db738	phone_number	claim.name
8c1c571f-8558-43cb-8e26-5286cc2db738	String	jsonType.label
2547f839-155c-42f4-8fa6-748ceaabb2be	true	introspection.token.claim
2547f839-155c-42f4-8fa6-748ceaabb2be	true	access.token.claim
46543c8e-f4af-4ed8-8801-571c576d6c29	true	introspection.token.claim
46543c8e-f4af-4ed8-8801-571c576d6c29	true	multivalued
46543c8e-f4af-4ed8-8801-571c576d6c29	foo	user.attribute
46543c8e-f4af-4ed8-8801-571c576d6c29	true	access.token.claim
46543c8e-f4af-4ed8-8801-571c576d6c29	realm_access.roles	claim.name
46543c8e-f4af-4ed8-8801-571c576d6c29	String	jsonType.label
83b779f4-3041-488d-913c-3fc65abd2530	true	introspection.token.claim
83b779f4-3041-488d-913c-3fc65abd2530	true	multivalued
83b779f4-3041-488d-913c-3fc65abd2530	foo	user.attribute
83b779f4-3041-488d-913c-3fc65abd2530	true	access.token.claim
83b779f4-3041-488d-913c-3fc65abd2530	resource_access.${client_id}.roles	claim.name
83b779f4-3041-488d-913c-3fc65abd2530	String	jsonType.label
9d262a96-86fd-42d0-bfd1-abbbf759266c	true	introspection.token.claim
9d262a96-86fd-42d0-bfd1-abbbf759266c	true	access.token.claim
53bcfb45-4b1d-4672-8df3-45f2b69231dc	true	introspection.token.claim
53bcfb45-4b1d-4672-8df3-45f2b69231dc	true	userinfo.token.claim
53bcfb45-4b1d-4672-8df3-45f2b69231dc	username	user.attribute
53bcfb45-4b1d-4672-8df3-45f2b69231dc	true	id.token.claim
53bcfb45-4b1d-4672-8df3-45f2b69231dc	true	access.token.claim
53bcfb45-4b1d-4672-8df3-45f2b69231dc	upn	claim.name
53bcfb45-4b1d-4672-8df3-45f2b69231dc	String	jsonType.label
5d9b06b8-55af-46bf-927d-aa8d5e7d010e	true	introspection.token.claim
5d9b06b8-55af-46bf-927d-aa8d5e7d010e	true	multivalued
5d9b06b8-55af-46bf-927d-aa8d5e7d010e	foo	user.attribute
5d9b06b8-55af-46bf-927d-aa8d5e7d010e	true	id.token.claim
5d9b06b8-55af-46bf-927d-aa8d5e7d010e	true	access.token.claim
5d9b06b8-55af-46bf-927d-aa8d5e7d010e	groups	claim.name
5d9b06b8-55af-46bf-927d-aa8d5e7d010e	String	jsonType.label
d80c2cb9-edff-4bb9-bc91-f15c6c711f02	true	introspection.token.claim
d80c2cb9-edff-4bb9-bc91-f15c6c711f02	true	id.token.claim
d80c2cb9-edff-4bb9-bc91-f15c6c711f02	true	access.token.claim
6e1fa6f4-0fa9-40d6-a121-c7e254d6f014	AUTH_TIME	user.session.note
6e1fa6f4-0fa9-40d6-a121-c7e254d6f014	true	introspection.token.claim
6e1fa6f4-0fa9-40d6-a121-c7e254d6f014	true	id.token.claim
6e1fa6f4-0fa9-40d6-a121-c7e254d6f014	true	access.token.claim
6e1fa6f4-0fa9-40d6-a121-c7e254d6f014	auth_time	claim.name
6e1fa6f4-0fa9-40d6-a121-c7e254d6f014	long	jsonType.label
8b280690-ea19-4c52-8ac5-6a8452d6a795	true	introspection.token.claim
8b280690-ea19-4c52-8ac5-6a8452d6a795	true	access.token.claim
b72e6702-0a7d-4091-b1ff-66738a0b6bbe	clientAddress	user.session.note
b72e6702-0a7d-4091-b1ff-66738a0b6bbe	true	introspection.token.claim
b72e6702-0a7d-4091-b1ff-66738a0b6bbe	true	id.token.claim
b72e6702-0a7d-4091-b1ff-66738a0b6bbe	true	access.token.claim
b72e6702-0a7d-4091-b1ff-66738a0b6bbe	clientAddress	claim.name
b72e6702-0a7d-4091-b1ff-66738a0b6bbe	String	jsonType.label
da32b4fb-2f90-4c00-9bcb-a79ffd466565	clientHost	user.session.note
da32b4fb-2f90-4c00-9bcb-a79ffd466565	true	introspection.token.claim
da32b4fb-2f90-4c00-9bcb-a79ffd466565	true	id.token.claim
da32b4fb-2f90-4c00-9bcb-a79ffd466565	true	access.token.claim
da32b4fb-2f90-4c00-9bcb-a79ffd466565	clientHost	claim.name
da32b4fb-2f90-4c00-9bcb-a79ffd466565	String	jsonType.label
e5f4e98f-5f89-4300-ba55-e33dd5bcf279	client_id	user.session.note
e5f4e98f-5f89-4300-ba55-e33dd5bcf279	true	introspection.token.claim
e5f4e98f-5f89-4300-ba55-e33dd5bcf279	true	id.token.claim
e5f4e98f-5f89-4300-ba55-e33dd5bcf279	true	access.token.claim
e5f4e98f-5f89-4300-ba55-e33dd5bcf279	client_id	claim.name
e5f4e98f-5f89-4300-ba55-e33dd5bcf279	String	jsonType.label
15629814-535c-4864-b36f-5ce4b32d4b19	true	introspection.token.claim
15629814-535c-4864-b36f-5ce4b32d4b19	true	multivalued
15629814-535c-4864-b36f-5ce4b32d4b19	true	id.token.claim
15629814-535c-4864-b36f-5ce4b32d4b19	true	access.token.claim
15629814-535c-4864-b36f-5ce4b32d4b19	organization	claim.name
15629814-535c-4864-b36f-5ce4b32d4b19	String	jsonType.label
590b29ec-499e-4d89-9fc1-cf9d709b1e5b	true	introspection.token.claim
590b29ec-499e-4d89-9fc1-cf9d709b1e5b	true	userinfo.token.claim
590b29ec-499e-4d89-9fc1-cf9d709b1e5b	locale	user.attribute
590b29ec-499e-4d89-9fc1-cf9d709b1e5b	true	id.token.claim
590b29ec-499e-4d89-9fc1-cf9d709b1e5b	true	access.token.claim
590b29ec-499e-4d89-9fc1-cf9d709b1e5b	locale	claim.name
590b29ec-499e-4d89-9fc1-cf9d709b1e5b	String	jsonType.label
\.


COPY keycloak.realm (id, access_code_lifespan, user_action_lifespan, access_token_lifespan, account_theme, admin_theme,
                     email_theme, enabled, events_enabled, events_expiration, login_theme, name, not_before,
                     password_policy, registration_allowed, remember_me, reset_password_allowed, social, ssl_required,
                     sso_idle_timeout, sso_max_lifespan, update_profile_on_soc_login, verify_email, master_admin_client,
                     login_lifespan, internationalization_enabled, default_locale, reg_email_as_username,
                     admin_events_enabled, admin_events_details_enabled, edit_username_allowed, otp_policy_counter,
                     otp_policy_window, otp_policy_period, otp_policy_digits, otp_policy_alg, otp_policy_type,
                     browser_flow, registration_flow, direct_grant_flow, reset_credentials_flow, client_auth_flow,
                     offline_session_idle_timeout, revoke_refresh_token, access_token_life_implicit,
                     login_with_email_allowed, duplicate_emails_allowed, docker_auth_flow, refresh_token_max_reuse,
                     allow_user_managed_access, sso_max_lifespan_remember_me, sso_idle_timeout_remember_me,
                     default_role) FROM stdin;
bde3ba76-fd10-4c67-8d50-9435f4f48ccc	60	300	300	\N	\N	\N	t	f	0	\N	jodrive-realm	0	\N	t	t	t	f	NONE	1800	36000	f	f	0936c0bc-f90c-4bd0-8208-1d3a997b8eca	1800	f	\N	f	f	f	f	0	1	30	6	HmacSHA1	totp	5eaaa984-74ce-4b09-a9e0-d827adbeb2ff	534bfb6a-494c-43c6-a685-dc96179d6ad0	28eb0bb5-8a43-493d-8fd2-2daf1e15d6ac	773c9c90-190a-4366-b83e-3abec43ccb4b	55c325b4-b949-4d1d-aa8e-3d89eae34f1d	2592000	f	900	t	f	3bfcbfd0-dbd8-40b3-8d07-f440ec385ea4	0	f	0	0	da241a9c-0230-44b2-859b-d198a2b0863a
fc4359ab-c586-4d33-a1e0-6382887b081c	60	300	60	\N	\N	\N	t	f	0	\N	master	0	\N	f	f	f	f	NONE	1800	36000	f	f	ae3ea88f-aecf-4fec-b290-bb7594719e44	1800	f	\N	f	f	f	f	0	1	30	6	HmacSHA1	totp	ec8c7366-98cc-4c0a-88cf-c366091f9814	60be8aa9-c1a4-4260-b9e2-b8f3e664626a	c76c5562-b5db-4f4b-9af0-62a9dee98b0a	77ad6b43-3234-4d5f-b305-5851d4a75e11	d69a6bf8-de1a-4a74-b68d-d0b5f9439dd7	2592000	f	900	t	f	63f29d08-cb16-43cf-bb50-a7e73f1227a8	0	f	0	0	ee605334-ad03-4dd9-b1e1-abdda1eec33a
\.


COPY keycloak.realm_attribute (name, realm_id, value) FROM stdin;
_browser_header.contentSecurityPolicyReportOnly	fc4359ab-c586-4d33-a1e0-6382887b081c
_browser_header.xContentTypeOptions	fc4359ab-c586-4d33-a1e0-6382887b081c	nosniff
_browser_header.referrerPolicy	fc4359ab-c586-4d33-a1e0-6382887b081c	no-referrer
_browser_header.xRobotsTag	fc4359ab-c586-4d33-a1e0-6382887b081c	none
_browser_header.xFrameOptions	fc4359ab-c586-4d33-a1e0-6382887b081c	SAMEORIGIN
_browser_header.contentSecurityPolicy	fc4359ab-c586-4d33-a1e0-6382887b081c	frame-src 'self'; frame-ancestors 'self'; object-src 'none';
_browser_header.strictTransportSecurity	fc4359ab-c586-4d33-a1e0-6382887b081c	max-age=31536000; includeSubDomains
bruteForceProtected	fc4359ab-c586-4d33-a1e0-6382887b081c	false
permanentLockout	fc4359ab-c586-4d33-a1e0-6382887b081c	false
maxTemporaryLockouts	fc4359ab-c586-4d33-a1e0-6382887b081c	0
bruteForceStrategy	fc4359ab-c586-4d33-a1e0-6382887b081c	MULTIPLE
maxFailureWaitSeconds	fc4359ab-c586-4d33-a1e0-6382887b081c	900
minimumQuickLoginWaitSeconds	fc4359ab-c586-4d33-a1e0-6382887b081c	60
waitIncrementSeconds	fc4359ab-c586-4d33-a1e0-6382887b081c	60
quickLoginCheckMilliSeconds	fc4359ab-c586-4d33-a1e0-6382887b081c	1000
maxDeltaTimeSeconds	fc4359ab-c586-4d33-a1e0-6382887b081c	43200
failureFactor	fc4359ab-c586-4d33-a1e0-6382887b081c	30
realmReusableOtpCode	fc4359ab-c586-4d33-a1e0-6382887b081c	false
firstBrokerLoginFlowId	fc4359ab-c586-4d33-a1e0-6382887b081c	d7dff088-db5c-427a-bac1-8472d75ef7e6
displayName	fc4359ab-c586-4d33-a1e0-6382887b081c	Keycloak
displayNameHtml	fc4359ab-c586-4d33-a1e0-6382887b081c	<div class="kc-logo-text"><span>Keycloak</span></div>
defaultSignatureAlgorithm	fc4359ab-c586-4d33-a1e0-6382887b081c	RS256
offlineSessionMaxLifespanEnabled	fc4359ab-c586-4d33-a1e0-6382887b081c	false
offlineSessionMaxLifespan	fc4359ab-c586-4d33-a1e0-6382887b081c	5184000
_browser_header.contentSecurityPolicyReportOnly	bde3ba76-fd10-4c67-8d50-9435f4f48ccc
_browser_header.xContentTypeOptions	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	nosniff
_browser_header.referrerPolicy	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	no-referrer
_browser_header.xRobotsTag	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	none
_browser_header.xFrameOptions	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	SAMEORIGIN
_browser_header.contentSecurityPolicy	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	frame-src 'self'; frame-ancestors 'self'; object-src 'none';
_browser_header.strictTransportSecurity	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	max-age=31536000; includeSubDomains
bruteForceProtected	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	false
permanentLockout	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	false
maxTemporaryLockouts	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	0
bruteForceStrategy	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	MULTIPLE
maxFailureWaitSeconds	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	900
minimumQuickLoginWaitSeconds	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	60
waitIncrementSeconds	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	60
quickLoginCheckMilliSeconds	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	1000
maxDeltaTimeSeconds	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	43200
failureFactor	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	30
realmReusableOtpCode	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	false
defaultSignatureAlgorithm	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	RS256
offlineSessionMaxLifespanEnabled	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	false
offlineSessionMaxLifespan	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	5184000
actionTokenGeneratedByAdminLifespan	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	43200
actionTokenGeneratedByUserLifespan	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	300
oauth2DeviceCodeLifespan	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	600
oauth2DevicePollingInterval	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	5
webAuthnPolicyRpEntityName	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	keycloak
webAuthnPolicySignatureAlgorithms	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	ES256,RS256
webAuthnPolicyRpId	bde3ba76-fd10-4c67-8d50-9435f4f48ccc
webAuthnPolicyAttestationConveyancePreference	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	not specified
webAuthnPolicyAuthenticatorAttachment	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	not specified
webAuthnPolicyRequireResidentKey	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	not specified
webAuthnPolicyUserVerificationRequirement	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	not specified
webAuthnPolicyCreateTimeout	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	0
webAuthnPolicyAvoidSameAuthenticatorRegister	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	false
webAuthnPolicyRpEntityNamePasswordless	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	keycloak
webAuthnPolicySignatureAlgorithmsPasswordless	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	ES256,RS256
webAuthnPolicyRpIdPasswordless	bde3ba76-fd10-4c67-8d50-9435f4f48ccc
webAuthnPolicyAttestationConveyancePreferencePasswordless	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	not specified
webAuthnPolicyAuthenticatorAttachmentPasswordless	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	not specified
webAuthnPolicyRequireResidentKeyPasswordless	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	not specified
webAuthnPolicyUserVerificationRequirementPasswordless	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	not specified
webAuthnPolicyCreateTimeoutPasswordless	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	0
webAuthnPolicyAvoidSameAuthenticatorRegisterPasswordless	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	false
cibaBackchannelTokenDeliveryMode	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	poll
cibaExpiresIn	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	120
cibaInterval	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	5
cibaAuthRequestedUserHint	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	login_hint
parRequestUriLifespan	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	60
firstBrokerLoginFlowId	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	3b756761-9f06-41a6-bfea-c9f5c2eb8f84
organizationsEnabled	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	false
adminPermissionsEnabled	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	false
verifiableCredentialsEnabled	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	false
clientSessionIdleTimeout	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	0
clientSessionMaxLifespan	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	0
clientOfflineSessionIdleTimeout	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	0
clientOfflineSessionMaxLifespan	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	0
client-policies.profiles	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	{"profiles":[]}
client-policies.policies	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	{"policies":[]}
\.


COPY keycloak.realm_default_groups (realm_id, group_id) FROM stdin;
\.


COPY keycloak.realm_enabled_event_types (realm_id, value) FROM stdin;
\.


COPY keycloak.realm_events_listeners (realm_id, value) FROM stdin;
fc4359ab-c586-4d33-a1e0-6382887b081c	jboss-logging
bde3ba76-fd10-4c67-8d50-9435f4f48ccc	jboss-logging
\.


COPY keycloak.realm_localizations (realm_id, locale, texts) FROM stdin;
\.


COPY keycloak.realm_required_credential (type, form_label, input, secret, realm_id) FROM stdin;
password	password	t	t	fc4359ab-c586-4d33-a1e0-6382887b081c
password	password	t	t	bde3ba76-fd10-4c67-8d50-9435f4f48ccc
\.


COPY keycloak.realm_smtp_config (realm_id, value, name) FROM stdin;
\.


COPY keycloak.realm_supported_locales (realm_id, value) FROM stdin;
\.


COPY keycloak.redirect_uris (client_id, value) FROM stdin;
b0de488c-4330-4955-91e2-f8dfb13e054b	/realms/master/account/*
ee76a050-87a8-4470-82e6-1a09a0e47223	/realms/master/account/*
108d6f49-22ba-43f2-84e8-105fbc9e9cdd	/admin/master/console/*
da0a22ac-4993-40b2-a719-2a2f01790525	/realms/jodrive-realm/account/*
c7633729-6ca9-4cc4-9153-86f6d352230d	/realms/jodrive-realm/account/*
495ee007-a202-41b2-b78c-3ce14d1da8ce	/admin/jodrive-realm/console/*
05b20b1d-d90a-4d28-b31f-73b413c99bf1	http://localhost:5173/*
\.


COPY keycloak.required_action_config (required_action_id, value, name) FROM stdin;
\.


COPY keycloak.required_action_provider (id, alias, name, realm_id, enabled, default_action, provider_id,
                                        priority) FROM stdin;
0baab8a7-d934-4ede-9f8d-583a784fea74	VERIFY_EMAIL	Verify Email	fc4359ab-c586-4d33-a1e0-6382887b081c	t	f	VERIFY_EMAIL	50
4eb1d059-9ef4-4c20-a990-476503c3e554	UPDATE_PROFILE	Update Profile	fc4359ab-c586-4d33-a1e0-6382887b081c	t	f	UPDATE_PROFILE	40
ec9aead8-5037-4779-9924-8c20683984bc	CONFIGURE_TOTP	Configure OTP	fc4359ab-c586-4d33-a1e0-6382887b081c	t	f	CONFIGURE_TOTP	10
513fac34-d41b-4a3d-947e-eab1b8a4e0d0	UPDATE_PASSWORD	Update Password	fc4359ab-c586-4d33-a1e0-6382887b081c	t	f	UPDATE_PASSWORD	30
8e7bda59-f23d-48e8-a6fa-ff5513e60ba9	TERMS_AND_CONDITIONS	Terms and Conditions	fc4359ab-c586-4d33-a1e0-6382887b081c	f	f	TERMS_AND_CONDITIONS	20
6599b04b-1d98-4243-8801-3286f38b4b72	delete_account	Delete Account	fc4359ab-c586-4d33-a1e0-6382887b081c	f	f	delete_account	60
c5287c11-08f6-4b1d-ab88-7125d1c61cfc	delete_credential	Delete Credential	fc4359ab-c586-4d33-a1e0-6382887b081c	t	f	delete_credential	100
74a8aba2-7c45-4cf6-880b-17ca32dee474	update_user_locale	Update User Locale	fc4359ab-c586-4d33-a1e0-6382887b081c	t	f	update_user_locale	1000
1cb7c36a-d313-4591-bee0-8912c47f5183	webauthn-register	Webauthn Register	fc4359ab-c586-4d33-a1e0-6382887b081c	t	f	webauthn-register	70
86ae53ea-3f10-4608-88f3-55d0c09f2e99	webauthn-register-passwordless	Webauthn Register Passwordless	fc4359ab-c586-4d33-a1e0-6382887b081c	t	f	webauthn-register-passwordless	80
9e8ed4e8-71f4-483c-abc0-e183d604d74f	VERIFY_PROFILE	Verify Profile	fc4359ab-c586-4d33-a1e0-6382887b081c	t	f	VERIFY_PROFILE	90
0066cb76-9845-4bfc-9a85-ecc9ce24f036	VERIFY_EMAIL	Verify Email	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	t	f	VERIFY_EMAIL	50
e6dd02e3-fa72-4264-afc4-13412e9c99fe	UPDATE_PROFILE	Update Profile	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	t	f	UPDATE_PROFILE	40
d5fe1c92-30c5-4ff0-955e-74b126eacea7	CONFIGURE_TOTP	Configure OTP	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	t	f	CONFIGURE_TOTP	10
5258d106-e610-47df-92c7-898b7fa7c38f	UPDATE_PASSWORD	Update Password	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	t	f	UPDATE_PASSWORD	30
ee57602f-8d8e-464b-b757-ead9e5cf3d00	TERMS_AND_CONDITIONS	Terms and Conditions	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	f	f	TERMS_AND_CONDITIONS	20
55ef1cc8-a261-4b6a-a117-8c70bd90545e	delete_account	Delete Account	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	f	f	delete_account	60
4b7dc04f-32cd-4ea2-b999-f8b32454140c	delete_credential	Delete Credential	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	t	f	delete_credential	100
e1f01f8a-c687-4520-8094-0ee163e7eaed	update_user_locale	Update User Locale	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	t	f	update_user_locale	1000
7d513027-e96e-40e0-b159-dc5792f7472c	webauthn-register	Webauthn Register	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	t	f	webauthn-register	70
8fc4b220-f9d8-4331-81ef-00af74cc1d99	webauthn-register-passwordless	Webauthn Register Passwordless	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	t	f	webauthn-register-passwordless	80
ce6ac40a-801f-491c-b0ae-2a166264d0b7	VERIFY_PROFILE	Verify Profile	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	t	f	VERIFY_PROFILE	90
\.


COPY keycloak.resource_attribute (id, name, value, resource_id) FROM stdin;
\.


COPY keycloak.resource_policy (resource_id, policy_id) FROM stdin;
\.


COPY keycloak.resource_scope (resource_id, scope_id) FROM stdin;
\.


COPY keycloak.resource_server (id, allow_rs_remote_mgmt, policy_enforce_mode, decision_strategy) FROM stdin;
\.


COPY keycloak.resource_server_perm_ticket (id, owner, requester, created_timestamp, granted_timestamp, resource_id,
                                           scope_id, resource_server_id, policy_id) FROM stdin;
\.


COPY keycloak.resource_server_policy (id, name, description, type, decision_strategy, logic, resource_server_id,
                                      owner) FROM stdin;
\.


COPY keycloak.resource_server_resource (id, name, type, icon_uri, owner, resource_server_id, owner_managed_access,
                                        display_name) FROM stdin;
\.


COPY keycloak.resource_server_scope (id, name, icon_uri, resource_server_id, display_name) FROM stdin;
\.


COPY keycloak.resource_uris (resource_id, value) FROM stdin;
\.


COPY keycloak.revoked_token (id, expire) FROM stdin;
\.


COPY keycloak.role_attribute (id, role_id, name, value) FROM stdin;
\.


COPY keycloak.scope_mapping (client_id, role_id) FROM stdin;
ee76a050-87a8-4470-82e6-1a09a0e47223	5c738e1f-c579-4144-a96c-18a066451b74
ee76a050-87a8-4470-82e6-1a09a0e47223	1630d110-0b4e-4099-bdc0-38a6651e3550
c7633729-6ca9-4cc4-9153-86f6d352230d	2248cd8f-a233-4393-9257-f2e4b3953667
c7633729-6ca9-4cc4-9153-86f6d352230d	fffbb75c-d70b-4b6c-9048-158d57e5ee99
\.


COPY keycloak.scope_policy (scope_id, policy_id) FROM stdin;
\.


COPY keycloak.server_config (server_config_key, value, version) FROM stdin;
\.


COPY keycloak.user_attribute (name, value, user_id, id, long_value_hash, long_value_hash_lower_case,
                              long_value) FROM stdin;
is_temporary_admin	true	1d071bc2-3f12-462a-b686-079304d649a0	08c1df48-89e5-4c36-b2e3-0c2921549083	\N	\N	\N
\.


COPY keycloak.user_consent (id, client_id, user_id, created_date, last_updated_date, client_storage_provider,
                            external_client_id) FROM stdin;
\.


COPY keycloak.user_consent_client_scope (user_consent_id, scope_id) FROM stdin;
\.


COPY keycloak.user_entity (id, email, email_constraint, email_verified, enabled, federation_link, first_name, last_name,
                           realm_id, username, created_timestamp, service_account_client_link, not_before) FROM stdin;
1d071bc2-3f12-462a-b686-079304d649a0	\N	f5f23332-6999-4887-86c0-59e0e4a49895	f	t	\N	\N	\N	fc4359ab-c586-4d33-a1e0-6382887b081c	admin	1745443443678	\N	0
29849880-ddd4-4000-b100-460f4c505045	example@gmail.com	example@gmail.com	f	t	\N	Sanity	Last	bde3ba76-fd10-4c67-8d50-9435f4f48ccc	sanity	1745447362610	\N	0
\.


COPY keycloak.user_federation_config (user_federation_provider_id, value, name) FROM stdin;
\.


COPY keycloak.user_federation_mapper (id, name, federation_provider_id, federation_mapper_type, realm_id) FROM stdin;
\.


COPY keycloak.user_federation_mapper_config (user_federation_mapper_id, value, name) FROM stdin;
\.


COPY keycloak.user_federation_provider (id, changed_sync_period, display_name, full_sync_period, last_sync, priority,
                                        provider_name, realm_id) FROM stdin;
\.


COPY keycloak.user_group_membership (group_id, user_id, membership_type) FROM stdin;
\.


COPY keycloak.user_required_action (user_id, required_action) FROM stdin;
\.


COPY keycloak.user_role_mapping (role_id, user_id) FROM stdin;
ee605334-ad03-4dd9-b1e1-abdda1eec33a	1d071bc2-3f12-462a-b686-079304d649a0
6af59e01-d323-4062-93d1-bb37cc8b53e9	1d071bc2-3f12-462a-b686-079304d649a0
da241a9c-0230-44b2-859b-d198a2b0863a	29849880-ddd4-4000-b100-460f4c505045
553f1779-7b3c-4b71-87b9-230f6ac03d5d	29849880-ddd4-4000-b100-460f4c505045
\.


COPY keycloak.web_origins (client_id, value) FROM stdin;
108d6f49-22ba-43f2-84e8-105fbc9e9cdd	+
495ee007-a202-41b2-b78c-3ce14d1da8ce	+
05b20b1d-d90a-4d28-b31f-73b413c99bf1	+
\.


ALTER TABLE ONLY keycloak.org_domain
    ADD CONSTRAINT "ORG_DOMAIN_pkey" PRIMARY KEY (id, name);



ALTER TABLE ONLY keycloak.org
    ADD CONSTRAINT "ORG_pkey" PRIMARY KEY (id);



ALTER TABLE ONLY keycloak.server_config
    ADD CONSTRAINT "SERVER_CONFIG_pkey" PRIMARY KEY (server_config_key);



ALTER TABLE ONLY keycloak.keycloak_role
    ADD CONSTRAINT "UK_J3RWUVD56ONTGSUHOGM184WW2-2" UNIQUE (name, client_realm_constraint);



ALTER TABLE ONLY keycloak.client_auth_flow_bindings
    ADD CONSTRAINT c_cli_flow_bind PRIMARY KEY (client_id, binding_name);



ALTER TABLE ONLY keycloak.client_scope_client
    ADD CONSTRAINT c_cli_scope_bind PRIMARY KEY (client_id, scope_id);



ALTER TABLE ONLY keycloak.client_initial_access
    ADD CONSTRAINT cnstr_client_init_acc_pk PRIMARY KEY (id);



ALTER TABLE ONLY keycloak.realm_default_groups
    ADD CONSTRAINT con_group_id_def_groups UNIQUE (group_id);



ALTER TABLE ONLY keycloak.broker_link
    ADD CONSTRAINT constr_broker_link_pk PRIMARY KEY (identity_provider, user_id);



ALTER TABLE ONLY keycloak.component_config
    ADD CONSTRAINT constr_component_config_pk PRIMARY KEY (id);



ALTER TABLE ONLY keycloak.component
    ADD CONSTRAINT constr_component_pk PRIMARY KEY (id);



ALTER TABLE ONLY keycloak.fed_user_required_action
    ADD CONSTRAINT constr_fed_required_action PRIMARY KEY (required_action, user_id);



ALTER TABLE ONLY keycloak.fed_user_attribute
    ADD CONSTRAINT constr_fed_user_attr_pk PRIMARY KEY (id);



ALTER TABLE ONLY keycloak.fed_user_consent
    ADD CONSTRAINT constr_fed_user_consent_pk PRIMARY KEY (id);



ALTER TABLE ONLY keycloak.fed_user_credential
    ADD CONSTRAINT constr_fed_user_cred_pk PRIMARY KEY (id);



ALTER TABLE ONLY keycloak.fed_user_group_membership
    ADD CONSTRAINT constr_fed_user_group PRIMARY KEY (group_id, user_id);



ALTER TABLE ONLY keycloak.fed_user_role_mapping
    ADD CONSTRAINT constr_fed_user_role PRIMARY KEY (role_id, user_id);



ALTER TABLE ONLY keycloak.federated_user
    ADD CONSTRAINT constr_federated_user PRIMARY KEY (id);



ALTER TABLE ONLY keycloak.realm_default_groups
    ADD CONSTRAINT constr_realm_default_groups PRIMARY KEY (realm_id, group_id);



ALTER TABLE ONLY keycloak.realm_enabled_event_types
    ADD CONSTRAINT constr_realm_enabl_event_types PRIMARY KEY (realm_id, value);



ALTER TABLE ONLY keycloak.realm_events_listeners
    ADD CONSTRAINT constr_realm_events_listeners PRIMARY KEY (realm_id, value);



ALTER TABLE ONLY keycloak.realm_supported_locales
    ADD CONSTRAINT constr_realm_supported_locales PRIMARY KEY (realm_id, value);



ALTER TABLE ONLY keycloak.identity_provider
    ADD CONSTRAINT constraint_2b PRIMARY KEY (internal_id);



ALTER TABLE ONLY keycloak.client_attributes
    ADD CONSTRAINT constraint_3c PRIMARY KEY (client_id, name);



ALTER TABLE ONLY keycloak.event_entity
    ADD CONSTRAINT constraint_4 PRIMARY KEY (id);



ALTER TABLE ONLY keycloak.federated_identity
    ADD CONSTRAINT constraint_40 PRIMARY KEY (identity_provider, user_id);



ALTER TABLE ONLY keycloak.realm
    ADD CONSTRAINT constraint_4a PRIMARY KEY (id);



ALTER TABLE ONLY keycloak.user_federation_provider
    ADD CONSTRAINT constraint_5c PRIMARY KEY (id);



ALTER TABLE ONLY keycloak.client
    ADD CONSTRAINT constraint_7 PRIMARY KEY (id);



ALTER TABLE ONLY keycloak.scope_mapping
    ADD CONSTRAINT constraint_81 PRIMARY KEY (client_id, role_id);



ALTER TABLE ONLY keycloak.client_node_registrations
    ADD CONSTRAINT constraint_84 PRIMARY KEY (client_id, name);



ALTER TABLE ONLY keycloak.realm_attribute
    ADD CONSTRAINT constraint_9 PRIMARY KEY (name, realm_id);



ALTER TABLE ONLY keycloak.realm_required_credential
    ADD CONSTRAINT constraint_92 PRIMARY KEY (realm_id, type);



ALTER TABLE ONLY keycloak.keycloak_role
    ADD CONSTRAINT constraint_a PRIMARY KEY (id);



ALTER TABLE ONLY keycloak.admin_event_entity
    ADD CONSTRAINT constraint_admin_event_entity PRIMARY KEY (id);



ALTER TABLE ONLY keycloak.authenticator_config_entry
    ADD CONSTRAINT constraint_auth_cfg_pk PRIMARY KEY (authenticator_id, name);



ALTER TABLE ONLY keycloak.authentication_execution
    ADD CONSTRAINT constraint_auth_exec_pk PRIMARY KEY (id);



ALTER TABLE ONLY keycloak.authentication_flow
    ADD CONSTRAINT constraint_auth_flow_pk PRIMARY KEY (id);



ALTER TABLE ONLY keycloak.authenticator_config
    ADD CONSTRAINT constraint_auth_pk PRIMARY KEY (id);



ALTER TABLE ONLY keycloak.user_role_mapping
    ADD CONSTRAINT constraint_c PRIMARY KEY (role_id, user_id);



ALTER TABLE ONLY keycloak.composite_role
    ADD CONSTRAINT constraint_composite_role PRIMARY KEY (composite, child_role);



ALTER TABLE ONLY keycloak.identity_provider_config
    ADD CONSTRAINT constraint_d PRIMARY KEY (identity_provider_id, name);



ALTER TABLE ONLY keycloak.policy_config
    ADD CONSTRAINT constraint_dpc PRIMARY KEY (policy_id, name);



ALTER TABLE ONLY keycloak.realm_smtp_config
    ADD CONSTRAINT constraint_e PRIMARY KEY (realm_id, name);



ALTER TABLE ONLY keycloak.credential
    ADD CONSTRAINT constraint_f PRIMARY KEY (id);



ALTER TABLE ONLY keycloak.user_federation_config
    ADD CONSTRAINT constraint_f9 PRIMARY KEY (user_federation_provider_id, name);



ALTER TABLE ONLY keycloak.resource_server_perm_ticket
    ADD CONSTRAINT constraint_fapmt PRIMARY KEY (id);



ALTER TABLE ONLY keycloak.resource_server_resource
    ADD CONSTRAINT constraint_farsr PRIMARY KEY (id);



ALTER TABLE ONLY keycloak.resource_server_policy
    ADD CONSTRAINT constraint_farsrp PRIMARY KEY (id);



ALTER TABLE ONLY keycloak.associated_policy
    ADD CONSTRAINT constraint_farsrpap PRIMARY KEY (policy_id, associated_policy_id);



ALTER TABLE ONLY keycloak.resource_policy
    ADD CONSTRAINT constraint_farsrpp PRIMARY KEY (resource_id, policy_id);



ALTER TABLE ONLY keycloak.resource_server_scope
    ADD CONSTRAINT constraint_farsrs PRIMARY KEY (id);



ALTER TABLE ONLY keycloak.resource_scope
    ADD CONSTRAINT constraint_farsrsp PRIMARY KEY (resource_id, scope_id);



ALTER TABLE ONLY keycloak.scope_policy
    ADD CONSTRAINT constraint_farsrsps PRIMARY KEY (scope_id, policy_id);



ALTER TABLE ONLY keycloak.user_entity
    ADD CONSTRAINT constraint_fb PRIMARY KEY (id);



ALTER TABLE ONLY keycloak.user_federation_mapper_config
    ADD CONSTRAINT constraint_fedmapper_cfg_pm PRIMARY KEY (user_federation_mapper_id, name);



ALTER TABLE ONLY keycloak.user_federation_mapper
    ADD CONSTRAINT constraint_fedmapperpm PRIMARY KEY (id);



ALTER TABLE ONLY keycloak.fed_user_consent_cl_scope
    ADD CONSTRAINT constraint_fgrntcsnt_clsc_pm PRIMARY KEY (user_consent_id, scope_id);



ALTER TABLE ONLY keycloak.user_consent_client_scope
    ADD CONSTRAINT constraint_grntcsnt_clsc_pm PRIMARY KEY (user_consent_id, scope_id);



ALTER TABLE ONLY keycloak.user_consent
    ADD CONSTRAINT constraint_grntcsnt_pm PRIMARY KEY (id);



ALTER TABLE ONLY keycloak.keycloak_group
    ADD CONSTRAINT constraint_group PRIMARY KEY (id);



ALTER TABLE ONLY keycloak.group_attribute
    ADD CONSTRAINT constraint_group_attribute_pk PRIMARY KEY (id);



ALTER TABLE ONLY keycloak.group_role_mapping
    ADD CONSTRAINT constraint_group_role PRIMARY KEY (role_id, group_id);



ALTER TABLE ONLY keycloak.identity_provider_mapper
    ADD CONSTRAINT constraint_idpm PRIMARY KEY (id);



ALTER TABLE ONLY keycloak.idp_mapper_config
    ADD CONSTRAINT constraint_idpmconfig PRIMARY KEY (idp_mapper_id, name);



ALTER TABLE ONLY keycloak.jgroups_ping
    ADD CONSTRAINT constraint_jgroups_ping PRIMARY KEY (address);



ALTER TABLE ONLY keycloak.migration_model
    ADD CONSTRAINT constraint_migmod PRIMARY KEY (id);



ALTER TABLE ONLY keycloak.offline_client_session
    ADD CONSTRAINT constraint_offl_cl_ses_pk3 PRIMARY KEY (user_session_id, client_id, client_storage_provider,
                                                           external_client_id, offline_flag);



ALTER TABLE ONLY keycloak.offline_user_session
    ADD CONSTRAINT constraint_offl_us_ses_pk2 PRIMARY KEY (user_session_id, offline_flag);



ALTER TABLE ONLY keycloak.protocol_mapper
    ADD CONSTRAINT constraint_pcm PRIMARY KEY (id);



ALTER TABLE ONLY keycloak.protocol_mapper_config
    ADD CONSTRAINT constraint_pmconfig PRIMARY KEY (protocol_mapper_id, name);



ALTER TABLE ONLY keycloak.redirect_uris
    ADD CONSTRAINT constraint_redirect_uris PRIMARY KEY (client_id, value);



ALTER TABLE ONLY keycloak.required_action_config
    ADD CONSTRAINT constraint_req_act_cfg_pk PRIMARY KEY (required_action_id, name);



ALTER TABLE ONLY keycloak.required_action_provider
    ADD CONSTRAINT constraint_req_act_prv_pk PRIMARY KEY (id);



ALTER TABLE ONLY keycloak.user_required_action
    ADD CONSTRAINT constraint_required_action PRIMARY KEY (required_action, user_id);



ALTER TABLE ONLY keycloak.resource_uris
    ADD CONSTRAINT constraint_resour_uris_pk PRIMARY KEY (resource_id, value);



ALTER TABLE ONLY keycloak.role_attribute
    ADD CONSTRAINT constraint_role_attribute_pk PRIMARY KEY (id);



ALTER TABLE ONLY keycloak.revoked_token
    ADD CONSTRAINT constraint_rt PRIMARY KEY (id);



ALTER TABLE ONLY keycloak.user_attribute
    ADD CONSTRAINT constraint_user_attribute_pk PRIMARY KEY (id);



ALTER TABLE ONLY keycloak.user_group_membership
    ADD CONSTRAINT constraint_user_group PRIMARY KEY (group_id, user_id);



ALTER TABLE ONLY keycloak.web_origins
    ADD CONSTRAINT constraint_web_origins PRIMARY KEY (client_id, value);



ALTER TABLE ONLY keycloak.databasechangeloglock
    ADD CONSTRAINT databasechangeloglock_pkey PRIMARY KEY (id);



ALTER TABLE ONLY keycloak.client_scope_attributes
    ADD CONSTRAINT pk_cl_tmpl_attr PRIMARY KEY (scope_id, name);



ALTER TABLE ONLY keycloak.client_scope
    ADD CONSTRAINT pk_cli_template PRIMARY KEY (id);



ALTER TABLE ONLY keycloak.resource_server
    ADD CONSTRAINT pk_resource_server PRIMARY KEY (id);



ALTER TABLE ONLY keycloak.client_scope_role_mapping
    ADD CONSTRAINT pk_template_scope PRIMARY KEY (scope_id, role_id);



ALTER TABLE ONLY keycloak.default_client_scope
    ADD CONSTRAINT r_def_cli_scope_bind PRIMARY KEY (realm_id, scope_id);



ALTER TABLE ONLY keycloak.realm_localizations
    ADD CONSTRAINT realm_localizations_pkey PRIMARY KEY (realm_id, locale);



ALTER TABLE ONLY keycloak.resource_attribute
    ADD CONSTRAINT res_attr_pk PRIMARY KEY (id);



ALTER TABLE ONLY keycloak.keycloak_group
    ADD CONSTRAINT sibling_names UNIQUE (realm_id, parent_group, name);



ALTER TABLE ONLY keycloak.identity_provider
    ADD CONSTRAINT uk_2daelwnibji49avxsrtuf6xj33 UNIQUE (provider_alias, realm_id);



ALTER TABLE ONLY keycloak.client
    ADD CONSTRAINT uk_b71cjlbenv945rb6gcon438at UNIQUE (realm_id, client_id);



ALTER TABLE ONLY keycloak.client_scope
    ADD CONSTRAINT uk_cli_scope UNIQUE (realm_id, name);



ALTER TABLE ONLY keycloak.user_entity
    ADD CONSTRAINT uk_dykn684sl8up1crfei6eckhd7 UNIQUE (realm_id, email_constraint);



ALTER TABLE ONLY keycloak.user_consent
    ADD CONSTRAINT uk_external_consent UNIQUE (client_storage_provider, external_client_id, user_id);



ALTER TABLE ONLY keycloak.resource_server_resource
    ADD CONSTRAINT uk_frsr6t700s9v50bu18ws5ha6 UNIQUE (name, owner, resource_server_id);



ALTER TABLE ONLY keycloak.resource_server_perm_ticket
    ADD CONSTRAINT uk_frsr6t700s9v50bu18ws5pmt UNIQUE (owner, requester, resource_server_id, resource_id, scope_id);



ALTER TABLE ONLY keycloak.resource_server_policy
    ADD CONSTRAINT uk_frsrpt700s9v50bu18ws5ha6 UNIQUE (name, resource_server_id);



ALTER TABLE ONLY keycloak.resource_server_scope
    ADD CONSTRAINT uk_frsrst700s9v50bu18ws5ha6 UNIQUE (name, resource_server_id);



ALTER TABLE ONLY keycloak.user_consent
    ADD CONSTRAINT uk_local_consent UNIQUE (client_id, user_id);



ALTER TABLE ONLY keycloak.org
    ADD CONSTRAINT uk_org_alias UNIQUE (realm_id, alias);



ALTER TABLE ONLY keycloak.org
    ADD CONSTRAINT uk_org_group UNIQUE (group_id);



ALTER TABLE ONLY keycloak.org
    ADD CONSTRAINT uk_org_name UNIQUE (realm_id, name);



ALTER TABLE ONLY keycloak.realm
    ADD CONSTRAINT uk_orvsdmla56612eaefiq6wl5oi UNIQUE (name);



ALTER TABLE ONLY keycloak.user_entity
    ADD CONSTRAINT uk_ru8tt6t700s9v50bu18ws5ha6 UNIQUE (realm_id, username);



CREATE INDEX fed_user_attr_long_values ON keycloak.fed_user_attribute USING btree (long_value_hash, name);



CREATE INDEX fed_user_attr_long_values_lower_case ON keycloak.fed_user_attribute USING btree (long_value_hash_lower_case, name);



CREATE INDEX idx_admin_event_time ON keycloak.admin_event_entity USING btree (realm_id, admin_event_time);



CREATE INDEX idx_assoc_pol_assoc_pol_id ON keycloak.associated_policy USING btree (associated_policy_id);



CREATE INDEX idx_auth_config_realm ON keycloak.authenticator_config USING btree (realm_id);



CREATE INDEX idx_auth_exec_flow ON keycloak.authentication_execution USING btree (flow_id);



CREATE INDEX idx_auth_exec_realm_flow ON keycloak.authentication_execution USING btree (realm_id, flow_id);



CREATE INDEX idx_auth_flow_realm ON keycloak.authentication_flow USING btree (realm_id);



CREATE INDEX idx_cl_clscope ON keycloak.client_scope_client USING btree (scope_id);



CREATE INDEX idx_client_att_by_name_value ON keycloak.client_attributes USING btree (name, substr(value, 1, 255));



CREATE INDEX idx_client_id ON keycloak.client USING btree (client_id);



CREATE INDEX idx_client_init_acc_realm ON keycloak.client_initial_access USING btree (realm_id);



CREATE INDEX idx_clscope_attrs ON keycloak.client_scope_attributes USING btree (scope_id);



CREATE INDEX idx_clscope_cl ON keycloak.client_scope_client USING btree (client_id);



CREATE INDEX idx_clscope_protmap ON keycloak.protocol_mapper USING btree (client_scope_id);



CREATE INDEX idx_clscope_role ON keycloak.client_scope_role_mapping USING btree (scope_id);



CREATE INDEX idx_compo_config_compo ON keycloak.component_config USING btree (component_id);



CREATE INDEX idx_component_provider_type ON keycloak.component USING btree (provider_type);



CREATE INDEX idx_component_realm ON keycloak.component USING btree (realm_id);



CREATE INDEX idx_composite ON keycloak.composite_role USING btree (composite);



CREATE INDEX idx_composite_child ON keycloak.composite_role USING btree (child_role);



CREATE INDEX idx_defcls_realm ON keycloak.default_client_scope USING btree (realm_id);



CREATE INDEX idx_defcls_scope ON keycloak.default_client_scope USING btree (scope_id);



CREATE INDEX idx_event_time ON keycloak.event_entity USING btree (realm_id, event_time);



CREATE INDEX idx_fedidentity_feduser ON keycloak.federated_identity USING btree (federated_user_id);



CREATE INDEX idx_fedidentity_user ON keycloak.federated_identity USING btree (user_id);



CREATE INDEX idx_fu_attribute ON keycloak.fed_user_attribute USING btree (user_id, realm_id, name);



CREATE INDEX idx_fu_cnsnt_ext ON keycloak.fed_user_consent USING btree (user_id, client_storage_provider, external_client_id);



CREATE INDEX idx_fu_consent ON keycloak.fed_user_consent USING btree (user_id, client_id);



CREATE INDEX idx_fu_consent_ru ON keycloak.fed_user_consent USING btree (realm_id, user_id);



CREATE INDEX idx_fu_credential ON keycloak.fed_user_credential USING btree (user_id, type);



CREATE INDEX idx_fu_credential_ru ON keycloak.fed_user_credential USING btree (realm_id, user_id);



CREATE INDEX idx_fu_group_membership ON keycloak.fed_user_group_membership USING btree (user_id, group_id);



CREATE INDEX idx_fu_group_membership_ru ON keycloak.fed_user_group_membership USING btree (realm_id, user_id);



CREATE INDEX idx_fu_required_action ON keycloak.fed_user_required_action USING btree (user_id, required_action);



CREATE INDEX idx_fu_required_action_ru ON keycloak.fed_user_required_action USING btree (realm_id, user_id);



CREATE INDEX idx_fu_role_mapping ON keycloak.fed_user_role_mapping USING btree (user_id, role_id);



CREATE INDEX idx_fu_role_mapping_ru ON keycloak.fed_user_role_mapping USING btree (realm_id, user_id);



CREATE INDEX idx_group_att_by_name_value ON keycloak.group_attribute USING btree (name, ((value)::character varying(250)));



CREATE INDEX idx_group_attr_group ON keycloak.group_attribute USING btree (group_id);



CREATE INDEX idx_group_role_mapp_group ON keycloak.group_role_mapping USING btree (group_id);



CREATE INDEX idx_id_prov_mapp_realm ON keycloak.identity_provider_mapper USING btree (realm_id);



CREATE INDEX idx_ident_prov_realm ON keycloak.identity_provider USING btree (realm_id);



CREATE INDEX idx_idp_for_login ON keycloak.identity_provider USING btree (realm_id, enabled, link_only, hide_on_login, organization_id);



CREATE INDEX idx_idp_realm_org ON keycloak.identity_provider USING btree (realm_id, organization_id);



CREATE INDEX idx_keycloak_role_client ON keycloak.keycloak_role USING btree (client);



CREATE INDEX idx_keycloak_role_realm ON keycloak.keycloak_role USING btree (realm);



CREATE INDEX idx_offline_uss_by_broker_session_id ON keycloak.offline_user_session USING btree (broker_session_id, realm_id);



CREATE INDEX idx_offline_uss_by_last_session_refresh ON keycloak.offline_user_session USING btree (realm_id, offline_flag, last_session_refresh);



CREATE INDEX idx_offline_uss_by_user ON keycloak.offline_user_session USING btree (user_id, realm_id, offline_flag);



CREATE INDEX idx_org_domain_org_id ON keycloak.org_domain USING btree (org_id);



CREATE INDEX idx_perm_ticket_owner ON keycloak.resource_server_perm_ticket USING btree (owner);



CREATE INDEX idx_perm_ticket_requester ON keycloak.resource_server_perm_ticket USING btree (requester);



CREATE INDEX idx_protocol_mapper_client ON keycloak.protocol_mapper USING btree (client_id);



CREATE INDEX idx_realm_attr_realm ON keycloak.realm_attribute USING btree (realm_id);



CREATE INDEX idx_realm_clscope ON keycloak.client_scope USING btree (realm_id);



CREATE INDEX idx_realm_def_grp_realm ON keycloak.realm_default_groups USING btree (realm_id);



CREATE INDEX idx_realm_evt_list_realm ON keycloak.realm_events_listeners USING btree (realm_id);



CREATE INDEX idx_realm_evt_types_realm ON keycloak.realm_enabled_event_types USING btree (realm_id);



CREATE INDEX idx_realm_master_adm_cli ON keycloak.realm USING btree (master_admin_client);



CREATE INDEX idx_realm_supp_local_realm ON keycloak.realm_supported_locales USING btree (realm_id);



CREATE INDEX idx_redir_uri_client ON keycloak.redirect_uris USING btree (client_id);



CREATE INDEX idx_req_act_prov_realm ON keycloak.required_action_provider USING btree (realm_id);



CREATE INDEX idx_res_policy_policy ON keycloak.resource_policy USING btree (policy_id);



CREATE INDEX idx_res_scope_scope ON keycloak.resource_scope USING btree (scope_id);



CREATE INDEX idx_res_serv_pol_res_serv ON keycloak.resource_server_policy USING btree (resource_server_id);



CREATE INDEX idx_res_srv_res_res_srv ON keycloak.resource_server_resource USING btree (resource_server_id);



CREATE INDEX idx_res_srv_scope_res_srv ON keycloak.resource_server_scope USING btree (resource_server_id);



CREATE INDEX idx_rev_token_on_expire ON keycloak.revoked_token USING btree (expire);



CREATE INDEX idx_role_attribute ON keycloak.role_attribute USING btree (role_id);



CREATE INDEX idx_role_clscope ON keycloak.client_scope_role_mapping USING btree (role_id);



CREATE INDEX idx_scope_mapping_role ON keycloak.scope_mapping USING btree (role_id);



CREATE INDEX idx_scope_policy_policy ON keycloak.scope_policy USING btree (policy_id);



CREATE INDEX idx_update_time ON keycloak.migration_model USING btree (update_time);



CREATE INDEX idx_usconsent_clscope ON keycloak.user_consent_client_scope USING btree (user_consent_id);



CREATE INDEX idx_usconsent_scope_id ON keycloak.user_consent_client_scope USING btree (scope_id);



CREATE INDEX idx_user_attribute ON keycloak.user_attribute USING btree (user_id);



CREATE INDEX idx_user_attribute_name ON keycloak.user_attribute USING btree (name, value);



CREATE INDEX idx_user_consent ON keycloak.user_consent USING btree (user_id);



CREATE INDEX idx_user_credential ON keycloak.credential USING btree (user_id);



CREATE INDEX idx_user_email ON keycloak.user_entity USING btree (email);



CREATE INDEX idx_user_group_mapping ON keycloak.user_group_membership USING btree (user_id);



CREATE INDEX idx_user_reqactions ON keycloak.user_required_action USING btree (user_id);



CREATE INDEX idx_user_role_mapping ON keycloak.user_role_mapping USING btree (user_id);



CREATE INDEX idx_user_service_account ON keycloak.user_entity USING btree (realm_id, service_account_client_link);



CREATE INDEX idx_usr_fed_map_fed_prv ON keycloak.user_federation_mapper USING btree (federation_provider_id);



CREATE INDEX idx_usr_fed_map_realm ON keycloak.user_federation_mapper USING btree (realm_id);



CREATE INDEX idx_usr_fed_prv_realm ON keycloak.user_federation_provider USING btree (realm_id);



CREATE INDEX idx_web_orig_client ON keycloak.web_origins USING btree (client_id);



CREATE INDEX user_attr_long_values ON keycloak.user_attribute USING btree (long_value_hash, name);



CREATE INDEX user_attr_long_values_lower_case ON keycloak.user_attribute USING btree (long_value_hash_lower_case, name);



ALTER TABLE ONLY keycloak.identity_provider
    ADD CONSTRAINT fk2b4ebc52ae5c3b34 FOREIGN KEY (realm_id) REFERENCES keycloak.realm (id);



ALTER TABLE ONLY keycloak.client_attributes
    ADD CONSTRAINT fk3c47c64beacca966 FOREIGN KEY (client_id) REFERENCES keycloak.client (id);



ALTER TABLE ONLY keycloak.federated_identity
    ADD CONSTRAINT fk404288b92ef007a6 FOREIGN KEY (user_id) REFERENCES keycloak.user_entity (id);



ALTER TABLE ONLY keycloak.client_node_registrations
    ADD CONSTRAINT fk4129723ba992f594 FOREIGN KEY (client_id) REFERENCES keycloak.client (id);



ALTER TABLE ONLY keycloak.redirect_uris
    ADD CONSTRAINT fk_1burs8pb4ouj97h5wuppahv9f FOREIGN KEY (client_id) REFERENCES keycloak.client (id);



ALTER TABLE ONLY keycloak.user_federation_provider
    ADD CONSTRAINT fk_1fj32f6ptolw2qy60cd8n01e8 FOREIGN KEY (realm_id) REFERENCES keycloak.realm (id);



ALTER TABLE ONLY keycloak.realm_required_credential
    ADD CONSTRAINT fk_5hg65lybevavkqfki3kponh9v FOREIGN KEY (realm_id) REFERENCES keycloak.realm (id);



ALTER TABLE ONLY keycloak.resource_attribute
    ADD CONSTRAINT fk_5hrm2vlf9ql5fu022kqepovbr FOREIGN KEY (resource_id) REFERENCES keycloak.resource_server_resource (id);



ALTER TABLE ONLY keycloak.user_attribute
    ADD CONSTRAINT fk_5hrm2vlf9ql5fu043kqepovbr FOREIGN KEY (user_id) REFERENCES keycloak.user_entity (id);



ALTER TABLE ONLY keycloak.user_required_action
    ADD CONSTRAINT fk_6qj3w1jw9cvafhe19bwsiuvmd FOREIGN KEY (user_id) REFERENCES keycloak.user_entity (id);



ALTER TABLE ONLY keycloak.keycloak_role
    ADD CONSTRAINT fk_6vyqfe4cn4wlq8r6kt5vdsj5c FOREIGN KEY (realm) REFERENCES keycloak.realm (id);



ALTER TABLE ONLY keycloak.realm_smtp_config
    ADD CONSTRAINT fk_70ej8xdxgxd0b9hh6180irr0o FOREIGN KEY (realm_id) REFERENCES keycloak.realm (id);



ALTER TABLE ONLY keycloak.realm_attribute
    ADD CONSTRAINT fk_8shxd6l3e9atqukacxgpffptw FOREIGN KEY (realm_id) REFERENCES keycloak.realm (id);



ALTER TABLE ONLY keycloak.composite_role
    ADD CONSTRAINT fk_a63wvekftu8jo1pnj81e7mce2 FOREIGN KEY (composite) REFERENCES keycloak.keycloak_role (id);



ALTER TABLE ONLY keycloak.authentication_execution
    ADD CONSTRAINT fk_auth_exec_flow FOREIGN KEY (flow_id) REFERENCES keycloak.authentication_flow (id);



ALTER TABLE ONLY keycloak.authentication_execution
    ADD CONSTRAINT fk_auth_exec_realm FOREIGN KEY (realm_id) REFERENCES keycloak.realm (id);



ALTER TABLE ONLY keycloak.authentication_flow
    ADD CONSTRAINT fk_auth_flow_realm FOREIGN KEY (realm_id) REFERENCES keycloak.realm (id);



ALTER TABLE ONLY keycloak.authenticator_config
    ADD CONSTRAINT fk_auth_realm FOREIGN KEY (realm_id) REFERENCES keycloak.realm (id);



ALTER TABLE ONLY keycloak.user_role_mapping
    ADD CONSTRAINT fk_c4fqv34p1mbylloxang7b1q3l FOREIGN KEY (user_id) REFERENCES keycloak.user_entity (id);



ALTER TABLE ONLY keycloak.client_scope_attributes
    ADD CONSTRAINT fk_cl_scope_attr_scope FOREIGN KEY (scope_id) REFERENCES keycloak.client_scope (id);



ALTER TABLE ONLY keycloak.client_scope_role_mapping
    ADD CONSTRAINT fk_cl_scope_rm_scope FOREIGN KEY (scope_id) REFERENCES keycloak.client_scope (id);



ALTER TABLE ONLY keycloak.protocol_mapper
    ADD CONSTRAINT fk_cli_scope_mapper FOREIGN KEY (client_scope_id) REFERENCES keycloak.client_scope (id);



ALTER TABLE ONLY keycloak.client_initial_access
    ADD CONSTRAINT fk_client_init_acc_realm FOREIGN KEY (realm_id) REFERENCES keycloak.realm (id);



ALTER TABLE ONLY keycloak.component_config
    ADD CONSTRAINT fk_component_config FOREIGN KEY (component_id) REFERENCES keycloak.component (id);



ALTER TABLE ONLY keycloak.component
    ADD CONSTRAINT fk_component_realm FOREIGN KEY (realm_id) REFERENCES keycloak.realm (id);



ALTER TABLE ONLY keycloak.realm_default_groups
    ADD CONSTRAINT fk_def_groups_realm FOREIGN KEY (realm_id) REFERENCES keycloak.realm (id);



ALTER TABLE ONLY keycloak.user_federation_mapper_config
    ADD CONSTRAINT fk_fedmapper_cfg FOREIGN KEY (user_federation_mapper_id) REFERENCES keycloak.user_federation_mapper (id);



ALTER TABLE ONLY keycloak.user_federation_mapper
    ADD CONSTRAINT fk_fedmapperpm_fedprv FOREIGN KEY (federation_provider_id) REFERENCES keycloak.user_federation_provider (id);



ALTER TABLE ONLY keycloak.user_federation_mapper
    ADD CONSTRAINT fk_fedmapperpm_realm FOREIGN KEY (realm_id) REFERENCES keycloak.realm (id);



ALTER TABLE ONLY keycloak.associated_policy
    ADD CONSTRAINT fk_frsr5s213xcx4wnkog82ssrfy FOREIGN KEY (associated_policy_id) REFERENCES keycloak.resource_server_policy (id);



ALTER TABLE ONLY keycloak.scope_policy
    ADD CONSTRAINT fk_frsrasp13xcx4wnkog82ssrfy FOREIGN KEY (policy_id) REFERENCES keycloak.resource_server_policy (id);



ALTER TABLE ONLY keycloak.resource_server_perm_ticket
    ADD CONSTRAINT fk_frsrho213xcx4wnkog82sspmt FOREIGN KEY (resource_server_id) REFERENCES keycloak.resource_server (id);



ALTER TABLE ONLY keycloak.resource_server_resource
    ADD CONSTRAINT fk_frsrho213xcx4wnkog82ssrfy FOREIGN KEY (resource_server_id) REFERENCES keycloak.resource_server (id);



ALTER TABLE ONLY keycloak.resource_server_perm_ticket
    ADD CONSTRAINT fk_frsrho213xcx4wnkog83sspmt FOREIGN KEY (resource_id) REFERENCES keycloak.resource_server_resource (id);



ALTER TABLE ONLY keycloak.resource_server_perm_ticket
    ADD CONSTRAINT fk_frsrho213xcx4wnkog84sspmt FOREIGN KEY (scope_id) REFERENCES keycloak.resource_server_scope (id);



ALTER TABLE ONLY keycloak.associated_policy
    ADD CONSTRAINT fk_frsrpas14xcx4wnkog82ssrfy FOREIGN KEY (policy_id) REFERENCES keycloak.resource_server_policy (id);



ALTER TABLE ONLY keycloak.scope_policy
    ADD CONSTRAINT fk_frsrpass3xcx4wnkog82ssrfy FOREIGN KEY (scope_id) REFERENCES keycloak.resource_server_scope (id);



ALTER TABLE ONLY keycloak.resource_server_perm_ticket
    ADD CONSTRAINT fk_frsrpo2128cx4wnkog82ssrfy FOREIGN KEY (policy_id) REFERENCES keycloak.resource_server_policy (id);



ALTER TABLE ONLY keycloak.resource_server_policy
    ADD CONSTRAINT fk_frsrpo213xcx4wnkog82ssrfy FOREIGN KEY (resource_server_id) REFERENCES keycloak.resource_server (id);



ALTER TABLE ONLY keycloak.resource_scope
    ADD CONSTRAINT fk_frsrpos13xcx4wnkog82ssrfy FOREIGN KEY (resource_id) REFERENCES keycloak.resource_server_resource (id);



ALTER TABLE ONLY keycloak.resource_policy
    ADD CONSTRAINT fk_frsrpos53xcx4wnkog82ssrfy FOREIGN KEY (resource_id) REFERENCES keycloak.resource_server_resource (id);



ALTER TABLE ONLY keycloak.resource_policy
    ADD CONSTRAINT fk_frsrpp213xcx4wnkog82ssrfy FOREIGN KEY (policy_id) REFERENCES keycloak.resource_server_policy (id);



ALTER TABLE ONLY keycloak.resource_scope
    ADD CONSTRAINT fk_frsrps213xcx4wnkog82ssrfy FOREIGN KEY (scope_id) REFERENCES keycloak.resource_server_scope (id);



ALTER TABLE ONLY keycloak.resource_server_scope
    ADD CONSTRAINT fk_frsrso213xcx4wnkog82ssrfy FOREIGN KEY (resource_server_id) REFERENCES keycloak.resource_server (id);



ALTER TABLE ONLY keycloak.composite_role
    ADD CONSTRAINT fk_gr7thllb9lu8q4vqa4524jjy8 FOREIGN KEY (child_role) REFERENCES keycloak.keycloak_role (id);



ALTER TABLE ONLY keycloak.user_consent_client_scope
    ADD CONSTRAINT fk_grntcsnt_clsc_usc FOREIGN KEY (user_consent_id) REFERENCES keycloak.user_consent (id);



ALTER TABLE ONLY keycloak.user_consent
    ADD CONSTRAINT fk_grntcsnt_user FOREIGN KEY (user_id) REFERENCES keycloak.user_entity (id);



ALTER TABLE ONLY keycloak.group_attribute
    ADD CONSTRAINT fk_group_attribute_group FOREIGN KEY (group_id) REFERENCES keycloak.keycloak_group (id);



ALTER TABLE ONLY keycloak.group_role_mapping
    ADD CONSTRAINT fk_group_role_group FOREIGN KEY (group_id) REFERENCES keycloak.keycloak_group (id);



ALTER TABLE ONLY keycloak.realm_enabled_event_types
    ADD CONSTRAINT fk_h846o4h0w8epx5nwedrf5y69j FOREIGN KEY (realm_id) REFERENCES keycloak.realm (id);



ALTER TABLE ONLY keycloak.realm_events_listeners
    ADD CONSTRAINT fk_h846o4h0w8epx5nxev9f5y69j FOREIGN KEY (realm_id) REFERENCES keycloak.realm (id);



ALTER TABLE ONLY keycloak.identity_provider_mapper
    ADD CONSTRAINT fk_idpm_realm FOREIGN KEY (realm_id) REFERENCES keycloak.realm (id);



ALTER TABLE ONLY keycloak.idp_mapper_config
    ADD CONSTRAINT fk_idpmconfig FOREIGN KEY (idp_mapper_id) REFERENCES keycloak.identity_provider_mapper (id);



ALTER TABLE ONLY keycloak.web_origins
    ADD CONSTRAINT fk_lojpho213xcx4wnkog82ssrfy FOREIGN KEY (client_id) REFERENCES keycloak.client (id);



ALTER TABLE ONLY keycloak.scope_mapping
    ADD CONSTRAINT fk_ouse064plmlr732lxjcn1q5f1 FOREIGN KEY (client_id) REFERENCES keycloak.client (id);



ALTER TABLE ONLY keycloak.protocol_mapper
    ADD CONSTRAINT fk_pcm_realm FOREIGN KEY (client_id) REFERENCES keycloak.client (id);



ALTER TABLE ONLY keycloak.credential
    ADD CONSTRAINT fk_pfyr0glasqyl0dei3kl69r6v0 FOREIGN KEY (user_id) REFERENCES keycloak.user_entity (id);



ALTER TABLE ONLY keycloak.protocol_mapper_config
    ADD CONSTRAINT fk_pmconfig FOREIGN KEY (protocol_mapper_id) REFERENCES keycloak.protocol_mapper (id);



ALTER TABLE ONLY keycloak.default_client_scope
    ADD CONSTRAINT fk_r_def_cli_scope_realm FOREIGN KEY (realm_id) REFERENCES keycloak.realm (id);



ALTER TABLE ONLY keycloak.required_action_provider
    ADD CONSTRAINT fk_req_act_realm FOREIGN KEY (realm_id) REFERENCES keycloak.realm (id);



ALTER TABLE ONLY keycloak.resource_uris
    ADD CONSTRAINT fk_resource_server_uris FOREIGN KEY (resource_id) REFERENCES keycloak.resource_server_resource (id);



ALTER TABLE ONLY keycloak.role_attribute
    ADD CONSTRAINT fk_role_attribute_id FOREIGN KEY (role_id) REFERENCES keycloak.keycloak_role (id);



ALTER TABLE ONLY keycloak.realm_supported_locales
    ADD CONSTRAINT fk_supported_locales_realm FOREIGN KEY (realm_id) REFERENCES keycloak.realm (id);



ALTER TABLE ONLY keycloak.user_federation_config
    ADD CONSTRAINT fk_t13hpu1j94r2ebpekr39x5eu5 FOREIGN KEY (user_federation_provider_id) REFERENCES keycloak.user_federation_provider (id);



ALTER TABLE ONLY keycloak.user_group_membership
    ADD CONSTRAINT fk_user_group_user FOREIGN KEY (user_id) REFERENCES keycloak.user_entity (id);



ALTER TABLE ONLY keycloak.policy_config
    ADD CONSTRAINT fkdc34197cf864c4e43 FOREIGN KEY (policy_id) REFERENCES keycloak.resource_server_policy (id);



ALTER TABLE ONLY keycloak.identity_provider_config
    ADD CONSTRAINT fkdc4897cf864c4e43 FOREIGN KEY (identity_provider_id) REFERENCES keycloak.identity_provider (internal_id);






