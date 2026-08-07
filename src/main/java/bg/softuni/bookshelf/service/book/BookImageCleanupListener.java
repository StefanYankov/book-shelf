package bg.softuni.bookshelf.service.book;

import bg.softuni.bookshelf.shared.infrastructure.filestorage.image.BookImageDeletionEvent;
import bg.softuni.bookshelf.shared.infrastructure.filestorage.image.ImageUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Deletes a book's cover image after the book's removal has committed.
 * <p>
 * The listener runs on the {@code AFTER_COMMIT} phase, so the image is deleted only when the book
 * removal actually succeeded — a rolled-back deletion never destroys a live book's image. It runs
 * asynchronously so the provider call does not block the request thread, and deletion is best-effort:
 * a failed remote delete is handled within the image service and does not surface to the user.
 */
@Component
@RequiredArgsConstructor
public class BookImageCleanupListener {

    private final ImageUploadService imageUploadService;

    /**
     * Deletes the image identified by the event once the surrounding transaction has committed.
     *
     * @param event the deletion event carrying the image's public id.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBookImageDeletion(BookImageDeletionEvent event) {
        imageUploadService.deleteImage(event.publicId());
    }
}