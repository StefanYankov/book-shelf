package bg.softuni.bookshelf.data.entity;

import bg.softuni.bookshelf.data.entity.base.BaseUUIDEntity;
import bg.softuni.bookshelf.shared.ValidationConstants;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "reviews", uniqueConstraints = {
        @UniqueConstraint(name = "uk_reviews_user_target", columnNames = {"user_id", "target_id", "target_type"})
})
public class Review extends BaseUUIDEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    @Column(name = "target_type", nullable = false, length = ValidationConstants.Review.TARGET_TYPE_MAX_LENGTH)
    private String targetType;

    @Column(nullable = false)
    private Integer rating;

    @Column(length = ValidationConstants.Review.MAX_TITLE_LENGTH)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String comment;
}
