package com.mkhaled.library.book;

import com.mkhaled.library.author.Author;
import com.mkhaled.library.common.exception.BusinessRuleException;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String isbn;

    private String title;

    private Integer publishedYear;

    private int totalCopies;

    private int availableCopies;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id")
    private Author author;

    @Version
    private long version;

    protected Book() {
    }

    public Book(String isbn, String title, Integer publishedYear, int totalCopies, Author author) {
        this.isbn = isbn;
        this.title = title;
        this.publishedYear = publishedYear;
        this.totalCopies = totalCopies;
        this.availableCopies = totalCopies;
        this.author = author;
    }

    public void updateDetails(String isbn, String title, Integer publishedYear, Author author) {
        this.isbn = isbn;
        this.title = title;
        this.publishedYear = publishedYear;
        this.author = author;
    }

    public void changeTotalCopies(int newTotal) {
        int onLoan = totalCopies - availableCopies;
        if (newTotal < onLoan) {
            throw new BusinessRuleException(
                    "Cannot set total copies to " + newTotal + ": " + onLoan + " copies are on loan");
        }
        this.totalCopies = newTotal;
        this.availableCopies = newTotal - onLoan;
    }

    public void checkOut() {
        if (availableCopies == 0) {
            throw new BusinessRuleException("No copies of \"" + title + "\" are available");
        }
        availableCopies--;
    }

    public void returnCopy() {
        if (availableCopies == totalCopies) {
            throw new IllegalStateException("No copy of book " + id + " is on loan");
        }
        availableCopies++;
    }

    public Long getId() {
        return id;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public Integer getPublishedYear() {
        return publishedYear;
    }

    public int getTotalCopies() {
        return totalCopies;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public Author getAuthor() {
        return author;
    }
}
