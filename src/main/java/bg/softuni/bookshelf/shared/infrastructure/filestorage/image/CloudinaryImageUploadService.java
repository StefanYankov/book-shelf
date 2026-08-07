package bg.softuni.bookshelf.shared.infrastructure.filestorage.image;

import bg.softuni.bookshelf.shared.exception.BusinessException;
import bg.softuni.bookshelf.shared.exception.ErrorCode;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * Cloudinary-backed {@link ImageUploadService}. Active only when {@code cloudinary.enabled=true},
 * so it coexists with the no-op implementation and is selected by configuration.
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "cloudinary.enabled", havingValue = "true")
@RequiredArgsConstructor
public class CloudinaryImageUploadService implements ImageUploadService {

    private final Cloudinary cloudinary;

    @Override
    public UploadResult uploadImage(MultipartFile file) {
        try {
            Map<?, ?> response = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            String url = (String) response.get("secure_url");
            String publicId = (String) response.get("public_id");
            log.info("Uploaded image to Cloudinary with public id {}", publicId);
            return new UploadResult(url, publicId);
        } catch (IOException e) {
            log.error("Cloudinary image upload failed", e);
            throw new BusinessException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }

    @Override
    public void deleteImage(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Deleted image {} from Cloudinary", publicId);
        } catch (IOException e) {
            // Best-effort: a failed remote delete must not break the calling operation. The orphaned
            // asset can be reconciled later (see the orphan-cleanup backlog item).
            log.warn("Failed to delete image {} from Cloudinary", publicId, e);
        }
    }
}