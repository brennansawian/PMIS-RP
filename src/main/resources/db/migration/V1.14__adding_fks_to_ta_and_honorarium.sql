ALTER TABLE nerie.m_taform DROP COLUMN program;
ALTER TABLE nerie.m_honorarium DROP COLUMN programtitle;

ALTER TABLE nerie.m_taform
ADD COLUMN phaseid VARCHAR(3),
ADD CONSTRAINT fk_m_taform_phaseid
    FOREIGN KEY (phaseid)
    REFERENCES nerie.m_phases (phaseid)
    ON UPDATE NO ACTION
    ON DELETE NO ACTION;


ALTER TABLE nerie.m_honorarium
ADD COLUMN phaseid VARCHAR(3),
ADD CONSTRAINT fk_m_honorarium_phaseid
    FOREIGN KEY (phaseid)
    REFERENCES nerie.m_phases (phaseid)
    ON UPDATE NO ACTION
    ON DELETE NO ACTION;