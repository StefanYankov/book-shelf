package bg.softuni.bookshelf.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BookshelfBookId implements Serializable {

    @Column(name = "bookshelf_id")
    private UUID bookshelfId;

    @Column(name = "book_id")
    private UUID bookId;
}
