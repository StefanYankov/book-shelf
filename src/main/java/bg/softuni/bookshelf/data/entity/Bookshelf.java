package bg.softuni.bookshelf.data.entity;

import bg.softuni.bookshelf.data.entity.base.BaseUUIDEntity;
import bg.softuni.bookshelf.data.entity.identity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "bookshelves")
public class Bookshelf extends BaseUUIDEntity {

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(
            mappedBy = "bookshelf",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<BookshelfBook> books = new ArrayList<>();
}
