package bg.softuni.bookshelf.data.entity;

import bg.softuni.bookshelf.data.entity.base.BaseUUIDEntity;
import bg.softuni.bookshelf.data.entity.identity.ApplicationUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "user_books", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "book_id"})
})
@ToString(exclude = {"user", "book"})
public class UserBook extends BaseUUIDEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private ApplicationUser user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookStatus status;

    @Column(nullable = false)
    private boolean isFavorite = false;
}
