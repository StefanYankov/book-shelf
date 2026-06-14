package bg.softuni.bookshelf.data.entity.identity;

import bg.softuni.bookshelf.data.entity.base.BaseUUIDEntity;
import bg.softuni.bookshelf.shared.ValidationConstants;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Abstract base class for all users in the system.
 */
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@ToString
public abstract class User extends BaseUUIDEntity {

    @Column(nullable = false, unique = true, length = ValidationConstants.User.MAX_USERNAME_LENGTH)
    private String username;

    @Column(unique = true, length = ValidationConstants.User.MAX_EMAIL_LENGTH)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "first_name", nullable = false, length = ValidationConstants.User.MAX_NAME_LENGTH)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = ValidationConstants.User.MAX_NAME_LENGTH)
    private String lastName;
}
