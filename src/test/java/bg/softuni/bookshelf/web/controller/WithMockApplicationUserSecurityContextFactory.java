package bg.softuni.bookshelf.web.controller;

import bg.softuni.bookshelf.service.auth.CustomUserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Security context factory that instantiates CustomUserDetails dynamically
 * using values declared on the WithMockApplicationUser annotation.
 */
public class WithMockApplicationUserSecurityContextFactory implements WithSecurityContextFactory<WithMockApplicationUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockApplicationUser annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        List<GrantedAuthority> authorities = new ArrayList<>();

        Arrays.stream(annotation.roles())
                .map(role -> new SimpleGrantedAuthority(role.startsWith("ROLE_") ? role : "ROLE_" + role))
                .forEach(authorities::add);

        Arrays.stream(annotation.permissions())
                .map(SimpleGrantedAuthority::new)
                .forEach(authorities::add);

        CustomUserDetails principal = new CustomUserDetails(
                UUID.randomUUID(),
                annotation.username(),
                "password",
                true,
                annotation.passwordChangeRequired(),
                authorities
        );

        Authentication auth = new UsernamePasswordAuthenticationToken(principal, "password", principal.getAuthorities());
        context.setAuthentication(auth);
        return context;
    }
}