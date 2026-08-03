package bg.softuni.bookshelf.data.repository;

import bg.softuni.bookshelf.data.entity.identity.TokenType;
import bg.softuni.bookshelf.data.entity.identity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {

    /**
     * Looks up a verification token by its SHA-256 hash.
     */
    Optional<VerificationToken> findByTokenHash(String tokenHash);

    /**
     * Finds the most recently issued token for a specific user and type.
     * This is useful if a user requested multiple password resets and we only want to honor the newest one.
     */
    Optional<VerificationToken> findFirstByUser_IdAndTokenTypeOrderByCreatedAtDesc(UUID userId, TokenType tokenType);

    /**
     * Bulk-deletes all tokens whose expiry has passed. Used by the scheduled token-purge job to
     * remove dead single-use tokens that will never be redeemed again.
     * <p>
     * Every token carries a non-null {@code expiryDate} (set at issuance), so this predicate is
     * unambiguous — there is no "permanent" token concept here, unlike account locks.
     *
     * @param now the current instant; tokens expiring strictly before this are removed.
     * @return the number of tokens deleted.
     */
    @Modifying
    @Query("DELETE FROM VerificationToken t WHERE t.expiryDate < :now")
    int deleteByExpiryDateBefore(@Param("now") Instant now);
}