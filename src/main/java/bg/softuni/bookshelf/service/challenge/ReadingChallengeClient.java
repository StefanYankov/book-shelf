package bg.softuni.bookshelf.service.challenge;

import bg.softuni.bookshelf.service.challenge.dto.LogProgressDto;
import bg.softuni.bookshelf.service.challenge.dto.ReadingChallengeCreateDto;
import bg.softuni.bookshelf.service.challenge.dto.ReadingChallengeViewDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Feign client for the reading challenge microservice. Method signatures mirror the
 * microservice's REST contract; the base URL is supplied by configuration.
 */
@FeignClient(name = "reading-challenge", url = "${reading-challenge.service.url}")
public interface ReadingChallengeClient {

    @PostMapping("/api/challenges")
    ReadingChallengeViewDto createChallenge(@RequestBody ReadingChallengeCreateDto createDto);

    @PutMapping("/api/challenges/{challengeId}/progress")
    ReadingChallengeViewDto logProgress(@PathVariable("challengeId") UUID challengeId,
                                        @RequestBody LogProgressDto progressDto);

    @GetMapping("/api/challenges")
    ReadingChallengeViewDto getChallenge(@RequestParam("year") int year);
}