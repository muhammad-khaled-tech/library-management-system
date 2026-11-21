package com.mkhaled.library.loan;

/**
 * Optional filters for listing loans; a null field means "any".
 */
public record LoanSearchCriteria(Long memberId, Long bookId, LoanStatus status) {
}
