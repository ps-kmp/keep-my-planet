BEGIN;

-- CLEANUP
DELETE FROM event_state_changes;
DELETE FROM zone_state_changes;
DELETE FROM messages;
DELETE FROM event_attendances;
DELETE FROM event_participants;
DELETE FROM zone_photos;
DELETE FROM events;
DELETE FROM zones;
DELETE FROM photos;
DELETE FROM users;

-- RESET SEQUENCES
ALTER SEQUENCE users_id_seq RESTART WITH 1;
ALTER SEQUENCE photos_id_seq RESTART WITH 1;
ALTER SEQUENCE zones_id_seq RESTART WITH 1;
ALTER SEQUENCE events_id_seq RESTART WITH 1;
ALTER SEQUENCE messages_id_seq RESTART WITH 1;
ALTER SEQUENCE event_state_changes_id_seq RESTART WITH 1;
ALTER SEQUENCE zone_state_changes_id_seq RESTART WITH 1;

-- SEED USERS
INSERT INTO users (name, email, password_hash, created_at, updated_at) VALUES
('rafael', 'rafael@example.com', '310000:k5WWtU34hbJAWj6rfvNxyQ==:N3MqTsrmCeqoRYIY4PWIA6Db/NTdy/DBoy8oFMo0E3o=', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('diogo', 'diogo@example.com', '310000:k5WWtU34hbJAWj6rfvNxyQ==:N3MqTsrmCeqoRYIY4PWIA6Db/NTdy/DBoy8oFMo0E3o=', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('palex', 'palex@example.com', '310000:k5WWtU34hbJAWj6rfvNxyQ==:N3MqTsrmCeqoRYIY4PWIA6Db/NTdy/DBoy8oFMo0E3o=', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('alice', 'alice@example.com', '310000:k5WWtU34hbJAWj6rfvNxyQ==:N3MqTsrmCeqoRYIY4PWIA6Db/NTdy/DBoy8oFMo0E3o=', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('bob', 'bob@example.com', '310000:k5WWtU34hbJAWj6rfvNxyQ==:N3MqTsrmCeqoRYIY4PWIA6Db/NTdy/DBoy8oFMo0E3o=', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('anon', 'anon@example.com', '310000:k5WWtU34hbJAWj6rfvNxyQ==:N3MqTsrmCeqoRYIY4PWIA6Db/NTdy/DBoy8oFMo0E3o=', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- SEED ZONES
INSERT INTO zones (latitude, longitude, description, reporter_id, status, zone_severity, is_active, created_at, updated_at) VALUES
(38.716670, -9.133330, 'Praia da Figueirinha needs urgent cleaning. Lots of plastic bottles and fishing nets.', 1, 'REPORTED', 'HIGH', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(41.150000, -8.616670, 'Parque da Cidade has accumulated a lot of litter near the picnic areas.', 2, 'REPORTED', 'MEDIUM', TRUE,CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(39.750000, -8.800000, 'Minor debris along the Lis River banks after recent rains.', 3, 'REPORTED', 'LOW', TRUE,CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(37.016670, -7.933330, 'Forest trail near Faro is littered with camping waste.', 4, 'REPORTED', 'MEDIUM', TRUE,CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(38.7593, -9.1553, 'Litter scattered around Jardim do Campo Grande.', 5, 'REPORTED', 'HIGH', TRUE,CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(40.6405, -8.6538, 'Some trash found near the University of Aveiro campus.', 6, 'REPORTED', 'LOW', TRUE,CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- SEED EVENTS & LINK TO ZONES
INSERT INTO events (title, description, start_datetime, end_datetime, zone_id, organizer_id, status, max_participants, pending_organizer_id, transfer_request_time, created_at, updated_at) VALUES
('Porto City Park Cleanup', 'Let''s make our park beautiful again. Refreshments provided.', CURRENT_TIMESTAMP + INTERVAL '10 days', NULL, 2, 2, 'PLANNED', 20, NULL, NULL,CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Lis River Bank Restoration', 'Cleanup of Lis River banks. Great job everyone!', CURRENT_TIMESTAMP - INTERVAL '15 days', CURRENT_TIMESTAMP - INTERVAL '15 days' + INTERVAL '3 hours', 3, 3, 'COMPLETED', 10, NULL, NULL,CURRENT_TIMESTAMP - INTERVAL '20 days', CURRENT_TIMESTAMP - INTERVAL '15 days'),
('Faro Forest Trail Cleanup', 'Let''s clean this beautiful trail. Bring sturdy shoes!', CURRENT_TIMESTAMP - INTERVAL '1 hour', CURRENT_TIMESTAMP + INTERVAL '2 hours', 4, 4, 'IN_PROGRESS', 4, NULL, NULL,CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP - INTERVAL '1 hour'),
('Campo Grande Garden Tidy Up', 'Event cancelled due to bad weather forecast.', CURRENT_TIMESTAMP + INTERVAL '5 days', NULL, 5, 5, 'CANCELLED', 15, NULL, NULL,CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '1 day'),
('Figueirinha Beach Mega-Cleanup', 'Let''s tackle this high-priority zone together! We need all hands on deck.', CURRENT_TIMESTAMP + INTERVAL '14 days', NULL, 1, 1, 'PLANNED', 50, NULL, NULL,CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Update Zone status based on linked event
UPDATE zones SET event_id = 1, status = 'CLEANING_SCHEDULED' WHERE id = 2;
UPDATE zones SET event_id = 2, status = 'CLEANED', is_active = FALSE WHERE id = 3;
UPDATE zones SET event_id = 3, status = 'CLEANING_SCHEDULED' WHERE id = 4;
UPDATE zones SET event_id = NULL WHERE id = 5;
UPDATE zones SET event_id = 5, status = 'CLEANING_SCHEDULED' WHERE id = 1;

-- SEED EVENT PARTICIPANTS
INSERT INTO event_participants (event_id, user_id) VALUES (1, 2), (1, 1), (1, 6);
INSERT INTO event_participants (event_id, user_id) VALUES (2, 3), (2, 1), (2, 4);
INSERT INTO event_participants (event_id, user_id) VALUES (3, 4), (3, 1), (3, 2), (3, 5);
INSERT INTO event_participants (event_id, user_id) VALUES (4, 5), (4, 3);
INSERT INTO event_participants (event_id, user_id) VALUES (5, 1), (5, 2), (5, 3), (5, 4), (5, 5), (5, 6);

-- SEED EVENT ATTENDANCES
INSERT INTO event_attendances (event_id, user_id, checked_in_at) VALUES
(2, 1, CURRENT_TIMESTAMP - INTERVAL '15 days'),
(2, 4, CURRENT_TIMESTAMP - INTERVAL '15 days');
INSERT INTO event_attendances (event_id, user_id, checked_in_at) VALUES
(3, 1, CURRENT_TIMESTAMP - INTERVAL '30 minutes'),
(3, 5, CURRENT_TIMESTAMP - INTERVAL '15 minutes');

-- SEED EVENT STATE CHANGES
INSERT INTO event_state_changes (event_id, new_status, changed_by, change_time) VALUES
(1, 'PLANNED', 2, CURRENT_TIMESTAMP),
(2, 'PLANNED', 3, CURRENT_TIMESTAMP - INTERVAL '20 days'),
(2, 'IN_PROGRESS', 3, CURRENT_TIMESTAMP - INTERVAL '15 days'),
(2, 'COMPLETED', 3, CURRENT_TIMESTAMP - INTERVAL '15 days' + INTERVAL '3 hours'),
(3, 'PLANNED', 4, CURRENT_TIMESTAMP - INTERVAL '5 days'),
(3, 'IN_PROGRESS', 4, CURRENT_TIMESTAMP - INTERVAL '1 hour'),
(4, 'PLANNED', 5, CURRENT_TIMESTAMP - INTERVAL '2 days'),
(4, 'CANCELLED', 5, CURRENT_TIMESTAMP - INTERVAL '1 day'),
(5, 'PLANNED', 1, CURRENT_TIMESTAMP);

-- SEED MESSAGES
INSERT INTO messages (event_id, sender_id, content, "timestamp", chat_position) VALUES
(1, 2, 'Hi team! Who is bringing snacks for the Porto cleanup?', CURRENT_TIMESTAMP - INTERVAL '1 day', 0),
(1, 1, 'I can bring some sandwiches!', CURRENT_TIMESTAMP - INTERVAL '23 hours', 1),
(1, 6, 'I will bring water and juice for everyone.', CURRENT_TIMESTAMP - INTERVAL '22 hours', 2),
(3, 4, 'I am at the entrance of the trail now, where is everyone?', CURRENT_TIMESTAMP - INTERVAL '55 minutes', 0),
(3, 1, 'Almost there, 5 minutes away!', CURRENT_TIMESTAMP - INTERVAL '50 minutes', 1),
(3, 5, 'I found a huge pile of tires. Need some help here when you arrive.', CURRENT_TIMESTAMP - INTERVAL '45 minutes', 2),
(3, 4, 'Okay Bob, we will head your way once Rafael and Diogo get here.', CURRENT_TIMESTAMP - INTERVAL '44 minutes', 3);

INSERT INTO zone_state_changes (zone_id, new_status, changed_by, triggered_by_event_id, change_time) VALUES
(2, 'CLEANING_SCHEDULED', 2, 1, CURRENT_TIMESTAMP),
(3, 'CLEANED', 3, 2, CURRENT_TIMESTAMP - INTERVAL '15 days' + INTERVAL '3 hours'),
(4, 'CLEANING_SCHEDULED', 4, 3, CURRENT_TIMESTAMP - INTERVAL '5 days'),
(1, 'CLEANING_SCHEDULED', 1, 5, CURRENT_TIMESTAMP);

-- UPDATE SEQUENCES
DO $$
DECLARE
    max_id INT;
BEGIN
    SELECT COALESCE(MAX(id), 0) INTO max_id FROM users;
    EXECUTE format('ALTER SEQUENCE users_id_seq RESTART WITH %s;', max_id + 1);

    SELECT COALESCE(MAX(id), 0) INTO max_id FROM photos;
    EXECUTE format('ALTER SEQUENCE photos_id_seq RESTART WITH %s;', max_id + 1);

    SELECT COALESCE(MAX(id), 0) INTO max_id FROM zones;
    EXECUTE format('ALTER SEQUENCE zones_id_seq RESTART WITH %s;', max_id + 1);

    SELECT COALESCE(MAX(id), 0) INTO max_id FROM events;
    EXECUTE format('ALTER SEQUENCE events_id_seq RESTART WITH %s;', max_id + 1);

    SELECT COALESCE(MAX(id), 0) INTO max_id FROM event_state_changes;
    EXECUTE format('ALTER SEQUENCE event_state_changes_id_seq RESTART WITH %s;', max_id + 1);

    SELECT COALESCE(MAX(id), 0) INTO max_id FROM zone_state_changes;
    EXECUTE format('ALTER SEQUENCE zone_state_changes_id_seq RESTART WITH %s;', max_id + 1);

    SELECT COALESCE(MAX(id), 0) INTO max_id FROM messages;
    EXECUTE format('ALTER SEQUENCE messages_id_seq RESTART WITH %s;', max_id + 1);
END $$;


COMMIT;