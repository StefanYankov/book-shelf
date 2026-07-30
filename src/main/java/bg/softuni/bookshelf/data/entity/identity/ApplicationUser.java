package bg.softuni.bookshelf.data.entity.identity;

import bg.softuni.bookshelf.data.entity.Bookshelf;
import bg.softuni.bookshelf.data.entity.UserBook;
import bg.softuni.bookshelf.data.enums.Permission;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.*;

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

    /**
     * Capabilities granted to this user by an administrator, mapped to
     * Spring Security authorities alongside the base role.
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "user_permissions",
            joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "permission", nullable = false)
    private Set<Permission> permissions = EnumSet.noneOf(Permission.class);

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserBook> libraryEntries = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Bookshelf> bookshelves = new ArrayList<>();

    /**
     * A chronological log of all status-changing events for this user's account, to provide a full
     * audit trail for administrative actions.
     * The user's current active/locked/banned status is derived from the most recent event in this list.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccountStatusEvent> statusEvents = new ArrayList<>();
}