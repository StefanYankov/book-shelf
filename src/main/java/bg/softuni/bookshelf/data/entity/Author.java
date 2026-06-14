package bg.softuni.bookshelf.data.entity;

import bg.softuni.bookshelf.data.entity.base.BaseUUIDEntity;
import bg.softuni.bookshelf.data.entity.value.Image;
import bg.softuni.bookshelf.shared.ValidationConstants;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "authors")
@ToString
public class Author extends BaseUUIDEntity {

    @Column(
            nullable = false,
            length = ValidationConstants.Author.MAX_NAME_LENGTH)
    private String name;

    @Column(length = ValidationConstants.Author.MAX_SUMMARY_LENGTH)
    private String summary;

    @Embedded
    private Image image;
}
