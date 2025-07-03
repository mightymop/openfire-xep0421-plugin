CREATE TABLE IF NOT EXISTS ofMucPrivateKeys (
    roomjid              VARCHAR(255) NOT NULL PRIMARY KEY,
    id                  VARCHAR(128) NOT NULL
);

MERGE INTO ofVersion (name, version)
KEY (name)
VALUES ('xep0421', 1);