-- WARNING: DO NOT MODIFY THIS FILE

DO $$
BEGIN
    -- Check if table exists before creating
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'nerie'
        AND table_name = 'mt_userloginrole'
    ) THEN
        INSERT INTO nerie.mt_userloginrole(role_id, role_name, role_code)
        SELECT 7, 'RESOURCE_PERSON', 'R'
        WHERE NOT EXISTS (SELECT 1 FROM nerie.mt_userloginrole WHERE role_id = 7);
    END IF;
END $$;