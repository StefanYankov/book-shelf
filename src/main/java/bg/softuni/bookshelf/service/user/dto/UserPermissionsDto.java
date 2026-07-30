package bg.softuni.bookshelf.service.user.dto;

import bg.softuni.bookshelf.data.enums.Permission;

import java.util.Set;
import java.util.UUID;

/**
 * Purpose-built read projection for a user's granted permissions.
 * Kept separate from AdminUserViewDto so the permission read model can evolve
 * independently and stays lean for on-demand loading.
 */
public record UserPermissionsDto(
        UUID userId,
        Set<Permission> permissions
) {

}