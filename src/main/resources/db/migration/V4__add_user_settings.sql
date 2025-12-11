CREATE TABLE user_settings (
    user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
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
