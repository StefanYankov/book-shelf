package bg.softuni.bookshelf.data.entity.identity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "admin_users")
@Getter
@Setter
public class AdminUser extends User {
    // This entity can be expanded with admin-specific fields in the future,
    // such as permissions or internal notes.
}
