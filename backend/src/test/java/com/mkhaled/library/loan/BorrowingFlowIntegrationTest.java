package com.mkhaled.library.loan;

import com.jayway.jsonpath.JsonPath;
import com.mkhaled.library.support.MutableClock;
import com.mkhaled.library.support.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.convention.TestBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Clock;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Runs the whole lending flow over HTTP against a real PostgreSQL: controllers, services,
 * transactions, Flyway schema and constraints all take part.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class BorrowingFlowIntegrationTest {

    private static final LocalDate MARCH_1 = LocalDate.of(2026, 3, 1);
    private static final MutableClock TODAY = new MutableClock(MARCH_1);

    @TestBean
    private Clock clock;

    @Autowired
    private MockMvc mockMvc;

    static Clock clock() {
        return TODAY;
    }

    @BeforeEach
    void resetDate() {
        TODAY.setDate(MARCH_1);
    }

    @Test
    void lastCopyIsBorrowedReturnedLateAndBorrowedAgain() throws Exception {
        long authorId = createAuthor("Naguib Mahfouz");
        long bookId = createBook("9780306406157", "Palace Walk", 1, authorId);
        long sara = createMember("Sara Ali", "sara.flow@example.com");
        long omar = createMember("Omar Adel", "omar.flow@example.com");

        long loanId = idOf(borrow(bookId, sara)
                .andExpect(jsonPath("$.dueDate").value("2026-03-15")));

        borrow(bookId, omar)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("No copies of \"Palace Walk\" are available"));
        mockMvc.perform(get("/api/books/{id}", bookId))
                .andExpect(jsonPath("$.availableCopies").value(0));

        TODAY.setDate(LocalDate.of(2026, 3, 18));
        mockMvc.perform(get("/api/loans").param("memberId", String.valueOf(sara)).param("status", "OVERDUE"))
                .andExpect(jsonPath("$.content[0].id").value(loanId));

        mockMvc.perform(post("/api/loans/{id}/return", loanId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RETURNED"))
                .andExpect(jsonPath("$.lateFee").value(15.00));
        mockMvc.perform(get("/api/books/{id}", bookId))
                .andExpect(jsonPath("$.availableCopies").value(1));

        borrow(bookId, omar)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dueDate").value("2026-04-01"));
    }

    @Test
    void memberCannotHoldMoreThanThreeBooks() throws Exception {
        long authorId = createAuthor("Taha Hussein");
        long member = createMember("Mona Samir", "mona.flow@example.com");
        String[] isbns = {"9780131103627", "9780262033848", "9780596009205", "9780134685991"};
        long[] books = new long[isbns.length];
        for (int i = 0; i < isbns.length; i++) {
            books[i] = createBook(isbns[i], "Book " + i, 1, authorId);
        }

        for (int i = 0; i < 3; i++) {
            borrow(books[i], member).andExpect(status().isCreated());
        }

        borrow(books[3], member)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Member " + member + " already has 3 active loans (limit is 3)"));
        mockMvc.perform(get("/api/books/{id}", books[3]))
                .andExpect(jsonPath("$.availableCopies").value(1));
    }

    private ResultActions borrow(long bookId, long memberId) throws Exception {
        return postJson("/api/loans", """
                {"bookId": %d, "memberId": %d}
                """.formatted(bookId, memberId));
    }

    private long createAuthor(String name) throws Exception {
        return idOf(postJson("/api/authors", """
                {"name": "%s"}
                """.formatted(name)));
    }

    private long createBook(String isbn, String title, int copies, long authorId) throws Exception {
        return idOf(postJson("/api/books", """
                {"isbn": "%s", "title": "%s", "totalCopies": %d, "authorId": %d}
                """.formatted(isbn, title, copies, authorId)));
    }

    private long createMember(String fullName, String email) throws Exception {
        return idOf(postJson("/api/members", """
                {"fullName": "%s", "email": "%s"}
                """.formatted(fullName, email)));
    }

    private ResultActions postJson(String url, String json) throws Exception {
        return mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private static long idOf(ResultActions result) throws Exception {
        result.andExpect(status().isCreated());
        Number id = JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }
}
