package bg.softuni.bookshelf.service.user;

import bg.softuni.bookshelf.data.entity.identity.ApplicationUser;
import bg.softuni.bookshelf.data.repository.UserRepository;
import bg.softuni.bookshelf.service.user.dto.ChangePasswordDto;
import bg.softuni.bookshelf.service.user.dto.UpdateProfileDto;
import bg.softuni.bookshelf.shared.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Nested
    @DisplayName("updateProfile Tests")
    class UpdateProfileTests {
        @Test
        void shouldUpdateUserProfile() {
            // Arrange
            UUID userId = UUID.randomUUID();
            UpdateProfileDto dto = new UpdateProfileDto("NewFirst", "NewLast");
            ApplicationUser user = new ApplicationUser();
            user.setFirstName("OldFirst");
            user.setLastName("OldLast");

            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // Act
            userService.updateProfile(userId, dto);

            // Assert
            assertThat(user.getFirstName()).isEqualTo("NewFirst");
            assertThat(user.getLastName()).isEqualTo("NewLast");
            verify(userRepository).save(user);
        }
    }

    @Nested
    @DisplayName("changePassword Tests")
    class ChangePasswordTests {
        @Test
        void shouldChangePasswordWhenCurrentPasswordIsCorrect() {
            // Arrange
            UUID userId = UUID.randomUUID();
            ChangePasswordDto dto = new ChangePasswordDto("currentPass", "newPass");
            ApplicationUser user = new ApplicationUser();
            user.setPassword("encodedCurrentPass");

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(passwordEncoder.matches("currentPass", "encodedCurrentPass")).willReturn(true);
            given(passwordEncoder.encode("newPass")).willReturn("encodedNewPass");

            // Act
            userService.changePassword(userId, dto);

            // Assert
            assertThat(user.getPassword()).isEqualTo("encodedNewPass");
            verify(userRepository).save(user);
        }

        @Test
        void shouldThrowBusinessExceptionWhenCurrentPasswordIsIncorrect() {
            // Arrange
            UUID userId = UUID.randomUUID();
            ChangePasswordDto dto = new ChangePasswordDto("wrongCurrentPass", "newPass");
            ApplicationUser user = new ApplicationUser();
            user.setPassword("encodedCurrentPass");

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(passwordEncoder.matches("wrongCurrentPass", "encodedCurrentPass")).willReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> userService.changePassword(userId, dto))
                    .isInstanceOf(BusinessException.class);
        }
    }
}
