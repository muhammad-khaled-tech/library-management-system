package com.mkhaled.library.loan;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Lending rules, bound from {@code library.loan.*} in application.properties.
 * Validated at startup so a bad value stops the app instead of producing wrong fees.
 */
@Validated
@ConfigurationProperties(prefix = "library.loan")
public record LoanPolicy(
        @Positive int loanDays,
        @Positive int maxActiveLoans,
        @NotNull @PositiveOrZero BigDecimal lateFeePerDay
) {

    public LocalDate dueDateFor(LocalDate borrowedOn) {
        return borrowedOn.plusDays(loanDays);
    }

    public BigDecimal lateFeeFor(LocalDate dueDate, LocalDate returnedOn) {
        long daysLate = Math.max(0, ChronoUnit.DAYS.between(dueDate, returnedOn));
        return lateFeePerDay.multiply(BigDecimal.valueOf(daysLate));
    }
}
