CREATE TABLE refresh_tokens (
                                id BIGINT NOT NULL AUTO_INCREMENT,
                                token_hash VARCHAR(64) NOT NULL,
                                user_id BIGINT NOT NULL,
                                created_at TIMESTAMP(6) NOT NULL,
                                expires_at TIMESTAMP(6) NOT NULL,
                                revoked BOOLEAN NOT NULL DEFAULT FALSE,
                                PRIMARY KEY (id),
                                CONSTRAINT uk_refresh_tokens_token_hash UNIQUE (token_hash),
                                CONSTRAINT fk_refresh_tokens_user
                                    FOREIGN KEY (user_id) REFERENCES app_users(id)
);