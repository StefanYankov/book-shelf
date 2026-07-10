package bg.softuni.bookshelf.data.repository;

import bg.softuni.bookshelf.config.JpaConfig;
import bg.softuni.bookshelf.data.entity.identity.AccountStatusEvent;
import bg.softuni.bookshelf.data.entity.identity.ApplicationUser;
import bg.softuni.bookshelf.data.enums.StatusEventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@Import(JpaConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AccountStatusEventRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:17-alpine"));

    @Autowired
    private AccountStatusEventRepository accountStatusEventRepository;

    @Autowired
    private UserRepository userRepository;

    private ApplicationUser testUser;
    private ApplicationUser adminUser;

    @BeforeEach
    void setUp() {
        accountStatusEventRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        testUser = new ApplicationUser();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        userRepository.save(testUser);

        adminUser = new ApplicationUser();
        adminUser.setUsername("adminuser");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("password");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        userRepository.save(adminUser);
    }

    @Nested
    @DisplayName("findMostRecentEventForUser Tests")
    class FindMostRecentEventForUserTests {

        @Test
        @DisplayName("Happy Path: Should return the latest event with actor fetched")
        void shouldReturnLatestEvent() throws InterruptedException {
            // Arrange
            AccountStatusEvent oldEvent = new AccountStatusEvent();
            oldEvent.setUser(testUser);
            oldEvent.setActor(adminUser);
            oldEvent.setEventType(StatusEventType.ACCOUNT_LOCKED);
            accountStatusEventRepository.save(oldEvent);

            Thread.sleep(10);

            AccountStatusEvent newEvent = new AccountStatusEvent();
            newEvent.setUser(testUser);
            newEvent.setActor(adminUser);
            newEvent.setEventType(StatusEventType.ACCOUNT_UNLOCKED);
            accountStatusEventRepository.save(newEvent);

            // Act
            List<AccountStatusEvent> result = accountStatusEventRepository.findMostRecentEventForUser(testUser.getId(), PageRequest.of(0, 1));

            // Assert
            assertThat(result).hasSize(1);
            AccountStatusEvent foundEvent = result.getFirst();
            assertThat(foundEvent.getEventType()).isEqualTo(StatusEventType.ACCOUNT_UNLOCKED);
            assertThat(foundEvent.getActor()).isNotNull();
            assertThat(foundEvent.getActor().getUsername()).isEqualTo("adminuser");
        }

        @Test
        @DisplayName("Edge Case: Should return an empty list when user has no events")
        void shouldReturnEmptyList_WhenUserHasNoEvents() {
            // Act
            List<AccountStatusEvent> result = accountStatusEventRepository.findMostRecentEventForUser(testUser.getId(), PageRequest.of(0, 1));

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Edge Case: Should not return events for other users")
        void shouldNotReturnEvents_ForOtherUsers() {
            // Arrange
            ApplicationUser otherUser = new ApplicationUser();
            otherUser.setUsername("otheruser");
            otherUser.setEmail("other@example.com");
            otherUser.setPassword("password");
            otherUser.setFirstName("Other");
            otherUser.setLastName("User");
            userRepository.save(otherUser);

            AccountStatusEvent otherUserEvent = new AccountStatusEvent();
            otherUserEvent.setUser(otherUser);
            otherUserEvent.setActor(adminUser);
            otherUserEvent.setEventType(StatusEventType.ACCOUNT_LOCKED);
            accountStatusEventRepository.save(otherUserEvent);

            // Act
            List<AccountStatusEvent> result = accountStatusEventRepository.findMostRecentEventForUser(testUser.getId(), PageRequest.of(0, 1));

            // Assert
            assertThat(result).isEmpty();
        }
    }
}
