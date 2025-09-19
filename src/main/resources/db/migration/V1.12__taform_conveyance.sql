CREATE TABLE m_taform (
    id BIGSERIAL PRIMARY KEY,

    program VARCHAR(255),
    venue VARCHAR(255),
    fromdate DATE,
    todate DATE,

    namerecord VARCHAR(255),
    designation VARCHAR(255),
    basicpay DOUBLE PRECISION,
    address TEXT,
    city VARCHAR(100),
    pincode VARCHAR(20),

    resaddress TEXT,
    rcity VARCHAR(100),
    rpincode VARCHAR(20),

    accountnumber VARCHAR(50),
    bankname VARCHAR(100),
    branch VARCHAR(100),
    ifsc VARCHAR(20),
    pancardnumber VARCHAR(20),

    rp_userlogin_id VARCHAR(8) NOT NULL,

    CONSTRAINT fk_rp_userlogin
        FOREIGN KEY (rp_userlogin_id)
        REFERENCES mt_userlogin(usercode)
        ON DELETE CASCADE
);


CREATE TABLE t_conveyancecharge (
    id BIGSERIAL PRIMARY KEY,
    taform_id BIGINT NOT NULL,
    date DATE,
    placeofdeparture VARCHAR(255),
    placeofarrival VARCHAR(255),
    kms DOUBLE PRECISION,
    modeofconveyance VARCHAR(255),
    amount DOUBLE PRECISION,

    CONSTRAINT fk_taform
      FOREIGN KEY (taform_id)
      REFERENCES m_taform(id)
      ON DELETE CASCADE
); 