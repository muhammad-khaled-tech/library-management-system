package com.mkhaled.library.loan;

import com.mkhaled.library.book.Book;
import com.mkhaled.library.common.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LoanController.class)
class LoanControllerTest {

    private static final LoanResponse ACTIVE_LOAN = new LoanResponse(
            50L, 10L, "Palace Walk", 3L, "Sara Ali",
            LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 15), null, null, LoanStatus.ACTIVE);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoanService loanService;

    @Test
    void listBindsFiltersAndShowsNewestLoansFirst() throws Exception {
        PageRequest expectedPage = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "borrowedOn"));
        when(loanService.search(new LoanSearchCriteria(3L, null, LoanStatus.OVERDUE), expectedPage))
                .thenReturn(new PageImpl<>(List.of(ACTIVE_LOAN), expectedPage, 1));

        mockMvc.perform(get("/api/loans").param("memberId", "3").param("status", "OVERDUE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(50))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    void listRejectsUnknownStatus() throws Exception {
        mockMvc.perform(get("/api/loans").param("status", "LOST"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.status").value("Invalid value 'LOST'"));

        verifyNoInteractions(loanService);
    }

    @Test
    void borrowReturns201WithLocation() throws Exception {
        when(loanService.borrow(new BorrowRequest(10L, 3L))).thenReturn(ACTIVE_LOAN);

        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"bookId": 10, "memberId": 3}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/loans/50"))
                .andExpect(jsonPath("$.dueDate").value("2026-03-15"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void borrowRequiresBookAndMember() throws Exception {
        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.bookId").exists())
                .andExpect(jsonPath("$.errors.memberId").exists());

        verifyNoInteractions(loanService);
    }

    @Test
    void borrowReturns409WhenMemberIsAtTheLimit() throws Exception {
        when(loanService.borrow(new BorrowRequest(10L, 3L)))
                .thenThrow(new BusinessRuleException("Member 3 already has 3 active loans (limit is 3)"));

        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"bookId": 10, "memberId": 3}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Member 3 already has 3 active loans (limit is 3)"));
    }

    @Test
    void borrowReturns409WhenAnotherRequestChangedTheBookFirst() throws Exception {
        when(loanService.borrow(new BorrowRequest(10L, 3L)))
                .thenThrow(new ObjectOptimisticLockingFailureException(Book.class, 10L));

        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"bookId": 10, "memberId": 3}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("The record was changed by another request, please retry"));
    }

    @Test
    void returnGivesTheLateFee() throws Exception {
        LoanResponse returned = new LoanResponse(
                50L, 10L, "Palace Walk", 3L, "Sara Ali",
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 15), LocalDate.of(2026, 3, 18),
                new BigDecimal("15.00"), LoanStatus.RETURNED);
        when(loanService.returnLoan(50L)).thenReturn(returned);

        mockMvc.perform(post("/api/loans/50/return"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RETURNED"))
                .andExpect(jsonPath("$.lateFee").value(15.00));
    }

    @Test
    void getReturnsLoan() throws Exception {
        when(loanService.get(50L)).thenReturn(ACTIVE_LOAN);

        mockMvc.perform(get("/api/loans/50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberName").value("Sara Ali"));
    }
}
