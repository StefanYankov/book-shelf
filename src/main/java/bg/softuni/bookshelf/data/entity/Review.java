package bg.softuni.bookshelf.data.entity;

import bg.softuni.bookshelf.data.entity.base.BaseUUIDEntity;
import bg.softuni.bookshelf.data.entity.identity.ApplicationUser;
import bg.softuni.bookshelf.shared.ValidationConstants;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "reviews")
@ToString(exclude = {"user", "book"})
public class Review extends BaseUUIDEntity {

    @Column(nullable = false, length = ValidationConstants.Review.MAX_TITLE_LENGTH)
    private String title;

    @Lob
    @Column(nullable = false)
    private String comment;

    @Column(nullable = false)
    private int rating;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private ApplicationUser user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;
}
