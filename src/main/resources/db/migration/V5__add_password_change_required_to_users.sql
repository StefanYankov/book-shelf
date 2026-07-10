ALTER TABLE users
    ADD password_change_required BOOLEAN;

ALTER TABLE users
    ALTER COLUMN password_change_required SET NOT NULL;