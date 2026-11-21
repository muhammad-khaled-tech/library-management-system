package com.mkhaled.library.book;

import com.mkhaled.library.author.Author;
import com.mkhaled.library.support.TestcontainersConfiguration;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Author mahfouz;

    @BeforeEach
    void setUp() {
        mahfouz = entityManager.persist(new Author("Naguib Mahfouz"));
        Author hussein = entityManager.persist(new Author("Taha Hussein"));
        entityManager.persist(new Book("9780306406157", "Palace Walk", 1956, 3, mahfouz));
        entityManager.persist(new Book("9781402894626", "Sugar Street", 1957, 2, mahfouz));
        entityManager.persist(new Book("9780140449136", "The Days", 1929, 1, hussein));
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void searchMatchesTitleIgnoringCase() {
        Page<Book> result = bookRepository.search("PALACE", PageRequest.of(0, 10));

        assertThat(result.getContent()).extracting(Book::getTitle).containsExactly("Palace Walk");
    }

    @Test
    void searchMatchesAuthorName() {
        Page<Book> result = bookRepository.search("mahfouz", PageRequest.of(0, 10, Sort.by("title")));

        assertThat(result.getContent()).extracting(Book::getTitle).containsExactly("Palace Walk", "Sugar Street");
    }

    @Test
    void searchMatchesExactIsbn() {
        Page<Book> result = bookRepository.search("9780140449136", PageRequest.of(0, 10));

        assertThat(result.getContent()).extracting(Book::getTitle).containsExactly("The Days");
    }

    @Test
    void searchPaginatesAndCountsAllMatches() {
        Page<Book> result = bookRepository.search("mahfouz", PageRequest.of(1, 1, Sort.by("title")));

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).extracting(Book::getTitle).containsExactly("Sugar Street");
    }

    @Test
    void findAllLoadsAuthorInTheSameQuery() {
        Page<Book> result = bookRepository.findAll(PageRequest.of(0, 10));

        assertThat(result.getContent())
                .allSatisfy(book -> assertThat(Hibernate.isInitialized(book.getAuthor())).isTrue());
    }

    @Test
    void databaseRejectsDuplicateIsbn() {
        Book duplicate = new Book("9780306406157", "Another Copy", 1960, 1, mahfouz);

        assertThatThrownBy(() -> bookRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
