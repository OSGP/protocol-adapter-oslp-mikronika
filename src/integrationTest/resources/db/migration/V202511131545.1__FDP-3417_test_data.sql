INSERT INTO oslp_mikronika_device (creation_time, modification_time, version, device_identification, device_uid,
                                   sequence_number, random_device, random_platform, public_key)
VALUES (now(), now(), 0, 'device_001', 'MIK--UID-001', 1, 12345, 67890, 'public_key_001'),
       (now(), now(), 0, 'device_002', 'MIK--UID-002', 2, 22345, 77890, 'public_key_002'),
       (now(), now(), 0, 'device_003', 'MIK--UID-003', 3, 32345, 87890, 'public_key_003');
