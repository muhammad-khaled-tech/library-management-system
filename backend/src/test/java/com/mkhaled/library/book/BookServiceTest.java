package com.mkhaled.library.book;

import com.mkhaled.library.author.Author;
import com.mkhaled.library.author.AuthorRepository;
import com.mkhaled.library.common.exception.BusinessRuleException;
import com.mkhaled.library.common.exception.ResourceNotFoundException;
import com.mkhaled.library.loan.LoanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    private static final String ISBN = "9780306406157";
    private static final String OTHER_ISBN = "9780140449136";

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private LoanRepository loanRepository;

    @InjectMocks
    private BookService bookService;

    private final Author mahfouz = withId(new Author("Naguib Mahfouz"), 1L);
    private final Pageable firstPage = PageRequest.of(0, 20);
    // What the service passes on: the same page with id added as a tiebreaker sort
    private final Pageable firstPageById = PageRequest.of(0, 20, Sort.by("id"));

    @Test
    void searchMatchesQueryWhenGiven() {
        when(bookRepository.search("palace", firstPageById))
                .thenReturn(new PageImpl<>(List.of(book(10L, ISBN, 3))));

        Page<BookResponse> result = bookService.search(" palace ", firstPage);

        assertThat(result.getContent()).extracting(BookResponse::title).containsExactly("Palace Walk");
    }

    @Test
    void searchReturnsAllBooksWhenQueryIsBlank() {
        when(bookRepository.findAll(firstPageById)).thenReturn(Page.empty());

        bookService.search(null, firstPage);

        verify(bookRepository).findAll(firstPageById);
    }

    @Test
    void getThrowsWhenBookDoesNotExist() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.get(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Book with id 99 not found");
    }

    @Test
    void createStoresIsbnWithoutSeparators() {
        when(bookRepository.existsByIsbn(ISBN)).thenReturn(false);
        when(authorRepository.findById(1L)).thenReturn(Optional.of(mahfouz));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookResponse created = bookService.create(new BookRequest("978-0-306-40615-7", "Palace Walk", 1956, 3, 1L));

        assertThat(created.isbn()).isEqualTo(ISBN);
        assertThat(created.availableCopies()).isEqualTo(3);
        assertThat(created.authorName()).isEqualTo("Naguib Mahfouz");
    }

    @Test
    void createRejectsDuplicateIsbn() {
        when(bookRepository.existsByIsbn(ISBN)).thenReturn(true);

        assertThatThrownBy(() -> bookService.create(new BookRequest(ISBN, "Palace Walk", 1956, 3, 1L)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("A book with ISBN " + ISBN + " already exists");
        verify(bookRepository, never()).save(any());
    }

    @Test
    void createThrowsWhenAuthorDoesNotExist() {
        when(bookRepository.existsByIsbn(ISBN)).thenReturn(false);
        when(authorRepository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.create(new BookRequest(ISBN, "Palace Walk", 1956, 3, 7L)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Author with id 7 not found");
    }

    @Test
    void updateChangesDetailsAndKeepsCopiesOnLoan() {
        Book existing = book(10L, ISBN, 3);
        existing.checkOut();
        existing.checkOut();
        when(bookRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(authorRepository.findById(1L)).thenReturn(Optional.of(mahfouz));

        BookResponse updated = bookService.update(10L, new BookRequest(ISBN, "Palace of Desire", 1957, 5, 1L));

        assertThat(updated.title()).isEqualTo("Palace of Desire");
        assertThat(updated.totalCopies()).isEqualTo(5);
        assertThat(updated.availableCopies()).isEqualTo(3);
    }

    @Test
    void updateRejectsIsbnOfAnotherBook() {
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book(10L, ISBN, 3)));
        when(bookRepository.existsByIsbn(OTHER_ISBN)).thenReturn(true);

        assertThatThrownBy(() -> bookService.update(10L, new BookRequest(OTHER_ISBN, "Palace Walk", 1956, 3, 1L)))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void deleteRemovesBookWithoutLoans() {
        when(bookRepository.existsById(10L)).thenReturn(true);
        when(loanRepository.existsByBookId(10L)).thenReturn(false);

        bookService.delete(10L);

        verify(bookRepository).deleteById(10L);
    }

    @Test
    void deleteRejectsBookWithLoanHistory() {
        when(bookRepository.existsById(10L)).thenReturn(true);
        when(loanRepository.existsByBookId(10L)).thenReturn(true);

        assertThatThrownBy(() -> bookService.delete(10L))
                .isInstanceOf(BusinessRuleException.class);
        verify(bookRepository, never()).deleteById(10L);
    }

    private Book book(Long id, String isbn, int copies) {
        return withId(new Book(isbn, "Palace Walk", 1956, copies, mahfouz), id);
    }

    private static <T> T withId(T entity, Long id) {
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }
}
