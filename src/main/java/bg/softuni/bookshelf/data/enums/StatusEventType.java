package bg.softuni.bookshelf.data.enums;

/**
 * Defines the types of events that can affect a user's account status.
 * This provides a clear, auditable trail of administrative actions.
 */
public enum StatusEventType {
    ACCOUNT_LOCKED,
    ACCOUNT_UNLOCKED,
    ACCOUNT_BANNED,
    ACCOUNT_UNBANNED
}
