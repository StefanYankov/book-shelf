package bg.softuni.bookshelf.data.entity.identity;

import bg.softuni.bookshelf.data.entity.base.BaseUUIDEntity;
import bg.softuni.bookshelf.shared.ValidationConstants;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Root entity for the application's user hierarchy, acting as the principal for Spring Security.
 * Implements a JOINED inheritance strategy, meaning shared authentication data is stored here,
 * while specific role datais stored in sub-tables.
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

    @Column(name = "first_name", nullable = false, length = ValidationConstants.User.MAX_FIRST_NAME_LENGTH)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = ValidationConstants.User.MAX_LAST_NAME_LENGTH)
    private String lastName;
}
