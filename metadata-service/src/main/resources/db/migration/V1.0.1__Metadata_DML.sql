insert into metadata_db.user_subscriptions (title, description, storage_limit, workspaces_limit) values ('Normal', 'Default subscription', 10737418240, 3); -- 10GiB
insert into metadata_db.user_subscriptions (title, description, storage_limit, workspaces_limit) values ('Advanced', 'Advanced subscription', 429496729600, 10); -- 400 GiB

insert into metadata_db.users (id, username, email, subscription_id, created_at, updated_at) values ('5a9bf3fa-d99a-4ccc-b64f-b2ddf20ee5e5', 'admin', 'example@gmail.com', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
insert into metadata_db.users (id, username, email, subscription_id, created_at, updated_at) values ('29849880-ddd4-4000-b100-460f4c505045', 'anonymous', 'anon@gmail.com', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
insert into metadata_db.workspaces (name, description) values ('w1', 'first workspace');
insert into metadata_db.workspaces (name, description) values ('w2', 'second workspace');
insert into metadata_db.user_workspaces (user_id, ws_id, role, joined_at) values ('5a9bf3fa-d99a-4ccc-b64f-b2ddf20ee5e5', 1, 'OWNER', CURRENT_TIMESTAMP);
insert into metadata_db.user_workspaces (user_id, ws_id, role, joined_at) values ('5a9bf3fa-d99a-4ccc-b64f-b2ddf20ee5e5', 2, 'USER', CURRENT_TIMESTAMP);
insert into metadata_db.user_workspaces (user_id, ws_id, role, joined_at) values ('29849880-ddd4-4000-b100-460f4c505045', 1, 'USER', CURRENT_TIMESTAMP);
insert into metadata_db.user_workspaces (user_id, ws_id, role, joined_at) values ('29849880-ddd4-4000-b100-460f4c505045', 2, 'OWNER', CURRENT_TIMESTAMP);
insert into metadata_db.file_journal (ws_id, file_id, latest, path, uploader_id, state, "size", blocklist, history_id, created_at, updated_at) values (1, (SELECT metadata_db.next_file_id(1)), 1, '/', '5a9bf3fa-d99a-4ccc-b64f-b2ddf20ee5e5', 'UPLOADED', 0, null, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
insert into metadata_db.file_journal (ws_id, file_id, latest, path, uploader_id, state, "size", blocklist, history_id, created_at, updated_at) values (2, (SELECT metadata_db.next_file_id(2)), 1, '/', '29849880-ddd4-4000-b100-460f4c505045', 'UPLOADED', 0, null, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
insert into metadata_db.statistics(quota, description) values ('USER_STORAGE_USED', 'Indicates how much storage user has used up');
insert into metadata_db.statistics(quota, description) values ('USER_WORKSPACES', 'Indicates to how many workspaces user is connected');
insert into metadata_db.user_statistics(user_id, statistics_id, value) values ('5a9bf3fa-d99a-4ccc-b64f-b2ddf20ee5e5', 1, '0');
insert into metadata_db.user_statistics(user_id, statistics_id, value) values ('5a9bf3fa-d99a-4ccc-b64f-b2ddf20ee5e5', 2, '2');
insert into metadata_db.user_statistics(user_id, statistics_id, value) values ('29849880-ddd4-4000-b100-460f4c505045', 1, '0');
insert into metadata_db.user_statistics(user_id, statistics_id, value) values ('29849880-ddd4-4000-b100-460f4c505045', 2, '2')