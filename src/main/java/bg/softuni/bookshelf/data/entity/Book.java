package bg.softuni.bookshelf.data.entity;

import bg.softuni.bookshelf.data.entity.base.BaseUUIDEntity;
import bg.softuni.bookshelf.data.entity.value.Image;
import bg.softuni.bookshelf.data.enums.BookFormat;
import bg.softuni.bookshelf.shared.ValidationConstants;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.validator.constraints.ISBN;

import java.util.HashSet;
import java.util.Set;

// TODO: split books from editions, as we have shared book properties across multiple editions. Make book abstract
@Entity
@Table(name = "books")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"author", "genres", "language", "publisher", "reviews", "userEntries"})
public class Book extends BaseUUIDEntity {

    @Column(
            nullable = false,
            length = ValidationConstants.Book.MAX_TITLE_LENGTH
    )
    private String title;

    @ISBN
    private String ISBN;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private Author author;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "books_genres",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    @Builder.Default
    private Set<Genre> genres = new HashSet<>();

    // TODO: Add support for multiple images (gallery) by converting this to a @OneToMany with a BookImage entity.
    @Embedded
    private Image coverImage;

    @Column(nullable = false)
    private int pages;

    @Column(nullable = false)
    private int yearPublished;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_id", nullable = false)
    private Language language;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id", nullable = false)
    private Publisher publisher;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookFormat format;

    @Column(
            nullable = false,
            length = ValidationConstants.Book.MAX_SUMMARY_LENGTH
    )
    private String summary;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Review> reviews = new HashSet<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<UserBook> userEntries = new HashSet<>();
}
