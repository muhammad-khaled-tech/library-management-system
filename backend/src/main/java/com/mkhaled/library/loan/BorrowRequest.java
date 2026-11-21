package com.mkhaled.library.loan;

import jakarta.validation.constraints.NotNull;

public record BorrowRequest(
        @NotNull Long bookId,
        @NotNull Long memberId
) {
}
