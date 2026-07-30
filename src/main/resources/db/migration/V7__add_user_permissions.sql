CREATE TABLE user_permissions
(
    user_id UUID NOT NULL,
    permission VARCHAR(255) NOT NULL,

    CONSTRAINT pk_user_permissions PRIMARY KEY (user_id, permission),

    CONSTRAINT fk_user_permissions_on_application_user
        FOREIGN KEY (user_id) REFERENCES application_users (id)
);