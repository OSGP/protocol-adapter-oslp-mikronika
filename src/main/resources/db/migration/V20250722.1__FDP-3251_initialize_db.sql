CREATE TABLE oslp_mikronika_device
(
    id                    bigint                NOT NULL,
    creation_time         timestamp without time zone NOT NULL,
    modification_time     timestamp without time zone NOT NULL,
    version               bigint,
    device_identification character varying(40) NOT NULL,
    device_type           character varying(255),
    device_uid            character varying(255),
    sequence_number       integer,
    random_device         integer,
    random_platform       integer,
    public_key            character varying(255)
);

ALTER TABLE public.oslp_mikronika_device OWNER TO osp_admin;


CREATE SEQUENCE oslp_mikronika_device_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;

ALTER TABLE public.oslp_mikronika_device_id_seq OWNER TO osp_admin;

ALTER SEQUENCE oslp_mikronika_device_id_seq OWNED BY oslp_mikronika_device.id;

ALTER TABLE ONLY oslp_mikronika_device ALTER COLUMN id SET DEFAULT nextval('oslp_mikronika_device_id_seq'::regclass);


ALTER TABLE ONLY oslp_mikronika_device
    ADD CONSTRAINT oslp_mikronika_device_device_identification_key UNIQUE (device_identification);

ALTER TABLE ONLY oslp_mikronika_device
    ADD CONSTRAINT oslp_mikronika_device_device_uid_key UNIQUE (device_uid);

ALTER TABLE ONLY oslp_mikronika_device
    ADD CONSTRAINT oslp_mikronika_device_pkey PRIMARY KEY (id);


CREATE TABLE pending_set_schedule_request
(
    id                                  bigserial                NOT NULL,
    creation_time                       timestamp without time zone NOT NULL,
    modification_time                   timestamp without time zone NOT NULL,
    version                             bigint,
    device_identification               varchar(40)              NOT NULL,
    expired_at                          timestamp with time zone NOT NULL,
    schedule_message_data_container_dto bytea                    NOT NULL,
    device_request                      bytea                    NOT NULL,
    PRIMARY KEY (id)
);

ALTER TABLE public.pending_set_schedule_request
    OWNER TO osp_admin;
