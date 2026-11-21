package com.mkhaled.library.loan;

import com.mkhaled.library.book.Book;
import com.mkhaled.library.common.exception.BusinessRuleException;
import com.mkhaled.library.member.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "loans")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id")
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id")
    private Member member;

    private LocalDate borrowedOn;

    private LocalDate dueDate;

    private LocalDate returnedOn;

    private BigDecimal lateFee;

    protected Loan() {
    }

    private Loan(Book book, Member member, LocalDate borrowedOn, LocalDate dueDate) {
        this.book = book;
        this.member = member;
        this.borrowedOn = borrowedOn;
        this.dueDate = dueDate;
    }

    public static Loan borrow(Book book, Member member, LocalDate today, LoanPolicy policy) {
        book.checkOut();
        return new Loan(book, member, today, policy.dueDateFor(today));
    }

    public void markReturned(LocalDate today, LoanPolicy policy) {
        if (!isActive()) {
            throw new BusinessRuleException("Loan " + id + " was already returned");
        }
        book.returnCopy();
        returnedOn = today;
        lateFee = policy.lateFeeFor(dueDate, today);
    }

    public boolean isActive() {
        return returnedOn == null;
    }

    public LoanStatus statusOn(LocalDate today) {
        if (!isActive()) {
            return LoanStatus.RETURNED;
        }
        return today.isAfter(dueDate) ? LoanStatus.OVERDUE : LoanStatus.ACTIVE;
    }

    public Long getId() {
        return id;
    }

    public Book getBook() {
        return book;
    }

    public Member getMember() {
        return member;
    }

    public LocalDate getBorrowedOn() {
        return borrowedOn;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getReturnedOn() {
        return returnedOn;
    }

    public BigDecimal getLateFee() {
        return lateFee;
    }
}
