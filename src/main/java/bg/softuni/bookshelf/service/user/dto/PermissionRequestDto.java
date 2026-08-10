package bg.softuni.bookshelf.service.user.dto;

import bg.softuni.bookshelf.data.enums.Permission;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PermissionRequestDto(
        @Schema(description = "Privilege to grant or revoke.",
                example = "MODERATE_REVIEWS")
        @NotNull(message = "{admin.user.permission.notNull}")
        Permission permission,

        @Schema(description = "Administrative reason, recorded on the audit event.",
                example = "Trusted contributor promoted to review moderator")
        @NotBlank(message = "{admin.user.permission.reason.notBlank}")
        String reason
) {

}