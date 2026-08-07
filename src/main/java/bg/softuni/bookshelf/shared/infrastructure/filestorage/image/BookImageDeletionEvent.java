package bg.softuni.bookshelf.shared.infrastructure.filestorage.image;

/**
 * Published after a book is removed, carrying the public id of its cover image so the image can be
 * deleted from the storage provider once the removal has committed.
 *
 * @param publicId the storage provider's public identifier of the image to delete.
 */
public record BookImageDeletionEvent(String publicId) {

}