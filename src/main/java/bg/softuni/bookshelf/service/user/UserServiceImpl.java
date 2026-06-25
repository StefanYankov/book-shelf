package bg.softuni.bookshelf.service.user;

import bg.softuni.bookshelf.data.entity.identity.AccountStatusEvent;
import bg.softuni.bookshelf.data.entity.identity.User;
import bg.softuni.bookshelf.data.enums.StatusEventType;
import bg.softuni.bookshelf.data.repository.AccountStatusEventRepository;
import bg.softuni.bookshelf.data.repository.UserRepository;
import bg.softuni.bookshelf.service.base.BaseService;
import bg.softuni.bookshelf.service.user.dto.AdminUpdateUserDto;
import bg.softuni.bookshelf.service.user.dto.AdminUserViewDto;
import bg.softuni.bookshelf.service.user.dto.ChangePasswordDto;
import bg.softuni.bookshelf.service.user.dto.UpdateProfileDto;
import bg.softuni.bookshelf.service.user.dto.UserProfileDto;
import bg.softuni.bookshelf.shared.exception.BusinessException;
import bg.softuni.bookshelf.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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
    }

    @Override
    @Transactional
    public void changePassword(UUID userId, ChangePasswordDto dto) {
        User user = findOrThrow(() -> userRepository.findById(userId), ErrorCode.USER_NOT_FOUND, userId);

        if (!passwordEncoder.matches(dto.currentPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        user.setPassword(passwordEncoder.encode(dto.newPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminUserViewDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toAdminUserViewDto);
    }

    @Override
    @Transactional
    public void lockUser(UUID userId, String reason, UUID actorId) {
        User user = findOrThrow(() -> userRepository.findById(userId), ErrorCode.USER_NOT_FOUND, userId);
        User actor = findOrThrow(() -> userRepository.findById(actorId), ErrorCode.USER_NOT_FOUND, actorId);

        AccountStatusEvent event = new AccountStatusEvent();
        event.setUser(user);
        event.setActor(actor);
        event.setReason(reason);
        event.setEventType(StatusEventType.ACCOUNT_LOCKED);
        accountStatusEventRepository.save(event);
    }

    @Override
    @Transactional
    public void unlockUser(UUID userId, String reason, UUID actorId) {
        User user = findOrThrow(() -> userRepository.findById(userId), ErrorCode.USER_NOT_FOUND, userId);
        User actor = findOrThrow(() -> userRepository.findById(actorId), ErrorCode.USER_NOT_FOUND, actorId);

        AccountStatusEvent event = new AccountStatusEvent();
        event.setUser(user);
        event.setActor(actor);
        event.setReason(reason);
        event.setEventType(StatusEventType.ACCOUNT_UNLOCKED);
        accountStatusEventRepository.save(event);
    }
}
