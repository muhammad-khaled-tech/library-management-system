package com.mkhaled.library.book;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.ISBN;

public record BookRequest(
        @NotBlank @ISBN String isbn,
        @NotBlank @Size(max = 255) String title,
        @Positive Integer publishedYear,
        @NotNull @Positive Integer totalCopies,
        @NotNull Long authorId
) {
}
