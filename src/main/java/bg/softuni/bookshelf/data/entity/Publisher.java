package bg.softuni.bookshelf.data.entity;

import bg.softuni.bookshelf.data.entity.base.BaseUUIDEntity;
import bg.softuni.bookshelf.shared.ValidationConstants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "publishers")
public class Publisher extends BaseUUIDEntity {

    @Column(
            nullable = false,
            unique = true,
            length = ValidationConstants.Publisher.MAX_NAME_LENGTH
    )
    private String name;

}
