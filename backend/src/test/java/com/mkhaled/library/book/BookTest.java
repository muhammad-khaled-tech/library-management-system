package com.mkhaled.library.book;

import com.mkhaled.library.author.Author;
import com.mkhaled.library.common.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookTest {

    private final Author author = new Author("Naguib Mahfouz");

    @Test
    void newBookHasAllCopiesAvailable() {
        Book book = new Book("9780306406157", "Palace Walk", 1956, 3, author);

        assertThat(book.getAvailableCopies()).isEqualTo(3);
    }

    @Test
    void changeTotalCopiesKeepsCopiesOnLoan() {
        Book book = bookWithCopiesOnLoan(3, 2);

        book.changeTotalCopies(5);

        assertThat(book.getTotalCopies()).isEqualTo(5);
        assertThat(book.getAvailableCopies()).isEqualTo(3);
    }

    @Test
    void changeTotalCopiesRejectsFewerCopiesThanAreOnLoan() {
        Book book = bookWithCopiesOnLoan(3, 2);

        assertThatThrownBy(() -> book.changeTotalCopies(1))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("2 copies are on loan");
        assertThat(book.getTotalCopies()).isEqualTo(3);
        assertThat(book.getAvailableCopies()).isEqualTo(1);
    }

    private Book bookWithCopiesOnLoan(int total, int onLoan) {
        Book book = new Book("9780306406157", "Palace Walk", 1956, total, author);
        ReflectionTestUtils.setField(book, "availableCopies", total - onLoan);
        return book;
    }
}
