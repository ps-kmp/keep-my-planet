-- ===================================================================================
-- 0. Reset Database State
-- ===================================================================================
TRUNCATE TABLE
    users, photos, zones, events, event_state_changes, zone_state_changes,
    messages, event_participants, event_attendances, zone_photos, user_devices
RESTART IDENTITY CASCADE;

-- ===================================================================================
-- 1. User Accounts (without profile pictures)
-- ===================================================================================
INSERT INTO users (id, name, email, password_hash, role, profile_picture_id, created_at, updated_at) VALUES
(1, 'Admin', 'admin@kmp.com', '310000:k5WWtU34hbJAWj6rfvNxyQ==:N3MqTsrmCeqoRYIY4PWIA6Db/NTdy/DBoy8oFMo0E3o=', 'ADMIN', NULL, NOW() - INTERVAL '30 days', NOW() - INTERVAL '5 days'),
(2, 'Rafael', 'rafael@kmp.com', '310000:k5WWtU34hbJAWj6rfvNxyQ==:N3MqTsrmCeqoRYIY4PWIA6Db/NTdy/DBoy8oFMo0E3o=', 'USER', NULL, NOW() - INTERVAL '28 days', NOW() - INTERVAL '2 days'),
(3, 'Diogo', 'diogo@kmp.com', '310000:k5WWtU34hbJAWj6rfvNxyQ==:N3MqTsrmCeqoRYIY4PWIA6Db/NTdy/DBoy8oFMo0E3o=', 'USER', NULL, NOW() - INTERVAL '25 days', NOW() - INTERVAL '1 day'),
(4, 'Palex', 'palex@kmp.com', '310000:k5WWtU34hbJAWj6rfvNxyQ==:N3MqTsrmCeqoRYIY4PWIA6Db/NTdy/DBoy8oFMo0E3o=', 'USER', NULL, NOW() - INTERVAL '20 days', NOW()),
(5, 'Ana', 'ana@kmp.com', '310000:k5WWtU34hbJAWj6rfvNxyQ==:N3MqTsrmCeqoRYIY4PWIA6Db/NTdy/DBoy8oFMo0E3o=', 'USER', NULL, NOW() - INTERVAL '15 days', NOW()),
(6, 'Castro', 'castro@kmp.com', '310000:k5WWtU34hbJAWj6rfvNxyQ==:N3MqTsrmCeqoRYIY4PWIA6Db/NTdy/DBoy8oFMo0E3o=', 'USER', NULL, NOW() - INTERVAL '10 days', NOW()),
(7, 'Paulo', 'paulo@kmp.com', '310000:k5WWtU34hbJAWj6rfvNxyQ==:N3MqTsrmCeqoRYIY4PWIA6Db/NTdy/DBoy8oFMo0E3o=', 'USER', NULL, NOW() - INTERVAL '5 days', NOW());

-- ===================================================================================
-- 2. Photos
-- ===================================================================================
INSERT INTO photos (id, url, uploader_id, uploaded_at) VALUES
(1, 'https://res.cloudinary.com/dpej7n5m3/image/upload/v1721516244/profile_pictures/profile_admin.jpg', 1, NOW() - INTERVAL '30 days'),
(2, 'https://res.cloudinary.com/dpej7n5m3/image/upload/v1721516244/profile_pictures/profile_organizer.jpg', 2, NOW() - INTERVAL '28 days'),
(3, 'https://res.cloudinary.com/dpej7n5m3/image/upload/v1721516244/profile_pictures/profile_participant1.jpg', 3, NOW() - INTERVAL '25 days'),
(4, 'https://res.cloudinary.com/dpej7n5m3/image/upload/v1721516244/profile_pictures/profile_participant2.jpg', 5, NOW() - INTERVAL '15 days'),
(10, 'https://res.cloudinary.com/dpej7n5m3/image/upload/v1721516246/profile_pictures/zone_10_before.jpg', 6, NOW() - INTERVAL '12 days'),
(11, 'https://res.cloudinary.com/dpej7n5m3/image/upload/v1721516246/profile_pictures/zone_11_before.jpg', 2, NOW() - INTERVAL '8 days'),
(12, 'https://res.cloudinary.com/dpej7n5m3/image/upload/v1721516246/profile_pictures/zone_12_before.jpg', 3, NOW() - INTERVAL '20 days'),
(13, 'https://res.cloudinary.com/dpej7n5m3/image/upload/v1721516246/profile_pictures/zone_13_before.jpg', 4, NOW() - INTERVAL '14 days'),
(14, 'https://res.cloudinary.com/dpej7n5m3/image/upload/v1721516246/profile_pictures/zone_14_before.jpg', 6, NOW() - INTERVAL '6 days'),
(15, 'https://res.cloudinary.com/dpej7n5m3/image/upload/v1721516246/profile_pictures/zone_15_before.jpg', 2, NOW() - INTERVAL '22 days'),
(20, 'https://res.cloudinary.com/dpej7n5m3/image/upload/v1721516246/profile_pictures/zone_12_after.jpg', 2, NOW() - INTERVAL '12 days'),
(21, 'https://res.cloudinary.com/dpej7n5m3/image/upload/v1721516246/profile_pictures/zone_15_after.jpg', 2, NOW() - INTERVAL '18 days');

-- ===================================================================================
-- 4. Zones
-- ===================================================================================
INSERT INTO zones (id, latitude, longitude, radius, description, reporter_id, event_id, status, zone_severity, is_active, created_at, updated_at) VALUES
(1, 38.7166, -9.1333, 50.0, 'Large plastic pile near Rossio Square. Requires immediate attention.', 6, NULL, 'REPORTED', 'HIGH', TRUE, NOW() - INTERVAL '12 days', NOW() - INTERVAL '12 days'),
(2, 38.6788, -9.3369, 75.5, 'Carcavelos beach with plastic waste and cigarette butts near the main access.', 2, NULL, 'CLEANING_SCHEDULED', 'MEDIUM', TRUE, NOW() - INTERVAL '8 days', NOW() - INTERVAL '7 days'),
(3, 38.7249, -9.1824, 100.0, 'Monsanto Forest Park picnic area full of leftover trash and bottles.', 3, NULL, 'CLEANED', 'LOW', FALSE, NOW() - INTERVAL '20 days', NOW() - INTERVAL '12 days'),
(4, 38.7072, -9.1354, 30.0, 'Graffiti and dumped waste in a back alley in Bairro Alto.', 4, NULL, 'CLEANING_SCHEDULED', 'MEDIUM', TRUE, NOW() - INTERVAL '14 days', NOW() - INTERVAL '1 hour'),
(5, 38.7997, -9.3828, 120.0, 'Illegal dumping of construction materials on a trail in the Sintra hills.', 6, NULL, 'REPORTED', 'HIGH', TRUE, NOW() - INTERVAL '6 days', NOW() - INTERVAL '1 day'),
(6, 38.6975, -9.2063, 40.0, 'Gardens near Belém Tower littered with tourist waste. Cleaned by a small group.', 2, NULL, 'CLEANED', 'LOW', FALSE, NOW() - INTERVAL '22 days', NOW() - INTERVAL '18 days'),
(7, 38.7170, -9.1330, 25.0, 'Overflowing trash can at a bus stop near Rossio.', 3, NULL, 'REPORTED', 'LOW', TRUE, NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),
(8, 38.7601, -9.0945, 60.0, 'Parque das Nações riverside path needs debris clearing.', 5, NULL, 'CLEANING_SCHEDULED', 'MEDIUM', TRUE, NOW() - INTERVAL '11 days', NOW() - INTERVAL '10 days'),
(9, 38.6432, -9.2361, 200.0, 'Dunes at Costa da Caparica full of plastic bottles and food wrappers.', 2, NULL, 'CLEANING_SCHEDULED', 'MEDIUM', TRUE, NOW() - INTERVAL '16 days', NOW() - INTERVAL '15 days');


-- ===================================================================================
-- 5. Events
-- ===================================================================================
INSERT INTO events (id, title, description, start_datetime, end_datetime, zone_id, organizer_id, status, max_participants, pending_organizer_id, transfer_request_time, created_at, updated_at) VALUES
(1, 'Carcavelos Beach Cleanup', 'Let''s clean the beautiful Carcavelos beach together. Gloves and bags will be provided.', NOW() + INTERVAL '7 days', (NOW() + INTERVAL '7 days') + INTERVAL '3 hours', 2, 2, 'PLANNED', 50, NULL, NULL, NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days'),
(2, 'Monsanto Park Restoration', 'We successfully cleaned the main picnic area. Thanks to all volunteers!', NOW() - INTERVAL '15 days', (NOW() - INTERVAL '15 days') + INTERVAL '4 hours', 3, 3, 'COMPLETED', 20, NULL, NULL, NOW() - INTERVAL '18 days', NOW() - INTERVAL '12 days'),
(3, 'Bairro Alto Alley Sweep', 'Currently cleaning up the area. Join us if you are nearby!', NOW() - INTERVAL '1 hour', (NOW() - INTERVAL '1 hour') + INTERVAL '3 hours', 4, 2, 'IN_PROGRESS', NULL, NULL, NULL, NOW() - INTERVAL '5 days', NOW() - INTERVAL '1 hour'),
(4, 'Sintra Hills Trail Cleanup', 'This event has been cancelled due to unforeseen circumstances.', NOW() + INTERVAL '2 days', (NOW() + INTERVAL '2 days') + INTERVAL '3 hours', 5, 6, 'CANCELLED', 30, NULL, NULL, NOW() - INTERVAL '4 days', NOW() - INTERVAL '1 day'),
(5, 'Parque das Nações Riverside Rally', 'Clearing debris from the riverside path. A great way to spend a Saturday!', NOW() + INTERVAL '10 days', (NOW() + INTERVAL '10 days') + INTERVAL '2 hours', 8, 2, 'PLANNED', 40, 5, NOW() - INTERVAL '1 day', NOW() - INTERVAL '11 days', NOW() - INTERVAL '1 day'),
(6, 'Caparica Dune Restoration', 'A walk on the beach combined with cleaning our beautiful dunes. This event is currently full.', NOW() + INTERVAL '12 days', (NOW() + INTERVAL '12 days') + INTERVAL '5 hours', 9, 2, 'PLANNED', 2, NULL, NULL, NOW() - INTERVAL '15 days', NOW() - INTERVAL '14 days');

-- ===================================================================================
-- 6. Update Zones with Event Links
-- ===================================================================================
UPDATE zones SET event_id = 1 WHERE id = 2;
UPDATE zones SET event_id = 2 WHERE id = 3;
UPDATE zones SET event_id = 3 WHERE id = 4;
UPDATE zones SET event_id = 5 WHERE id = 8;
UPDATE zones SET event_id = 6 WHERE id = 9;

-- ===================================================================================
-- 7. Junction & Relational Tables
-- ===================================================================================
-- event_participants
INSERT INTO event_participants (event_id, user_id) VALUES
(1, 2), (1, 3), (1, 4), (1, 5),
(2, 3), (2, 2), (2, 4),
(3, 2), (3, 3),
(4, 6),
(5, 2), (5, 5), (5, 3),
(6, 2), (6, 4);

-- event_attendances
INSERT INTO event_attendances (event_id, user_id, checked_in_at) VALUES
(2, 3, NOW() - INTERVAL '15 days'), (2, 2, NOW() - INTERVAL '15 days'),
(3, 2, NOW() - INTERVAL '55 minutes');

-- zone_photos
INSERT INTO zone_photos (zone_id, photo_id, photo_type) VALUES
(1, 10, 'BEFORE'),
(2, 11, 'BEFORE'),
(3, 12, 'BEFORE'), (3, 20, 'AFTER'),
(4, 13, 'BEFORE'),
(5, 14, 'BEFORE'),
(6, 15, 'BEFORE'), (6, 21, 'AFTER');

-- ===================================================================================
-- 8. State Change History
-- ===================================================================================
-- event_state_changes
INSERT INTO event_state_changes (id, event_id, new_status, changed_by, change_time) VALUES
(1, 2, 'PLANNED', 3, NOW() - INTERVAL '18 days'),
(2, 2, 'IN_PROGRESS', 3, NOW() - INTERVAL '15 days'),
(3, 2, 'COMPLETED', 3, NOW() - INTERVAL '12 days'),
(4, 3, 'PLANNED', 2, NOW() - INTERVAL '5 days'),
(5, 3, 'IN_PROGRESS', 2, NOW() - INTERVAL '1 hour'),
(6, 4, 'PLANNED', 6, NOW() - INTERVAL '4 days'),
(7, 4, 'CANCELLED', 6, NOW() - INTERVAL '1 day');

-- zone_state_changes
INSERT INTO zone_state_changes (id, zone_id, new_status, changed_by, triggered_by_event_id, change_time) VALUES
(1, 2, 'REPORTED', 2, NULL, NOW() - INTERVAL '8 days'),
(2, 2, 'CLEANING_SCHEDULED', 2, 1, NOW() - INTERVAL '7 days'),
(3, 3, 'REPORTED', 3, NULL, NOW() - INTERVAL '20 days'),
(4, 3, 'CLEANING_SCHEDULED', 3, 2, NOW() - INTERVAL '18 days'),
(5, 3, 'CLEANED', 3, 2, NOW() - INTERVAL '12 days'),
(6, 5, 'REPORTED', 6, NULL, NOW() - INTERVAL '6 days'),
(7, 5, 'CLEANING_SCHEDULED', 6, 4, NOW() - INTERVAL '4 days'),
(8, 5, 'REPORTED', 6, 4, NOW() - INTERVAL '1 day');

-- ===================================================================================
-- 9. Messages
-- ===================================================================================
INSERT INTO messages (event_id, sender_id, content, "timestamp", chat_position) VALUES
(3, 2, 'Hey everyone, I''m at the meeting point. The alley is to the left of the green door.', NOW() - INTERVAL '50 minutes', 0),
(3, 3, 'On my way! I should be there in 5 minutes. Did you bring extra gloves?', NOW() - INTERVAL '48 minutes', 1),
(3, 2, 'Yep, got a whole box. No worries!', NOW() - INTERVAL '47 minutes', 2),
(3, 3, 'Awesome, see you soon!', NOW() - INTERVAL '46 minutes', 3);

-- ===================================================================================
-- 10. Fix Sequences to Resume from MAX(id)
-- ===================================================================================
SELECT setval('users_id_seq', (SELECT COALESCE(MAX(id), 1) FROM users));
SELECT setval('photos_id_seq', (SELECT COALESCE(MAX(id), 1) FROM photos));
SELECT setval('zones_id_seq', (SELECT COALESCE(MAX(id), 1) FROM zones));
SELECT setval('events_id_seq', (SELECT COALESCE(MAX(id), 1) FROM events));
SELECT setval('event_state_changes_id_seq', (SELECT COALESCE(MAX(id), 1) FROM event_state_changes));
SELECT setval('zone_state_changes_id_seq', (SELECT COALESCE(MAX(id), 1) FROM zone_state_changes));
SELECT setval('messages_id_seq', (SELECT COALESCE(MAX(id), 1) FROM messages));
SELECT setval('user_devices_id_seq', (SELECT COALESCE(MAX(id), 1) FROM user_devices));
