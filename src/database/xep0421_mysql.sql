CREATE TABLE IF NOT EXISTS ofMucPrivateKeys (
    roomjid              VARCHAR(255)    NOT NULL,
    id                  VARCHAR(128)    NOT NULL,
    PRIMARY KEY (_roomjid)
);

INSERT INTO ofVersion (name, version)
VALUES ('xep0421', 1)
ON DUPLICATE KEY UPDATE version = 1;