package com.mkhaled.library.loan;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    long countByMemberIdAndReturnedOnIsNull(Long memberId);

    boolean existsByBookId(Long bookId);

    boolean existsByMemberId(Long memberId);
}
