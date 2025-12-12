-- Banners
CREATE TABLE banners (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500) NOT NULL,
    image_url VARCHAR(500) NOT NULL
);

-- Users
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    username VARCHAR(255) NOT NULL UNIQUE,
    display_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    profile_image VARCHAR(255) NOT NULL,
    bio TEXT NOT NULL,
    organization VARCHAR(255) NOT NULL,
    problem_rating INT NOT NULL,
    contest_rating INT NOT NULL,
    current_streak INT NOT NULL DEFAULT 0,
    max_streak INT NOT NULL DEFAULT 0,
    last_solved_date DATE,
    selected_banner_id BIGINT REFERENCES banners(id),
    role VARCHAR(50) NOT NULL
);

CREATE TABLE user_settings (
    user_id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    country VARCHAR(100),
    country_visible BOOLEAN NOT NULL DEFAULT true,
    birth_date DATE,
    birth_date_visible BOOLEAN NOT NULL DEFAULT false,
    gender VARCHAR(20),
    gender_other VARCHAR(100),
    gender_visible BOOLEAN NOT NULL DEFAULT true,
    pronouns VARCHAR(50),
    pronouns_visible BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE user_banners (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    user_id BIGINT NOT NULL REFERENCES users(id),
    banner_id BIGINT NOT NULL REFERENCES banners(id),
    acquired_at TIMESTAMP NOT NULL,
    UNIQUE(user_id, banner_id)
);

CREATE TABLE user_oauths (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    user_id BIGINT NOT NULL REFERENCES users(id),
    provider VARCHAR(50) NOT NULL,
    provider_id VARCHAR(255) NOT NULL,
    UNIQUE(provider, provider_id)
);

CREATE TABLE user_tier_histories (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    user_id BIGINT NOT NULL REFERENCES users(id),
    old_tier VARCHAR(50) NOT NULL,
    new_tier VARCHAR(50) NOT NULL,
    rating INT NOT NULL,
    achieved_at TIMESTAMP NOT NULL
);

CREATE TABLE user_rating_histories (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    user_id BIGINT NOT NULL REFERENCES users(id),
    contest_id BIGINT,
    rating INT NOT NULL,
    rating_type VARCHAR(50) NOT NULL,
    recorded_at TIMESTAMP NOT NULL
);

CREATE TABLE user_activities (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    user_id BIGINT NOT NULL REFERENCES users(id),
    date DATE NOT NULL,
    solved_count INT NOT NULL,
    submission_count INT NOT NULL,
    UNIQUE(user_id, date)
);

-- Tags
CREATE TABLE tags (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    name VARCHAR(255) NOT NULL UNIQUE
);

-- Problems
CREATE TABLE problems (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    input_format TEXT NOT NULL,
    output_format TEXT NOT NULL,
    difficulty VARCHAR(50) NOT NULL,
    time_limit INT NOT NULL,
    memory_limit INT NOT NULL,
    author_id BIGINT NOT NULL REFERENCES users(id),
    is_public BOOLEAN NOT NULL,
    type VARCHAR(50) NOT NULL,
    checker_code TEXT,
    checker_language VARCHAR(50),
    interactor_code TEXT,
    interactor_language VARCHAR(50)
);

CREATE TABLE problem_examples (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    problem_id BIGINT NOT NULL REFERENCES problems(id),
    input TEXT NOT NULL,
    output TEXT NOT NULL,
    "order" INT NOT NULL
);

CREATE TABLE problem_subtasks (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    problem_id BIGINT NOT NULL REFERENCES problems(id),
    score INT NOT NULL,
    "order" INT NOT NULL,
    description TEXT
);

CREATE TABLE problem_test_cases (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    problem_id BIGINT NOT NULL REFERENCES problems(id),
    input TEXT NOT NULL,
    output TEXT NOT NULL,
    "order" INT NOT NULL,
    subtask_id BIGINT REFERENCES problem_subtasks(id)
);

CREATE TABLE problem_tags (
    problem_id BIGINT NOT NULL REFERENCES problems(id),
    tag_id BIGINT NOT NULL REFERENCES tags(id),
    PRIMARY KEY(problem_id, tag_id)
);

CREATE TABLE problem_stats (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    problem_id BIGINT NOT NULL UNIQUE REFERENCES problems(id),
    submission_count INT NOT NULL,
    accepted_count INT NOT NULL,
    user_count INT NOT NULL,
    accepted_user_count INT NOT NULL
);

-- Contests
CREATE TABLE contests (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    host_id BIGINT NOT NULL REFERENCES users(id),
    start_at TIMESTAMP NOT NULL,
    end_at TIMESTAMP NOT NULL,
    type VARCHAR(50) NOT NULL,
    invite_code VARCHAR(255),
    scoring_type VARCHAR(50) NOT NULL,
    scoreboard_type VARCHAR(50) NOT NULL,
    freeze_minutes INT,
    is_rated BOOLEAN NOT NULL
);

CREATE TABLE contest_problems (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    contest_id BIGINT NOT NULL REFERENCES contests(id),
    problem_id BIGINT NOT NULL REFERENCES problems(id),
    "order" INT NOT NULL,
    score INT,
    UNIQUE(contest_id, problem_id)
);

CREATE TABLE contest_participants (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    contest_id BIGINT NOT NULL REFERENCES contests(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    total_score INT NOT NULL,
    penalty BIGINT NOT NULL,
    "rank" INT,
    rating_change INT,
    joined_at TIMESTAMP NOT NULL,
    UNIQUE(contest_id, user_id)
);

CREATE TABLE contest_results (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    contest_id BIGINT NOT NULL REFERENCES contests(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    problem_id BIGINT NOT NULL REFERENCES problems(id),
    score INT NOT NULL,
    attempts INT NOT NULL,
    solved_at TIMESTAMP,
    UNIQUE(contest_id, user_id, problem_id)
);

-- Submissions
CREATE TABLE submissions (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    problem_id BIGINT NOT NULL REFERENCES problems(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    contest_id BIGINT REFERENCES contests(id),
    language VARCHAR(50) NOT NULL,
    code TEXT NOT NULL,
    status VARCHAR(50) NOT NULL,
    result VARCHAR(50),
    score INT,
    time_used INT,
    memory_used INT,
    error TEXT,
    judged_at TIMESTAMP
);

CREATE TABLE submission_results (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    submission_id BIGINT NOT NULL REFERENCES submissions(id),
    testcase_id BIGINT NOT NULL REFERENCES problem_test_cases(id),
    result VARCHAR(50) NOT NULL,
    time_used INT,
    memory_used INT
);

-- Workbooks
CREATE TABLE workbooks (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    author_id BIGINT NOT NULL REFERENCES users(id)
);

CREATE TABLE workbook_problems (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    workbook_id BIGINT NOT NULL REFERENCES workbooks(id),
    problem_id BIGINT NOT NULL REFERENCES problems(id),
    "order" INT NOT NULL,
    UNIQUE(workbook_id, problem_id)
);

-- Indexes
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_user_oauths_user_id ON user_oauths(user_id);
CREATE INDEX idx_user_tier_histories_user_id ON user_tier_histories(user_id);
CREATE INDEX idx_user_rating_histories_user_id ON user_rating_histories(user_id);
CREATE INDEX idx_user_activities_user_id ON user_activities(user_id);
CREATE INDEX idx_problems_author_id ON problems(author_id);
CREATE INDEX idx_problem_examples_problem_id ON problem_examples(problem_id);
CREATE INDEX idx_problem_subtasks_problem_id ON problem_subtasks(problem_id);
CREATE INDEX idx_problem_test_cases_problem_id ON problem_test_cases(problem_id);
CREATE INDEX idx_submissions_user_id ON submissions(user_id);
CREATE INDEX idx_submissions_problem_id ON submissions(problem_id);
CREATE INDEX idx_submissions_contest_id ON submissions(contest_id);
CREATE INDEX idx_submission_results_submission_id ON submission_results(submission_id);
CREATE INDEX idx_contests_host_id ON contests(host_id);
CREATE INDEX idx_contest_problems_contest_id ON contest_problems(contest_id);
CREATE INDEX idx_contest_participants_contest_id ON contest_participants(contest_id);
CREATE INDEX idx_contest_participants_user_id ON contest_participants(user_id);
CREATE INDEX idx_contest_results_contest_id ON contest_results(contest_id);
CREATE INDEX idx_workbooks_author_id ON workbooks(author_id);
CREATE INDEX idx_workbook_problems_workbook_id ON workbook_problems(workbook_id);
CREATE INDEX idx_user_banners_user_id ON user_banners(user_id);
