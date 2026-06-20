package bg.softuni.bookshelf.service.auth;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.UUID;

/**
 * Custom extension of Spring Security's User object to include the database UUID.
 * <p>
 * This allows the application to have immediate access to the user's primary key
 * from the security principal without needing a secondary database lookup.
 */
@Getter
public class CustomUserDetails extends User {

    private final UUID id;

    public CustomUserDetails(
            UUID id,
            String username,
            String password,
            boolean isActive,
            Collection<? extends GrantedAuthority> authorities) {

        super(username, password, isActive, true, true, true, authorities);
        this.id = id;
    }
}
