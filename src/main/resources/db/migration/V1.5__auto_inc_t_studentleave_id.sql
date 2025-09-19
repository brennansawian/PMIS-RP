-- WARNING: DO NOT MODIFY THIS FILE

-- Create a sequence if it does not exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_class WHERE relkind = 'S' AND relname = 'studentleaveid_seq') THEN
        CREATE SEQUENCE nerie.studentleaveid_seq START 1;
    END IF;
END $$;

-- Update the studentleaveid column to use the sequence
ALTER TABLE nerie.t_studentleave ALTER COLUMN studentleaveid SET DEFAULT nextval('nerie.studentleaveid_seq');

-- Create a trigger to automatically set the studentleaveid before insert
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.triggers
        WHERE event_object_table = 't_studentleave'
        AND trigger_name = 'set_studentleaveid_trigger'
    ) THEN
        CREATE OR REPLACE FUNCTION set_studentleaveid() RETURNS TRIGGER AS $func$
        BEGIN
            NEW.studentleaveid := nextval('nerie.studentleaveid_seq')::text;
            RETURN NEW;
        END;
        $func$ LANGUAGE plpgsql;

        CREATE TRIGGER set_studentleaveid_trigger
        BEFORE INSERT ON nerie.t_studentleave
        FOR EACH ROW
        EXECUTE FUNCTION set_studentleaveid();
    END IF;
END $$;
