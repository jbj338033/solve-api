-- Add number column to problems table
ALTER TABLE problems ADD COLUMN number INT;

-- Assign numbers to existing problems based on creation order
WITH numbered AS (
    SELECT id, ROW_NUMBER() OVER (ORDER BY created_at) as rn
    FROM problems
)
UPDATE problems p
SET number = n.rn
FROM numbered n
WHERE p.id = n.id;

-- Make number NOT NULL and UNIQUE after populating
ALTER TABLE problems ALTER COLUMN number SET NOT NULL;
CREATE UNIQUE INDEX idx_problems_number ON problems(number);
