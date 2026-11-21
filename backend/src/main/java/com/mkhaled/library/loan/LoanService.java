package com.mkhaled.library.loan;

import com.mkhaled.library.book.Book;
import com.mkhaled.library.book.BookRepository;
import com.mkhaled.library.common.exception.BusinessRuleException;
import com.mkhaled.library.common.exception.ResourceNotFoundException;
import com.mkhaled.library.member.Member;
import com.mkhaled.library.member.MemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
public class LoanService {

    private static final Logger log = LoggerFactory.getLogger(LoanService.class);

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final LoanPolicy policy;
    private final Clock clock;

    public LoanService(LoanRepository loanRepository,
                       BookRepository bookRepository,
                       MemberRepository memberRepository,
                       LoanPolicy policy,
                       Clock clock) {
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
        this.policy = policy;
        this.clock = clock;
    }

    @Transactional
    public LoanResponse borrow(BorrowRequest request) {
        Member member = memberRepository.findWithLockById(request.memberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member", request.memberId()));
        ensureBelowLoanLimit(member);
        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book", request.bookId()));

        LocalDate today = LocalDate.now(clock);
        Loan loan = loanRepository.save(Loan.borrow(book, member, today, policy));
        log.info("Member {} borrowed book {} (loan {}), due {}",
                member.getId(), book.getId(), loan.getId(), loan.getDueDate());
        return LoanResponse.from(loan, today);
    }

    @Transactional
    public LoanResponse returnLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan", loanId));

        LocalDate today = LocalDate.now(clock);
        loan.markReturned(today, policy);
        log.info("Loan {} returned, late fee {}", loan.getId(), loan.getLateFee());
        return LoanResponse.from(loan, today);
    }

    private void ensureBelowLoanLimit(Member member) {
        long activeLoans = loanRepository.countByMemberIdAndReturnedOnIsNull(member.getId());
        if (activeLoans >= policy.maxActiveLoans()) {
            throw new BusinessRuleException("Member " + member.getId() + " already has " + activeLoans
                    + " active loans (limit is " + policy.maxActiveLoans() + ")");
        }
    }
}
