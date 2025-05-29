create table if not exists metadata_db.file_id_sequence (
    ws_id bigint primary key,
    next_id bigint not null default 1
);
create or replace function metadata_db.next_file_id(p_ws_id bigint)
    returns bigint
    language plpgsql
as $$
declare
    result bigint;
begin
    insert into metadata_db.file_id_sequence(ws_id)
    values (p_ws_id)
    on conflict do nothing;

    update metadata_db.file_id_sequence
    set next_id = next_id + 1
    where ws_id = p_ws_id
    returning next_id - 1 into result;

    return result;
end;
$$;

create table if not exists metadata_db.file_journal (
    ws_id bigint not null,
    file_id bigint not null,
    latest smallint,
    path text NOT NULL,
    uploader_id uuid not null,
    state varchar(50) check (state in ('UPLOADED', 'IN_UPLOAD', 'DELETED', 'DELETING')),
    size bigint,
    blocklist text,
    history_id integer,
    updated_by uuid,
    created_at timestamptz,
    updated_at timestamptz,
    primary key (ws_id, file_id)
);
create table if not exists metadata_db.statistics (
    id smallint generated always as identity,
    quota varchar(255) check (quota in ('USER_STORAGE_USED','USER_WORKSPACES')),
    description varchar(255),
    primary key (id)
);
create table if not exists metadata_db.user_statistics (
    statistics_id smallint not null,
    user_id UUID not null,
    value varchar(255),
    primary key (statistics_id, user_id)
);
create table if not exists metadata_db.user_subscriptions (
    id smallint generated always as identity,
    description varchar(255),
    title varchar(255),
    storage_limit bigint,
    workspaces_limit integer,
    primary key (id)
);
create table if not exists metadata_db.user_workspaces (
    user_id UUID not null,
    ws_id bigint not null,
    role varchar(255),
    joined_at timestamptz,
    primary key (user_id, ws_id)
);
create table if not exists metadata_db.users (
    id UUID NOT NULL,
    email varchar(255),
    username varchar(255),
    avatar varchar(255),
    subscription_id smallint not null,
    created_at timestamptz,
    updated_at timestamptz,
    primary key (id)
);
create table if not exists metadata_db.workspace_subscriptions (
    id smallint generated always as identity,
    description varchar(255),
    title varchar(255),
    primary key (id)
);
create table if not exists metadata_db.workspaces (
    id bigint generated always as identity,
    name varchar(255),
    description varchar(255),
    primary key (id)
);
create table if not exists metadata_db.links (
    link varchar(255) NOT NULL,
    issuer UUID NOT NULL,
    times_used integer,
    created_at timestamptz,
    expires_at timestamptz,
    primary key (link)
);

create table if not exists metadata_db.tasks (
    id integer generated always as identity,
    action varchar(255) check (action in ('DELETE_FILE')),
    status varchar(255) check (status in ('CREATED','FINISHED','FAILED')),
    object_id varchar(255),
    metadata jsonb,
    updated_at timestamptz,
    perform_at timestamptz,
    primary key (id)
);

alter table if exists metadata_db.file_journal
    add constraint file_journal_ws
        foreign key (ws_id)
            references metadata_db.workspaces;
alter table if exists metadata_db.file_journal
    add constraint file_uploader
        foreign key (uploader_id)
            references metadata_db.users;
alter table if exists metadata_db.file_journal
    add constraint file_journal_updated_by
        foreign key (updated_by)
            references metadata_db.users;
alter table if exists metadata_db.user_statistics
    add constraint user_statistics_statistics
        foreign key (statistics_id)
            references metadata_db.statistics;
alter table if exists metadata_db.user_statistics
    add constraint user_statistics_user
        foreign key (user_id)
            references metadata_db.users;
alter table if exists metadata_db.user_workspaces
    add constraint user_ws_user
        foreign key (user_id)
            references metadata_db.users;
alter table if exists metadata_db.user_workspaces
    add constraint user_ws_ws
        foreign key (ws_id)
            references metadata_db.workspaces;
alter table if exists metadata_db.users
    add constraint user_subscription
        foreign key (subscription_id)
            references metadata_db.user_subscriptions;
alter table if exists metadata_db.links
    add constraint link_issuer
        foreign key (issuer)
            references metadata_db.users;

insert into metadata_db.user_subscriptions (title, description, storage_limit, workspaces_limit) values ('Normal', 'Default subscription', 10737418240, 3); -- 10GiB
insert into metadata_db.user_subscriptions (title, description, storage_limit, workspaces_limit) values ('Advanced', 'Advanced subscription', 429496729600, 10); -- 400 GiB

insert into metadata_db.users (id, username, email, subscription_id, created_at, updated_at) values ('5a9bf3fa-d99a-4ccc-b64f-b2ddf20ee5e5', 'example@gmail.com', 'admin', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
insert into metadata_db.users (id, username, email, subscription_id, created_at, updated_at) values ('4d70da54-5ec5-4042-b011-b829bff6f8de', 'anon@gmail.com', 'admin', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
insert into metadata_db.workspaces (name, description) values ('w1', 'first workspace');
insert into metadata_db.workspaces (name, description) values ('w2', 'second workspace');
insert into metadata_db.user_workspaces (user_id, ws_id, role, joined_at) values ('5a9bf3fa-d99a-4ccc-b64f-b2ddf20ee5e5', 1, 'OWNER', CURRENT_TIMESTAMP);
insert into metadata_db.user_workspaces (user_id, ws_id, role, joined_at) values ('5a9bf3fa-d99a-4ccc-b64f-b2ddf20ee5e5', 2, 'USER', CURRENT_TIMESTAMP);
insert into metadata_db.user_workspaces (user_id, ws_id, role, joined_at) values ('29849880-ddd4-4000-b100-460f4c505045', 1, 'USER', CURRENT_TIMESTAMP);
insert into metadata_db.user_workspaces (user_id, ws_id, role, joined_at) values ('29849880-ddd4-4000-b100-460f4c505045', 2, 'OWNER', CURRENT_TIMESTAMP);
insert into metadata_db.file_journal (ws_id, file_id, latest, path, uploader_id, state, "size", blocklist, history_id, created_at, updated_at) values (1, (SELECT metadata_db.next_file_id(1)), 1, '/', '5a9bf3fa-d99a-4ccc-b64f-b2ddf20ee5e5', 'UPLOADED', 0, null, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
insert into metadata_db.file_journal (ws_id, file_id, latest, path, uploader_id, state, "size", blocklist, history_id, created_at, updated_at) values (2, (SELECT metadata_db.next_file_id(2)), 1, '/', '4d70da54-5ec5-4042-b011-b829bff6f8de', 'UPLOADED', 0, null, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
insert into metadata_db.statistics(quota, description) values ('USER_STORAGE_USED', 'Indicates how much storage user has used up');
insert into metadata_db.statistics(quota, description) values ('USER_WORKSPACES', 'Indicates to how many workspaces user is connected');
insert into metadata_db.user_statistics(user_id, statistics_id, value) values ('5a9bf3fa-d99a-4ccc-b64f-b2ddf20ee5e5', 1, '0');
insert into metadata_db.user_statistics(user_id, statistics_id, value) values ('5a9bf3fa-d99a-4ccc-b64f-b2ddf20ee5e5', 2, '2');

CREATE SCHEMA IF NOT EXISTS quartz;

DROP TABLE IF EXISTS quartz.QRTZ_FIRED_TRIGGERS;
DROP TABLE IF EXISTS quartz.QRTZ_PAUSED_TRIGGER_GRPS;
DROP TABLE IF EXISTS quartz.QRTZ_SCHEDULER_STATE;
DROP TABLE IF EXISTS quartz.QRTZ_LOCKS;
DROP TABLE IF EXISTS quartz.QRTZ_SIMPLE_TRIGGERS;
DROP TABLE IF EXISTS quartz.QRTZ_CRON_TRIGGERS;
DROP TABLE IF EXISTS quartz.QRTZ_SIMPROP_TRIGGERS;
DROP TABLE IF EXISTS quartz.QRTZ_BLOB_TRIGGERS;
DROP TABLE IF EXISTS quartz.QRTZ_TRIGGERS;
DROP TABLE IF EXISTS quartz.QRTZ_JOB_DETAILS;
DROP TABLE IF EXISTS quartz.QRTZ_CALENDARS;
DROP TABLE IF EXISTS quartz.TASKS;
DROP SEQUENCE IF EXISTS quartz.TASKS_SEQ;

CREATE SEQUENCE quartz.TASKS_SEQ start 1 increment 1;

CREATE TABLE quartz.TASKS
(
    id int8 NOT NULL,
    createdAt TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE quartz.QRTZ_JOB_DETAILS
(
    SCHED_NAME        VARCHAR(120) NOT NULL,
    JOB_NAME          VARCHAR(200) NOT NULL,
    JOB_GROUP         VARCHAR(200) NOT NULL,
    DESCRIPTION       VARCHAR(250) NULL,
    JOB_CLASS_NAME    VARCHAR(250) NOT NULL,
    IS_DURABLE        BOOL         NOT NULL,
    IS_NONCONCURRENT  BOOL         NOT NULL,
    IS_UPDATE_DATA    BOOL         NOT NULL,
    REQUESTS_RECOVERY BOOL         NOT NULL,
    JOB_DATA          BYTEA        NULL,
    PRIMARY KEY (SCHED_NAME, JOB_NAME, JOB_GROUP)
);

CREATE TABLE quartz.QRTZ_TRIGGERS
(
    SCHED_NAME     VARCHAR(120) NOT NULL,
    TRIGGER_NAME   VARCHAR(200) NOT NULL,
    TRIGGER_GROUP  VARCHAR(200) NOT NULL,
    JOB_NAME       VARCHAR(200) NOT NULL,
    JOB_GROUP      VARCHAR(200) NOT NULL,
    DESCRIPTION    VARCHAR(250) NULL,
    NEXT_FIRE_TIME BIGINT       NULL,
    PREV_FIRE_TIME BIGINT       NULL,
    PRIORITY       INTEGER      NULL,
    TRIGGER_STATE  VARCHAR(16)  NOT NULL,
    TRIGGER_TYPE   VARCHAR(8)   NOT NULL,
    START_TIME     BIGINT       NOT NULL,
    END_TIME       BIGINT       NULL,
    CALENDAR_NAME  VARCHAR(200) NULL,
    MISFIRE_INSTR  SMALLINT     NULL,
    JOB_DATA       BYTEA        NULL,
    PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME, JOB_NAME, JOB_GROUP)
        REFERENCES quartz.QRTZ_JOB_DETAILS (SCHED_NAME, JOB_NAME, JOB_GROUP)
);

CREATE TABLE quartz.QRTZ_SIMPLE_TRIGGERS
(
    SCHED_NAME      VARCHAR(120) NOT NULL,
    TRIGGER_NAME    VARCHAR(200) NOT NULL,
    TRIGGER_GROUP   VARCHAR(200) NOT NULL,
    REPEAT_COUNT    BIGINT       NOT NULL,
    REPEAT_INTERVAL BIGINT       NOT NULL,
    TIMES_TRIGGERED BIGINT       NOT NULL,
    PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
        REFERENCES quartz.QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
);

CREATE TABLE quartz.QRTZ_CRON_TRIGGERS
(
    SCHED_NAME      VARCHAR(120) NOT NULL,
    TRIGGER_NAME    VARCHAR(200) NOT NULL,
    TRIGGER_GROUP   VARCHAR(200) NOT NULL,
    CRON_EXPRESSION VARCHAR(120) NOT NULL,
    TIME_ZONE_ID    VARCHAR(80),
    PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
        REFERENCES quartz.QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
);

CREATE TABLE quartz.QRTZ_SIMPROP_TRIGGERS
(
    SCHED_NAME    VARCHAR(120)   NOT NULL,
    TRIGGER_NAME  VARCHAR(200)   NOT NULL,
    TRIGGER_GROUP VARCHAR(200)   NOT NULL,
    STR_PROP_1    VARCHAR(512)   NULL,
    STR_PROP_2    VARCHAR(512)   NULL,
    STR_PROP_3    VARCHAR(512)   NULL,
    INT_PROP_1    INT            NULL,
    INT_PROP_2    INT            NULL,
    LONG_PROP_1   BIGINT         NULL,
    LONG_PROP_2   BIGINT         NULL,
    DEC_PROP_1    NUMERIC(13, 4) NULL,
    DEC_PROP_2    NUMERIC(13, 4) NULL,
    BOOL_PROP_1   BOOL           NULL,
    BOOL_PROP_2   BOOL           NULL,
    PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
        REFERENCES quartz.QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
);

CREATE TABLE quartz.QRTZ_BLOB_TRIGGERS
(
    SCHED_NAME    VARCHAR(120) NOT NULL,
    TRIGGER_NAME  VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    BLOB_DATA     BYTEA        NULL,
    PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
        REFERENCES quartz.QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
);

CREATE TABLE quartz.QRTZ_CALENDARS
(
    SCHED_NAME    VARCHAR(120) NOT NULL,
    CALENDAR_NAME VARCHAR(200) NOT NULL,
    CALENDAR      BYTEA        NOT NULL,
    PRIMARY KEY (SCHED_NAME, CALENDAR_NAME)
);


CREATE TABLE quartz.QRTZ_PAUSED_TRIGGER_GRPS
(
    SCHED_NAME    VARCHAR(120) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    PRIMARY KEY (SCHED_NAME, TRIGGER_GROUP)
);

CREATE TABLE quartz.QRTZ_FIRED_TRIGGERS
(
    SCHED_NAME        VARCHAR(120) NOT NULL,
    ENTRY_ID          VARCHAR(95)  NOT NULL,
    TRIGGER_NAME      VARCHAR(200) NOT NULL,
    TRIGGER_GROUP     VARCHAR(200) NOT NULL,
    INSTANCE_NAME     VARCHAR(200) NOT NULL,
    FIRED_TIME        BIGINT       NOT NULL,
    SCHED_TIME        BIGINT       NOT NULL,
    PRIORITY          INTEGER      NOT NULL,
    STATE             VARCHAR(16)  NOT NULL,
    JOB_NAME          VARCHAR(200) NULL,
    JOB_GROUP         VARCHAR(200) NULL,
    IS_NONCONCURRENT  BOOL         NULL,
    REQUESTS_RECOVERY BOOL         NULL,
    PRIMARY KEY (SCHED_NAME, ENTRY_ID)
);

CREATE TABLE quartz.QRTZ_SCHEDULER_STATE
(
    SCHED_NAME        VARCHAR(120) NOT NULL,
    INSTANCE_NAME     VARCHAR(200) NOT NULL,
    LAST_CHECKIN_TIME BIGINT       NOT NULL,
    CHECKIN_INTERVAL  BIGINT       NOT NULL,
    PRIMARY KEY (SCHED_NAME, INSTANCE_NAME)
);

CREATE TABLE quartz.QRTZ_LOCKS
(
    SCHED_NAME VARCHAR(120) NOT NULL,
    LOCK_NAME  VARCHAR(40)  NOT NULL,
    PRIMARY KEY (SCHED_NAME, LOCK_NAME)
);

CREATE INDEX IDX_QRTZ_J_REQ_RECOVERY
    ON quartz.QRTZ_JOB_DETAILS (SCHED_NAME, REQUESTS_RECOVERY);
CREATE INDEX IDX_QRTZ_J_GRP
    ON quartz.QRTZ_JOB_DETAILS (SCHED_NAME, JOB_GROUP);

CREATE INDEX IDX_QRTZ_T_J
    ON quartz.QRTZ_TRIGGERS (SCHED_NAME, JOB_NAME, JOB_GROUP);
CREATE INDEX IDX_QRTZ_T_JG
    ON quartz.QRTZ_TRIGGERS (SCHED_NAME, JOB_GROUP);
CREATE INDEX IDX_QRTZ_T_C
    ON quartz.QRTZ_TRIGGERS (SCHED_NAME, CALENDAR_NAME);
CREATE INDEX IDX_QRTZ_T_G
    ON quartz.QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_GROUP);
CREATE INDEX IDX_QRTZ_T_STATE
    ON quartz.QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_STATE);
CREATE index IDX_QRTZ_T_N_STATE
    ON quartz.QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP, TRIGGER_STATE);
CREATE index IDX_QRTZ_T_N_G_STATE
    ON quartz.QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_GROUP, TRIGGER_STATE);
CREATE index IDX_QRTZ_T_NEXT_FIRE_TIME
    ON quartz.QRTZ_TRIGGERS (SCHED_NAME, NEXT_FIRE_TIME);
CREATE index IDX_QRTZ_T_NFT_ST
    ON quartz.QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_STATE, NEXT_FIRE_TIME);
CREATE index IDX_QRTZ_T_NFT_MISFIRE
    ON quartz.QRTZ_TRIGGERS (SCHED_NAME, MISFIRE_INSTR, NEXT_FIRE_TIME);
CREATE index IDX_QRTZ_T_NFT_ST_MISFIRE
    ON quartz.QRTZ_TRIGGERS (SCHED_NAME, MISFIRE_INSTR, NEXT_FIRE_TIME, TRIGGER_STATE);
CREATE index IDX_QRTZ_T_NFT_ST_MISFIRE_GRP
    ON quartz.QRTZ_TRIGGERS (SCHED_NAME, MISFIRE_INSTR, NEXT_FIRE_TIME, TRIGGER_GROUP, TRIGGER_STATE);

CREATE index IDX_QRTZ_FT_TRIG_INST_NAME
    ON quartz.QRTZ_FIRED_TRIGGERS (SCHED_NAME, INSTANCE_NAME);
CREATE index IDX_QRTZ_FT_INST_JOB_REQ_RCVRY
    ON quartz.QRTZ_FIRED_TRIGGERS (SCHED_NAME, INSTANCE_NAME, REQUESTS_RECOVERY);
CREATE index IDX_QRTZ_FT_J_G
    ON quartz.QRTZ_FIRED_TRIGGERS (SCHED_NAME, JOB_NAME, JOB_GROUP);
CREATE index IDX_QRTZ_FT_JG
    ON quartz.QRTZ_FIRED_TRIGGERS (SCHED_NAME, JOB_GROUP);
CREATE index IDX_QRTZ_FT_T_G
    ON quartz.QRTZ_FIRED_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP);
CREATE index IDX_QRTZ_FT_TG
    ON quartz.QRTZ_FIRED_TRIGGERS (SCHED_NAME, TRIGGER_GROUP);