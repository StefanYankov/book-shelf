package bg.softuni.bookshelf.service.user.dto;

import jakarta.validation.constraints.NotBlank;

public record LockUserRequestDto(
        @NotBlank(message = "{admin.user.lock.reason.notBlank}")
        String reason
) {
}
