-- Problem Sources (정답 코드, 생성기)
CREATE TABLE problem_sources (
    problem_id BIGINT PRIMARY KEY REFERENCES problems(id) ON DELETE CASCADE,
    solution_code TEXT NOT NULL,
    solution_language VARCHAR(50) NOT NULL,
    generator_code TEXT,
    generator_language VARCHAR(50)
);

-- Problem Reviews (검수)
CREATE TABLE problem_reviews (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    problem_id BIGINT NOT NULL REFERENCES problems(id) ON DELETE CASCADE,
    requester_id BIGINT NOT NULL REFERENCES users(id),
    reviewer_id BIGINT REFERENCES users(id),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    summary TEXT,
    reviewed_at TIMESTAMP
);

-- Review Comments (검수 코멘트)
CREATE TABLE review_comments (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    review_id BIGINT NOT NULL REFERENCES problem_reviews(id) ON DELETE CASCADE,
    author_id BIGINT NOT NULL REFERENCES users(id),
    content TEXT NOT NULL
);

-- Indexes
CREATE INDEX idx_problem_reviews_problem_id ON problem_reviews(problem_id);
CREATE INDEX idx_problem_reviews_status ON problem_reviews(status);
CREATE INDEX idx_review_comments_review_id ON review_comments(review_id);
