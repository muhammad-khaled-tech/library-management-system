package com.mkhaled.library.loan;

import com.mkhaled.library.author.Author;
import com.mkhaled.library.book.Book;
import com.mkhaled.library.member.Member;
import com.mkhaled.library.support.TestcontainersConfiguration;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
class LoanRepositoryTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 3, 20);
    private static final LoanPolicy POLICY = new LoanPolicy(14, 3, new BigDecimal("5.00"));
    private static final PageRequest NEWEST_FIRST = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "borrowedOn"));

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Long saraId;
    private Long palaceWalkId;
    private Long overdueLoanId;
    private Long activeLoanId;
    private Long returnedLoanId;

    @BeforeEach
    void setUp() {
        Author mahfouz = entityManager.persist(new Author("Naguib Mahfouz"));
        Book palaceWalk = entityManager.persist(new Book("9780306406157", "Palace Walk", 1956, 3, mahfouz));
        Book sugarStreet = entityManager.persist(new Book("9781402894626", "Sugar Street", 1957, 1, mahfouz));
        Member sara = entityManager.persist(new Member("Sara Ali", "sara@example.com"));
        Member omar = entityManager.persist(new Member("Omar Adel", "omar@example.com"));

        Loan overdue = entityManager.persist(Loan.borrow(palaceWalk, sara, LocalDate.of(2026, 3, 1), POLICY));
        Loan active = entityManager.persist(Loan.borrow(sugarStreet, sara, LocalDate.of(2026, 3, 10), POLICY));
        Loan returned = Loan.borrow(palaceWalk, omar, LocalDate.of(2026, 2, 1), POLICY);
        returned.markReturned(LocalDate.of(2026, 2, 10), POLICY);
        entityManager.persist(returned);
        entityManager.flush();
        entityManager.clear();

        saraId = sara.getId();
        palaceWalkId = palaceWalk.getId();
        overdueLoanId = overdue.getId();
        activeLoanId = active.getId();
        returnedLoanId = returned.getId();
    }

    @Test
    void noFiltersReturnsAllLoansNewestFirst() {
        assertThat(search(new LoanSearchCriteria(null, null, null)))
                .containsExactly(activeLoanId, overdueLoanId, returnedLoanId);
    }

    @Test
    void filtersByMember() {
        assertThat(search(new LoanSearchCriteria(saraId, null, null)))
                .containsExactly(activeLoanId, overdueLoanId);
    }

    @Test
    void filtersByBook() {
        assertThat(search(new LoanSearchCriteria(null, palaceWalkId, null)))
                .containsExactly(overdueLoanId, returnedLoanId);
    }

    @Test
    void overdueMeansNotReturnedAndPastTheDueDate() {
        assertThat(search(new LoanSearchCriteria(null, null, LoanStatus.OVERDUE)))
                .containsExactly(overdueLoanId);
    }

    @Test
    void activeMeansNotReturnedAndNotYetDue() {
        assertThat(search(new LoanSearchCriteria(null, null, LoanStatus.ACTIVE)))
                .containsExactly(activeLoanId);
    }

    @Test
    void returnedStatusFindsClosedLoans() {
        assertThat(search(new LoanSearchCriteria(null, null, LoanStatus.RETURNED)))
                .containsExactly(returnedLoanId);
    }

    @Test
    void filtersCanBeCombined() {
        assertThat(search(new LoanSearchCriteria(saraId, palaceWalkId, LoanStatus.OVERDUE)))
                .containsExactly(overdueLoanId);
    }

    @Test
    void searchLoadsBookAndMemberInTheSameQuery() {
        Page<Loan> page = loanRepository.findAll(
                LoanSpecifications.matching(new LoanSearchCriteria(null, null, null), TODAY), NEWEST_FIRST);

        assertThat(page.getContent()).allSatisfy(loan -> {
            assertThat(Hibernate.isInitialized(loan.getBook())).isTrue();
            assertThat(Hibernate.isInitialized(loan.getMember())).isTrue();
        });
    }

    @Test
    void countsOnlyActiveLoansTowardsTheLimit() {
        assertThat(loanRepository.countByMemberIdAndReturnedOnIsNull(saraId)).isEqualTo(2);
    }

    private List<Long> search(LoanSearchCriteria criteria) {
        return loanRepository.findAll(LoanSpecifications.matching(criteria, TODAY), NEWEST_FIRST)
                .map(Loan::getId)
                .getContent();
    }
}
