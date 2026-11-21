package com.mkhaled.library.author;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthorRequest(
        @NotBlank @Size(max = 150) String name
) {
}
