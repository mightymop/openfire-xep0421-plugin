CREATE TABLE IF NOT EXISTS ofMucPrivateKeys (
    _roomjid              VARCHAR(255) NOT NULL PRIMARY KEY,
    _key                  VARCHAR(128) NOT NULL
);

MERGE INTO ofVersion (name, version)
KEY (name)
VALUES ('xep0421', 1);