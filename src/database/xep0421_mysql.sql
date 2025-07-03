CREATE TABLE IF NOT EXISTS ofMucPrivateKeys (
    _roomjid              VARCHAR(255)    NOT NULL,
    _key                  VARCHAR(128)    NOT NULL,
    PRIMARY KEY (__roomjid)
);

INSERT INTO ofVersion (name, version)
VALUES ('xep0421', 1)
ON DUPLICATE KEY UPDATE version = 1;