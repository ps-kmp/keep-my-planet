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
CREATE INDEX IF NOT EXISTS idx_zones_is_active ON zones(is_active);

-- Indexes for events table
CREATE INDEX IF NOT EXISTS idx_events_zone_id ON events(zone_id);
CREATE INDEX IF NOT EXISTS idx_events_organizer_id ON events(organizer_id);
CREATE INDEX IF NOT EXISTS idx_events_status ON events(status);
CREATE INDEX IF NOT EXISTS idx_events_start_datetime ON events(start_datetime);
CREATE INDEX IF NOT EXISTS idx_events_title_trgm ON events USING GIN (title gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_events_status_start_datetime ON events(status, start_datetime);

CREATE INDEX IF NOT EXISTS idx_esc_event_id_change_time ON event_state_changes(event_id, change_time DESC);
CREATE INDEX IF NOT EXISTS idx_esc_changed_by ON event_state_changes(changed_by);

-- Indexes for zone_state_changes table
CREATE INDEX IF NOT EXISTS idx_zsc_zone_id_change_time ON zone_state_changes(zone_id, change_time DESC);
CREATE INDEX IF NOT EXISTS idx_zsc_changed_by ON zone_state_changes(changed_by);
CREATE INDEX IF NOT EXISTS idx_zsc_triggered_by_event_id ON zone_state_changes(triggered_by_event_id);

-- Indexes for messages table
CREATE INDEX IF NOT EXISTS idx_messages_event_id_chat_position ON messages(event_id, chat_position);
CREATE INDEX IF NOT EXISTS idx_messages_sender_id ON messages(sender_id);
CREATE INDEX IF NOT EXISTS idx_messages_timestamp ON messages("timestamp");

-- Indexes for event_participants join table
CREATE INDEX IF NOT EXISTS idx_ep_user_id ON event_participants(user_id);

-- Indexes for zone_photos join table
CREATE INDEX IF NOT EXISTS idx_zp_photo_id ON zone_photos(photo_id);

-- Indexes for user_devices table
CREATE INDEX IF NOT EXISTS idx_ud_user_id ON user_devices(user_id);