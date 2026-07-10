package bg.softuni.bookshelf.data.repository;

import bg.softuni.bookshelf.data.entity.Book;
import bg.softuni.bookshelf.data.enums.BookFormat;
import bg.softuni.bookshelf.service.book.dto.BookSearchFilters;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Utility specification builder for constructing dynamic catalog search criteria.
 * Enforces transaction safety and prevents query execution anomalies.
 */
public final class BookSpecification {

    private BookSpecification() {}

    /**
     * Builds a JPA Specification predicate chain from a BookSearchFilters object.
     *
     * @param filters An object containing all nullable search criteria.
     * @return A JPA Specification for the Book entity.
     */
    public static Specification<Book> filterCatalog(BookSearchFilters filters) {
        return (root, criteriaQuery, cb) -> {
            executeEagerFetchJoin(root, criteriaQuery);

            List<Predicate> predicates = new ArrayList<>();

            addTextSearchPredicate(predicates, root, cb, filters.query());
            addGenreFilterPredicate(predicates, root, filters.genres());
            addFormatPredicate(predicates, root, cb, filters.format());
            addYearPublishedPredicates(predicates, root, cb, filters.yearMin(), filters.yearMax());

            enforceDistinctQuery(criteriaQuery);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static void executeEagerFetchJoin(Root<Book> root, @Nullable CriteriaQuery<?> criteriaQuery) {
        if (criteriaQuery != null && Long.class != criteriaQuery.getResultType()) {
            root.fetch("author", JoinType.INNER);
        }
    }

    private static void enforceDistinctQuery(@Nullable CriteriaQuery<?> criteriaQuery) {
        if (criteriaQuery != null) {
            criteriaQuery.distinct(true);
        }
    }

    private static void addTextSearchPredicate(
            List<Predicate> predicates,
            Root<Book> root,
            CriteriaBuilder cb,
            @Nullable String query
    ) {
        if (query != null && !query.isBlank()) {
            String wildCard = new StringBuilder("%")
                    .append(query.trim().toLowerCase())
                    .append("%")
                    .toString();
            Predicate titleMatch = cb.like(cb.lower(root.get("title")), wildCard);
            Predicate authorMatch = cb.like(cb.lower(root.get("author").get("name")), wildCard);
            predicates.add(cb.or(titleMatch, authorMatch));
        }
    }

    private static void addGenreFilterPredicate(
            List<Predicate> predicates,
            Root<Book> root,
            @Nullable Set<String> genreNames
    ) {
        if (genreNames != null && !genreNames.isEmpty()) {
            predicates.add(root.join("genres").get("name").in(genreNames));
        }
    }

    private static void addFormatPredicate(
            List<Predicate> predicates,
            Root<Book> root,
            CriteriaBuilder cb,
            @Nullable BookFormat format
    ) {
        if (format != null) {
            predicates.add(cb.equal(root.get("format"), format));
        }
    }

    private static void addYearPublishedPredicates(
            List<Predicate> predicates,
            Root<Book> root,
            CriteriaBuilder cb,
            @Nullable Integer yearMin,
            @Nullable Integer yearMax
    ) {
        if (yearMin != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("yearPublished"), yearMin));
        }
        if (yearMax != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("yearPublished"), yearMax));
        }
    }
}
