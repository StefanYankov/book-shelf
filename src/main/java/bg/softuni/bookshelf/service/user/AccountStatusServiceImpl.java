package bg.softuni.bookshelf.service.user;

import bg.softuni.bookshelf.data.entity.identity.AccountStatusEvent;
import bg.softuni.bookshelf.data.enums.StatusEventType;
import bg.softuni.bookshelf.data.repository.AccountStatusEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AccountStatusServiceImpl implements AccountStatusService {

    private final AccountStatusEventRepository accountStatusEventRepository;

    @Override
    @Transactional(readOnly = true)
    public boolean isUserActive(UUID userId) {
        List<AccountStatusEvent> latestEvents = accountStatusEventRepository.findMostRecentEventForUser(userId, PageRequest.of(0, 1));
        if (latestEvents.isEmpty()) {
            return true;
        }
        return isActiveGivenLatest(latestEvents.getFirst());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, Boolean> getActiveStatus(Collection<UUID> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }

        Map<UUID, AccountStatusEvent> latestByUser = new HashMap<>();
        for (AccountStatusEvent event : accountStatusEventRepository.findAllByUserIds(userIds)) {
            latestByUser.merge(event.getUser().getId(), event,
                    (existing, candidate) -> candidate.getCreatedAt().isAfter(existing.getCreatedAt()) ? candidate : existing);
        }

        Map<UUID, Boolean> result = new HashMap<>();
        for (UUID userId : userIds) {
            AccountStatusEvent latest = latestByUser.get(userId);
            result.put(userId, latest == null || isActiveGivenLatest(latest));
        }
        return result;
    }

    // Determines active status from a user's most recent status event. A temporary lock whose expiry
    // has passed counts as active immediately (read-side), before the reconciliation job runs; a
    // permanent lock (null expiry) never does.
    private boolean isActiveGivenLatest(AccountStatusEvent latest) {
        StatusEventType type = latest.getEventType();
        if (type == StatusEventType.ACCOUNT_LOCKED
                && latest.getExpiryDate() != null
                && latest.getExpiryDate().isBefore(Instant.now())) {
            return true;
        }
        return type == StatusEventType.ACCOUNT_UNLOCKED || type == StatusEventType.ACCOUNT_UNBANNED;
    }
}