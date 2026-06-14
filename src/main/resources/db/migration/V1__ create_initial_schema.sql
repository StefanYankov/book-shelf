CREATE TABLE admin_users
(
    id UUID NOT NULL,
    CONSTRAINT pk_admin_users PRIMARY KEY (id)
);

CREATE TABLE application_users
(
    id UUID NOT NULL,
    is_active      BOOLEAN NOT NULL,
    email_verified BOOLEAN NOT NULL,
    CONSTRAINT pk_application_users PRIMARY KEY (id)
);

CREATE TABLE authors
(
    id UUID NOT NULL,
    version         BIGINT,
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    name            VARCHAR(100) NOT NULL,
    summary         VARCHAR(1000),
    image_url       VARCHAR(255),
    image_public_id VARCHAR(255),
    CONSTRAINT pk_authors PRIMARY KEY (id)
);

CREATE TABLE books
(
    id UUID NOT NULL,
    version         BIGINT,
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    title           VARCHAR(26000) NOT NULL,
    isbn            VARCHAR(255),
    author_id UUID,
    pages           INTEGER        NOT NULL,
    year_published  INTEGER        NOT NULL,
    language_id UUID NOT NULL,
    publisher_id UUID NOT NULL,
    format          VARCHAR(255)   NOT NULL,
    summary         VARCHAR(1000)  NOT NULL,
    image_url       VARCHAR(255),
    image_public_id VARCHAR(255),
    CONSTRAINT pk_books PRIMARY KEY (id)
);

CREATE TABLE books_genres
(
    book_id UUID NOT NULL,
    genre_id UUID NOT NULL,
    CONSTRAINT pk_books_genres PRIMARY KEY (book_id, genre_id)
);

CREATE TABLE genres
(
    id UUID NOT NULL,
    version     BIGINT,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    name        VARCHAR(50) NOT NULL,
    description VARCHAR(1000),
    CONSTRAINT pk_genres PRIMARY KEY (id)
);

CREATE TABLE languages
(
    id UUID NOT NULL,
    version    BIGINT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    name       VARCHAR(255) NOT NULL,
    CONSTRAINT pk_languages PRIMARY KEY (id)
);

CREATE TABLE publishers
(
    id UUID NOT NULL,
    version    BIGINT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    name       VARCHAR(100) NOT NULL,
    CONSTRAINT pk_publishers PRIMARY KEY (id)
);

CREATE TABLE reviews
(
    id UUID NOT NULL,
    version    BIGINT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    title      VARCHAR(100) NOT NULL,
    comment OID NOT NULL,
    rating     INTEGER      NOT NULL,
    user_id UUID NOT NULL,
    book_id UUID NOT NULL,
    CONSTRAINT pk_reviews PRIMARY KEY (id)
);

CREATE TABLE user_books
(
    id UUID NOT NULL,
    version     BIGINT,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    user_id UUID NOT NULL,
    book_id UUID NOT NULL,
    status      VARCHAR(255) NOT NULL,
    is_favorite BOOLEAN      NOT NULL,
    CONSTRAINT pk_user_books PRIMARY KEY (id)
);

CREATE TABLE users
(
    id UUID NOT NULL,
    version    BIGINT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    username   VARCHAR(50)  NOT NULL,
    email      VARCHAR(100),
    password   VARCHAR(255) NOT NULL,
    first_name VARCHAR(50)  NOT NULL,
    last_name  VARCHAR(50)  NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

ALTER TABLE user_books
    ADD CONSTRAINT uc_01225114c2731ec626b88e8bf UNIQUE (user_id, book_id);

ALTER TABLE genres
    ADD CONSTRAINT uc_genres_name UNIQUE (name);

ALTER TABLE languages
    ADD CONSTRAINT uc_languages_name UNIQUE (name);

ALTER TABLE publishers
    ADD CONSTRAINT uc_publishers_name UNIQUE (name);

ALTER TABLE users
    ADD CONSTRAINT uc_users_email UNIQUE (email);

ALTER TABLE users
    ADD CONSTRAINT uc_users_username UNIQUE (username);

ALTER TABLE admin_users
    ADD CONSTRAINT FK_ADMIN_USERS_ON_ID FOREIGN KEY (id) REFERENCES users (id);

ALTER TABLE application_users
    ADD CONSTRAINT FK_APPLICATION_USERS_ON_ID FOREIGN KEY (id) REFERENCES users (id);

ALTER TABLE books
    ADD CONSTRAINT FK_BOOKS_ON_AUTHOR FOREIGN KEY (author_id) REFERENCES authors (id);

ALTER TABLE books
    ADD CONSTRAINT FK_BOOKS_ON_LANGUAGE FOREIGN KEY (language_id) REFERENCES languages (id);

ALTER TABLE books
    ADD CONSTRAINT FK_BOOKS_ON_PUBLISHER FOREIGN KEY (publisher_id) REFERENCES publishers (id);

ALTER TABLE reviews
    ADD CONSTRAINT FK_REVIEWS_ON_BOOK FOREIGN KEY (book_id) REFERENCES books (id);

ALTER TABLE reviews
    ADD CONSTRAINT FK_REVIEWS_ON_USER FOREIGN KEY (user_id) REFERENCES application_users (id);

ALTER TABLE user_books
    ADD CONSTRAINT FK_USER_BOOKS_ON_BOOK FOREIGN KEY (book_id) REFERENCES books (id);

ALTER TABLE user_books
    ADD CONSTRAINT FK_USER_BOOKS_ON_USER FOREIGN KEY (user_id) REFERENCES application_users (id);

ALTER TABLE books_genres
    ADD CONSTRAINT fk_boogen_on_book FOREIGN KEY (book_id) REFERENCES books (id);

ALTER TABLE books_genres
    ADD CONSTRAINT fk_boogen_on_genre FOREIGN KEY (genre_id) REFERENCES genres (id);