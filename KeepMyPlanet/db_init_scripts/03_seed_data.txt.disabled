BEGIN;

DELETE FROM event_state_changes;
DELETE FROM messages;
DELETE FROM event_participants;
DELETE FROM zone_photos;
DELETE FROM events;
DELETE FROM zones;
DELETE FROM users;

ALTER SEQUENCE users_id_seq RESTART WITH 1;
ALTER SEQUENCE zones_id_seq RESTART WITH 1;
ALTER SEQUENCE events_id_seq RESTART WITH 1;
ALTER SEQUENCE messages_id_seq RESTART WITH 1;
ALTER SEQUENCE event_state_changes_id_seq RESTART WITH 1;

-- Seed Users
INSERT INTO users (name, email, password_hash, profile_picture_id, created_at, updated_at) VALUES
('rafael', 'rafael@example.com', '310000:k5WWtU34hbJAWj6rfvNxyQ==:N3MqTsrmCeqoRYIY4PWIA6Db/NTdy/DBoy8oFMo0E3o=', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('diogo', 'diogo@example.com', '310000:k5WWtU34hbJAWj6rfvNxyQ==:N3MqTsrmCeqoRYIY4PWIA6Db/NTdy/DBoy8oFMo0E3o=', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('palex', 'palex@example.com', '310000:k5WWtU34hbJAWj6rfvNxyQ==:N3MqTsrmCeqoRYIY4PWIA6Db/NTdy/DBoy8oFMo0E3o=', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('anon', 'anon@example.com', '310000:k5WWtU34hbJAWj6rfvNxyQ==:N3MqTsrmCeqoRYIY4PWIA6Db/NTdy/DBoy8oFMo0E3o=', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('admin', 'admin@example.com', '310000:k5WWtU34hbJAWj6rfvNxyQ==:N3MqTsrmCeqoRYIY4PWIA6Db/NTdy/DBoy8oFMo0E3o=', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Seed Zones
INSERT INTO zones (latitude, longitude, description, reporter_id, status, zone_severity, created_at, updated_at) VALUES
 (38.716670, -9.133330, 'Praia da Figueirinha needs urgent cleaning. Lots of plastic bottles and fishing nets.', 1, 'REPORTED', 'HIGH', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
 (41.150000, -8.616670, 'Parque da Cidade has accumulated a lot of litter near the picnic areas.', 2, 'REPORTED', 'MEDIUM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
 (39.750000, -8.800000, 'Significant debris along the Lis River banks after recent rains.', 3, 'CLEANING_SCHEDULED', 'MEDIUM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
 (37.016670, -7.933330, 'Forest trail near Faro was littered, but we cleaned it last month.', 1, 'CLEANED', 'LOW', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Seed Events
INSERT INTO events (title, description, start_datetime, end_datetime, zone_id, organizer_id, status, max_participants, created_at, updated_at) VALUES
(
   'Figueirinha Beach Cleanup Drive',
   'Join us to clean up Praia da Figueirinha! Bring gloves and water.',
   CURRENT_TIMESTAMP + INTERVAL '7 days',
   NULL,
   1,
   1,
   'PLANNED',
   20,
   CURRENT_TIMESTAMP,
   CURRENT_TIMESTAMP
),
(
   'Porto City Park Cleanup',
   'Let''s make our park beautiful again. Refreshments provided.',
   CURRENT_TIMESTAMP + INTERVAL '1 hour',
   NULL,
   2,
   2,
   'PLANNED',
   15,
   CURRENT_TIMESTAMP,
   CURRENT_TIMESTAMP
),
(
   'Lis River Bank Restoration',
   'Cleanup of Lis River banks. Successfully completed!',
   CURRENT_TIMESTAMP - INTERVAL '5 days',
   CURRENT_TIMESTAMP - INTERVAL '5 days' + INTERVAL '4 hours',
   3,
   3,
   'COMPLETED',
   30,
   CURRENT_TIMESTAMP,
   CURRENT_TIMESTAMP
);

-- Atualizar sequências
DO $$
DECLARE
max_id INT;
BEGIN
    -- Atualizar sequência de users
SELECT MAX(id) INTO max_id FROM users;
IF max_id IS NOT NULL THEN
        EXECUTE format('ALTER SEQUENCE users_id_seq RESTART WITH %s;', max_id + 1);
END IF;

    -- Atualizar sequência de zones
SELECT MAX(id) INTO max_id FROM zones;
IF max_id IS NOT NULL THEN
        EXECUTE format('ALTER SEQUENCE zones_id_seq RESTART WITH %s;', max_id + 1);
END IF;

    -- Atualizar sequência de events
SELECT MAX(id) INTO max_id FROM events;
IF max_id IS NOT NULL THEN
        EXECUTE format('ALTER SEQUENCE events_id_seq RESTART WITH %s;', max_id + 1);
END IF;

    -- Atualizar sequência de event_state_changes
SELECT MAX(id) INTO max_id FROM event_state_changes;
IF max_id IS NOT NULL THEN
        EXECUTE format('ALTER SEQUENCE event_state_changes_id_seq RESTART WITH %s;', max_id + 1);
END IF;

    -- Atualizar sequência de messages
SELECT MAX(id) INTO max_id FROM messages;
IF max_id IS NOT NULL THEN
        EXECUTE format('ALTER SEQUENCE messages_id_seq RESTART WITH %s;', max_id + 1);
END IF;
END $$;

-- Seed Event Participants

-- Event 1
INSERT INTO event_participants (event_id, user_id) VALUES
(1, 1),
(1, 2);

-- Event 2
INSERT INTO event_participants (event_id, user_id) VALUES
    (2, 1);

-- Event 3
INSERT INTO event_participants (event_id, user_id) VALUES
(3, 1),
(3, 3),
(3, 4);

-- Seed Event State Changes
INSERT INTO event_state_changes (event_id, new_status, changed_by, change_time) VALUES
-- Evento 1 (id=1) foi criado como PLANNED
(1, 'PLANNED', 1, CURRENT_TIMESTAMP),

-- Evento 2 (id=2) foi criado como PLANNED
(2, 'PLANNED', 2, CURRENT_TIMESTAMP),

-- Evento 3 (id=3) foi criado como PLANNED e depois movido para COMPLETED
(3, 'PLANNED', 3, CURRENT_TIMESTAMP - INTERVAL '6 days'),
(3, 'COMPLETED', 3, CURRENT_TIMESTAMP - INTERVAL '5 days' + INTERVAL '4 hours');

-- Update Zone
UPDATE zones SET status = 'CLEANING_SCHEDULED' WHERE id = 1;
UPDATE zones SET status = 'CLEANING_SCHEDULED' WHERE id = 2;
UPDATE zones SET status = 'CLEANED' WHERE id = 3;

-- Seed Messages

-- Messages for Event 1
INSERT INTO messages (event_id, sender_id, sender_name, content, "timestamp", chat_position) VALUES
(
 1, 4, 'anon',
 'Hi team! Looking forward to seeing you all at Figueirinha!',
 CURRENT_TIMESTAMP - INTERVAL '30 minutes', 0
),
(
 1, 1, 'rafael',
 'Can''t wait! I''ll bring extra bags.',
 CURRENT_TIMESTAMP - INTERVAL '25 minutes', 1
),
(
 1, 4, 'anon',
 'Great! Remember to wear sunscreen.',
 CURRENT_TIMESTAMP - INTERVAL '20 minutes', 2
);

-- Messages for Event 2
INSERT INTO messages (event_id, sender_id, sender_name, content, "timestamp", chat_position) VALUES
(
 2, 3, 'palex',
 'We are starting now at the main entrance!',
 CURRENT_TIMESTAMP - INTERVAL '5 minutes', 0
),
(
 2, 3, 'palex',
 'Found a lot of plastic near the lake, need help here!',
 CURRENT_TIMESTAMP + INTERVAL '30 minutes', 1
);

COMMIT;