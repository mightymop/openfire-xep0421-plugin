BEGIN
    DECLARE CONTINUE HANDLER FOR SQLSTATE '42710' BEGIN END; -- table already exists

    EXECUTE IMMEDIATE '
    CREATE TABLE ofMucPrivateKeys (
        _roomjid VARCHAR(255) NOT NULL PRIMARY KEY,
        _key VARCHAR(128) NOT NULL
    )';
END;
/

MERGE INTO ofVersion AS v
USING (SELECT 'xep0421' AS name FROM SYSIBM.SYSDUMMY1) AS src
ON v.name = src.name
WHEN MATCHED THEN
    UPDATE SET version = 1
WHEN NOT MATCHED THEN
    INSERT (name, version) VALUES ('xep0421', 1);