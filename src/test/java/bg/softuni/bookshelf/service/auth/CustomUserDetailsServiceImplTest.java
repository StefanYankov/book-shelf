package bg.softuni.bookshelf.service.auth;

import bg.softuni.bookshelf.data.entity.identity.AdminUser;
import bg.softuni.bookshelf.data.entity.identity.ApplicationUser;
import bg.softuni.bookshelf.data.enums.Permission;
import bg.softuni.bookshelf.data.repository.UserRepository;
import bg.softuni.bookshelf.service.user.AccountStatusService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService Unit Tests")
class CustomUserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private AccountStatusService accountStatusService;

    @InjectMocks
    private CustomUserDetailsServiceImpl userDetailsService;

    private ApplicationUser createTestUser(UUID id, String username) {
        ApplicationUser user = new ApplicationUser();
        user.setId(id);
        user.setUsername(username);
        user.setPassword("hashed-password");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail(username + "@example.com");
        return user;
    }

    private ApplicationUser createTestUser(UUID id, String username, Permission... permissions) {
        ApplicationUser user = createTestUser(id, username);
        user.setPermissions(new HashSet<>(Arrays.asList(permissions)));
        return user;
    }

    private AdminUser createTestAdmin(UUID id, String username) {
        AdminUser admin = new AdminUser();
        admin.setId(id);
        admin.setUsername(username);
        admin.setPassword("hashed-password");
        admin.setFirstName("Sys");
        admin.setLastName("Admin");
        admin.setEmail(username + "@example.com");
        return admin;
    }

    @Nested
    @DisplayName("loadUserByUsername(String) Tests")
    class LoadUserByUsernameTests {

        @Test
        @DisplayName("Happy Path: Should return CustomUserDetails when user is found and active")
        void shouldReturnUserDetails_WhenUserIsFoundAndActive() {
            // Arrange
            String username = "activeuser";
            UUID userId = UUID.randomUUID();
            ApplicationUser userEntity = createTestUser(userId, username);
            given(userRepository.findByUsername(username)).willReturn(Optional.of(userEntity));
            given(accountStatusService.isUserActive(userId)).willReturn(true);

            // Act
            UserDetails result = userDetailsService.loadUserByUsername(username);

            // Assert
            assertThat(result).isNotNull().isInstanceOf(CustomUserDetails.class);
            CustomUserDetails customUserDetails = (CustomUserDetails) result;

            assertThat(customUserDetails.getId()).isEqualTo(userId);
            assertThat(customUserDetails.getUsername()).isEqualTo(username);
            assertThat(customUserDetails.getPassword()).isEqualTo("hashed-password");
            assertThat(customUserDetails.isEnabled()).isTrue();
            assertThat(customUserDetails.getAuthorities())
                    .extracting(GrantedAuthority::getAuthority)
                    .containsExactly("ROLE_USER");

            verify(userRepository).findByUsername(username);
            verifyNoMoreInteractions(userRepository);
        }

        @Test
        @DisplayName("Error Case: Should throw UsernameNotFoundException when user is not found")
        void shouldThrowException_WhenUserNotFound() {
            // Arrange
            String username = "nonexistent";
            given(userRepository.findByUsername(username)).willReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> userDetailsService.loadUserByUsername(username))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessage("User not found: " + username);

            verify(userRepository).findByUsername(username);
            verifyNoMoreInteractions(userRepository);
        }

        @Test
        @DisplayName("Edge Case: Should return CustomUserDetails with isEnabled=false when user is inactive")
        void shouldReturnDisabledUserDetails_WhenUserIsInactive() {
            // Arrange
            String username = "inactiveuser";
            UUID userId = UUID.randomUUID();
            ApplicationUser userEntity = createTestUser(userId, username);
            given(userRepository.findByUsername(username)).willReturn(Optional.of(userEntity));
            given(accountStatusService.isUserActive(userId)).willReturn(false);

            // Act
            UserDetails result = userDetailsService.loadUserByUsername(username);

            // Assert
            assertThat(result).isNotNull().isInstanceOf(CustomUserDetails.class);
            CustomUserDetails customUserDetails = (CustomUserDetails) result;

            assertThat(customUserDetails.getId()).isEqualTo(userId);
            assertThat(customUserDetails.getUsername()).isEqualTo(username);
            assertThat(customUserDetails.isEnabled()).isFalse();

            verify(userRepository).findByUsername(username);
            verifyNoMoreInteractions(userRepository);
        }

        @Test
        @DisplayName("Should map granted permissions to authorities alongside the base role")
        void shouldMapPermissionsToAuthorities() {
            // Arrange: an active user who has been granted the MODERATE_REVIEWS permission
            String username = "moderator";
            UUID userId = UUID.randomUUID();
            ApplicationUser userEntity = createTestUser(userId, username, Permission.MODERATE_REVIEWS);
            given(userRepository.findByUsername(username)).willReturn(Optional.of(userEntity));
            given(accountStatusService.isUserActive(userId)).willReturn(true);

            // Act
            CustomUserDetails result = (CustomUserDetails) userDetailsService.loadUserByUsername(username);

            // Assert: the principal carries BOTH the base role and the granted permission
            assertThat(result.getAuthorities())
                    .extracting(GrantedAuthority::getAuthority)
                    .containsExactlyInAnyOrder("ROLE_USER", "MODERATE_REVIEWS");
        }

        @Test
        @DisplayName("Should map a user with no permissions to the base role only")
        void shouldMapUserWithoutPermissionsToRoleOnly() {
            // Arrange
            String username = "plainuser";
            UUID userId = UUID.randomUUID();
            ApplicationUser userEntity = createTestUser(userId, username);
            given(userRepository.findByUsername(username)).willReturn(Optional.of(userEntity));
            given(accountStatusService.isUserActive(userId)).willReturn(true);

            // Act
            CustomUserDetails result = (CustomUserDetails) userDetailsService.loadUserByUsername(username);

            // Assert
            assertThat(result.getAuthorities())
                    .extracting(GrantedAuthority::getAuthority)
                    .containsExactly("ROLE_USER");
        }

        @Test
        @DisplayName("Should map an AdminUser to ROLE_ADMIN, always enabled, without consulting account status")
        void shouldMapAdminUser() {
            // Arrange
            String username = "admin";
            UUID userId = UUID.randomUUID();
            AdminUser adminEntity = createTestAdmin(userId, username);
            given(userRepository.findByUsername(username)).willReturn(Optional.of(adminEntity));

            // Act
            CustomUserDetails result = (CustomUserDetails) userDetailsService.loadUserByUsername(username);

            // Assert
            assertThat(result.getId()).isEqualTo(userId);
            assertThat(result.isEnabled()).isTrue();
            assertThat(result.getAuthorities())
                    .extracting(GrantedAuthority::getAuthority)
                    .containsExactly("ROLE_ADMIN");
            verify(accountStatusService, never()).isUserActive(userId);
        }
    }
}