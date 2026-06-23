package bg.softuni.bookshelf.service.user;

import bg.softuni.bookshelf.data.entity.identity.User;
import bg.softuni.bookshelf.data.repository.UserRepository;
import bg.softuni.bookshelf.service.base.BaseService;
import bg.softuni.bookshelf.service.user.dto.ChangePasswordDto;
import bg.softuni.bookshelf.service.user.dto.UpdateProfileDto;
import bg.softuni.bookshelf.service.user.dto.UserProfileDto;
import bg.softuni.bookshelf.shared.exception.BusinessException;
import bg.softuni.bookshelf.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
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
}
