package bg.softuni.bookshelf.service.user;

import bg.softuni.bookshelf.data.entity.identity.AccountStatusEvent;
import bg.softuni.bookshelf.data.enums.StatusEventType;
import bg.softuni.bookshelf.data.repository.AccountStatusEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        StatusEventType latestEventType = latestEvents.getFirst().getEventType();
        return latestEventType == StatusEventType.ACCOUNT_UNLOCKED || latestEventType == StatusEventType.ACCOUNT_UNBANNED;
    }
}
