-- 1. BASE USERS TABLE
-- Passwords use the {noop}__SEED_DEFAULT__ sentinel; UserInitializationSeeder replaces it on boot
-- with a runtime-encoded hash, so credentials always match the active PasswordEncoder.
INSERT INTO users (id, version, username, email, password, first_name, last_name, created_at, updated_at, password_change_required)
VALUES ('11111111-0000-0000-0000-000000000001', 0, 'admin', 'admin@bookshelf.com', '{noop}__SEED_DEFAULT__', 'System',
        'Administrator', NOW(), NOW(), false),
       ('22222222-0000-0000-0000-000000000001', 0, 'user1', 'user1@bookshelf.com', '{noop}__SEED_DEFAULT__', 'Alice',
        'Reader', NOW(), NOW(), false),
       ('22222222-0000-0000-0000-000000000002', 0, 'user2', 'user2@bookshelf.com', '{noop}__SEED_DEFAULT__', 'Bob',
        'Bookworm', NOW(), NOW(), false),
       ('22222222-0000-0000-0000-000000000003', 0, 'user3', 'user3@bookshelf.com', '{noop}__SEED_DEFAULT__', 'Carol',
        'Critic', NOW(), NOW(), false),
       ('22222222-0000-0000-0000-000000000004', 0, 'user4', 'user4@bookshelf.com', '{noop}__SEED_DEFAULT__', 'Dave',
        'Editor', NOW(), NOW(), false),
       ('22222222-0000-0000-0000-000000000005', 0, 'user5', 'user5@bookshelf.com', '{noop}__SEED_DEFAULT__', 'Erin',
        'Newcomer', NOW(), NOW(), false)
ON CONFLICT (username) DO NOTHING;

-- 2. ROLE REALIZATION VIA JOINED TABLES
INSERT INTO admin_users (id)
VALUES ('11111111-0000-0000-0000-000000000001')
ON CONFLICT (id) DO NOTHING;

INSERT INTO application_users (id, email_verified)
VALUES
    ('22222222-0000-0000-0000-000000000001', true),
    ('22222222-0000-0000-0000-000000000002', true),
    ('22222222-0000-0000-0000-000000000003', true),
    ('22222222-0000-0000-0000-000000000004', true),
    ('22222222-0000-0000-0000-000000000005', true)
ON CONFLICT (id) DO NOTHING;

-- Delegated permissions (mapped to Spring Security authorities at authentication time).
-- user3 can moderate reviews; user4 can moderate books.
INSERT INTO user_permissions (user_id, permission)
VALUES ('22222222-0000-0000-0000-000000000003', 'MODERATE_REVIEWS'),
       ('22222222-0000-0000-0000-000000000004', 'MODERATE_BOOKS')
ON CONFLICT
    (user_id, permission)
    DO NOTHING;

-- Languages
INSERT INTO languages (id, version, name, created_at, updated_at) VALUES
                                                                      ('33333333-0000-0000-0000-000000000001', 0, 'English', NOW(), NOW()),
                                                                      ('33333333-0000-0000-0000-000000000002', 0, 'Spanish', NOW(), NOW()),
                                                                      ('33333333-0000-0000-0000-000000000003', 0, 'French', NOW(), NOW()),
                                                                      ('33333333-0000-0000-0000-000000000004', 0, 'German', NOW(), NOW()),
                                                                      ('33333333-0000-0000-0000-000000000005', 0, 'Japanese', NOW(), NOW()),
                                                                      ('33333333-0000-0000-0000-000000000006', 0, 'Russian', NOW(), NOW()),
                                                                      ('33333333-0000-0000-0000-000000000007', 0, 'Bulgarian', NOW(), NOW()),
                                                                      ('33333333-0000-0000-0000-000000000008', 0, 'Italian', NOW(), NOW()),
                                                                      ('33333333-0000-0000-0000-000000000009', 0, 'Portuguese', NOW(), NOW()),
                                                                      ('33333333-0000-0000-0000-000000000010', 0, 'Chinese', NOW(), NOW()),
                                                                      ('33333333-0000-0000-0000-000000000011', 0, 'Dutch', NOW(), NOW()),
                                                                      ('33333333-0000-0000-0000-000000000012', 0, 'Greek', NOW(), NOW()),
                                                                      ('33333333-0000-0000-0000-000000000013', 0, 'Turkish', NOW(), NOW()),
                                                                      ('33333333-0000-0000-0000-000000000014', 0, 'Polish', NOW(), NOW()),
                                                                      ('33333333-0000-0000-0000-000000000015', 0, 'Swedish', NOW(), NOW()),
                                                                      ('33333333-0000-0000-0000-000000000016', 0, 'Arabic', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Genres
INSERT INTO genres (id, version, name, description, created_at, updated_at) VALUES
                                                                                ('44444444-0000-0000-0000-000000000001', 0, 'Science Fiction', 'Imaginative and futuristic concepts, technology, space exploration, and time travel.', NOW(), NOW()),
                                                                                ('44444444-0000-0000-0000-000000000002', 0, 'Fantasy', 'Magical elements, mythical creatures, and wonderous fictional worlds.', NOW(), NOW()),
                                                                                ('44444444-0000-0000-0000-000000000003', 0, 'Mystery', 'Puzzles, crimes, and suspenseful investigations needing sorting out.', NOW(), NOW()),
                                                                                ('44444444-0000-0000-0000-000000000004', 0, 'Thriller', 'High stakes, tension, and rapid pacing designed to maximize excitement.', NOW(), NOW()),
                                                                                ('44444444-0000-0000-0000-000000000005', 0, 'Romance', 'Focuses on primary intimate interpersonal relationship dynamics.', NOW(), NOW()),
                                                                                ('44444444-0000-0000-0000-000000000006', 0, 'Historical Fiction', 'Narratives woven into verified real-world timeline settings.', NOW(), NOW()),
                                                                                ('44444444-0000-0000-0000-000000000007', 0, 'Non-Fiction', 'Informational texts sharing objective global facts, reports, and events.', NOW(), NOW()),
                                                                                ('44444444-0000-0000-0000-000000000008', 0, 'Biography', 'Detailed historical records of a specific actual human life track.', NOW(), NOW()),
                                                                                ('44444444-0000-0000-0000-000000000009', 0, 'Horror', 'Intended to scare, unnerve, startle, or invoke supernatural dread.', NOW(), NOW()),
                                                                                ('44444444-0000-0000-0000-000000000010', 0, 'Classic', 'Acclaimed historical works holding long-standing recognized literacy values.', NOW(), NOW()),
                                                                                ('44444444-0000-0000-0000-000000000011', 0, 'Dystopian', 'Explores society deconstructions, totalitarian regimes, and post-apocalypse environments.', NOW(), NOW()),
                                                                                ('44444444-0000-0000-0000-000000000012', 0, 'Poetry', 'Rhythmic prose writing chosen to elicit intense imaginative depth.', NOW(), NOW()),
                                                                                ('44444444-0000-0000-0000-000000000013', 0, 'Drama / Play', 'Dialogue-driven scripts composed directly for theatrical presentation.', NOW(), NOW()),
                                                                                ('44444444-0000-0000-0000-000000000014', 0, 'Self-Help', 'Instructional guides offering cognitive approaches to daily behavior metrics.', NOW(), NOW()),
                                                                                ('44444444-0000-0000-0000-000000000015', 0, 'Science & Technology', 'Empirical reporting across modern engineering, computation, and discoveries.', NOW(), NOW()),
                                                                                ('44444444-0000-0000-0000-000000000016', 0, 'Philosophy', 'Deep conceptual studies focusing on logic, existence, morals, and mind mechanics.', NOW(), NOW()),
                                                                                ('44444444-0000-0000-0000-000000000017', 0, 'Graphic Novel / Manga', 'Sequential illustrated panel artistry delivering continuous narratives.', NOW(), NOW()),
                                                                                ('44444444-0000-0000-0000-000000000018', 0, 'True Crime', 'Non-fiction examinations of documented real-world tracking profiles.', NOW(), NOW()),
                                                                                ('44444444-0000-0000-0000-000000000019', 0, 'Memoir / Autobiography', 'Personal real-life timeline recollections logged directly by the subject.', NOW(), NOW()),
                                                                                ('44444444-0000-0000-0000-000000000020', 0, 'Young Adult (YA)', 'Coming of age stories dealing with themes tailored for teen profiles.', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Authors
INSERT INTO authors (id, version, name, summary, image_url, image_public_id, created_at, updated_at) VALUES ('55555555-0000-0000-0000-000000000001',
                                                                                                             0,
                                                                                                             'Frank Herbert',
                                                                                                             'American science fiction author best known for the novel Dune.',
                                                                                                             'https://res.cloudinary.com/duntro9y8/image/upload/v1786048839/herbert_kifrw8.jpg',
                                                                                                             'herbert_kifrw8',
                                                                                                             NOW(),
                                                                                                             NOW()),
                                                                                                            ('55555555-0000-0000-0000-000000000002',
                                                                                                             0,
                                                                                                             'J.R.R. Tolkien',
                                                                                                             'English writer, poet, philologist, and academic, best known as the author of high fantasy works.',
                                                                                                             'https://res.cloudinary.com/duntro9y8/image/upload/v1786048928/tolkien_vvafhy.jpg',
                                                                                                             'tolkien_vvafhy',
                                                                                                             NOW(),
                                                                                                             NOW()),
                                                                                                            ('55555555-0000-0000-0000-000000000003',
                                                                                                             0,
                                                                                                             'Agatha Christie',
                                                                                                             'English writer known for her 66 detective novels and 14 short story collections.',
                                                                                                             'https://res.cloudinary.com/duntro9y8/image/upload/v1786049012/christie_amvlmo.jpg',
                                                                                                             'christie_amvlmo',
                                                                                                             NOW(),
                                                                                                             NOW()),
                                                                                                            ('55555555-0000-0000-0000-000000000004',
                                                                                                             0,
                                                                                                             'Stephen King',
                                                                                                             'American author of horror, supernatural fiction, suspense, crime, and fantasy novels.',
                                                                                                             'https://res.cloudinary.com/duntro9y8/image/upload/v1786049070/king_j2o8at.jpg',
                                                                                                             'king_j2o8at',
                                                                                                             NOW(),
                                                                                                             NOW()),
                                                                                                            ('55555555-0000-0000-0000-000000000005',
                                                                                                             0,
                                                                                                             'Isaac Asimov',
                                                                                                             'American science fiction writer and professor of biochemistry.',
                                                                                                             'https://res.cloudinary.com/duntro9y8/image/upload/v1786049170/asimov_a6ssvi.jpg',
                                                                                                             'asimov_a6ssvi',
                                                                                                             NOW(),
                                                                                                             NOW()),
                                                                                                            ('55555555-0000-0000-0000-000000000006',
                                                                                                             0,
                                                                                                             'Ivan Vazov',
                                                                                                             'Bulgarian poet, novelist and playwright, often referred to as the Patriarch of Bulgarian Literature.',
                                                                                                             'https://res.cloudinary.com/duntro9y8/image/upload/v1786049171/vazov_ajvzwn.jpg',
                                                                                                             'vazov_ajvzwn',
                                                                                                             NOW(),
                                                                                                             NOW())
ON CONFLICT (id) DO NOTHING;

-- Publishers
INSERT INTO publishers (id, version, name, created_at, updated_at) VALUES
                                                                       ('66666666-0000-0000-0000-000000000001', 0, 'Chilton Books', NOW(), NOW()),
                                                                       ('66666666-0000-0000-0000-000000000002', 0, 'George Allen & Unwin', NOW(), NOW()),
                                                                       ('66666666-0000-0000-0000-000000000003', 0, 'The Bodley Head', NOW(), NOW()),
                                                                       ('66666666-0000-0000-0000-000000000004', 0, 'Doubleday', NOW(), NOW()),
                                                                       ('66666666-0000-0000-0000-000000000005', 0, 'Gnome Press', NOW(), NOW()),
                                                                       ('66666666-0000-0000-0000-000000000006', 0, 'JiaHu Books', NOW(), NOW()),
                                                                       ('66666666-0000-0000-0000-000000000007', 0, 'Penguin Books', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Books
INSERT INTO books (id, version, title, isbn, pages, year_published, format, summary, author_id, language_id, publisher_id, image_url, image_public_id, created_at, updated_at) VALUES ('77777777-0000-0000-0000-000000000001',
                                                                                                                                                                                       0,
                                                                                                                                                                                       'Dune',
                                                                                                                                                                                       '9780441172719',
                                                                                                                                                                                       412,
                                                                                                                                                                                       1965,
                                                                                                                                                                                       'PAPERBACK',
                                                                                                                                                                                       'Set in the distant future amidst a feudal interstellar society in which various noble houses control planetary fiefs, it tells the story of young Paul Atreides, whose family accepts the stewardship of the planet Arrakis.',
                                                                                                                                                                                       '55555555-0000-0000-0000-000000000001',
                                                                                                                                                                                       '33333333-0000-0000-0000-000000000001',
                                                                                                                                                                                       '66666666-0000-0000-0000-000000000001',
                                                                                                                                                                                       'https://res.cloudinary.com/duntro9y8/image/upload/v1786048019/Dune_gfad4e.jpg',
                                                                                                                                                                                       'Dune_gfad4e',
                                                                                                                                                                                       NOW(),
                                                                                                                                                                                       NOW()),
                                                                                                                                                                                      ('77777777-0000-0000-0000-000000000002',
                                                                                                                                                                                       0,
                                                                                                                                                                                       'The Hobbit',
                                                                                                                                                                                       '9780007525492',
                                                                                                                                                                                       310,
                                                                                                                                                                                       1937,
                                                                                                                                                                                       'HARDCOVER',
                                                                                                                                                                                       'The Hobbit is set within Tolkiens fictional universe and follows the quest of home-loving Bilbo Baggins, the titular hobbit, to win a share of the treasure guarded by Smaug the dragon.',
                                                                                                                                                                                       '55555555-0000-0000-0000-000000000002',
                                                                                                                                                                                       '33333333-0000-0000-0000-000000000001',
                                                                                                                                                                                       '66666666-0000-0000-0000-000000000002',
                                                                                                                                                                                       'https://res.cloudinary.com/duntro9y8/image/upload/v1786048243/The-Hobbit-Book-Cover-with-Smaug_n2c9hj.png',
                                                                                                                                                                                       'The-Hobbit-Book-Cover-with-Smaug_n2c9hj',
                                                                                                                                                                                       NOW(),
                                                                                                                                                                                       NOW()),
                                                                                                                                                                                      ('77777777-0000-0000-0000-000000000003',
                                                                                                                                                                                       0,
                                                                                                                                                                                       'The Fellowship of the Ring',
                                                                                                                                                                                       '9780261103573',
                                                                                                                                                                                       423,
                                                                                                                                                                                       1954,
                                                                                                                                                                                       'PAPERBACK',
                                                                                                                                                                                       'The first volume of J. R. R. Tolkiens epic novel The Lord of the Rings. It takes place in the fictional universe of Middle-earth.',
                                                                                                                                                                                       '55555555-0000-0000-0000-000000000002',
                                                                                                                                                                                       '33333333-0000-0000-0000-000000000001',
                                                                                                                                                                                       '66666666-0000-0000-0000-000000000002',
                                                                                                                                                                                       'https://res.cloudinary.com/duntro9y8/image/upload/v1786048314/The_Fellowship_of_the_Ring_zltz3h.jpg',
                                                                                                                                                                                       'The_Fellowship_of_the_Ring_zltz3h',
                                                                                                                                                                                       NOW(),
                                                                                                                                                                                       NOW()),
                                                                                                                                                                                      ('77777777-0000-0000-0000-000000000004',
                                                                                                                                                                                       0,
                                                                                                                                                                                       'Foundation',
                                                                                                                                                                                       '9780553293357',
                                                                                                                                                                                       255,
                                                                                                                                                                                       1951,
                                                                                                                                                                                       'PAPERBACK',
                                                                                                                                                                                       'The Foundation series is a science fiction book series written by American author Isaac Asimov.',
                                                                                                                                                                                       '55555555-0000-0000-0000-000000000005',
                                                                                                                                                                                       '33333333-0000-0000-0000-000000000001',
                                                                                                                                                                                       '66666666-0000-0000-0000-000000000005',
                                                                                                                                                                                       'https://res.cloudinary.com/duntro9y8/image/upload/v1786048453/009Whelan_x1nd9z.png',
                                                                                                                                                                                       '009Whelan_x1nd9z',
                                                                                                                                                                                       NOW(),
                                                                                                                                                                                       NOW()),
                                                                                                                                                                                      ('77777777-0000-0000-0000-000000000005',
                                                                                                                                                                                       0,
                                                                                                                                                                                       'The Shining',
                                                                                                                                                                                       '9780345803788',
                                                                                                                                                                                       447,
                                                                                                                                                                                       1977,
                                                                                                                                                                                       'HARDCOVER',
                                                                                                                                                                                       'The Shining is a horror novel by American author Stephen King. Published in 1977, it is Kings third published novel and first hardback bestseller.',
                                                                                                                                                                                       '55555555-0000-0000-0000-000000000004',
                                                                                                                                                                                       '33333333-0000-0000-0000-000000000001',
                                                                                                                                                                                       '66666666-0000-0000-0000-000000000004',
                                                                                                                                                                                       'https://res.cloudinary.com/duntro9y8/image/upload/v1786048454/The_Shining_xrhm9g.jpg',
                                                                                                                                                                                       'The_Shining_xrhm9g',
                                                                                                                                                                                       NOW(),
                                                                                                                                                                                       NOW()),
                                                                                                                                                                                      ('77777777-0000-0000-0000-000000000006',
                                                                                                                                                                                       0,
                                                                                                                                                                                       'Under the Yoke',
                                                                                                                                                                                       '9781784351106',
                                                                                                                                                                                       424,
                                                                                                                                                                                       2016,
                                                                                                                                                                                       'PAPERBACK',
                                                                                                                                                                                       'A historical masterpiece detailing the struggle of the Bulgarian people against Ottoman rule in the late 19th century.',
                                                                                                                                                                                       '55555555-0000-0000-0000-000000000006',
                                                                                                                                                                                       '33333333-0000-0000-0000-000000000007',
                                                                                                                                                                                       '66666666-0000-0000-0000-000000000006',
                                                                                                                                                                                       'https://res.cloudinary.com/duntro9y8/image/upload/v1786048455/Under_the_Yoke_ehxqrn.jpg',
                                                                                                                                                                                       'Under_the_Yoke_ehxqrn',
                                                                                                                                                                                       NOW(),
                                                                                                                                                                                       NOW())
ON CONFLICT (id) DO NOTHING;

-- Book Genres
INSERT INTO books_genres (book_id, genre_id) VALUES
                                                 ('77777777-0000-0000-0000-000000000001', '44444444-0000-0000-0000-000000000001'), -- Dune -> Sci-Fi
                                                 ('77777777-0000-0000-0000-000000000002', '44444444-0000-0000-0000-000000000002'), -- The Hobbit -> Fantasy
                                                 ('77777777-0000-0000-0000-000000000003', '44444444-0000-0000-0000-000000000002'), -- Fellowship -> Fantasy
                                                 ('77777777-0000-0000-0000-000000000004', '44444444-0000-0000-0000-000000000001'), -- Foundation -> Sci-Fi
                                                 ('77777777-0000-0000-0000-000000000005', '44444444-0000-0000-0000-000000000009'), -- The Shining -> Horror
                                                 ('77777777-0000-0000-0000-000000000006', '44444444-0000-0000-0000-000000000006'), -- Under the Yoke -> Historical Fiction
                                                 ('77777777-0000-0000-0000-000000000006', '44444444-0000-0000-0000-000000000010')  -- Under the Yoke -> Classic
ON CONFLICT (book_id, genre_id) DO NOTHING;

-- Bookshelves
INSERT INTO bookshelves (id, version, name, description, user_id, created_at, updated_at) VALUES
                                                                                              ('88888888-0000-0000-0000-000000000001', 0, 'Favorites', 'My all-time favorite reads.', '22222222-0000-0000-0000-000000000001', NOW(), NOW()),
                                                                                              ('88888888-0000-0000-0000-000000000002', 0, 'To Read', 'Books I want to read soon.', '22222222-0000-0000-0000-000000000001', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Bookshelf Books
INSERT INTO bookshelf_books (bookshelf_id, book_id, added_at) VALUES
                                                                  ('88888888-0000-0000-0000-000000000001', '77777777-0000-0000-0000-000000000001', NOW()), -- Favorites -> Dune
                                                                  ('88888888-0000-0000-0000-000000000001', '77777777-0000-0000-0000-000000000002', NOW()), -- Favorites -> The Hobbit
                                                                  ('88888888-0000-0000-0000-000000000002', '77777777-0000-0000-0000-000000000004', NOW()), -- To Read -> Foundation
                                                                  ('88888888-0000-0000-0000-000000000001', '77777777-0000-0000-0000-000000000006', NOW())  -- Favorites -> Under the Yoke
ON CONFLICT (bookshelf_id, book_id) DO NOTHING;

-- User Personal Library Management
INSERT INTO user_books (id, version, user_id, book_id, status, is_favorite, created_at, updated_at) VALUES
                                                                                                        ('1a1a1a1a-0000-0000-0000-000000000001', 0, '22222222-0000-0000-0000-000000000001', '77777777-0000-0000-0000-000000000006', 'CURRENTLY_READING', true, NOW(), NOW()),
                                                                                                        ('2b2b2b2b-0000-0000-0000-000000000002', 0, '22222222-0000-0000-0000-000000000001', '77777777-0000-0000-0000-000000000001', 'READ', true, NOW(), NOW()),
                                                                                                        ('3c3c3c3c-0000-0000-0000-000000000003', 0, '22222222-0000-0000-0000-000000000002', '77777777-0000-0000-0000-000000000004', 'WISHLIST', false, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Reviews
INSERT INTO reviews (id, version, title, comment, rating, user_id, target_id, target_type, created_at, updated_at)
VALUES
    (
        '99999999-0000-0000-0000-000000000001',
        0,
        'Masterpiece',
        'A masterpiece of science fiction that is incredibly relevant today.',
        5, -- rating
        '22222222-0000-0000-0000-000000000001',
        '77777777-0000-0000-0000-000000000001',
        'BOOK',
        NOW(),
        NOW()
    ),
    (
        '99999999-0000-0000-0000-000000000002',
        0,
        'Fun Adventure',
        'A fun and engaging adventure.',
        5, -- rating
        '22222222-0000-0000-0000-000000000002',
        '77777777-0000-0000-0000-000000000002',
        'BOOK',
        NOW(),
        NOW()
    ),
    (
        '99999999-0000-0000-0000-000000000003',
        0,
        'Essential Read',
        'An essential epic read detailing foundational elements of cultural preservation and resilience.',
        5, -- rating
        '22222222-0000-0000-0000-000000000001',
        '77777777-0000-0000-0000-000000000006',
        'BOOK',
        NOW(),
        NOW()
    )
ON CONFLICT (id) DO NOTHING;
