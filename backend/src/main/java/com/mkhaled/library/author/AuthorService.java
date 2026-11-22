package com.mkhaled.library.author;

import com.mkhaled.library.book.BookRepository;
import com.mkhaled.library.common.StablePaging;
import com.mkhaled.library.common.exception.BusinessRuleException;
import com.mkhaled.library.common.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;

    public AuthorService(AuthorRepository authorRepository, BookRepository bookRepository) {
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
    }

    public Page<AuthorResponse> search(String name, Pageable pageable) {
        Pageable stable = StablePaging.withIdTiebreaker(pageable);
        Page<Author> authors = (name == null || name.isBlank())
                ? authorRepository.findAll(stable)
                : authorRepository.findByNameContainingIgnoreCase(name.strip(), stable);
        return authors.map(AuthorResponse::from);
    }

    public AuthorResponse get(Long id) {
        return AuthorResponse.from(findAuthor(id));
    }

    @Transactional
    public AuthorResponse create(AuthorRequest request) {
        Author author = authorRepository.save(new Author(request.name().strip()));
        return AuthorResponse.from(author);
    }

    @Transactional
    public AuthorResponse update(Long id, AuthorRequest request) {
        Author author = findAuthor(id);
        author.rename(request.name().strip());
        return AuthorResponse.from(author);
    }

    @Transactional
    public void delete(Long id) {
        if (!authorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Author", id);
        }
        if (bookRepository.existsByAuthorId(id)) {
            throw new BusinessRuleException("Author " + id + " still has books and cannot be deleted");
        }
        authorRepository.deleteById(id);
    }

    private Author findAuthor(Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author", id));
    }
}
