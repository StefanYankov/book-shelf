package bg.softuni.bookshelf.shared.infrastructure.filestorage.image;

/**
 * A standard record to hold the result of a successful image upload.
 *
 * @param url      The public URL of the uploaded image.
 * @param publicId The unique identifier for the image in the external storage service,
 *                 required for future management (e.g., deletion).
 */
public record UploadResult(String url, String publicId) {
}
