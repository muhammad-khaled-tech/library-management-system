package com.mkhaled.library.author;

public record AuthorResponse(Long id, String name) {

    static AuthorResponse from(Author author) {
        return new AuthorResponse(author.getId(), author.getName());
    }
}
