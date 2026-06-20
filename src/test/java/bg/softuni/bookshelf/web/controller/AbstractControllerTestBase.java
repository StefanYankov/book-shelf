package bg.softuni.bookshelf.web.controller;

import bg.softuni.bookshelf.config.ApplicationConfig;
import bg.softuni.bookshelf.config.SecurityConfig;
import bg.softuni.bookshelf.data.repository.UserRepository;
import bg.softuni.bookshelf.service.auth.AuthenticationService;
import bg.softuni.bookshelf.service.auth.JwtService;
import bg.softuni.bookshelf.service.book.BookService;
import bg.softuni.bookshelf.service.bookshelf.BookshelfService;
import bg.softuni.bookshelf.web.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest
@ActiveProfiles("test")
@Import({SecurityConfig.class, ApplicationConfig.class, GlobalExceptionHandler.class})
public abstract class AbstractControllerTestBase {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    // --- Mock Beans ---

    @MockitoBean
    protected AuthenticationService authenticationService;

    @MockitoBean
    protected JwtService jwtService;

    @MockitoBean
    protected UserDetailsService userDetailsService;

    @MockitoBean
    protected UserRepository userRepository;

    @MockitoBean
    protected BookService bookService;

    @MockitoBean
    protected BookshelfService bookshelfService;
}
