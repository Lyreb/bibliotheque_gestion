/*
 * Copyright (C) 2022 IUT Laval - Le Mans Université.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package mmiLibraryServer.services;

import java.util.List;
import java.util.NoSuchElementException;
import mmiLibraryServer.mongoModel.Book;
import mmiLibraryServer.mongoModel.BookCopy;
import mmiLibraryServer.mongoModel.BookState;
import mmiLibraryServer.mongoModel.Loan;
import mmiLibraryServer.mongoModel.utils.BookRequestFilter;
import mmiLibraryServer.services.exceptions.HasOngoingLoanException;
import org.springframework.dao.DuplicateKeyException;

/**
 *
 * @author Rémi Venant
 */
public interface BookService {

    /**
     * Get all books.
     *
     * @return books
     */
    List<Book> getBooks();

    /**
     * Get books that match the different given filters.
     *
     * @param filter the filters
     * @return the books
     * @throws IllegalArgumentException if filter is null
     */
    List<Book> getBooks(BookRequestFilter filter);

    /**
     * Get a book from its id.
     *
     * @param bookId the book id
     * @return the matching book
     * @throws IllegalArgumentException if bookId is null
     * @throws NoSuchElementException if bookId is unknown
     */
    Book getBookById(String bookId);

    /**
     * Create a book from book information.
     *
     * @param book the book information
     * @return the saved book
     * @throws IllegalArgumentException if book is null of if book.id is given
     * @throws DuplicateKeyException if isbn is alread known
     */
    Book createBook(Book book);

    /**
     * Update a book.
     *
     * @param book the book to update
     * @return the updated book
     * @throws IllegalArgumentException if book or book.id is null
     * @throws NoSuchElementException if book is unknown
     */
    Book updateBook(Book book);

    /**
     * Delete a book from its id.
     *
     * @param bookId the book id
     * @throws IllegalArgumentException if bookId is null
     * @throws NoSuchElementException if book is unknown
     */
//    void deleteBookById(String bookId);
    /**
     * Return the loan of a book, sorted by their loan date desc.
     *
     * @param bookId the id of the book
     * @return the loans
     * @throws IllegalArgumentException if bookId is null
     * @throws NoSuchElementException if book is unknown
     */
    List<Loan> getBookLoans(String bookId);

    /**
     * Create copies of a book.
     *
     * @param bookId the book id
     * @param numCopies the number of copies to create
     * @param initialState the state of the copies. NEW if initialState is null
     * @return the created copies
     * @throws IllegalArgumentException if bookId is null of if numCopies ≤ 0
     * @throws NoSuchElementException if book is unknown
     */
    List<BookCopy> createBookCopies(String bookId, int numCopies, BookState initialState);

    /**
     * Get a book copy by its book id and its id.
     *
     * @param bookId the book id
     * @param bookCopyId the book copy id
     * @return the book copy
     * @throws IllegalArgumentException if bookId is null or bookCopyId is null
     * @throws NoSuchElementException if bookCopy if unknown or book copy does not belong to book
     */
    BookCopy getBookCopyById(String bookId, String bookCopyId);

    /**
     * Get a book copy by its id.
     *
     * @param bookCopyId the book copy id
     * @return the book copy
     * @throws IllegalArgumentException if bookCopyId is null
     * @throws NoSuchElementException if bookCopy if unknown
     */
    BookCopy getBookCopyById(String bookCopyId);

    /**
     * Update a book copy, if the book copy is available. Only update removed indicator and state
     * (if not null).
     *
     * @param bookId the book id
     * @param bookCopy the book copy id
     * @return the updated book id
     * @throws IllegalArgumentException if bookId or bookCopy or bookCopy.id is null
     * @throws NoSuchElementException if bookCopy if unknown or book copy does not belong to book
     * @throws HasOngoingLoanException if bookCopy has on going loan
     */
    BookCopy updateBookCopy(String bookId, BookCopy bookCopy) throws HasOngoingLoanException;

    /**
     * Return the loans of a book copy.
     *
     * @param bookId the book id
     * @param bookCopyId the book copy id
     * @return the loans of the book copy
     * @throws IllegalArgumentException if bookId or bookCopy or bookCopy.id is null
     * @throws NoSuchElementException if bookCopy if unknown or book copy does not belong to book
     */
    List<Loan> getBookCopyLoans(String bookId, String bookCopyId);
}
