package bg.softuni.bookshelf.service.user;

import bg.softuni.bookshelf.data.entity.identity.AccountStatusEvent;
import bg.softuni.bookshelf.data.entity.identity.ApplicationUser;
import bg.softuni.bookshelf.data.entity.identity.User;
import bg.softuni.bookshelf.data.enums.Permission;
import bg.softuni.bookshelf.data.enums.StatusEventType;
import bg.softuni.bookshelf.data.repository.AccountStatusEventRepository;
import bg.softuni.bookshelf.data.repository.UserRepository;
import bg.softuni.bookshelf.service.base.BaseService;
import bg.softuni.bookshelf.service.user.dto.*;
import bg.softuni.bookshelf.shared.exception.BusinessException;
import bg.softuni.bookshelf.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends BaseService implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AccountStatusEventRepository accountStatusEventRepository;

    @Override
    @Transactional(readOnly = true)
    public UserProfileDto getProfile(UUID userId) {
        User user = findOrThrow(() -> userRepository.findById(userId), ErrorCode.USER_NOT_FOUND, userId);
        return userMapper.toUserProfileDto(user);
    }

    @Override
    @Transactional
    public void updateProfile(UUID userId, UpdateProfileDto dto) {
        User user = findOrThrow(() -> userRepository.findById(userId), ErrorCode.USER_NOT_FOUND, userId);
        user.setFirstName(dto.firstName());
        user.setLastName(dto.lastName());
        userRepository.save(user);
        log.info("Profile updated for user {}", userId);
    }

    @Override
    @Transactional
    public UserSecurityDto changePassword(UUID userId, ChangePasswordDto dto) {
        User user = findOrThrow(() -> userRepository.findById(userId), ErrorCode.USER_NOT_FOUND, userId);

        if (!passwordEncoder.matches(dto.currentPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        user.setPassword(passwordEncoder.encode(dto.newPassword()));
        user.setPasswordChangeRequired(false);
        User savedUser = userRepository.save(user);
        log.info("Password changed for user {}", userId);

        return new UserSecurityDto(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.isPasswordChangeRequired()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminUserViewDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toAdminUserViewDto);
    }

    @Override
    @Transactional
    public void lockUser(UUID userId, String reason, UUID actorId) {
        if (userId.equals(actorId)) {
            throw new BusinessException(ErrorCode.SELF_LOCK_PREVENTION);
        }

        User user = findOrThrow(() -> userRepository.findById(userId), ErrorCode.USER_NOT_FOUND, userId);
        User actor = findOrThrow(() -> userRepository.findById(actorId), ErrorCode.USER_NOT_FOUND, actorId);

        recordStatusEvent(user, actor, reason, StatusEventType.ACCOUNT_LOCKED);
        log.info("User {} locked by admin {}", userId, actorId);
    }

    @Override
    @Transactional
    public void unlockUser(UUID userId, String reason, UUID actorId) {
        User user = findOrThrow(() -> userRepository.findById(userId), ErrorCode.USER_NOT_FOUND, userId);
        User actor = findOrThrow(() -> userRepository.findById(actorId), ErrorCode.USER_NOT_FOUND, actorId);

        recordStatusEvent(user, actor, reason, StatusEventType.ACCOUNT_UNLOCKED);
        log.info("User {} unlocked by admin {}", userId, actorId);
    }

    @Override
    @Transactional
    public void grantPermission(UUID userId, Permission permission, String reason, UUID actorId) {
        ApplicationUser user = findApplicationUserOrThrow(userId);
        User actor = findOrThrow(() -> userRepository.findById(actorId), ErrorCode.USER_NOT_FOUND, actorId);

        user.getPermissions().add(permission);
        userRepository.save(user);

        recordStatusEvent(user, actor, reason, StatusEventType.PERMISSION_GRANTED);
        log.info("Admin {} granted permission {} to user {}", actorId, permission, userId);
    }

    @Override
    @Transactional
    public void revokePermission(UUID userId, Permission permission, String reason, UUID actorId) {
        ApplicationUser user = findApplicationUserOrThrow(userId);
        User actor = findOrThrow(() -> userRepository.findById(actorId), ErrorCode.USER_NOT_FOUND, actorId);

        user.getPermissions().remove(permission);
        userRepository.save(user);

        recordStatusEvent(user, actor, reason, StatusEventType.PERMISSION_REVOKED);
        log.info("Admin {} revoked permission {} from user {}", actorId, permission, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserPermissionsDto getUserPermissions(UUID userId) {
        ApplicationUser user = findApplicationUserOrThrow(userId);
        log.info("Permissions retrieved for user {}", userId);
        return new UserPermissionsDto(user.getId(), user.getPermissions());
    }


    // Narrows a user to ApplicationUser or rejects the operation. Permissions only exist on
    // standard user accounts; admins derive all capability from their role.
    private ApplicationUser findApplicationUserOrThrow(UUID userId) {
        User user = findOrThrow(() -> userRepository.findById(userId), ErrorCode.USER_NOT_FOUND, userId);
        if (!(user instanceof ApplicationUser applicationUser)) {
            throw new BusinessException(ErrorCode.PERMISSION_TARGET_INVALID);
        }
        return applicationUser;
    }

    // Centralizes the auditable account-status event write shared by lock/unlock and grant/revoke.
    private void recordStatusEvent(User user, User actor, String reason, StatusEventType type) {
        AccountStatusEvent event = new AccountStatusEvent();
        event.setUser(user);
        event.setActor(actor);
        event.setReason(reason);
        event.setEventType(type);
        accountStatusEventRepository.save(event);
    }


}