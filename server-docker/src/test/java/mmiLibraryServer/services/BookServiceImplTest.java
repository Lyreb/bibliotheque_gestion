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
import java.util.Optional;
import mmiLibraryServer.mongoModel.Book;
import mmiLibraryServer.mongoModel.BookCopy;
import mmiLibraryServer.mongoModel.BookCopyRepository;
import mmiLibraryServer.mongoModel.BookRepository;
import mmiLibraryServer.mongoModel.BookState;
import mmiLibraryServer.mongoModel.Loan;
import mmiLibraryServer.mongoModel.LoanRepository;
import mmiLibraryServer.mongoModel.TestInstanceBuilder;
import mmiLibraryServer.services.exceptions.HasOngoingLoanException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.*;
import org.mockito.AdditionalAnswers;
import static org.mockito.BDDMockito.given;
import org.mockito.Mockito;

/**
 *
 * @author Rémi Venant
 */
@ExtendWith(MockitoExtension.class)
public class BookServiceImplTest {

    private static final Log LOG = LogFactory.getLog(BookServiceImplTest.class);

    private AutoCloseable mocks;

    @Mock
    private BookRepository bookRepo;

    @Mock
    private BookCopyRepository bookCopyRepo;

    @Mock
    private LoanRepository loanRepo;

    @InjectMocks
    private BookServiceImpl testedService;

    public BookServiceImplTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
        this.mocks = MockitoAnnotations.openMocks(this);
        this.testedService = new BookServiceImpl(bookRepo, bookCopyRepo, loanRepo);
    }

    @AfterEach
    public void tearDown() throws Exception {
        this.mocks.close();
    }

    @Test
    public void getBooksThrows() {
        assertThatThrownBy(()
                -> this.testedService.getBooks(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void getBookLoansThrows() {
        assertThatThrownBy(()
                -> this.testedService.getBookLoans(null))
                .isInstanceOf(IllegalArgumentException.class);
        given(this.bookRepo.findById("badId")).willThrow(NoSuchElementException.class);
        assertThatThrownBy(()
                -> this.testedService.getBookLoans("badId"))
                .isInstanceOf(NoSuchElementException.class);
        Mockito.verify(this.bookRepo).findById("badId");
    }

    @Test
    public void getBookLoansOk() {
        List<Loan> loans = List.of(TestInstanceBuilder.emptyLoan(), TestInstanceBuilder.emptyLoan());
        List<BookCopy> copies = List.of(TestInstanceBuilder.emptyBookCopy(), TestInstanceBuilder.emptyBookCopy());
        Book book = TestInstanceBuilder.withCopies(TestInstanceBuilder.emptyBook(), copies);
        given(this.bookRepo.findById("gooId")).willReturn(Optional.of(book));
        given(this.loanRepo.findByBookCopyInOrderByLoanDateTimeDesc(copies)).willReturn(loans);
        List<Loan> givenLoans = this.testedService.getBookLoans("gooId");
        Mockito.verify(this.bookRepo).findById("gooId");
        Mockito.verify(this.loanRepo).findByBookCopyInOrderByLoanDateTimeDesc(copies);
        assertThat(givenLoans).isSameAs(loans);
    }

    @Test
    public void createBookCopiesThrows() {
        assertThatThrownBy(()
                -> this.testedService.createBookCopies(null, 1, BookState.GOOD))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(()
                -> this.testedService.createBookCopies("bookId", 0, BookState.GOOD))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(()
                -> this.testedService.createBookCopies("bookId", -5, BookState.GOOD))
                .isInstanceOf(IllegalArgumentException.class);
        Mockito.verify(this.bookRepo, Mockito.never()).findById(Mockito.any());
        Mockito.verify(this.bookCopyRepo, Mockito.never()).saveAll(Mockito.any());
    }

    @Test
    public void createBookCopiesOk() {
        final Book book = TestInstanceBuilder.emptyBook();
        given(this.bookRepo.findById("bookId")).willReturn(Optional.of(book));
        given(this.bookCopyRepo.saveAll(Mockito.anyIterable())).will(AdditionalAnswers.returnsFirstArg());
        List<BookCopy> copies = this.testedService.createBookCopies("bookId", 5, BookState.GOOD);
        List<BookCopy> copies2 = this.testedService.createBookCopies("bookId", 3, null);
        Mockito.verify(this.bookRepo, Mockito.times(2)).findById(Mockito.any());
        Mockito.verify(this.bookCopyRepo, Mockito.times(2)).saveAll(Mockito.any());
        assertThat(copies).hasSize(5).allMatch((bc) -> bc.getBook() == book
                && bc.getState() == BookState.GOOD && bc.isAvailable() && !bc.isRemoved());
        assertThat(copies2).hasSize(3).allMatch((bc) -> bc.getBook() == book
                && bc.getState() == BookState.NEW && bc.isAvailable() && !bc.isRemoved());
    }

    @Test
    public void getBookCopyByIdThrows() {
        // Global arguments preconditions
        assertThatThrownBy(()
                -> this.testedService.getBookCopyById(null, "bcId"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(()
                -> this.testedService.getBookCopyById("bookId", null))
                .isInstanceOf(IllegalArgumentException.class);

        final Book book = TestInstanceBuilder.withId(TestInstanceBuilder.emptyBook(), "bookId");
        final BookCopy bookCopy = TestInstanceBuilder.fullBookCopy("bcId", book, BookState.GOOD,
                false, true, null);
        given(this.bookCopyRepo.findById("bcId")).willReturn(Optional.of(bookCopy));
        assertThatThrownBy(()
                -> this.testedService.getBookCopyById("badBookId", "bcId"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Unknown book copy in book.");
    }

    public void getBookCopyByIdOk() {

    }

    @Test
    public void updateBookCopyThrows() {
        // Global arguments preconditions
        assertThatThrownBy(()
                -> this.testedService.updateBookCopy(null, TestInstanceBuilder.withId(TestInstanceBuilder.emptyBookCopy(), "bcId")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(()
                -> this.testedService.updateBookCopy("bookId", null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(()
                -> this.testedService.updateBookCopy("bookId", TestInstanceBuilder.emptyBookCopy()))
                .isInstanceOf(IllegalArgumentException.class);
        // Unknown bookCopy
        given(this.bookCopyRepo.findById("bcId")).willReturn(Optional.empty());
        assertThatThrownBy(()
                -> this.testedService.updateBookCopy("bookId", TestInstanceBuilder.withId(TestInstanceBuilder.emptyBookCopy(), "bcId")))
                .isInstanceOf(NoSuchElementException.class);
        // bookCopy.book.id == bookId
        final Book book = TestInstanceBuilder.withId(TestInstanceBuilder.emptyBook(), "bookId");
        final BookCopy bookCopy = TestInstanceBuilder.fullBookCopy("bcId", book, BookState.GOOD,
                false, true, null);
        given(this.bookCopyRepo.findById("bcId")).willReturn(Optional.of(bookCopy));
        assertThatThrownBy(()
                -> this.testedService.updateBookCopy("badBookId", TestInstanceBuilder.withId(TestInstanceBuilder.emptyBookCopy(), "bcId")))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Unknown book copy in book.");
    }

    @Test
    public void updateBookCopyStateWithOnGoingLoanThrow() {
        final Book book = TestInstanceBuilder.withId(TestInstanceBuilder.emptyBook(), "bookId");
        final BookCopy bookCopy = TestInstanceBuilder.fullBookCopy("bcId", book, BookState.GOOD,
                false, false, null);
        given(this.bookCopyRepo.findById("bcId")).willReturn(Optional.of(bookCopy));

        assertThatThrownBy(()
                -> this.testedService.updateBookCopy("bookId", TestInstanceBuilder.withId(TestInstanceBuilder.emptyBookCopy(), "bcId")))
                .isInstanceOf(HasOngoingLoanException.class);
    }

    @Test
    public void updateBookCopyOk() throws HasOngoingLoanException {
        final Book book = TestInstanceBuilder.withId(TestInstanceBuilder.emptyBook(), "bookId");
        final BookCopy bookCopy = TestInstanceBuilder.fullBookCopy("bcId", book, BookState.GOOD,
                false, true, null);
        Loan loan = TestInstanceBuilder.fullLoan("loanId", null, bookCopy,
                TestInstanceBuilder.parse("2021-12-10"), BookState.NEW,
                TestInstanceBuilder.parse("2021-12-12"), BookState.GOOD);
        TestInstanceBuilder.withLoans(bookCopy, loan);
        given(this.bookCopyRepo.findById("bcId")).willReturn(Optional.of(bookCopy));
        given(this.bookCopyRepo.save(Mockito.any())).willAnswer(AdditionalAnswers.returnsFirstArg());

        final Book book2 = TestInstanceBuilder.withId(TestInstanceBuilder.emptyBook(), "bookId2");
        final BookCopy inputBookCopy = TestInstanceBuilder.fullBookCopy("bcId", book2, BookState.USED,
                true, false, List.of());

        final BookCopy updatedBookCopy = this.testedService.updateBookCopy("bookId", inputBookCopy);

        Mockito.verify(this.bookCopyRepo, Mockito.times(1)).findById(Mockito.any());

        assertThat(updatedBookCopy).as("instance returned is the good one").isSameAs(bookCopy);
        assertThat(bookCopy).as("instance returned has unchanged props")
                .extracting("id", "book", "available")
                .containsExactly("bcId", book, true);
        assertThat(bookCopy.getLoans()).as("instance returned has unchanged loans")
                .hasSize(1);
        assertThat(bookCopy).as("instance returned has changed props")
                .extracting("state", "removed")
                .containsExactly(BookState.USED, true);
    }

    @Test
    public void getBookCopyLoansThrows() {
        // Global arguments preconditions
        assertThatThrownBy(()
                -> this.testedService.getBookCopyLoans(null, "bcId"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(()
                -> this.testedService.getBookCopyLoans("bookId", null))
                .isInstanceOf(IllegalArgumentException.class);

        final Book book = TestInstanceBuilder.withId(TestInstanceBuilder.emptyBook(), "bookId");
        final BookCopy bookCopy = TestInstanceBuilder.fullBookCopy("bcId", book, BookState.GOOD,
                false, true, null);
        given(this.bookCopyRepo.findById("bcId")).willReturn(Optional.of(bookCopy));
        assertThatThrownBy(()
                -> this.testedService.getBookCopyLoans("badBookId", "bcId"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Unknown book copy in book.");
    }

    public void getBookCopyLoansOk() {

    }
}
