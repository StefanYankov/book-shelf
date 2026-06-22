/**
 * This package contains all Spring Data JPA repository interfaces for the application.
 * By annotating this package with @NonNullApi, we are opting into Spring's null-safety contract.
 * This ensures that all repository methods will never return null for single-value results
 * (they will use Optional instead) and that all method parameters are treated as non-null by default.
 */
@org.springframework.lang.NonNullApi
package bg.softuni.bookshelf.data.repository;
