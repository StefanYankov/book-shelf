package bg.softuni.bookshelf.data.entity;

import bg.softuni.bookshelf.data.entity.base.BaseUUIDEntity;
import bg.softuni.bookshelf.shared.ValidationConstants;
import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a language in which a book can be written.
 */
@Entity
@Table(name = "languages")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Language extends BaseUUIDEntity {

    @Column(
            nullable = false,
            unique = true,
            length = ValidationConstants.Language.MAX_NAME_LENGTH
    )
    private String name;
}
