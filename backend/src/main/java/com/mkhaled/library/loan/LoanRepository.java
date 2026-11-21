package com.mkhaled.library.loan;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LoanRepository extends JpaRepository<Loan, Long>, JpaSpecificationExecutor<Loan> {

    // A loan row shows the book title and member name, so fetch both with the page
    @Override
    @EntityGraph(attributePaths = {"book", "member"})
    Page<Loan> findAll(Specification<Loan> spec, Pageable pageable);

    long countByMemberIdAndReturnedOnIsNull(Long memberId);

    boolean existsByBookId(Long bookId);

    boolean existsByMemberId(Long memberId);
}
