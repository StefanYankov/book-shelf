package bg.softuni.bookshelf.web.controller;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockApplicationUserSecurityContextFactory.class)
public @interface WithMockApplicationUser {

    String username() default "testuser";

    String email() default "test@example.com";
}
