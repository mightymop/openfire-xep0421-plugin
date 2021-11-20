IF NOT EXISTS (select * from sysobjects where name='ofMucPrivateKeys' and xtype='U')
    CREATE TABLE ofMucPrivateKeys (
	  _roomjid              VARCHAR(255)    NOT NULL,
	  _key                  VARCHAR(128)    NOT NULL,
	  CONSTRAINT ofMUCPrivateKeys_pk PRIMARY KEY (_roomjid)
	);
GO

UPDATE ofVersion SET    version = 1 WHERE name = 'xep0241' 

IF @@ROWCOUNT = 0 
INSERT INTO ofVersion (name, version) VALUES ('xep0241', 1);
