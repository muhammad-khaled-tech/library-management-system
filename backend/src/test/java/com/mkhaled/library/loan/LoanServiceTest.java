package com.mkhaled.library.loan;

import com.mkhaled.library.author.Author;
import com.mkhaled.library.book.Book;
import com.mkhaled.library.book.BookRepository;
import com.mkhaled.library.common.exception.BusinessRuleException;
import com.mkhaled.library.common.exception.ResourceNotFoundException;
import com.mkhaled.library.member.Member;
import com.mkhaled.library.member.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    private static final LocalDate MARCH_1 = LocalDate.of(2026, 3, 1);

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private MemberRepository memberRepository;

    private final LoanPolicy policy = new LoanPolicy(14, 3, new BigDecimal("5.00"));
    private final Member sara = withId(new Member("Sara Ali", "sara@example.com"), 3L);
    private final Book palaceWalk =
            withId(new Book("9780306406157", "Palace Walk", 1956, 2, new Author("Naguib Mahfouz")), 10L);

    @Test
    void borrowCreatesActiveLoanDueInFourteenDays() {
        when(memberRepository.findWithLockById(3L)).thenReturn(Optional.of(sara));
        when(loanRepository.countByMemberIdAndReturnedOnIsNull(3L)).thenReturn(0L);
        when(bookRepository.findById(10L)).thenReturn(Optional.of(palaceWalk));
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LoanResponse loan = serviceOn(MARCH_1).borrow(new BorrowRequest(10L, 3L));

        assertThat(loan.dueDate()).isEqualTo(LocalDate.of(2026, 3, 15));
        assertThat(loan.status()).isEqualTo(LoanStatus.ACTIVE);
        assertThat(loan.bookTitle()).isEqualTo("Palace Walk");
        assertThat(palaceWalk.getAvailableCopies()).isEqualTo(1);
    }

    @Test
    void borrowRejectsMemberAtTheLoanLimit() {
        when(memberRepository.findWithLockById(3L)).thenReturn(Optional.of(sara));
        when(loanRepository.countByMemberIdAndReturnedOnIsNull(3L)).thenReturn(3L);

        assertThatThrownBy(() -> serviceOn(MARCH_1).borrow(new BorrowRequest(10L, 3L)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Member 3 already has 3 active loans (limit is 3)");
        verify(loanRepository, never()).save(any());
        assertThat(palaceWalk.getAvailableCopies()).isEqualTo(2);
    }

    @Test
    void borrowRejectsBookWithNoCopiesLeft() {
        palaceWalk.checkOut();
        palaceWalk.checkOut();
        when(memberRepository.findWithLockById(3L)).thenReturn(Optional.of(sara));
        when(loanRepository.countByMemberIdAndReturnedOnIsNull(3L)).thenReturn(0L);
        when(bookRepository.findById(10L)).thenReturn(Optional.of(palaceWalk));

        assertThatThrownBy(() -> serviceOn(MARCH_1).borrow(new BorrowRequest(10L, 3L)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("No copies of \"Palace Walk\" are available");
        verify(loanRepository, never()).save(any());
    }

    @Test
    void borrowThrowsWhenMemberDoesNotExist() {
        when(memberRepository.findWithLockById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceOn(MARCH_1).borrow(new BorrowRequest(10L, 99L)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Member with id 99 not found");
    }

    @Test
    void borrowThrowsWhenBookDoesNotExist() {
        when(memberRepository.findWithLockById(3L)).thenReturn(Optional.of(sara));
        when(loanRepository.countByMemberIdAndReturnedOnIsNull(3L)).thenReturn(0L);
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceOn(MARCH_1).borrow(new BorrowRequest(99L, 3L)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Book with id 99 not found");
    }

    @Test
    void returnRecordsLateFeeAndFreesTheCopy() {
        Loan loan = withId(Loan.borrow(palaceWalk, sara, MARCH_1, policy), 50L);
        when(loanRepository.findById(50L)).thenReturn(Optional.of(loan));

        LoanResponse returned = serviceOn(LocalDate.of(2026, 3, 18)).returnLoan(50L);

        assertThat(returned.status()).isEqualTo(LoanStatus.RETURNED);
        assertThat(returned.returnedOn()).isEqualTo(LocalDate.of(2026, 3, 18));
        assertThat(returned.lateFee()).isEqualByComparingTo("15.00");
        assertThat(palaceWalk.getAvailableCopies()).isEqualTo(2);
    }

    @Test
    void returnThrowsWhenLoanDoesNotExist() {
        when(loanRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceOn(MARCH_1).returnLoan(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Loan with id 99 not found");
    }

    private LoanService serviceOn(LocalDate today) {
        Clock clock = Clock.fixed(today.atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        return new LoanService(loanRepository, bookRepository, memberRepository, policy, clock);
    }

    private static <T> T withId(T entity, Long id) {
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }
}
