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

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import mmiLibraryServer.mongoModel.Book;
import mmiLibraryServer.mongoModel.BookCategory;
import mmiLibraryServer.mongoModel.BookCopy;
import mmiLibraryServer.mongoModel.BookCopyRepository;
import mmiLibraryServer.mongoModel.BookState;
import mmiLibraryServer.mongoModel.Loan;
import mmiLibraryServer.mongoModel.LoanRepository;
import mmiLibraryServer.mongoModel.Member;
import mmiLibraryServer.mongoModel.TestInstanceBuilder;
import mmiLibraryServer.services.exceptions.LoanImpossibleException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.BDDMockito.given;

/**
 *
 * @author Rémi Venant
 */
@ExtendWith(MockitoExtension.class)
public class LoanServiceTest {

    private static final Log LOG = LogFactory.getLog(LoanServiceTest.class);

    private AutoCloseable mocks;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private BookCopyRepository bookCopyRepository;

    @InjectMocks
    private LoanServiceImpl testedService;

    public LoanServiceTest() {
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
        this.testedService = new LoanServiceImpl(loanRepository, bookCopyRepository);
    }

    @AfterEach
    public void tearDown() throws Exception {
        this.mocks.close();
    }

    @Test
    public void createLoanThrows() {
        // Test IllegalArg 
        assertThatThrownBy(()
                -> this.testedService.createLoan(null, TestInstanceBuilder.emptyBookCopy(), TestInstanceBuilder.now()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(()
                -> this.testedService.createLoan(TestInstanceBuilder.emptyMember(), null, TestInstanceBuilder.now()))
                .isInstanceOf(IllegalArgumentException.class);
        // Test LoanImpossibleException because of child protection
        Member minorMember = TestInstanceBuilder.withMinorStatus(TestInstanceBuilder.emptyMember());
        Book adultBook = new Book("isbn", "title", "editor", 100, 200, List.of(), List.of(
                new BookCategory("c1", "cat1", false),
                new BookCategory("c2", "cat2", true),
                new BookCategory("c3", "cat3", false)
        ));
        BookCopy bookCopy = new BookCopy(adultBook, BookState.GOOD);
        assertThatThrownBy(()
                -> this.testedService.createLoan(minorMember, bookCopy, TestInstanceBuilder.now()))
                .isInstanceOf(LoanImpossibleException.class);
        // Test LoanImpossibleException because of availability
        Member majorMember = TestInstanceBuilder.withMajorStatus(TestInstanceBuilder.emptyMember());
        BookCopy unAvailbleBookCopy = TestInstanceBuilder.withRemovedAndAvailable(new BookCopy(adultBook, BookState.GOOD), false, false);
        assertThatThrownBy(()
                -> this.testedService.createLoan(majorMember, unAvailbleBookCopy, TestInstanceBuilder.now()))
                .isInstanceOf(LoanImpossibleException.class);
        BookCopy removedBookCopy = TestInstanceBuilder.withRemovedAndAvailable(new BookCopy(adultBook, BookState.GOOD), true, true);
        assertThatThrownBy(()
                -> this.testedService.createLoan(majorMember, removedBookCopy, TestInstanceBuilder.now()))
                .isInstanceOf(LoanImpossibleException.class);
    }

    @Test
    public void createLoanLoanDateTimeBeforePreviousLoanThrows() {
        final Member majorMember = TestInstanceBuilder.withMajorStatus(TestInstanceBuilder.emptyMember());
        // Test mostRecentLoan != null ?=> mostRecentLoan.returnDate < loan.loanDateTime
        final BookCopy bookCopy = TestInstanceBuilder.withId(TestInstanceBuilder.emptyBookCopy(), "bookId");
        bookCopy.setAvailable(true);
        bookCopy.setState(BookState.GOOD);
        final Loan lP = TestInstanceBuilder.fullLoan("loanPosteriorId", null, bookCopy,
                TestInstanceBuilder.parse("2021-12-20"), BookState.NEW,
                TestInstanceBuilder.parse("2021-12-23"), BookState.GOOD);
        TestInstanceBuilder.withLoans(bookCopy, lP);
        assertThatThrownBy(()
                -> this.testedService.createLoan(majorMember, bookCopy, TestInstanceBuilder.parse("2021-12-22")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot create a loan with a dateTime before the returnTime of a previous loan");
        assertThatThrownBy(()
                -> this.testedService.createLoan(majorMember, bookCopy, TestInstanceBuilder.parse("2021-12-23")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot create a loan with a dateTime before the returnTime of a previous loan");
    }

    @Test
    public void createLoanOk() throws LoanImpossibleException {
        Member majorMember = TestInstanceBuilder.withId(
                TestInstanceBuilder.withMajorStatus(TestInstanceBuilder.emptyMember()),
                "m1");
        Member minorMember = TestInstanceBuilder.withId(
                TestInstanceBuilder.withMinorStatus(TestInstanceBuilder.emptyMember()),
                "m3");
        Book adultBook = TestInstanceBuilder.withId(new Book("isbn1", "title", "editor", 100, 200, List.of(), List.of(
                new BookCategory("c1", "cat1", false),
                new BookCategory("c2", "cat2", true),
                new BookCategory("c3", "cat3", false)
        )), "b1");
        Book childBook = TestInstanceBuilder.withId(new Book("isbn2", "title", "editor", 100, 200, List.of(), List.of(
                new BookCategory("c1", "cat1", false),
                new BookCategory("c3", "cat3", false)
        )), "b2");
        BookCopy bookCopy1 = TestInstanceBuilder.withNoLoans(TestInstanceBuilder.withId(new BookCopy(adultBook, BookState.GOOD), "bc1"));
        BookCopy bookCopy2 = TestInstanceBuilder.withNoLoans(TestInstanceBuilder.withId(new BookCopy(childBook, BookState.NEW), "bc2"));
        LocalDateTime loanDate1 = LocalDateTime.parse("2021-12-01T00:00:00");
        LocalDateTime loanDate2 = LocalDateTime.parse("2021-12-02T00:00:00");

        //Check that a loan is created and saved with an adult and a adultbook,
        //with automatic date create and proper state setting, aznd automatic book update
        given(this.loanRepository.save(Mockito.any()))
                .will(AdditionalAnswers.returnsFirstArg());
        given(this.bookCopyRepository.save(Mockito.any()))
                .will(AdditionalAnswers.returnsFirstArg());
        Loan l1 = this.testedService.createLoan(majorMember, bookCopy1, loanDate1);
        LOG.info("l1: " + l1);
        Loan l2 = this.testedService.createLoan(minorMember, bookCopy2, loanDate2);
        LOG.info("l2: " + l2);

        Mockito.verify(this.loanRepository, Mockito.times(1)).save(l1);
        Mockito.verify(this.loanRepository, Mockito.times(1)).save(l2);

        Mockito.verify(this.bookCopyRepository, Mockito.times(1)).save(bookCopy1);
        Mockito.verify(this.bookCopyRepository, Mockito.times(1)).save(bookCopy2);
        assertThat(l1).as("Loan 1 check").extracting("member", "bookCopy", "loanDateTime", "initialState", "returnDateTime", "returnState")
                .containsExactly(majorMember, bookCopy1, loanDate1, bookCopy1.getState(), null, null);
        assertThat(l2).as("Loan 2 check").extracting("member", "bookCopy", "loanDateTime", "initialState", "returnDateTime", "returnState")
                .containsExactly(minorMember, bookCopy2, loanDate2, bookCopy2.getState(), null, null);
        assertThat(bookCopy1.isAvailable()).as("Book copy 1 check").isFalse();
        assertThat(bookCopy2.isAvailable()).as("Book copy 2 check").isFalse();
    }

    @Test
    public void getLoanByIdThrows() {
        assertThatThrownBy(()
                -> this.testedService.getLoanById(null))
                .isInstanceOf(IllegalArgumentException.class);
        given(this.loanRepository.findById(Mockito.any()))
                .willThrow(NoSuchElementException.class);
        assertThatThrownBy(()
                -> this.testedService.getLoanById("aaa"))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    public void getLoanByIdOk() {
        Loan loan = TestInstanceBuilder.emptyLoan();
        given(this.loanRepository.findById("id"))
                .willReturn(Optional.of(loan));
        Loan loanGot = this.testedService.getLoanById("id");
        Mockito.verify(this.loanRepository, Mockito.times(1)).findById("id");
        assertThat(loanGot).isSameAs(loan);
    }

    @Test
    public void deleteLoanByIdThrows() {
        given(this.loanRepository.findById(null)).willThrow(IllegalArgumentException.class);
        assertThatThrownBy(()
                -> this.testedService.deleteLoanById(null))
                .isInstanceOf(IllegalArgumentException.class);
        given(this.loanRepository.findById("lid")).willReturn(Optional.empty());
        assertThatThrownBy(()
                -> this.testedService.deleteLoanById("lid"))
                .isInstanceOf(NoSuchElementException.class);
        Mockito.verify(this.loanRepository, Mockito.times(1)).findById("lid");
        Mockito.verify(this.loanRepository, Mockito.never()).deleteById(Mockito.any());
        Mockito.verify(this.bookCopyRepository, Mockito.never()).save(Mockito.any());
    }
}
