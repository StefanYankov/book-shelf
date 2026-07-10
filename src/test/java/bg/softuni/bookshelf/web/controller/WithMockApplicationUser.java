package bg.softuni.bookshelf.web.controller;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Custom security test annotation enhanced to support role-based authorization scenarios.
 * Declares a dynamic "roles" attribute that maps automatically to granted authorities inside
 * the security context factory.
 */
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockApplicationUserSecurityContextFactory.class)
public @interface WithMockApplicationUser {

    String username() default "testuser";

    String email() default "test@example.com";

    String[] roles() default {"USER"};

    boolean passwordChangeRequired() default false;
}