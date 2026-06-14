package bg.softuni.bookshelf.data.entity.identity;

import bg.softuni.bookshelf.data.entity.Review;
import bg.softuni.bookshelf.data.entity.UserBook;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "application_users")
@Getter
@Setter
@ToString(exclude = {"reviews", "libraryEntries"})
public class ApplicationUser extends User {

    /**
     * Soft-delete flag used by Spring Security.
     * If false, the user cannot authenticate.
     */
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    /**
     * Indicates whether the user has verified their email address.
     */
    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Review> reviews = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserBook> libraryEntries = new HashSet<>();
}
