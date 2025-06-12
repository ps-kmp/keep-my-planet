CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Indexes for users table
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_name ON users(name);
CREATE INDEX IF NOT EXISTS idx_users_profile_picture_id ON users(profile_picture_id);

-- Indexes for photos table
CREATE INDEX IF NOT EXISTS idx_photos_uploader_id ON photos(uploader_id);

-- Indexes for zones table
CREATE INDEX IF NOT EXISTS idx_zones_reporter_id ON zones(reporter_id);
CREATE INDEX IF NOT EXISTS idx_zones_event_id ON zones(event_id);
CREATE INDEX IF NOT EXISTS idx_zones_status ON zones(status);
CREATE INDEX IF NOT EXISTS idx_zones_severity ON zones(zone_severity);
CREATE INDEX IF NOT EXISTS idx_zones_coordinates ON zones(latitude, longitude);

-- Indexes for events table
CREATE INDEX IF NOT EXISTS idx_events_zone_id ON events(zone_id);
CREATE INDEX IF NOT EXISTS idx_events_organizer_id ON events(organizer_id);
CREATE INDEX IF NOT EXISTS idx_events_status ON events(status);
CREATE INDEX IF NOT EXISTS idx_events_start_datetime ON events(start_datetime);
CREATE INDEX IF NOT EXISTS idx_events_title_trgm ON events USING GIN (title gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_events_status_start_datetime ON events(status, start_datetime);

-- Indexes for messages table
CREATE INDEX IF NOT EXISTS idx_messages_event_id_chat_position ON messages(event_id, chat_position);
CREATE INDEX IF NOT EXISTS idx_messages_sender_id ON messages(sender_id);
CREATE INDEX IF NOT EXISTS idx_messages_timestamp ON messages("timestamp");

-- Indexes for event_participants join table
CREATE INDEX IF NOT EXISTS idx_ep_user_id ON event_participants(user_id);

-- Indexes for zone_photos join table
CREATE INDEX IF NOT EXISTS idx_zp_photo_id ON zone_photos(photo_id);