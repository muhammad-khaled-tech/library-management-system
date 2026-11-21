package com.mkhaled.library.author;

import com.mkhaled.library.book.BookRepository;
import com.mkhaled.library.common.exception.BusinessRuleException;
import com.mkhaled.library.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
class AuthorServiceTest {

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private AuthorService authorService;

    private final Pageable firstPage = PageRequest.of(0, 20);

    @Test
    void searchFiltersByNameWhenGiven() {
        Page<Author> page = new PageImpl<>(List.of(author(1L, "Naguib Mahfouz")));
        when(authorRepository.findByNameContainingIgnoreCase("mahf", firstPage)).thenReturn(page);

        Page<AuthorResponse> result = authorService.search("mahf", firstPage);

        assertThat(result.getContent()).containsExactly(new AuthorResponse(1L, "Naguib Mahfouz"));
    }

    @Test
    void searchReturnsAllAuthorsWhenNameIsBlank() {
        when(authorRepository.findAll(firstPage)).thenReturn(Page.empty());

        authorService.search("  ", firstPage);

        verify(authorRepository).findAll(firstPage);
    }

    @Test
    void getReturnsAuthor() {
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author(1L, "Taha Hussein")));

        assertThat(authorService.get(1L)).isEqualTo(new AuthorResponse(1L, "Taha Hussein"));
    }

    @Test
    void getThrowsWhenAuthorDoesNotExist() {
        when(authorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authorService.get(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Author with id 99 not found");
    }

    @Test
    void createSavesAuthor() {
        when(authorRepository.save(any(Author.class)))
                .thenAnswer(invocation -> withId(invocation.getArgument(0), 5L));

        AuthorResponse created = authorService.create(new AuthorRequest("Tawfiq al-Hakim"));

        assertThat(created).isEqualTo(new AuthorResponse(5L, "Tawfiq al-Hakim"));
    }

    @Test
    void updateRenamesAuthor() {
        Author existing = author(1L, "Old Name");
        when(authorRepository.findById(1L)).thenReturn(Optional.of(existing));

        AuthorResponse updated = authorService.update(1L, new AuthorRequest("New Name"));

        assertThat(updated.name()).isEqualTo("New Name");
        assertThat(existing.getName()).isEqualTo("New Name");
    }

    @Test
    void deleteRemovesAuthorWithoutBooks() {
        when(authorRepository.existsById(1L)).thenReturn(true);
        when(bookRepository.existsByAuthorId(1L)).thenReturn(false);

        authorService.delete(1L);

        verify(authorRepository).deleteById(1L);
    }

    @Test
    void deleteRejectsAuthorWhoHasBooks() {
        when(authorRepository.existsById(1L)).thenReturn(true);
        when(bookRepository.existsByAuthorId(1L)).thenReturn(true);

        assertThatThrownBy(() -> authorService.delete(1L))
                .isInstanceOf(BusinessRuleException.class);
        verify(authorRepository, never()).deleteById(1L);
    }

    @Test
    void deleteThrowsWhenAuthorDoesNotExist() {
        when(authorRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> authorService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private static Author author(Long id, String name) {
        return withId(new Author(name), id);
    }

    private static Author withId(Author author, Long id) {
        ReflectionTestUtils.setField(author, "id", id);
        return author;
    }
}
