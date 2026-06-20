package bg.softuni.bookshelf.web.controller;

import bg.softuni.bookshelf.service.auth.CustomUserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.Collections;
import java.util.UUID;

public class WithMockApplicationUserSecurityContextFactory implements WithSecurityContextFactory<WithMockApplicationUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockApplicationUser annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        CustomUserDetails principal = new CustomUserDetails(
                UUID.randomUUID(),
                annotation.username(),
                "password",
                true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        Authentication auth = new UsernamePasswordAuthenticationToken(principal, "password", principal.getAuthorities());
        context.setAuthentication(auth);
        return context;
    }
}
