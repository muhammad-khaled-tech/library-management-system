package com.mkhaled.library.book;

import com.mkhaled.library.common.exception.BusinessRuleException;
import com.mkhaled.library.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
class BookControllerTest {

    private static final BookResponse PALACE_WALK =
            new BookResponse(10L, "9780306406157", "Palace Walk", 1956, 3, 3, 1L, "Naguib Mahfouz");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @Test
    void listSearchesAndSortsByTitleByDefault() throws Exception {
        PageRequest expectedPage = PageRequest.of(0, 20, Sort.by("title"));
        when(bookService.search("palace", expectedPage))
                .thenReturn(new PageImpl<>(List.of(PALACE_WALK), expectedPage, 1));

        mockMvc.perform(get("/api/books").param("q", "palace"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Palace Walk"))
                .andExpect(jsonPath("$.content[0].authorName").value("Naguib Mahfouz"))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    void getReturns404WhenBookIsMissing() throws Exception {
        when(bookService.get(99L)).thenThrow(new ResourceNotFoundException("Book", 99L));

        mockMvc.perform(get("/api/books/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Book with id 99 not found"));
    }

    @Test
    void createAcceptsHyphenatedIsbnAndReturns201() throws Exception {
        when(bookService.create(any(BookRequest.class))).thenReturn(PALACE_WALK);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"isbn": "978-0-306-40615-7", "title": "Palace Walk",
                                 "publishedYear": 1956, "totalCopies": 3, "authorId": 1}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/books/10"))
                .andExpect(jsonPath("$.availableCopies").value(3));
    }

    @Test
    void createRejectsInvalidIsbnAndZeroCopies() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"isbn": "9780306406158", "title": "Palace Walk",
                                 "totalCopies": 0, "authorId": 1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.isbn").exists())
                .andExpect(jsonPath("$.errors.totalCopies").exists());

        verifyNoInteractions(bookService);
    }

    @Test
    void createReturns409WhenTheDatabaseRejectsADuplicate() throws Exception {
        when(bookService.create(any(BookRequest.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key value violates unique constraint"));

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"isbn": "9780306406157", "title": "Palace Walk",
                                 "totalCopies": 3, "authorId": 1}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("The request conflicts with existing data"));
    }

    @Test
    void updateReturns409WhenCopiesWouldDropBelowLoans() throws Exception {
        when(bookService.update(eq(10L), any(BookRequest.class)))
                .thenThrow(new BusinessRuleException("Cannot set total copies to 1: 2 copies are on loan"));

        mockMvc.perform(put("/api/books/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"isbn": "9780306406157", "title": "Palace Walk",
                                 "totalCopies": 1, "authorId": 1}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Cannot set total copies to 1: 2 copies are on loan"));
    }

    @Test
    void deleteReturns204() throws Exception {
        mockMvc.perform(delete("/api/books/10"))
                .andExpect(status().isNoContent());
    }
}
