package bg.softuni.bookshelf.service.challenge;

import bg.softuni.bookshelf.service.challenge.dto.LogProgressDto;
import bg.softuni.bookshelf.service.challenge.dto.ReadingChallengeCreateDto;
import bg.softuni.bookshelf.service.challenge.dto.ReadingChallengeViewDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Application-facing service for reading challenges, delegating to the reading challenge
 * microservice through a Feign client. The caller's JWT is propagated by the Feign interceptor,
 * so the microservice resolves the same user.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReadingChallengeProxyService {

    private final ReadingChallengeClient readingChallengeClient;

    /**
     * Creates a reading challenge for the current user via the microservice.
     */
    public ReadingChallengeViewDto createChallenge(ReadingChallengeCreateDto createDto) {
        log.info("Proxying create-challenge request to the reading challenge service (year {})", createDto.year());
        return readingChallengeClient.createChallenge(createDto);
    }

    /**
     * Logs reading progress against a challenge via the microservice.
     */
    public ReadingChallengeViewDto logProgress(UUID challengeId, LogProgressDto progressDto) {
        log.info("Proxying log-progress request for challenge {} to the reading challenge service", challengeId);
        return readingChallengeClient.logProgress(challengeId, progressDto);
    }

    /**
     * Retrieves the current user's challenge for a given year via the microservice.
     */
    public ReadingChallengeViewDto getChallenge(int year) {
        log.info("Proxying get-challenge request (year {}) to the reading challenge service", year);
        return readingChallengeClient.getChallenge(year);
    }
}