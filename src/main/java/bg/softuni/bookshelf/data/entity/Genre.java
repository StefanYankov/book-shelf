package bg.softuni.bookshelf.data.entity;

import bg.softuni.bookshelf.data.entity.base.BaseUUIDEntity;
import bg.softuni.bookshelf.shared.ValidationConstants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a book genre or category (e.g., "Science Fiction", "Fantasy").
 * Stored as a separate entity to allow for dynamic management and to
 * facilitate a many-to-many relationship with Books.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "genres")
public class Genre extends BaseUUIDEntity {

    @Column(
            nullable = false,
            unique = true,
            length = ValidationConstants.Genre.MAX_NAME_LENGTH
    )
    private String name;

    @Column(length = ValidationConstants.Genre.MAX_DESCRIPTION_LENGTH)
    private String description;
}