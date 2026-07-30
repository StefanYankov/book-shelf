package bg.softuni.bookshelf.web.controller;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Custom security test annotation enhanced to support role-based authorization scenarios.
 * Declares a dynamic "roles" attribute that maps automatically to granted authorities inside
 * the security context factory. Also supports fine-grained "permissions" that are mapped to
 * authorities alongside the roles.
 */
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockApplicationUserSecurityContextFactory.class)
public @interface WithMockApplicationUser {

    String username() default "testuser";

    String email() default "test@example.com";

    String[] roles() default {"USER"};

    String[] permissions() default {};

    boolean passwordChangeRequired() default false;
}