package com.mkhaled.library.loan;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class LoanPolicyTest {

    private final LoanPolicy policy = new LoanPolicy(14, 3, new BigDecimal("5.00"));
    private final LocalDate dueDate = LocalDate.of(2026, 3, 15);

    @Test
    void dueDateIsFourteenDaysAfterBorrowing() {
        assertThat(policy.dueDateFor(LocalDate.of(2026, 3, 1))).isEqualTo(dueDate);
    }

    @Test
    void noFeeWhenReturnedOnTheDueDate() {
        assertThat(policy.lateFeeFor(dueDate, dueDate)).isEqualByComparingTo("0");
    }

    @Test
    void noFeeWhenReturnedEarly() {
        assertThat(policy.lateFeeFor(dueDate, dueDate.minusDays(4))).isEqualByComparingTo("0");
    }

    @Test
    void feeIsChargedPerOverdueDay() {
        assertThat(policy.lateFeeFor(dueDate, dueDate.plusDays(3))).isEqualByComparingTo("15.00");
    }

    @Test
    void feeCountsDaysAcrossMonthBoundaries() {
        assertThat(policy.lateFeeFor(LocalDate.of(2026, 2, 27), LocalDate.of(2026, 3, 2)))
                .isEqualByComparingTo("15.00");
    }
}
