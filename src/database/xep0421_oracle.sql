BEGIN
    EXECUTE IMMEDIATE '
    CREATE TABLE ofMucPrivateKeys (
        _roomjid VARCHAR2(255) PRIMARY KEY,
        _key VARCHAR2(128) NOT NULL
    )';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -955 THEN -- ORA-00955: name is already used by an existing object
            RAISE;
        END IF;
END;
/

MERGE INTO ofVersion v
USING (SELECT 'xep0421' AS name FROM dual) s
ON (v.name = s.name)
WHEN MATCHED THEN
    UPDATE SET v.version = 1
WHEN NOT MATCHED THEN
    INSERT (name, version) VALUES ('xep0421', 1);