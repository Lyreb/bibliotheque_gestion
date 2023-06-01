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

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import mmiLibraryServer.mongoModel.Book;
import mmiLibraryServer.mongoModel.BookCopy;
import mmiLibraryServer.mongoModel.BookCopyRepository;
import mmiLibraryServer.mongoModel.BookRepository;
import mmiLibraryServer.mongoModel.BookState;
import mmiLibraryServer.mongoModel.Loan;
import mmiLibraryServer.mongoModel.LoanRepository;
import mmiLibraryServer.mongoModel.utils.BookRequestFilter;
import mmiLibraryServer.services.exceptions.HasOngoingLoanException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Rémi Venant
 */
@Service
public class BookServiceImpl implements BookService {

    private static final Log LOG = LogFactory.getLog(BookServiceImpl.class);

    private final BookRepository bookRepo;

    private final BookCopyRepository bookCopyRepo;

    private final LoanRepository loanRepo;

    @Autowired
    public BookServiceImpl(BookRepository bookRepo, BookCopyRepository bookCopyRepo, LoanRepository loanRepo) {
        this.bookRepo = bookRepo;
        this.bookCopyRepo = bookCopyRepo;
        this.loanRepo = loanRepo;
    }

    @Override
    public List<Book> getBooks() {
        return StreamSupport.stream(this.bookRepo.findAll().spliterator(), false).collect(Collectors.toList());
    }

    @Override
    public List<Book> getBooks(BookRequestFilter filter) {
        if (filter == null) {
            throw new IllegalArgumentException("Missing filter.");
        }
        return this.bookRepo.findAllByFilter(filter);
    }

    @Override
    public Book getBookById(String bookId) {
        if (bookId == null) {
            throw new IllegalArgumentException("Missing book id.");
        }
        try {
            return this.bookRepo.findById(bookId).get();
        } catch (NoSuchElementException ex) {
            throw new NoSuchElementException("Unknown book.");
        }
    }

    @Override
    public Book createBook(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("Missing information to create a new book.");
        }
        if (book.getId() != null) {
            throw new IllegalArgumentException("A new book cannot have already an id.");
        }
        // Force the reload of the book to get full cats
        return this.bookRepo.findById(this.bookRepo.save(book).getId()).get();

    }

    @Override
    public Book updateBook(Book bookToUpdate) {
        if (bookToUpdate == null || bookToUpdate.getId() == null) {
            throw new IllegalArgumentException("Missing information to update book.");
        }
        Book book = this.bookRepo.findById(bookToUpdate.getId())
                .orElseThrow(() -> new NoSuchElementException("Unknown book to update."));
        if (!Strings.isBlank(bookToUpdate.getIsbn())) {
            book.setIsbn(bookToUpdate.getIsbn());
        }
        if (!Strings.isBlank(bookToUpdate.getTitle())) {
            book.setTitle(bookToUpdate.getTitle());
        }
        book.setEditor(bookToUpdate.getEditor());
        book.setNumOfPages(bookToUpdate.getNumOfPages());
        book.setPublicationYear(bookToUpdate.getPublicationYear());
        book.setAuthors(bookToUpdate.getAuthors());
        book.setCategories(bookToUpdate.getCategories());
        // Force the reload of the book to get full cats
        return this.bookRepo.findById(this.bookRepo.save(book).getId()).get();
    }

    //@Override
    public void deleteBookById(String bookId) {
        if (bookId == null) {
            throw new IllegalArgumentException("missing bookId.");
        }
        if (!this.bookRepo.existsById(bookId)) {
            throw new NoSuchElementException("Unknown book to delete.");
        }
        this.bookRepo.deleteById(bookId);
    }

    @Override
    public List<Loan> getBookLoans(String bookId) {
        if (bookId == null) {
            throw new IllegalArgumentException("Missing book id.");
        }
        try {
            final Book book = this.bookRepo.findById(bookId).get();
            return this.loanRepo.findByBookCopyInOrderByLoanDateTimeDesc(book.getCopies());
        } catch (NoSuchElementException ex) {
            throw new NoSuchElementException("Unknown book.");
        }
    }

    @Override
    public List<BookCopy> createBookCopies(String bookId, int numCopies, BookState initialState) {
        if (bookId == null) {
            throw new IllegalArgumentException("Missing book id.");
        }
        if (numCopies <= 0) {
            throw new IllegalArgumentException("numCopies cannot be lower or equal to 0");
        }
        try {
            final Book book = this.bookRepo.findById(bookId).get();
            final BookState state = initialState == null ? BookState.NEW : initialState;
            ArrayList<BookCopy> copies = new ArrayList<>(numCopies);
            for (int i = 0; i < numCopies; i++) {
                copies.add(new BookCopy(book, state));
            }
            return StreamSupport.stream(this.bookCopyRepo.saveAll(copies).spliterator(), false)
                    .collect(Collectors.toList());
        } catch (NoSuchElementException ex) {
            throw new NoSuchElementException("Unknown book.");
        }
    }

    @Override
    public BookCopy getBookCopyById(String bookId, String bookCopyId) {
        if (bookId == null || bookCopyId == null) {
            throw new IllegalArgumentException("Missing book id or book copy id.");
        }
        final BookCopy bookCopy = this.bookCopyRepo.findById(bookCopyId)
                .orElseThrow(() -> new NoSuchElementException("Unknown book copy."));
        if (!bookId.equals(bookCopy.getBook().getId())) {
            throw new NoSuchElementException("Unknown book copy in book.");
        }
        return bookCopy;
    }

    @Override
    public BookCopy getBookCopyById(String bookCopyId) {
        if (bookCopyId == null) {
            throw new IllegalArgumentException("Missing book copy id.");
        }
        try {
            return this.bookCopyRepo.findById(bookCopyId).get();
        } catch (NoSuchElementException ex) {
            throw new NoSuchElementException("Unknown book copy.");
        }
    }

    @Override
    public BookCopy updateBookCopy(String bookId, BookCopy inputBookCopy) throws HasOngoingLoanException {
        // Global preconditions
        if (bookId == null || inputBookCopy == null || inputBookCopy.getId() == null) {
            throw new IllegalArgumentException("Missing book id or book copy id or book copy information.");
        }

        final BookCopy bookCopy = this.bookCopyRepo.findById(inputBookCopy.getId())
                .orElseThrow(() -> new NoSuchElementException("Unknown book."));

        // Precondition check
        // bookCopy.book.id is equal to bookId
        if (!bookId.equals(bookCopy.getBook().getId())) {
            throw new NoSuchElementException("Unknown book copy in book.");
        }
        // bookCopy is available
        if (!bookCopy.isAvailable()) {
            throw new HasOngoingLoanException("Cannot update a book copy that has on going loan");
        }

        // apply updates
        if (inputBookCopy.getState() != null) {
            bookCopy.setState(inputBookCopy.getState());
        }
        if (inputBookCopy.isRemoved() != null) {
            bookCopy.setRemoved(inputBookCopy.isRemoved());
        }
        return this.bookCopyRepo.save(bookCopy);
    }

    @Override
    public List<Loan> getBookCopyLoans(String bookId, String bookCopyId) {
        if (bookId == null || bookCopyId == null) {
            throw new IllegalArgumentException("Missing book id or book copy id.");
        }
        final BookCopy bookCopy = this.bookCopyRepo.findById(bookCopyId)
                .orElseThrow(() -> new NoSuchElementException("Unknown book copy."));
        if (!bookCopy.getBook().getId().equals(bookId)) {
            throw new NoSuchElementException("Unknown book copy in book.");
        }
        return bookCopy.getLoans();
    }

}
