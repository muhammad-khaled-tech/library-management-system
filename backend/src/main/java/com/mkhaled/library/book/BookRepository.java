package com.mkhaled.library.book;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Long> {

    // Load the author in the same query so a page of books does not trigger N+1 selects
    @Override
    @EntityGraph(attributePaths = "author")
    Page<Book> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "author")
    @Query("""
            select b from Book b
            where lower(b.title) like lower(concat('%', :query, '%'))
               or lower(b.author.name) like lower(concat('%', :query, '%'))
               or b.isbn = :query
            """)
    Page<Book> search(@Param("query") String query, Pageable pageable);

    boolean existsByIsbn(String isbn);

    boolean existsByAuthorId(Long authorId);
}
