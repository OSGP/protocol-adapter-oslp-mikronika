CREATE DATABASE osgp_core;
\connect osgp_core

CREATE USER osgp_core_db_api_user WITH PASSWORD 'test';

GRANT CONNECT ON DATABASE osgp_core TO osgp_core_db_api_user;

CREATE TABLE device
(
    id                    BIGSERIAL PRIMARY KEY,
    device_identification VARCHAR(255) NOT NULL,
    gps_latitude          FLOAT        NOT NULL,
    gps_longitude         FLOAT        NOT NULL
);

GRANT SELECT ON TABLE device TO osgp_core_db_api_user;

INSERT INTO device (device_identification, gps_latitude, gps_longitude)
SELECT 'MIK' || '-' || gen_id
     , 51.985007
     , 5.894768
FROM generate_series(1, 10) gen_id;
