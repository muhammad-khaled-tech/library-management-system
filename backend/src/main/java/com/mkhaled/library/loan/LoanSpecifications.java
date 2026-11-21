package com.mkhaled.library.loan;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

/**
 * Builds the WHERE clause for loan searches from whichever filters were given.
 */
final class LoanSpecifications {

    private LoanSpecifications() {
    }

    static Specification<Loan> matching(LoanSearchCriteria criteria, LocalDate today) {
        Specification<Loan> spec = Specification.unrestricted();
        if (criteria.memberId() != null) {
            spec = spec.and(forMember(criteria.memberId()));
        }
        if (criteria.bookId() != null) {
            spec = spec.and(forBook(criteria.bookId()));
        }
        if (criteria.status() != null) {
            spec = spec.and(withStatus(criteria.status(), today));
        }
        return spec;
    }

    private static Specification<Loan> forMember(Long memberId) {
        return (loan, query, cb) -> cb.equal(loan.get("member").get("id"), memberId);
    }

    private static Specification<Loan> forBook(Long bookId) {
        return (loan, query, cb) -> cb.equal(loan.get("book").get("id"), bookId);
    }

    // Mirrors Loan.statusOn(today) so the list filter and the status shown in a response agree
    private static Specification<Loan> withStatus(LoanStatus status, LocalDate today) {
        return switch (status) {
            case RETURNED -> (loan, query, cb) -> cb.isNotNull(loan.get("returnedOn"));
            case OVERDUE -> (loan, query, cb) -> cb.and(
                    cb.isNull(loan.get("returnedOn")),
                    cb.lessThan(loan.get("dueDate"), today));
            case ACTIVE -> (loan, query, cb) -> cb.and(
                    cb.isNull(loan.get("returnedOn")),
                    cb.greaterThanOrEqualTo(loan.get("dueDate"), today));
        };
    }
}
