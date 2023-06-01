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
package mmiLibraryServer.controllers;

import com.fasterxml.jackson.annotation.JsonView;
import java.util.List;
import mmiLibraryServer.controllers.model.BookCopiesCreationOrder;
import mmiLibraryServer.controllers.views.CompositeViews;
import mmiLibraryServer.mongoModel.Book;
import mmiLibraryServer.mongoModel.BookCopy;
import mmiLibraryServer.mongoModel.Loan;
import mmiLibraryServer.mongoModel.utils.BookRequestFilter;
import mmiLibraryServer.mongoModel.views.BookCopyViews;
import mmiLibraryServer.mongoModel.views.BookViews;
import mmiLibraryServer.mongoModel.views.LoanViews;
import mmiLibraryServer.services.BookService;
import mmiLibraryServer.services.exceptions.HasOngoingLoanException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Rémi Venant
 */
@RestController
@RequestMapping("/api/v1/rest/books")
public class BooksController {

    private static final Log LOG = LogFactory.getLog(CategoriesController.class);

    private final BookService bookService;

    @Autowired
    public BooksController(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * GET /books?isbn&title&language&child&available&nbPages&cat*. Get books with optional filters
     *
     * @param params the optional filters
     * @return the books
     */
    @GetMapping
    @JsonView(BookViews.Normal.class)
    public List<Book> getBooks(@RequestParam(required = false) MultiValueMap<String, String> params) {
        if (params != null && params.values().stream()
                .flatMap(List::stream).anyMatch((s) -> !Strings.isBlank(s))) {
            final BookRequestFilter.Builder filterBuilder = BookRequestFilter.getBuilder();
            String currentVal = params.getFirst("isbn");
            if (!Strings.isBlank(currentVal)) {
                filterBuilder.withIsbn(currentVal);
            }
            currentVal = params.getFirst("title");
            if (!Strings.isBlank(currentVal)) {
                String language = params.getFirst("language");
                filterBuilder.withTitleAlike(currentVal, language);
            }
            currentVal = params.getFirst("child");
            if (!Strings.isBlank(currentVal)) {
                filterBuilder.withChildCompliancy();
            }
            currentVal = params.getFirst("available");
            if (!Strings.isBlank(currentVal)) {
                filterBuilder.withAvailability();
            }
            currentVal = params.getFirst("nbPages");
            if (!Strings.isBlank(currentVal)) {
                try {
                    filterBuilder.withNumberOfPages(Integer.parseInt(currentVal));
                } catch (NumberFormatException ex) {

                }
            }
            List<String> cats = params.get("cat");
            if (cats != null && !cats.isEmpty()) {
                filterBuilder.withCategories(cats);
            }
            return this.bookService.getBooks(filterBuilder.build());
        } else {
            return this.bookService.getBooks();
        }
    }

    /**
     * POST /books. Create a book
     *
     * @param bookToCreate the book to create
     * @return the created book
     */
    @PostMapping
    @JsonView(BookViews.Normal.class)
    public Book createBook(@RequestBody Book bookToCreate) {
        return this.bookService.createBook(bookToCreate);
    }

    /**
     * GET /books/:id. Get a book
     *
     * @param bookId the book id
     * @return the book
     */
    @GetMapping("{bookId}")
    @JsonView(BookViews.WithCopies.class)
    public Book getBook(@PathVariable String bookId) {
        return this.bookService.getBookById(bookId);
    }

    /**
     * PUT /books/:id. update a book
     *
     * @param bookId the book id
     * @param bookToUpdate the book to update
     * @return the udpated book
     */
    @PutMapping("{bookId}")
    @JsonView(BookViews.WithCopies.class)
    public Book updateBook(@PathVariable String bookId,
            @RequestBody Book bookToUpdate) {
        if (bookToUpdate == null) {
            throw new IllegalArgumentException("Miising book information.");
        }
        if (!bookId.equals(bookToUpdate.getId())) {
            throw new IllegalArgumentException("Wrong book id to update.");
        }
        return this.bookService.updateBook(bookToUpdate);
    }

//    @DeleteMapping(":bookId")
//    public void deleteBook(@PathVariable String bookId) {
//        this.bookService.deleteBookById(bookId);
//    }
    /**
     * GET /books/:id/loans. Get a book loans (for all its copies)
     *
     * @param bookId the book id
     * @return the loans of all the book copies of the booj
     */
    @GetMapping("{bookId}/loans")
    @JsonView(LoanViews.WithMemberAndBookCopy.class)
    public List<Loan> getBookLoans(@PathVariable String bookId) {
        return this.bookService.getBookLoans(bookId);
    }

    /**
     * POST /books/:bookId/copies. Create book copies
     *
     * @param bookId the book id
     * @param creationOrder the order of creation with number of copies and optional initial state
     * @return the created copies
     */
    @PostMapping("{bookId}/copies")
    @JsonView(BookCopyViews.Normal.class)
    public List<BookCopy> createCopies(@PathVariable String bookId,
            @RequestBody BookCopiesCreationOrder creationOrder) {
        if (creationOrder == null) {
            throw new IllegalArgumentException("Miising creation order information.");
        }
        return this.bookService.createBookCopies(bookId,
                creationOrder.getNumCopies(), creationOrder.getInitialState());
    }

    /**
     * GET /books/:bookId/copies/:bookCopyId. Get a book copy of a book
     *
     * @param bookId the book id
     * @param bookCopyId the book copy id
     * @return the book copy with its related book and its loans
     */
    @GetMapping("{bookId}/copies/{bookCopyId}")
    @JsonView(CompositeViews.BookCopyWithLoansWithMember.class)
    public BookCopy getBookCopy(@PathVariable String bookId, @PathVariable String bookCopyId) {
        return this.bookService.getBookCopyById(bookId, bookCopyId);
    }

    /**
     * PUT /books/:bookId/copies/:bookCopyId.Update a book copy of a book
     *
     * @param bookId the book id
     * @param bookCopyId the book copy id
     * @param bookCopyToUpdate the book copy to update
     * @return the book copy with its related book and its loans
     * @throws mmiLibraryServer.services.exceptions.HasOngoingLoanException
     */
    @PutMapping("{bookId}/copies/{bookCopyId}")
    @JsonView(CompositeViews.BookCopyWithLoansWithMember.class)
    public BookCopy getBookCopy(@PathVariable String bookId, @PathVariable String bookCopyId,
            @RequestBody BookCopy bookCopyToUpdate) throws HasOngoingLoanException {
        if (!bookCopyId.equals(bookCopyToUpdate.getId())) {
            throw new IllegalArgumentException("Wrong book copy id to update.");
        }
        return this.bookService.updateBookCopy(bookId, bookCopyToUpdate);
    }

    /**
     * GET /books/:bookId/copies/:bookCopyId/loans. Get loans of a book copy of a book
     *
     * @param bookId the book id
     * @param bookCopyId the book copy id
     * @return the loans of the book copy with their member
     */
    @GetMapping("{bookId}/copies/{bookCopyId}/loans")
    @JsonView(LoanViews.WithMember.class)
    public List<Loan> getBookCopyLoans(@PathVariable String bookId, @PathVariable String bookCopyId) {
        return this.bookService.getBookCopyLoans(bookId, bookCopyId);
    }

}
