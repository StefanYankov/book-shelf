package bg.softuni.bookshelf.service.book;

import bg.softuni.bookshelf.shared.infrastructure.filestorage.image.BookImageDeletionEvent;
import bg.softuni.bookshelf.shared.infrastructure.filestorage.image.ImageUploadService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookImageCleanupListener Unit Tests")
class BookImageCleanupListenerTest {

    @Mock
    private ImageUploadService imageUploadService;

    @InjectMocks
    private BookImageCleanupListener listener;

    @Test
    @DisplayName("Delegates the event's public id to the image service for deletion")
    void shouldDeleteImageFromEvent() {
        // Act
        listener.onBookImageDeletion(new BookImageDeletionEvent("books/x"));

        // Assert
        verify(imageUploadService).deleteImage("books/x");
    }
}