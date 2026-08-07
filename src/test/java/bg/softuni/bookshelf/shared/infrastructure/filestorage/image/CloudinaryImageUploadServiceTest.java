package bg.softuni.bookshelf.shared.infrastructure.filestorage.image;

import bg.softuni.bookshelf.shared.exception.BusinessException;
import bg.softuni.bookshelf.shared.exception.ErrorCode;
import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("CloudinaryImageUploadService Unit Tests")
class CloudinaryImageUploadServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @InjectMocks
    private CloudinaryImageUploadService service;

    private MockMultipartFile sampleFile() {
        return new MockMultipartFile("image", "cover.jpg", "image/jpeg", new byte[]{1, 2, 3});
    }

    @Test
    @DisplayName("uploadImage returns the secure URL and public id from the Cloudinary response")
    void uploadImage_shouldReturnUrlAndPublicId() throws IOException {
        // Arrange
        given(cloudinary.uploader()).willReturn(uploader);
        given(uploader.upload(any(), any())).willReturn(Map.of(
                "secure_url", "https://res.cloudinary.com/demo/image/upload/books/cover.jpg",
                "public_id", "books/cover"
        ));

        // Act
        UploadResult result = service.uploadImage(sampleFile());

        // Assert
        assertThat(result.url()).isEqualTo("https://res.cloudinary.com/demo/image/upload/books/cover.jpg");
        assertThat(result.publicId()).isEqualTo("books/cover");
    }

    @Test
    @DisplayName("uploadImage throws IMAGE_UPLOAD_FAILED when Cloudinary raises an IOException")
    void uploadImage_shouldThrowOnFailure() throws IOException {
        // Arrange
        given(cloudinary.uploader()).willReturn(uploader);
        given(uploader.upload(any(), any())).willThrow(new IOException("network down"));

        // Act & Assert
        assertThatThrownBy(() -> service.uploadImage(sampleFile()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.IMAGE_UPLOAD_FAILED);
    }

    @Test
    @DisplayName("deleteImage calls destroy with the given public id")
    void deleteImage_shouldCallDestroy() throws IOException {
        // Arrange
        given(cloudinary.uploader()).willReturn(uploader);
        given(uploader.destroy(any(), any())).willReturn(Map.of("result", "ok"));

        // Act
        service.deleteImage("books/cover");

        // Assert
        verify(uploader).destroy(any(), any());
    }

    @Test
    @DisplayName("deleteImage swallows IOException so a failed remote delete does not propagate")
    void deleteImage_shouldNotThrowOnFailure() throws IOException {
        // Arrange
        given(cloudinary.uploader()).willReturn(uploader);
        given(uploader.destroy(any(), any())).willThrow(new IOException("network down"));

        // Act & Assert
        service.deleteImage("books/cover");
        verify(uploader).destroy(any(), any());
    }
}