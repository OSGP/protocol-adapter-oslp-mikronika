CREATE DATABASE osgp_core;

CREATE USER osgp_core_db_api_user WITH PASSWORD 'test';

GRANT CONNECT ON DATABASE osgp_core TO osgp_core_db_api_user;

CREATE TABLE device (
                        id BIGSERIAL PRIMARY KEY,
                        device_identification VARCHAR(255) NOT NULL,
                        gps_latitude FLOAT NOT NULL,
                        gps_longitude FLOAT NOT NULL
);
