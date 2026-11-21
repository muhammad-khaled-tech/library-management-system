package com.mkhaled.library.book;

public record BookResponse(
        Long id,
        String isbn,
        String title,
        Integer publishedYear,
        int totalCopies,
        int availableCopies,
        Long authorId,
        String authorName
) {

    static BookResponse from(Book book) {
        return new BookResponse(
                book.getId(),
                book.getIsbn(),
                book.getTitle(),
                book.getPublishedYear(),
                book.getTotalCopies(),
                book.getAvailableCopies(),
                book.getAuthor().getId(),
                book.getAuthor().getName());
    }
}
