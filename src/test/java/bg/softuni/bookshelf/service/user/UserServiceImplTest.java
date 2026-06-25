package bg.softuni.bookshelf.service.user;

import bg.softuni.bookshelf.data.entity.identity.AccountStatusEvent;
import bg.softuni.bookshelf.data.entity.identity.ApplicationUser;
import bg.softuni.bookshelf.data.entity.identity.User;
import bg.softuni.bookshelf.data.enums.StatusEventType;
import bg.softuni.bookshelf.data.repository.AccountStatusEventRepository;
import bg.softuni.bookshelf.data.repository.UserRepository;
import bg.softuni.bookshelf.service.user.dto.ChangePasswordDto;
import bg.softuni.bookshelf.service.user.dto.UpdateProfileDto;
import bg.softuni.bookshelf.shared.exception.BusinessException;
import bg.softuni.bookshelf.shared.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private AccountStatusEventRepository accountStatusEventRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Captor
    private ArgumentCaptor<AccountStatusEvent> eventCaptor;
    @Captor
    private ArgumentCaptor<ApplicationUser> userCaptor;

    @Nested
    @DisplayName("getProfile Tests")
    class GetProfileTests {
        @Test
        @DisplayName("Happy Path: Should return user profile DTO")
        void shouldReturnUserProfileDto() {
            // Arrange
            UUID userId = UUID.randomUUID();
            ApplicationUser user = new ApplicationUser();
            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // Act
            userService.getProfile(userId);

            // Assert
            verify(userMapper).toUserProfileDto(user);
        }

        @Test
        @DisplayName("Error Case: Should throw when user not found")
        void shouldThrow_WhenUserNotFound() {
            // Arrange
            UUID userId = UUID.randomUUID();
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> userService.getProfile(userId))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("updateProfile Tests")
    class UpdateProfileTests {
        @Test
        @DisplayName("Happy Path: Should update first and last name")
        void shouldUpdateFirstAndLastName() {
            // Arrange
            UUID userId = UUID.randomUUID();
            UpdateProfileDto dto = UpdateProfileDto.builder().firstName("New").lastName("User").build();
            ApplicationUser user = new ApplicationUser();
            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // Act
            userService.updateProfile(userId, dto);

            // Assert
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getFirstName()).isEqualTo("New");
            assertThat(userCaptor.getValue().getLastName()).isEqualTo("User");
        }

        @Test
        @DisplayName("Error Case: Should throw when user not found")
        void shouldThrow_WhenUserNotFound() {
            // Arrange
            UUID userId = UUID.randomUUID();
            UpdateProfileDto dto = UpdateProfileDto.builder().firstName("New").lastName("User").build();
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> userService.updateProfile(userId, dto))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("changePassword Tests")
    class ChangePasswordTests {
        @Test
        @DisplayName("Happy Path: Should change password when current password is correct")
        void shouldChangePassword_WhenCurrentPasswordIsCorrect() {
            // Arrange
            UUID userId = UUID.randomUUID();
            ChangePasswordDto dto = ChangePasswordDto.builder().currentPassword("old").newPassword("new").build();
            ApplicationUser user = new ApplicationUser();
            user.setPassword("encodedOld");
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(passwordEncoder.matches("old", "encodedOld")).willReturn(true);
            given(passwordEncoder.encode("new")).willReturn("encodedNew");

            // Act
            userService.changePassword(userId, dto);

            // Assert
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getPassword()).isEqualTo("encodedNew");
        }

        @Test
        @DisplayName("Error Case: Should throw when current password is incorrect")
        void shouldThrow_WhenCurrentPasswordIsIncorrect() {
            // Arrange
            UUID userId = UUID.randomUUID();
            ChangePasswordDto dto = ChangePasswordDto.builder().currentPassword("wrong").newPassword("new").build();
            ApplicationUser user = new ApplicationUser();
            user.setPassword("encodedOld");
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(passwordEncoder.matches("wrong", "encodedOld")).willReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> userService.changePassword(userId, dto))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CREDENTIALS);
        }

        @Test
        @DisplayName("Error Case: Should throw when user not found")
        void shouldThrow_WhenUserNotFound() {
            // Arrange
            UUID userId = UUID.randomUUID();
            ChangePasswordDto dto = ChangePasswordDto.builder().currentPassword("old").newPassword("new").build();
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> userService.changePassword(userId, dto))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
            verifyNoInteractions(passwordEncoder);
        }
    }

    @Nested
    @DisplayName("getAllUsers Tests")
    class GetAllUsersTests {
        @Test
        @DisplayName("Happy Path: Should return paged admin user view DTOs")
        void shouldReturnPagedAdminUserViewDtos() {
            // Arrange
            PageRequest pageable = PageRequest.of(0, 10);
            Page<User> userPage = new PageImpl<>(Collections.singletonList(new ApplicationUser()), pageable, 1);
            given(userRepository.findAll(pageable)).willReturn(userPage);

            // Act
            userService.getAllUsers(pageable);

            // Assert
            verify(userMapper).toAdminUserViewDto(any(ApplicationUser.class));
        }

        @Test
        @DisplayName("Edge Case: Should return empty page when no users exist")
        void shouldReturnEmptyPage_WhenNoUsersExist() {
            // Arrange
            PageRequest pageable = PageRequest.of(0, 10);
            given(userRepository.findAll(pageable)).willReturn(Page.empty());

            // Act
            Page<?> result = userService.getAllUsers(pageable);

            // Assert
            assertThat(result.isEmpty()).isTrue();
            verifyNoInteractions(userMapper);
        }
    }

    @Nested
    @DisplayName("Lock/Unlock User Tests")
    class LockUnlockUserTests {

        @Test
        @DisplayName("Happy Path: lockUser should create and save a lock event")
        void lockUser_shouldCreateAndSaveLockEvent() {
            // Arrange
            UUID userId = UUID.randomUUID();
            UUID actorId = UUID.randomUUID();
            ApplicationUser user = new ApplicationUser();
            ApplicationUser actor = new ApplicationUser();
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(userRepository.findById(actorId)).willReturn(Optional.of(actor));

            // Act
            userService.lockUser(userId, "Test reason", actorId);

            // Assert
            verify(accountStatusEventRepository).save(eventCaptor.capture());
            AccountStatusEvent capturedEvent = eventCaptor.getValue();
            assertThat(capturedEvent.getEventType()).isEqualTo(StatusEventType.ACCOUNT_LOCKED);
            assertThat(capturedEvent.getReason()).isEqualTo("Test reason");
            assertThat(capturedEvent.getUser()).isEqualTo(user);
            assertThat(capturedEvent.getActor()).isEqualTo(actor);
        }

        @Test
        @DisplayName("Happy Path: unlockUser should create and save an unlock event")
        void unlockUser_shouldCreateAndSaveUnlockEvent() {
            // Arrange
            UUID userId = UUID.randomUUID();
            UUID actorId = UUID.randomUUID();
            ApplicationUser user = new ApplicationUser();
            ApplicationUser actor = new ApplicationUser();
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(userRepository.findById(actorId)).willReturn(Optional.of(actor));

            // Act
            userService.unlockUser(userId, "Test reason", actorId);

            // Assert
            verify(accountStatusEventRepository).save(eventCaptor.capture());
            AccountStatusEvent capturedEvent = eventCaptor.getValue();
            assertThat(capturedEvent.getEventType()).isEqualTo(StatusEventType.ACCOUNT_UNLOCKED);
        }

        @Test
        @DisplayName("Error Case: lockUser should throw USER_NOT_FOUND if target user does not exist")
        void lockUser_shouldThrowWhenUserNotFound() {
            // Arrange
            UUID userId = UUID.randomUUID();
            UUID actorId = UUID.randomUUID();
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> userService.lockUser(userId, "reason", actorId))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

            verifyNoInteractions(accountStatusEventRepository);
        }

        @Test
        @DisplayName("Error Case: lockUser should throw USER_NOT_FOUND if actor does not exist")
        void lockUser_shouldThrowWhenActorNotFound() {
            // Arrange
            UUID userId = UUID.randomUUID();
            UUID actorId = UUID.randomUUID();
            ApplicationUser user = new ApplicationUser();
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(userRepository.findById(actorId)).willReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> userService.lockUser(userId, "reason", actorId))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

            verifyNoInteractions(accountStatusEventRepository);
        }
    }
}
