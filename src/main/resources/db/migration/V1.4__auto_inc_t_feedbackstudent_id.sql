-- WARNING: DO NOT MODIFY THIS FILE

-- Create a sequence if it does not exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_class WHERE relkind = 'S' AND relname = 'feedbackid_seq') THEN
        CREATE SEQUENCE nerie.feedbackid_seq START 1;
    END IF;
END $$;

-- Update the feedbackid column to use the sequence
ALTER TABLE nerie.t_feedback_student ALTER COLUMN feedbackid SET DEFAULT nextval('nerie.feedbackid_seq');

-- Create a trigger to automatically set the feedbackid before insert
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.triggers
        WHERE event_object_table = 't_feedback_student'
        AND trigger_name = 'set_feedbackid_trigger'
    ) THEN
        CREATE OR REPLACE FUNCTION set_feedbackid() RETURNS TRIGGER AS $func$
        BEGIN
            NEW.feedbackid := nextval('nerie.feedbackid_seq')::text;
            RETURN NEW;
        END;
        $func$ LANGUAGE plpgsql;

        CREATE TRIGGER set_feedbackid_trigger
        BEFORE INSERT ON nerie.t_feedback_student
        FOR EACH ROW
        EXECUTE FUNCTION set_feedbackid();
    END IF;
END $$;