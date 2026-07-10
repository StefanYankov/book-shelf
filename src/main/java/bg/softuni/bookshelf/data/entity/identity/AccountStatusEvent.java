package bg.softuni.bookshelf.data.entity.identity;

import bg.softuni.bookshelf.data.entity.base.BaseUUIDEntity;
import bg.softuni.bookshelf.data.enums.StatusEventType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Represents a single, auditable event in a user's account lifecycle.
 * This entity creates an immutable log of actions such as locking, banning, or unlocking an account,
 * replacing a simple boolean flag with a full audit trail.
 */
@Entity
@Table(name = "account_status_events")
@Getter
@Setter
public class AccountStatusEvent extends BaseUUIDEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusEventType eventType;

    @Column(length = 500)
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;

    private Instant expiryDate;
}
