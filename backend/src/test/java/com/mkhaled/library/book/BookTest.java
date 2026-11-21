package com.mkhaled.library.book;

import com.mkhaled.library.author.Author;
import com.mkhaled.library.common.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookTest {

    private final Author author = new Author("Naguib Mahfouz");

    @Test
    void newBookHasAllCopiesAvailable() {
        assertThat(book(3).getAvailableCopies()).isEqualTo(3);
    }

    @Test
    void checkOutTakesOneCopy() {
        Book book = book(2);

        book.checkOut();

        assertThat(book.getAvailableCopies()).isEqualTo(1);
    }

    @Test
    void checkOutFailsWhenNoCopiesAreAvailable() {
        Book book = book(1);
        book.checkOut();

        assertThatThrownBy(book::checkOut)
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("No copies of \"Palace Walk\" are available");
        assertThat(book.getAvailableCopies()).isZero();
    }

    @Test
    void returnCopyPutsOneCopyBack() {
        Book book = book(2);
        book.checkOut();

        book.returnCopy();

        assertThat(book.getAvailableCopies()).isEqualTo(2);
    }

    @Test
    void returnCopyFailsWhenNoCopyIsOnLoan() {
        assertThatThrownBy(book(2)::returnCopy).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void changeTotalCopiesKeepsCopiesOnLoan() {
        Book book = book(3);
        book.checkOut();
        book.checkOut();

        book.changeTotalCopies(5);

        assertThat(book.getTotalCopies()).isEqualTo(5);
        assertThat(book.getAvailableCopies()).isEqualTo(3);
    }

    @Test
    void changeTotalCopiesRejectsFewerCopiesThanAreOnLoan() {
        Book book = book(3);
        book.checkOut();
        book.checkOut();

        assertThatThrownBy(() -> book.changeTotalCopies(1))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("2 copies are on loan");
        assertThat(book.getTotalCopies()).isEqualTo(3);
        assertThat(book.getAvailableCopies()).isEqualTo(1);
    }

    private Book book(int copies) {
        return new Book("9780306406157", "Palace Walk", 1956, copies, author);
    }
}
