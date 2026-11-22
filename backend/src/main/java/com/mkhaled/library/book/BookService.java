package com.mkhaled.library.book;

import com.mkhaled.library.author.Author;
import com.mkhaled.library.author.AuthorRepository;
import com.mkhaled.library.common.StablePaging;
import com.mkhaled.library.common.exception.BusinessRuleException;
import com.mkhaled.library.common.exception.ResourceNotFoundException;
import com.mkhaled.library.loan.LoanRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final LoanRepository loanRepository;

    public BookService(BookRepository bookRepository,
                       AuthorRepository authorRepository,
                       LoanRepository loanRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.loanRepository = loanRepository;
    }

    public Page<BookResponse> search(String query, Pageable pageable) {
        Pageable stable = StablePaging.withIdTiebreaker(pageable);
        Page<Book> books = (query == null || query.isBlank())
                ? bookRepository.findAll(stable)
                : bookRepository.search(query.strip(), stable);
        return books.map(BookResponse::from);
    }

    public BookResponse get(Long id) {
        return BookResponse.from(findBook(id));
    }

    @Transactional
    public BookResponse create(BookRequest request) {
        String isbn = normalizeIsbn(request.isbn());
        ensureIsbnIsFree(isbn);
        Author author = findAuthor(request.authorId());

        Book book = new Book(isbn, request.title().strip(), request.publishedYear(), request.totalCopies(), author);
        return BookResponse.from(bookRepository.save(book));
    }

    @Transactional
    public BookResponse update(Long id, BookRequest request) {
        Book book = findBook(id);
        String isbn = normalizeIsbn(request.isbn());
        if (!isbn.equals(book.getIsbn())) {
            ensureIsbnIsFree(isbn);
        }
        Author author = findAuthor(request.authorId());

        book.updateDetails(isbn, request.title().strip(), request.publishedYear(), author);
        book.changeTotalCopies(request.totalCopies());
        return BookResponse.from(book);
    }

    @Transactional
    public void delete(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new ResourceNotFoundException("Book", id);
        }
        if (loanRepository.existsByBookId(id)) {
            throw new BusinessRuleException("Book " + id + " has loan history and cannot be deleted");
        }
        bookRepository.deleteById(id);
    }

    private void ensureIsbnIsFree(String isbn) {
        if (bookRepository.existsByIsbn(isbn)) {
            throw new BusinessRuleException("A book with ISBN " + isbn + " already exists");
        }
    }

    private Book findBook(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", id));
    }

    private Author findAuthor(Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author", id));
    }

    // @ISBN accepts hyphens and spaces; store digits only so "978-0-306-40615-7" and "9780306406157" match
    private static String normalizeIsbn(String isbn) {
        return isbn.replaceAll("\\D", "");
    }
}
