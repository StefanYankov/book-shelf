package bg.softuni.bookshelf.service.user;

import bg.softuni.bookshelf.data.entity.identity.AccountStatusEvent;
import bg.softuni.bookshelf.data.enums.StatusEventType;
import bg.softuni.bookshelf.data.repository.AccountStatusEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

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

        AccountStatusEvent latest = latestEvents.getFirst();
        StatusEventType type = latest.getEventType();

        // A temporary lock whose expiry has passed is treated as active immediately, without waiting
        // for the reconciliation job. Read-side correctness: the moment the window elapses, the user
        // is effectively active. A null expiry is a PERMANENT lock and never satisfies this — it stays
        // locked until an explicit unlock event supersedes it.
        if (type == StatusEventType.ACCOUNT_LOCKED
                && latest.getExpiryDate() != null
                && latest.getExpiryDate().isBefore(Instant.now())) {
            return true;
        }

        return type == StatusEventType.ACCOUNT_UNLOCKED || type == StatusEventType.ACCOUNT_UNBANNED;
    }
}