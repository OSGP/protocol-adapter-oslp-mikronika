CREATE TABLE IF NOT EXISTS device (
    id SERIAL PRIMARY KEY,
    device_identification VARCHAR(40) UNIQUE NOT NULL,
    gps_latitude VARCHAR(15),
    gps_longitude VARCHAR(15)
);

INSERT INTO device (device_identification, gps_latitude, gps_longitude) VALUES
('device_001', '52.3676', '4.9041'),    -- Amsterdam
('device_002', '53.2012', '5.7999'),    -- Leeuwarden
('device_003', '51.9851', '5.8987');    -- Arnhem
