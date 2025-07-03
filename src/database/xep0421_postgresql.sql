DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables 
                   WHERE table_name = 'ofMucPrivateKeys') THEN
        CREATE TABLE ofMucPrivateKeys (
            _roomjid VARCHAR(255) PRIMARY KEY,
            _key VARCHAR(128) NOT NULL
        );
    END IF;

    UPDATE ofVersion SET version = 1 WHERE name = 'xep0421';

    IF NOT FOUND THEN
        INSERT INTO ofVersion (name, version) VALUES ('xep0421', 1);
    END IF;
END$$;