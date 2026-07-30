package bg.softuni.bookshelf.service.user.dto;

import bg.softuni.bookshelf.data.enums.Permission;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PermissionRequestDto(
        @NotNull(message = "{admin.user.permission.notNull}")
        Permission permission,

        @NotBlank(message = "{admin.user.permission.reason.notBlank}")
        String reason
) {

}