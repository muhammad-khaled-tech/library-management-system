package com.mkhaled.library.loan;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LoanResponse(
        Long id,
        Long bookId,
        String bookTitle,
        Long memberId,
        String memberName,
        LocalDate borrowedOn,
        LocalDate dueDate,
        LocalDate returnedOn,
        BigDecimal lateFee,
        LoanStatus status
) {

    static LoanResponse from(Loan loan, LocalDate today) {
        return new LoanResponse(
                loan.getId(),
                loan.getBook().getId(),
                loan.getBook().getTitle(),
                loan.getMember().getId(),
                loan.getMember().getFullName(),
                loan.getBorrowedOn(),
                loan.getDueDate(),
                loan.getReturnedOn(),
                loan.getLateFee(),
                loan.statusOn(today));
    }
}
