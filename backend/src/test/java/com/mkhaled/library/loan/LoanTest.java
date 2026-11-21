package com.mkhaled.library.loan;

import com.mkhaled.library.author.Author;
import com.mkhaled.library.book.Book;
import com.mkhaled.library.common.exception.BusinessRuleException;
import com.mkhaled.library.member.Member;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoanTest {

    private static final LocalDate MARCH_1 = LocalDate.of(2026, 3, 1);

    private final LoanPolicy policy = new LoanPolicy(14, 3, new BigDecimal("5.00"));
    private final Book book = new Book("9780306406157", "Palace Walk", 1956, 2, new Author("Naguib Mahfouz"));
    private final Member member = new Member("Sara Ali", "sara@example.com");

    @Test
    void borrowTakesACopyAndSetsTheDueDate() {
        Loan loan = Loan.borrow(book, member, MARCH_1, policy);

        assertThat(book.getAvailableCopies()).isEqualTo(1);
        assertThat(loan.getBorrowedOn()).isEqualTo(MARCH_1);
        assertThat(loan.getDueDate()).isEqualTo(LocalDate.of(2026, 3, 15));
        assertThat(loan.isActive()).isTrue();
    }

    @Test
    void returnPutsTheCopyBackAndRecordsTheFee() {
        Loan loan = Loan.borrow(book, member, MARCH_1, policy);

        loan.markReturned(LocalDate.of(2026, 3, 17), policy);

        assertThat(book.getAvailableCopies()).isEqualTo(2);
        assertThat(loan.isActive()).isFalse();
        assertThat(loan.getReturnedOn()).isEqualTo(LocalDate.of(2026, 3, 17));
        assertThat(loan.getLateFee()).isEqualByComparingTo("10.00");
    }

    @Test
    void onTimeReturnHasZeroFee() {
        Loan loan = Loan.borrow(book, member, MARCH_1, policy);

        loan.markReturned(LocalDate.of(2026, 3, 10), policy);

        assertThat(loan.getLateFee()).isEqualByComparingTo("0");
    }

    @Test
    void statusDependsOnDueDateAndReturn() {
        Loan loan = Loan.borrow(book, member, MARCH_1, policy);

        assertThat(loan.statusOn(LocalDate.of(2026, 3, 15))).isEqualTo(LoanStatus.ACTIVE);
        assertThat(loan.statusOn(LocalDate.of(2026, 3, 16))).isEqualTo(LoanStatus.OVERDUE);

        loan.markReturned(LocalDate.of(2026, 3, 16), policy);

        assertThat(loan.statusOn(LocalDate.of(2026, 3, 20))).isEqualTo(LoanStatus.RETURNED);
    }

    @Test
    void aLoanCannotBeReturnedTwice() {
        Loan loan = Loan.borrow(book, member, MARCH_1, policy);
        loan.markReturned(LocalDate.of(2026, 3, 10), policy);

        assertThatThrownBy(() -> loan.markReturned(LocalDate.of(2026, 3, 11), policy))
                .isInstanceOf(BusinessRuleException.class);
        assertThat(book.getAvailableCopies()).isEqualTo(2);
    }
}
