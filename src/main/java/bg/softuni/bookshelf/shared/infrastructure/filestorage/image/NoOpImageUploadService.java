package bg.softuni.bookshelf.shared.infrastructure.filestorage.image;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * No-op {@link ImageUploadService} used when Cloudinary is disabled ({@code cloudinary.enabled}
 * absent or false). It performs no network calls and returns a placeholder image, so the
 * application boots and functions without a Cloudinary account — for tests and local development.
 * <p>
 * {@code matchIfMissing = true} guarantees an {@link ImageUploadService} bean always exists (the
 * catalog service depends on it), so a missing toggle degrades to no-op rather than failing to start.
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "cloudinary.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpImageUploadService implements ImageUploadService {

    private static final String PLACEHOLDER_URL = "https://placehold.co/300x450?text=No+Image";
    private static final String PLACEHOLDER_PUBLIC_ID = "noop";

    @Override
    public UploadResult uploadImage(MultipartFile file) {
        log.info("Cloudinary disabled; skipping upload and returning a placeholder image.");
        return new UploadResult(PLACEHOLDER_URL, PLACEHOLDER_PUBLIC_ID);
    }

    @Override
    public void deleteImage(String publicId) {
        log.info("Cloudinary disabled; skipping delete for public id {}", publicId);
    }
}