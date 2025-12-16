-- Add number and status columns to problems table
ALTER TABLE problems ADD COLUMN number INT UNIQUE;
ALTER TABLE problems ADD COLUMN status VARCHAR(50) NOT NULL DEFAULT 'DRAFT';

-- Migrate existing public problems: set status to APPROVED and assign sequential numbers
UPDATE problems
SET status = 'APPROVED',
    number = sub.row_num
FROM (
    SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS row_num
    FROM problems
    WHERE is_public = true
) AS sub
WHERE problems.id = sub.id;

-- Create index for number lookups
CREATE INDEX idx_problems_number ON problems(number);
CREATE INDEX idx_problems_status ON problems(status);
