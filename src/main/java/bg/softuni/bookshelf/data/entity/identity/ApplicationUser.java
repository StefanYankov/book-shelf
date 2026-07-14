package bg.softuni.bookshelf.data.entity.identity;

import bg.softuni.bookshelf.data.entity.Bookshelf;
import bg.softuni.bookshelf.data.entity.UserBook;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "application_users")
@Getter
@Setter
@ToString(exclude = {"libraryEntries", "bookshelves", "statusEvents"})
public class ApplicationUser extends User {

    /**
     * Indicates whether the user has verified their email address.
     */
    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserBook> libraryEntries = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Bookshelf> bookshelves = new ArrayList<>();

    /**
     * A chronological log of all status-changing events for this user's account.
     * The user's current active/locked/banned status is derived from the most recent event in this list.
     * This provides a full audit trail for administrative actions.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccountStatusEvent> statusEvents = new ArrayList<>();
}