package bg.softuni.bookshelf.shared.infrastructure.filestorage.image;

import org.springframework.web.multipart.MultipartFile;

/**
 * Defines the contract for a service that handles image uploads to an external storage provider.
 * This abstraction decouples the application's business logic from any specific
 * cloud storage implementation (e.g., Cloudinary, AWS S3).
 */
public interface ImageUploadService {

    /**
     * Uploads an image file to the external storage provider.
     *
     * @param file The image file to upload.
     * @return An {@link UploadResult} containing the public URL and unique ID of the uploaded image.
     */
    UploadResult uploadImage(MultipartFile file);

    /**
     * Deletes an image from the external storage provider using its public ID.
     *
     * @param publicId The unique public identifier of the image to delete.
     */
    void deleteImage(String publicId);
}
