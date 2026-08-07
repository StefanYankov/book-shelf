package bg.softuni.bookshelf.shared.infrastructure.filestorage.image;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("NoOpImageUploadService Unit Tests")
class NoOpImageUploadServiceTest {

    private final NoOpImageUploadService service = new NoOpImageUploadService();

    @Test
    @DisplayName("uploadImage returns a placeholder result without any network call")
    void uploadImage_returnsPlaceholder() {
        // Act
        UploadResult result = service.uploadImage(
                new MockMultipartFile("image", "x.jpg", "image/jpeg", new byte[]{1}));

        // Assert
        assertThat(result.url()).contains("placehold.co");
        assertThat(result.publicId()).isEqualTo("noop");
    }

    @Test
    @DisplayName("deleteImage is a no-op and does not throw")
    void deleteImage_isNoOp() {
        // Act & Assert
        service.deleteImage("anything");
    }
}