CREATE TABLE bookshelf_books
(
    added_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    bookshelf_id UUID NOT NULL,
    book_id UUID NOT NULL,
    CONSTRAINT pk_bookshelf_books PRIMARY KEY (bookshelf_id, book_id)
);

CREATE TABLE bookshelves
(
    id UUID NOT NULL,
    version     BIGINT,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    user_id UUID NOT NULL,
    CONSTRAINT pk_bookshelves PRIMARY KEY (id)
);

ALTER TABLE bookshelf_books
    ADD CONSTRAINT FK_BOOKSHELF_BOOKS_ON_BOOK FOREIGN KEY (book_id) REFERENCES books (id);

ALTER TABLE bookshelf_books
    ADD CONSTRAINT FK_BOOKSHELF_BOOKS_ON_BOOKSHELF FOREIGN KEY (bookshelf_id) REFERENCES bookshelves (id);

ALTER TABLE bookshelves
    ADD CONSTRAINT FK_BOOKSHELVES_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);