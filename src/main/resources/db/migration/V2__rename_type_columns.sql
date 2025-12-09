-- Rename contestType to type in contests table
ALTER TABLE contests RENAME COLUMN contest_type TO type;

-- Rename problemType to type in problems table
ALTER TABLE problems RENAME COLUMN problem_type TO type;
