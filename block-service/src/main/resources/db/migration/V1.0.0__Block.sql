create table if not exists block_db.blocks (
    created_at timestamptz,
    hash varchar(255) not null,
    status varchar(255) check (status in ('AWAITING_UPLOAD','UPLOADED')),
    primary key (hash)
);

create table if not exists block_db.tasks (
    id integer generated always as identity,
    retries smallint,
    updated_at timestamptz,
    action varchar(255) check (action in ('DELETE_BLOCKS')),
    object_id varchar(255),
    status varchar(255) check (status in ('CREATED','FINISHED','IN_RETRY','FAILED')),
    metadata jsonb,
    primary key (id)
);