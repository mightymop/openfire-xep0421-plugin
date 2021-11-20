CREATE TABLE ofMucPrivateKeys (
  _roomjid              VARCHAR(255)    NOT NULL,
  _key                  VARCHAR(128)    NOT NULL,
  CONSTRAINT ofMUCPrivateKeys_pk PRIMARY KEY (_roomjid)
);

INSERT INTO ofVersion (name, version) VALUES ('xep0241', 1);