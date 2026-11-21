package com.mkhaled.library.author;

import com.mkhaled.library.common.exception.BusinessRuleException;
import com.mkhaled.library.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.data.core.TypeInformation;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthorController.class)
class AuthorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthorService authorService;

    @Test
    void listReturnsPageSortedByNameByDefault() throws Exception {
        PageRequest expectedPage = PageRequest.of(1, 5, Sort.by("name"));
        when(authorService.search("mah", expectedPage))
                .thenReturn(new PageImpl<>(List.of(new AuthorResponse(1L, "Naguib Mahfouz")), expectedPage, 6));

        mockMvc.perform(get("/api/authors").param("name", "mah").param("page", "1").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Naguib Mahfouz"))
                .andExpect(jsonPath("$.page.totalElements").value(6))
                .andExpect(jsonPath("$.page.number").value(1));
    }

    @Test
    void getReturnsAuthor() throws Exception {
        when(authorService.get(1L)).thenReturn(new AuthorResponse(1L, "Taha Hussein"));

        mockMvc.perform(get("/api/authors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Taha Hussein"));
    }

    @Test
    void getReturnsProblemDetailWhenAuthorIsMissing() throws Exception {
        when(authorService.get(99L)).thenThrow(new ResourceNotFoundException("Author", 99L));

        mockMvc.perform(get("/api/authors/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Author with id 99 not found"));
    }

    @Test
    void createReturns201WithLocation() throws Exception {
        when(authorService.create(new AuthorRequest("Tawfiq al-Hakim")))
                .thenReturn(new AuthorResponse(5L, "Tawfiq al-Hakim"));

        mockMvc.perform(post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Tawfiq al-Hakim"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/authors/5"))
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void createRejectsBlankName() throws Exception {
        mockMvc.perform(post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "  "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());

        verifyNoInteractions(authorService);
    }

    @Test
    void deleteReturnsConflictWhenAuthorHasBooks() throws Exception {
        doThrow(new BusinessRuleException("Author 1 still has books and cannot be deleted"))
                .when(authorService).delete(1L);

        mockMvc.perform(delete("/api/authors/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Author 1 still has books and cannot be deleted"));
    }

    @Test
    void deleteReturns204() throws Exception {
        mockMvc.perform(delete("/api/authors/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void listRejectsUnknownSortProperty() throws Exception {
        when(authorService.search(any(), any()))
                .thenThrow(new PropertyReferenceException("salary", TypeInformation.of(Author.class), List.of()));

        mockMvc.perform(get("/api/authors").param("sort", "salary"))
                .andExpect(status().isBadRequest());
    }
}
