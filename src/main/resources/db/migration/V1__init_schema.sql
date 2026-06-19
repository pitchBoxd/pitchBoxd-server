-- 초기 스키마 생성 (JPA 엔티티 기반)

CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nickname VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    favorite_team_id BIGINT,
    provider VARCHAR(255),
    provider_key VARCHAR(255),
    role VARCHAR(255) NOT NULL DEFAULT 'USER',
    created_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_users_nickname UNIQUE (nickname),
    CONSTRAINT uk_users_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE refresh_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    token_value VARCHAR(255) NOT NULL,
    user_id BIGINT NOT NULL,
    issued_at DATETIME(6) NOT NULL,
    expired_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_refresh_tokens_user_id FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE seasons (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255),
    created_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE teams (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    naver_id VARCHAR(255) NOT NULL,
    stadium VARCHAR(255),
    created_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE players (
    id BIGINT NOT NULL AUTO_INCREMENT,
    team_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    naver_id VARCHAR(255) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE matches (
    id BIGINT NOT NULL AUTO_INCREMENT,
    season_id BIGINT NOT NULL,
    round VARCHAR(255) NOT NULL,
    home_team_id BIGINT NOT NULL,
    away_team_id BIGINT NOT NULL,
    start_time DATETIME(6) NOT NULL,
    finished_at DATETIME(6),
    status VARCHAR(255) NOT NULL,
    location VARCHAR(255) NOT NULL,
    home_score INT,
    away_score INT,
    home_scorers JSON,
    away_scorers JSON,
    naver_id VARCHAR(255) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_matches_naver_id UNIQUE (naver_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE match_lineups (
    id BIGINT NOT NULL AUTO_INCREMENT,
    match_id BIGINT NOT NULL,
    player_id BIGINT NOT NULL,
    back_number INT,
    status VARCHAR(255),
    created_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE match_reviews (
    id BIGINT NOT NULL AUTO_INCREMENT,
    match_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    point INT NOT NULL,
    fan_type VARCHAR(255),
    content VARCHAR(255),
    like_count BIGINT,
    updated_at DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE match_review_likes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    match_review_id BIGINT,
    user_id BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT uk_match_review_like_user_review UNIQUE (match_review_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE match_statistics (
    match_id BIGINT NOT NULL,
    total_rating_sum BIGINT NOT NULL,
    total_review_count INT NOT NULL,
    home_fan_rating_sum BIGINT NOT NULL,
    home_fan_review_count INT NOT NULL,
    away_fan_rating_sum BIGINT NOT NULL,
    away_fan_review_count INT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6),
    PRIMARY KEY (match_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE player_reviews (
    id BIGINT NOT NULL AUTO_INCREMENT,
    match_id BIGINT NOT NULL,
    player_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    point INT NOT NULL,
    content VARCHAR(255),
    like_count BIGINT,
    fan_type VARCHAR(255),
    updated_at DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE player_review_likes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    player_review_id BIGINT,
    user_id BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT uk_player_review_like_user_review UNIQUE (player_review_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE player_statistics (
    id BIGINT NOT NULL AUTO_INCREMENT,
    player_id BIGINT,
    match_id BIGINT,
    total_score BIGINT,
    review_count BIGINT,
    created_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
