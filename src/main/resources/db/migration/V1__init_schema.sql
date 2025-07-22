CREATE TABLE oslp_device (
                             id bigint NOT NULL,
                             creation_time timestamp without time zone NOT NULL,
                             modification_time timestamp without time zone NOT NULL,
                             version bigint,
                             device_identification character varying(40) NOT NULL,
                             device_type character varying(255),
                             device_uid character varying(255),
                             sequence_number integer,
                             random_device integer,
                             random_platform integer,
                             public_key character varying(255)
);
