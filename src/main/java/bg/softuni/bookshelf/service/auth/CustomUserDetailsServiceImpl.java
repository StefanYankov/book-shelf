package bg.softuni.bookshelf.service.auth;

import bg.softuni.bookshelf.data.entity.identity.AdminUser;
import bg.softuni.bookshelf.data.entity.identity.ApplicationUser;
import bg.softuni.bookshelf.data.entity.identity.User;
import bg.softuni.bookshelf.data.repository.UserRepository;
import bg.softuni.bookshelf.service.user.AccountStatusService;
import bg.softuni.bookshelf.shared.exception.BusinessException;
import bg.softuni.bookshelf.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Implementation of the custom user details service.
 * <p>
 * This service bridges the application's {@link UserRepository} with Spring Security's
 * {@link UserDetails} mechanism, facilitating the loading of users during authentication.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final AccountStatusService accountStatusService;

    /**
     * Loads a user from the database by their unique username.
     * <p>
     * This method is called by Spring Security during the authentication process.
     * It dynamically determines the user's role based on their entity type
     * (e.g., {@link ApplicationUser} or {@link AdminUser}) and maps it to a
     * Spring Security {@link CustomUserDetails} object.
     *
     * @param username the username identifying the user whose data is required.
     * @return a fully populated custom user record containing the UUID (never {@code null})
     * @throws UsernameNotFoundException if the user could not be found.
     */
    // TODO: Configure a cache manager (e.g., Caffeine) to enable this optimization.
    @Override
    @Cacheable("users")
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Attempting to load user by username: {}", username);

        return userRepository.findByUsername(username)
                .map(this::mapToUserDetails)
                .orElseThrow(() -> {
                    log.warn("Authentication failed. User with username [{}] not found.", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });
    }

    private CustomUserDetails mapToUserDetails(User user) {
        String role;
        boolean isEnabled;

        if (user instanceof AdminUser) {
            role = "ROLE_ADMIN";
            isEnabled = true; // Admins are always enabled
        } else if (user instanceof ApplicationUser) {
            role = "ROLE_USER";
            isEnabled = accountStatusService.isUserActive(user.getId());
        } else {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "Unknown user type: " + user.getClass().getSimpleName());
        }

        return new CustomUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                isEnabled,
                user.isPasswordChangeRequired(),
                Collections.singletonList(new SimpleGrantedAuthority(role))
        );
    }
}
