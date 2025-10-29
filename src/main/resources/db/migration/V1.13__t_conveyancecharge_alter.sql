ALTER TABLE nerie.t_conveyancecharge
ADD COLUMN dateofdeparture DATE,
ADD COLUMN dateofarrival DATE,
ADD COLUMN timeofdeparture VARCHAR(50),
ADD COLUMN timeofarrival VARCHAR(50),
ADD COLUMN detailsoftravel TEXT,
ADD COLUMN nonlocalpartno VARCHAR(50);
ALTER TABLE nerie.m_taform
ADD COLUMN islocal BOOLEAN;