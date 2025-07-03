EXEC sp_executesql N'
IF NOT EXISTS (
    SELECT * FROM INFORMATION_SCHEMA.TABLES
    WHERE TABLE_NAME = ''ofMucPrivateKeys''
)
BEGIN
    CREATE TABLE dbo.ofMucPrivateKeys (
        _roomjid NVARCHAR(255) PRIMARY KEY NOT NULL,
        _key NVARCHAR(128) NOT NULL
    )
END';

UPDATE ofVersion SET version = 1 WHERE name = 'xep0421';
INSERT INTO ofVersion (name, version)
SELECT 'xep0421', 1
WHERE NOT EXISTS (SELECT 1 FROM ofVersion WHERE name = 'xep0421');