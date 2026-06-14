package bg.softuni.bookshelf.data.entity.value;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A reusable, embeddable object representing an image stored in Cloudinary.
 * This object is flattened into the owning entity's table.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class Image {

    /**
     * The full, public URL of the image. Used for display purposes.
     */
    @Column(name = "image_url")
    private String url;

    /**
     * The unique public ID of the image in Cloudinary.
     * Essential for managing the image (updates, deletions).
     */
    @Column(name = "image_public_id")
    private String publicId;
}
