-- Step 1: Add the new nullable columns for the generic association
ALTER TABLE reviews ADD COLUMN IF NOT EXISTS target_id UUID;
ALTER TABLE reviews ADD COLUMN IF NOT EXISTS target_type VARCHAR(50);

-- Step 2: Migrate data from legacy relational columns (Required for Production)
UPDATE reviews SET target_id = book_id, target_type = 'BOOK' WHERE book_id IS NOT NULL;

-- Step 3: Enforce the new association constraints
ALTER TABLE reviews ALTER COLUMN target_id SET NOT NULL;
ALTER TABLE reviews ALTER COLUMN target_type SET NOT NULL;

-- Step 4: Safely convert the OID binary pointers to raw TEXT (Required for Production)
ALTER TABLE reviews ALTER COLUMN comment TYPE TEXT USING convert_from(lo_get(comment::oid), 'UTF8');

-- Step 5: Drop the legacy constraints and columns
ALTER TABLE reviews DROP CONSTRAINT IF EXISTS fk_reviews_on_book;
ALTER TABLE reviews DROP COLUMN IF EXISTS book_id;
-- Do not drop fk_reviews_on_user unless ApplicationUser was also fully decoupled in the database.

-- Step 6: Enforce the new generic unique constraint
ALTER TABLE reviews DROP CONSTRAINT IF EXISTS uk_reviews_user_target;
ALTER TABLE reviews ADD CONSTRAINT uk_reviews_user_target UNIQUE (user_id, target_id, target_type);