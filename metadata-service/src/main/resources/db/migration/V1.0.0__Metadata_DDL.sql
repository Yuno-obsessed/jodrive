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

create or replace function metadata_db.next_history_id(p_ws_id bigint)
    returns integer
    language plpgsql
as $$
declare
    result integer;
begin
    select max(metadata_db.file_journal.history_id) + 1 from file_journal
    where metadata_db.file_journal.ws_id = p_ws_id into result;
    return result;
end;
$$;

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
    add constraint file_journal_updated_by
        foreign key (updated_by)
            references metadata_db.users;
alter table if exists metadata_db.file_journal
    add constraint file_uploader
        foreign key (uploader_id)
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