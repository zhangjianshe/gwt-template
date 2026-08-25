-- Core Domains/Zones Table
CREATE TABLE domains (
                         id                    SERIAL PRIMARY KEY,
                         name                  VARCHAR(255) NOT NULL,
                         master                VARCHAR(128) DEFAULT NULL,
                         last_check            INT DEFAULT NULL,
                         type                  VARCHAR(8) NOT NULL,
                         notified_serial       INT DEFAULT NULL,
                         account               VARCHAR(40) DEFAULT NULL,
                         options               VARCHAR(64000) DEFAULT NULL,
                         catalog               VARCHAR(255) DEFAULT NULL,
                         CONSTRAINT c_lowercase_name CHECK (((name)::text = lower((name)::text)))
);

CREATE UNIQUE INDEX name_index ON domains(name);

-- Core Records Table
CREATE TABLE records (
                         id                    SERIAL PRIMARY KEY,
                         domain_id             INT REFERENCES domains(id) ON DELETE CASCADE,
                         name                  VARCHAR(255) DEFAULT NULL,
                         type                  VARCHAR(10) DEFAULT NULL,
                         content               VARCHAR(65535) DEFAULT NULL,
                         ttl                   INT DEFAULT NULL,
                         prio                  INT DEFAULT NULL,
                         disabled              BOOL DEFAULT FALSE,
                         ordername             VARCHAR(255),
                         auth                  BOOL DEFAULT TRUE,
                         CONSTRAINT c_lowercase_name CHECK (((name)::text = lower((name)::text)))
);

CREATE INDEX rec_name_index ON records(name);
CREATE INDEX nametype_index ON records(name,type);
CREATE INDEX domain_id ON records(domain_id);
CREATE INDEX ordername ON records(ordername);

-- Supermasters (For automatic provisioning via primary/secondary setups)
CREATE TABLE supermasters (
                              ip                    VARCHAR(64) NOT NULL,
                              nameserver            VARCHAR(255) NOT NULL,
                              account               VARCHAR(40) NOT NULL,
                              PRIMARY KEY(ip, nameserver)
);

-- Comments on individual records
CREATE TABLE comments (
                          id                    SERIAL PRIMARY KEY,
                          domain_id             INT REFERENCES domains(id) ON DELETE CASCADE,
                          name                  VARCHAR(255) NOT NULL,
                          type                  VARCHAR(10) NOT NULL,
                          modified_at           INT NOT NULL,
                          account               VARCHAR(40) DEFAULT NULL,
                          comment               VARCHAR(65535) NOT NULL,
                          CONSTRAINT c_lowercase_name CHECK (((name)::text = lower((name)::text)))
);

CREATE INDEX comments_domain_id_idx ON comments(domain_id);
CREATE INDEX comments_name_type_idx ON comments(name, type);
CREATE INDEX comments_order_idx ON comments(domain_id, modified_at);

-- Domain Metadata (For configuring specific DNS options per zone)
CREATE TABLE domainmetadata (
                                id                    SERIAL PRIMARY KEY,
                                domain_id             INT REFERENCES domains(id) ON DELETE CASCADE,
                                kind                  VARCHAR(32),
                                content               TEXT
);

CREATE INDEX domainidmetaindex ON domainmetadata(domain_id);

-- Cryptokeys (Used for DNSSEC signing keys)
CREATE TABLE cryptokeys (
                            id                    SERIAL PRIMARY KEY,
                            domain_id             INT REFERENCES domains(id) ON DELETE CASCADE,
                            flags                 INT NOT NULL,
                            active                BOOL,
                            published             BOOL DEFAULT TRUE,
                            content               TEXT
);

CREATE INDEX domainidindex ON cryptokeys(domain_id);

-- TSIG Keys (For transactional security / signed AXFR)
CREATE TABLE tsigkeys (
                          id                    SERIAL PRIMARY KEY,
                          name                  VARCHAR(255),
                          algorithm             VARCHAR(50),
                          secret                VARCHAR(255),
                          CONSTRAINT c_lowercase_name CHECK (((name)::text = lower((name)::text)))
);

CREATE UNIQUE INDEX namealgoindex ON tsigkeys(name, algorithm);