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
import java.util.NoSuchElementException;
import java.util.Optional;
import mmiLibraryServer.mongoModel.BookCopy;
import mmiLibraryServer.mongoModel.BookCopyRepository;
import mmiLibraryServer.mongoModel.BookState;
import mmiLibraryServer.mongoModel.Loan;
import mmiLibraryServer.mongoModel.LoanRepository;
import mmiLibraryServer.mongoModel.TestInstanceBuilder;
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
public class LoanServiceUpdateTest {

    private static final Log LOG = LogFactory.getLog(LoanServiceUpdateTest.class);

    private AutoCloseable mocks;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private BookCopyRepository bookCopyRepository;

    @InjectMocks
    private LoanServiceImpl testedService;

    public LoanServiceUpdateTest() {
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
    public void updateLoanGlobalPreconditionsThrow() {
        // inputLoan != null
        assertThatThrownBy(()
                -> this.testedService.updateLoan(null))
                .as("inputLoan != null")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Missing loan to update.");
        // inputLoan.id != null
        given(this.loanRepository.findById(null)).willThrow(IllegalArgumentException.class);
        assertThatThrownBy(()
                -> this.testedService.updateLoan(TestInstanceBuilder.emptyLoan()))
                .as("inputLoan.id != null")
                .isInstanceOf(IllegalArgumentException.class);
        given(this.loanRepository.findById("loanId")).willReturn(Optional.empty());
        assertThatThrownBy(()
                -> this.testedService.updateLoan(TestInstanceBuilder.withId(TestInstanceBuilder.emptyLoan(), "loanId")))
                .as("inputLoan.id != null")
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    public void updateLoanDatePreconditions1Throw() {
        // update loanDate triggers:
        //   inputLoan.loanDate != null && inputLoan.loanDate != loan.loanDate
        BookCopy bookCopy = TestInstanceBuilder.withId(TestInstanceBuilder.emptyBookCopy(), "bookId");
        final Loan lA = TestInstanceBuilder.fullLoan("loanAncestorId", null, bookCopy,
                TestInstanceBuilder.parse("2021-12-11"), BookState.NEW,
                TestInstanceBuilder.parse("2021-12-13"), BookState.VERY_GOOD);
        final Loan loan = TestInstanceBuilder.fullLoan("loanId", null, bookCopy,
                TestInstanceBuilder.parse("2021-12-14"), BookState.NEW,
                TestInstanceBuilder.parse("2021-12-17"), BookState.GOOD);
        bookCopy = TestInstanceBuilder.withLoans(bookCopy, lA, loan);
        given(this.loanRepository.findById("loanId")).willReturn(Optional.of(loan));

        // loan.returnDate != null ?=> inputLoan.loanDateTime < loan.returnDateTime
        final Loan inputLoan = TestInstanceBuilder.withId(TestInstanceBuilder.emptyLoan(), "loanId");
        inputLoan.setLoanDateTime(TestInstanceBuilder.parse("2021-12-18"));
        assertThatThrownBy(()
                -> this.testedService.updateLoan(inputLoan))
                .as("loan.returnDate != null ?=> inputLoan.loanDateTime < loan.returnDateTime (A)")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot update loan dateTime with a value after the return date of the loan.");
        inputLoan.setLoanDateTime(TestInstanceBuilder.parse("2021-12-17"));
        assertThatThrownBy(()
                -> this.testedService.updateLoan(inputLoan))
                .as("loan.returnDate != null ?=> inputLoan.loanDateTime < loan.returnDateTime (B)")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot update loan dateTime with a value after the return date of the loan.");
        Mockito.verify(this.loanRepository, Mockito.times(2)).findById(Mockito.any());
        Mockito.verify(this.loanRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(this.bookCopyRepository, Mockito.never()).save(Mockito.any());

    }

    @Test
    public void updateLoanDatePreconditions2Throw() {
        /*
        update loanDate triggers: inputLoan.loanDate != null
         */
        // loanAnterior lA != null ?=> inputLoan.loanDateTime > lA.returnDateTime
        BookCopy bookCopy = TestInstanceBuilder.withId(TestInstanceBuilder.emptyBookCopy(), "bookId");
        final Loan lA = TestInstanceBuilder.fullLoan("loanAncestorId", null, bookCopy,
                TestInstanceBuilder.parse("2021-12-11"), BookState.NEW,
                TestInstanceBuilder.parse("2021-12-13"), BookState.VERY_GOOD);
        final Loan loan = TestInstanceBuilder.fullLoan("loanId", null, bookCopy,
                TestInstanceBuilder.parse("2021-12-14"), BookState.NEW,
                null, null);
        bookCopy = TestInstanceBuilder.withLoans(bookCopy, lA, loan);
        given(this.loanRepository.findById("loanId")).willReturn(Optional.of(loan));

        final Loan inputLoan = TestInstanceBuilder.withId(TestInstanceBuilder.emptyLoan(), "loanId");
        inputLoan.setLoanDateTime(TestInstanceBuilder.parse("2021-12-12"));
        assertThatThrownBy(()
                -> this.testedService.updateLoan(inputLoan))
                .as("loanAnterior lA != null ?=> inputLoan.loanDateTime > lA.returnDateTime (A)")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot update loan dateTime with a value before the return date of the previous loan.");
        inputLoan.setLoanDateTime(TestInstanceBuilder.parse("2021-12-13"));
        assertThatThrownBy(()
                -> this.testedService.updateLoan(inputLoan))
                .as("loanAnterior lA != null ?=> inputLoan.loanDateTime > lA.returnDateTime (B)")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot update loan dateTime with a value before the return date of the previous loan.");
        Mockito.verify(this.loanRepository, Mockito.times(2)).findById(Mockito.any());
        Mockito.verify(this.loanRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(this.bookCopyRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void updateLoanDatePostconditionsOK() {
        /*
        update loanDate triggers: inputLoan.loanDate != null
         */
        // loanAnterior lA != null ?=> inputLoan.loanDateTime > lA.returnDateTime
        BookCopy bookCopy = TestInstanceBuilder.withId(TestInstanceBuilder.emptyBookCopy(), "bookId");
        final Loan lA = TestInstanceBuilder.fullLoan("loanAncestorId", null, bookCopy,
                TestInstanceBuilder.parse("2021-12-10"), BookState.NEW,
                TestInstanceBuilder.parse("2021-12-12"), BookState.VERY_GOOD);
        final Loan loan = TestInstanceBuilder.fullLoan("loanId", null, bookCopy,
                TestInstanceBuilder.parse("2021-12-14"), BookState.VERY_GOOD,
                TestInstanceBuilder.parse("2021-12-17"), BookState.GOOD);
        bookCopy = TestInstanceBuilder.withLoans(bookCopy, lA, loan);
        given(this.loanRepository.findById("loanId")).willReturn(Optional.of(loan));
        given(this.loanRepository.save(Mockito.any())).willAnswer(AdditionalAnswers.returnsFirstArg());

        final Loan inputLoan = TestInstanceBuilder.fullLoan("loanId", null, null,
                TestInstanceBuilder.parse("2021-12-13"), null,
                TestInstanceBuilder.parse("2021-12-17"), BookState.GOOD);

        final Loan updatedLoan = this.testedService.updateLoan(inputLoan);

        Mockito.verify(this.loanRepository, Mockito.times(1)).findById(Mockito.any());
        Mockito.verify(this.loanRepository, Mockito.times(1)).save(Mockito.any());
        Mockito.verify(this.bookCopyRepository, Mockito.never()).save(Mockito.any());

        assertThat(updatedLoan).as("instance returned is the good one").isSameAs(loan);
        assertThat(updatedLoan).as("instance returned contains proper props")
                .extracting("loanDateTime", "initialState", "returnDateTime", "returnState")
                .containsExactly(TestInstanceBuilder.parse("2021-12-13"), BookState.VERY_GOOD,
                        TestInstanceBuilder.parse("2021-12-17"), BookState.GOOD);
    }

    @Test
    public void setReturnLoanPreconditionsThrow() {
        /*
        set return date and state loanDate triggers: loan.returnDate == null && inputLoan.returnDate != null && inputLoan.returnState != null
         */
        BookCopy bookCopy = TestInstanceBuilder.withId(TestInstanceBuilder.emptyBookCopy(), "bookId");
        final Loan loan = TestInstanceBuilder.fullLoan("loanId", null, bookCopy,
                TestInstanceBuilder.parse("2021-12-14"), BookState.NEW,
                null, null);
        bookCopy = TestInstanceBuilder.withLoans(bookCopy, loan);
        given(this.loanRepository.findById("loanId")).willReturn(Optional.of(loan));

        //test inputLoan.returnDateTime > loan.loanDateTime (A)
        Loan inputLoan = TestInstanceBuilder.fullLoan("loanId", null, null,
                null, null,
                TestInstanceBuilder.parse("2021-12-13"), BookState.GOOD);

        assertThatThrownBy(()
                -> this.testedService.updateLoan(inputLoan))
                .as("inputLoan.returnDateTime > loan.loanDateTime (A)")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot set loan returnDate with a value before the date of the loan.");

        //test inputLoan.returnDate > loan.loanDateTime (B)
        inputLoan.setReturnDateTime(TestInstanceBuilder.parse("2021-12-14"));
        assertThatThrownBy(()
                -> this.testedService.updateLoan(inputLoan))
                .as("inputLoan.returnDateTime > loan.loanDateTime (B)")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot set loan returnDate with a value before the date of the loan.");

        //test inputLoan.returnState != BookState.NEW
        inputLoan.setReturnDateTime(TestInstanceBuilder.parse("2021-12-17"));
        inputLoan.setReturnState(BookState.NEW);
        assertThatThrownBy(()
                -> this.testedService.updateLoan(inputLoan))
                .as("inputLoan.returnState != BookState.NEW")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot set loan returnState with a value NEW or a value > loan.initialState.");

        //test inputLoan.returnState <= loan.initialState
        loan.setInitialState(BookState.GOOD);
        inputLoan.setReturnState(BookState.VERY_GOOD);
        assertThatThrownBy(()
                -> this.testedService.updateLoan(inputLoan))
                .as("inputLoan.returnState <= loan.initialState")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot set loan returnState with a value NEW or a value > loan.initialState.");
        Mockito.verify(this.loanRepository, Mockito.times(4)).findById(Mockito.any());
        Mockito.verify(this.loanRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(this.bookCopyRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void setReturnLoanPostconditionsOK() {
        /*
        set return date and state loanDate triggers: loan.returnDate == null && inputLoan.returnDate != null && inputLoan.returnState != null
         */
        BookCopy bookCopy = TestInstanceBuilder.withId(TestInstanceBuilder.emptyBookCopy(), "bookId");
        bookCopy.setState(BookState.GOOD);
        bookCopy.setAvailable(false);
        final Loan loan = TestInstanceBuilder.fullLoan("loanId", null, bookCopy,
                TestInstanceBuilder.parse("2021-12-14"), BookState.GOOD,
                null, null);
        bookCopy = TestInstanceBuilder.withLoans(bookCopy, loan);
        given(this.loanRepository.findById("loanId")).willReturn(Optional.of(loan));
        given(this.loanRepository.save(Mockito.any())).willAnswer(AdditionalAnswers.returnsFirstArg());
        given(this.bookCopyRepository.save(Mockito.any())).willAnswer(AdditionalAnswers.returnsFirstArg());

        Loan inputLoan = TestInstanceBuilder.fullLoan("loanId", null, null,
                null, null,
                TestInstanceBuilder.parse("2021-12-17"), BookState.USED);

        final Loan updatedLoan = this.testedService.updateLoan(inputLoan);
        Mockito.verify(this.loanRepository, Mockito.times(1)).findById(Mockito.any());
        Mockito.verify(this.loanRepository, Mockito.times(1)).save(Mockito.any());
        Mockito.verify(this.bookCopyRepository, Mockito.times(1)).save(Mockito.any());

        assertThat(updatedLoan).as("instance returned is the good one").isSameAs(loan);
        assertThat(updatedLoan).as("instance returned contains proper props")
                .extracting("loanDateTime", "initialState", "returnDateTime", "returnState")
                .containsExactly(TestInstanceBuilder.parse("2021-12-14"), BookState.GOOD,
                        TestInstanceBuilder.parse("2021-12-17"), BookState.USED);
        assertThat(bookCopy).as("book copy updated with proper values")
                .extracting("state", "available")
                .containsExactly(BookState.USED, true);
    }

    @Test
    public void updateReturnDatePreconditionsThrow() {
        /*
        update return date triggers: loan.returnDate != null && inputLoan.returnDate != null && !inputLoan.returnDate.equals(loan.returnDate)
         */
        BookCopy bookCopy = TestInstanceBuilder.withId(TestInstanceBuilder.emptyBookCopy(), "bookId");
        final Loan loan = TestInstanceBuilder.fullLoan("loanId", null, bookCopy,
                TestInstanceBuilder.parse("2021-12-14"), BookState.NEW,
                TestInstanceBuilder.parse("2021-12-17"), BookState.GOOD);
        final Loan lP = TestInstanceBuilder.fullLoan("loanPosteriorId", null, bookCopy,
                TestInstanceBuilder.parse("2021-12-20"), BookState.GOOD,
                TestInstanceBuilder.parse("2021-12-23"), BookState.USED);
        bookCopy = TestInstanceBuilder.withLoans(bookCopy, loan, lP);
        given(this.loanRepository.findById("loanId")).willReturn(Optional.of(loan));

        // test inputLoan.returnDateTime > loan.loanDateTime (A)
        final Loan inputLoan = TestInstanceBuilder.fullLoan("loanId", null, null,
                null, null,
                TestInstanceBuilder.parse("2021-12-13"), null);

        assertThatThrownBy(()
                -> this.testedService.updateLoan(inputLoan))
                .as("inputLoan.returnDateTime > loan.loanDateTime (A)")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot update loan returnDate with a value before the date of the loan.");

        // test inputLoan.returnDateTime > loan.loanDateTime (B)
        final Loan inputLoan2 = TestInstanceBuilder.fullLoan("loanId", null, null,
                null, null,
                TestInstanceBuilder.parse("2021-12-14"), null);

        assertThatThrownBy(()
                -> this.testedService.updateLoan(inputLoan2))
                .as("inputLoan.returnDateTime > loan.loanDateTime (B)")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot update loan returnDate with a value before the date of the loan.");

        // test lP != null ?=> inputLoan.returnDate < lP.loanDateTime (A)
        final Loan inputLoan3 = TestInstanceBuilder.fullLoan("loanId", null, null,
                null, null,
                TestInstanceBuilder.parse("2021-12-21"), null);

        assertThatThrownBy(()
                -> this.testedService.updateLoan(inputLoan3))
                .as("lP != null ?=> inputLoan.returnDate < lP.loanDateTime (A)")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot update loan returnDate with a value after the date of the next loan.");

        // test lP != null ?=> inputLoan.returnDate < lP.loanDateTime (B)
        final Loan inputLoan4 = TestInstanceBuilder.fullLoan("loanId", null, null,
                null, null,
                TestInstanceBuilder.parse("2021-12-20"), null);

        assertThatThrownBy(()
                -> this.testedService.updateLoan(inputLoan4))
                .as("lP != null ?=> inputLoan.returnDate < lP.loanDateTime (B)")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot update loan returnDate with a value after the date of the next loan.");

        Mockito.verify(this.loanRepository, Mockito.times(4)).findById(Mockito.any());
        Mockito.verify(this.loanRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(this.bookCopyRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void updateReturnDatePostconditionsOK() {
        /*
        update return date triggers: loan.returnDate != null && inputLoan.returnDate != null && !inputLoan.returnDate.equals(loan.returnDate)
         */
        BookCopy bookCopy = TestInstanceBuilder.withId(TestInstanceBuilder.emptyBookCopy(), "bookId");
        final Loan loan = TestInstanceBuilder.fullLoan("loanId", null, bookCopy,
                TestInstanceBuilder.parse("2021-12-14"), BookState.NEW,
                TestInstanceBuilder.parse("2021-12-17"), BookState.GOOD);
        bookCopy = TestInstanceBuilder.withLoans(bookCopy, loan);
        given(this.loanRepository.findById("loanId")).willReturn(Optional.of(loan));
        given(this.loanRepository.save(Mockito.any())).willAnswer(AdditionalAnswers.returnsFirstArg());

        Loan inputLoan = TestInstanceBuilder.fullLoan("loanId", null, null,
                null, null,
                TestInstanceBuilder.parse("2021-12-19"), null);

        final Loan updatedLoan = this.testedService.updateLoan(inputLoan);
        Mockito.verify(this.loanRepository, Mockito.times(1)).findById(Mockito.any());
        Mockito.verify(this.loanRepository, Mockito.times(1)).save(Mockito.any());
        Mockito.verify(this.bookCopyRepository, Mockito.never()).save(Mockito.any());

        assertThat(updatedLoan).as("instance returned is the good one").isSameAs(loan);
        assertThat(updatedLoan).as("instance returned contains proper props")
                .extracting("loanDateTime", "initialState", "returnDateTime", "returnState")
                .containsExactly(TestInstanceBuilder.parse("2021-12-14"), BookState.NEW,
                        TestInstanceBuilder.parse("2021-12-19"), BookState.GOOD);
    }

    @Test
    public void updateReturnStatePreconditions1Throw() {
        /*
        update return state triggers: loan.returnDate != null && inputLoan.returnState != null && !inputLoan.returnState.equals(loan.retureturnStaternDate)
         */
        BookCopy bookCopy = TestInstanceBuilder.withId(TestInstanceBuilder.emptyBookCopy(), "bookId");
        final Loan loan = TestInstanceBuilder.fullLoan("loanId", null, bookCopy,
                TestInstanceBuilder.parse("2021-12-14"), BookState.GOOD,
                TestInstanceBuilder.parse("2021-12-17"), BookState.USED);
        bookCopy = TestInstanceBuilder.withLoans(bookCopy, loan);
        given(this.loanRepository.findById("loanId")).willReturn(Optional.of(loan));

        // test inputLoan.returnState <= loan.initialState
        final Loan inputLoan = TestInstanceBuilder.fullLoan("loanId", null, null,
                null, null,
                null, BookState.VERY_GOOD);

        assertThatThrownBy(()
                -> this.testedService.updateLoan(inputLoan))
                .as("inputLoan.returnState <= loan.initialState")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot update loan returnState with a value higher the initial state of the loan.");

        Mockito.verify(this.loanRepository, Mockito.times(1)).findById(Mockito.any());
        Mockito.verify(this.loanRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(this.bookCopyRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void updateReturnStatePreconditions2Throw() {
        /*
        update return state triggers: loan.returnDate != null && inputLoan.returnState != null && !inputLoan.returnState.equals(loan.retureturnStaternDate)
         */
        BookCopy bookCopy = TestInstanceBuilder.withId(TestInstanceBuilder.emptyBookCopy(), "bookId");
        final Loan loan = TestInstanceBuilder.fullLoan("loanId", null, bookCopy,
                TestInstanceBuilder.parse("2021-12-14"), BookState.GOOD,
                TestInstanceBuilder.parse("2021-12-17"), BookState.USED);
        final Loan lP = TestInstanceBuilder.fullLoan("loanPosteriorId", null, bookCopy,
                TestInstanceBuilder.parse("2021-12-20"), BookState.USED,
                TestInstanceBuilder.parse("2021-12-23"), BookState.USED);
        bookCopy = TestInstanceBuilder.withLoans(bookCopy, loan, lP);
        given(this.loanRepository.findById("loanId")).willReturn(Optional.of(loan));

        // test lP != null ?=> inputLoan.returnState >= lp.initialState
        final Loan inputLoan = TestInstanceBuilder.fullLoan("loanId", null, null,
                null, null,
                null, BookState.BAD);

        assertThatThrownBy(()
                -> this.testedService.updateLoan(inputLoan))
                .as("lP != null ?=> inputLoan.returnState <= lp.initialState")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot update loan returnState with a value higher the initial state of the next loan.");

        Mockito.verify(this.loanRepository, Mockito.times(1)).findById(Mockito.any());
        Mockito.verify(this.loanRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(this.bookCopyRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void updateReturnStatePostconditionsWithoutBookUpdateOK() {
        /*
        update return state triggers: loan.returnDate != null && inputLoan.returnState != null && !inputLoan.returnState.equals(loan.retureturnStaternDate)
         */
        BookCopy bookCopy = TestInstanceBuilder.withId(TestInstanceBuilder.emptyBookCopy(), "bookId");
        final Loan loan = TestInstanceBuilder.fullLoan("loanId", null, bookCopy,
                TestInstanceBuilder.parse("2021-12-14"), BookState.GOOD,
                TestInstanceBuilder.parse("2021-12-17"), BookState.USED);
        final Loan lP = TestInstanceBuilder.fullLoan("loanPosteriorId", null, bookCopy,
                TestInstanceBuilder.parse("2021-12-20"), BookState.USED,
                TestInstanceBuilder.parse("2021-12-23"), BookState.USED);
        bookCopy = TestInstanceBuilder.withLoans(bookCopy, loan, lP);
        given(this.loanRepository.findById("loanId")).willReturn(Optional.of(loan));
        given(this.loanRepository.save(Mockito.any())).willAnswer(AdditionalAnswers.returnsFirstArg());

        Loan inputLoan = TestInstanceBuilder.fullLoan("loanId", null, null,
                null, null,
                null, BookState.GOOD);

        final Loan updatedLoan = this.testedService.updateLoan(inputLoan);
        Mockito.verify(this.loanRepository, Mockito.times(1)).findById(Mockito.any());
        Mockito.verify(this.loanRepository, Mockito.times(1)).save(Mockito.any());
        Mockito.verify(this.bookCopyRepository, Mockito.never()).save(Mockito.any());

        assertThat(updatedLoan).as("instance returned is the good one").isSameAs(loan);
        assertThat(updatedLoan).as("instance returned contains proper props")
                .extracting("loanDateTime", "initialState", "returnDateTime", "returnState")
                .containsExactly(TestInstanceBuilder.parse("2021-12-14"), BookState.GOOD,
                        TestInstanceBuilder.parse("2021-12-17"), BookState.GOOD);

    }

    @Test
    public void updateReturnStatePostconditionsWithBookUpdateOK() {
        /*
        update return state triggers: loan.returnDate != null && inputLoan.returnState != null && !inputLoan.returnState.equals(loan.retureturnStaternDate)
         */
        BookCopy bookCopy = TestInstanceBuilder.withId(TestInstanceBuilder.emptyBookCopy(), "bookId");
        final Loan loan = TestInstanceBuilder.fullLoan("loanId", null, bookCopy,
                TestInstanceBuilder.parse("2021-12-14"), BookState.GOOD,
                TestInstanceBuilder.parse("2021-12-17"), BookState.USED);
        bookCopy = TestInstanceBuilder.withLoans(bookCopy, loan);
        bookCopy.setAvailable(true);
        bookCopy.setState(BookState.USED);
        given(this.loanRepository.findById("loanId")).willReturn(Optional.of(loan));
        given(this.loanRepository.save(Mockito.any())).willAnswer(AdditionalAnswers.returnsFirstArg());
        given(this.bookCopyRepository.save(Mockito.any())).willAnswer(AdditionalAnswers.returnsFirstArg());

        Loan inputLoan = TestInstanceBuilder.fullLoan("loanId", null, null,
                null, null,
                null, BookState.GOOD);

        final Loan updatedLoan = this.testedService.updateLoan(inputLoan);
        Mockito.verify(this.loanRepository, Mockito.times(1)).findById(Mockito.any());
        Mockito.verify(this.loanRepository, Mockito.times(1)).save(Mockito.any());
        Mockito.verify(this.bookCopyRepository, Mockito.times(1)).save(Mockito.any());

        assertThat(updatedLoan).as("instance returned is the good one").isSameAs(loan);
        assertThat(updatedLoan).as("instance returned contains proper props")
                .extracting("loanDateTime", "initialState", "returnDateTime", "returnState")
                .containsExactly(TestInstanceBuilder.parse("2021-12-14"), BookState.GOOD,
                        TestInstanceBuilder.parse("2021-12-17"), BookState.GOOD);
        assertThat(bookCopy).as("book copy updated with proper values")
                .extracting("state", "available")
                .containsExactly(BookState.GOOD, true);
    }

    @Test
    public void resetReturnPreconditionsThrow() {
        /*
        reset return date and state triugger: loan.returnDate != null && inputLoan.returnDate == null && inputLoan.returnState == null
         */
        BookCopy bookCopy = TestInstanceBuilder.withId(TestInstanceBuilder.emptyBookCopy(), "bookId");
        final Loan loan = TestInstanceBuilder.fullLoan("loanId", null, bookCopy,
                TestInstanceBuilder.parse("2021-12-14"), BookState.GOOD,
                TestInstanceBuilder.parse("2021-12-17"), BookState.USED);
        final Loan lP = TestInstanceBuilder.fullLoan("loanPosteriorId", null, bookCopy,
                TestInstanceBuilder.parse("2021-12-20"), BookState.USED,
                TestInstanceBuilder.parse("2021-12-23"), BookState.USED);
        bookCopy = TestInstanceBuilder.withLoans(bookCopy, loan, lP);
        given(this.loanRepository.findById("loanId")).willReturn(Optional.of(loan));

        // test lP == null
        Loan inputLoan = TestInstanceBuilder.fullLoan("loanId", null, null,
                null, null,
                null, null);

        assertThatThrownBy(()
                -> this.testedService.updateLoan(inputLoan))
                .as("lP == null")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot reset loan return with an existing next loan.");

        Mockito.verify(this.loanRepository, Mockito.times(1)).findById(Mockito.any());
        Mockito.verify(this.loanRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(this.bookCopyRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void resetReturnDateAndStatePostconditionsOK() {
        /*
        reset return date and state triugger: loan.returnDate != null && inputLoan.returnDate == null && inputLoan.returnState == null
         */
        BookCopy bookCopy = TestInstanceBuilder.withId(TestInstanceBuilder.emptyBookCopy(), "bookId");
        final Loan loan = TestInstanceBuilder.fullLoan("loanId", null, bookCopy,
                TestInstanceBuilder.parse("2021-12-14"), BookState.GOOD,
                TestInstanceBuilder.parse("2021-12-17"), BookState.USED);
        bookCopy = TestInstanceBuilder.withLoans(bookCopy, loan);
        bookCopy.setAvailable(true);
        bookCopy.setState(BookState.USED);
        given(this.loanRepository.findById("loanId")).willReturn(Optional.of(loan));
        given(this.loanRepository.save(Mockito.any())).willAnswer(AdditionalAnswers.returnsFirstArg());
        given(this.bookCopyRepository.save(Mockito.any())).willAnswer(AdditionalAnswers.returnsFirstArg());

        Loan inputLoan = TestInstanceBuilder.fullLoan("loanId", null, null,
                null, null,
                null, null);

        final Loan updatedLoan = this.testedService.updateLoan(inputLoan);
        Mockito.verify(this.loanRepository, Mockito.times(1)).findById(Mockito.any());
        Mockito.verify(this.loanRepository, Mockito.times(1)).save(Mockito.any());
        Mockito.verify(this.bookCopyRepository, Mockito.times(1)).save(Mockito.any());

        assertThat(updatedLoan).as("instance returned is the good one").isSameAs(loan);
        assertThat(updatedLoan).as("instance returned contains proper props")
                .extracting("loanDateTime", "initialState", "returnDateTime", "returnState")
                .containsExactly(TestInstanceBuilder.parse("2021-12-14"), BookState.GOOD,
                        null, null);
        assertThat(bookCopy).as("book copy updated with proper values")
                .extracting("state", "available")
                .containsExactly(BookState.GOOD, false);
    }

    @Test
    public void updateLoadDateAndSetReturnLoanPostconditionsOK() {
        BookCopy bookCopy = TestInstanceBuilder.withId(TestInstanceBuilder.emptyBookCopy(), "bookId");
        final Loan loan = TestInstanceBuilder.fullLoan("loanId", null, bookCopy,
                TestInstanceBuilder.parse("2021-12-14"), BookState.GOOD,
                null, null);
        bookCopy = TestInstanceBuilder.withLoans(bookCopy, loan);
        bookCopy.setAvailable(false);
        bookCopy.setState(BookState.GOOD);
        given(this.loanRepository.findById("loanId")).willReturn(Optional.of(loan));
        given(this.loanRepository.save(Mockito.any())).willAnswer(AdditionalAnswers.returnsFirstArg());
        given(this.bookCopyRepository.save(Mockito.any())).willAnswer(AdditionalAnswers.returnsFirstArg());

        Loan inputLoan = TestInstanceBuilder.fullLoan("loanId", null, null,
                TestInstanceBuilder.parse("2021-12-11"), BookState.NEW,
                TestInstanceBuilder.parse("2021-12-13"), BookState.USED);

        final Loan updatedLoan = this.testedService.updateLoan(inputLoan);
        Mockito.verify(this.loanRepository, Mockito.times(1)).findById(Mockito.any());
        Mockito.verify(this.loanRepository, Mockito.times(1)).save(Mockito.any());
        Mockito.verify(this.bookCopyRepository, Mockito.times(1)).save(Mockito.any());

        assertThat(updatedLoan).as("instance returned is the good one").isSameAs(loan);
        assertThat(updatedLoan).as("instance returned contains proper props")
                .extracting("loanDateTime", "initialState", "returnDateTime", "returnState")
                .containsExactly(TestInstanceBuilder.parse("2021-12-11"), BookState.GOOD,
                        TestInstanceBuilder.parse("2021-12-13"), BookState.USED);
        assertThat(bookCopy).as("book copy updated with proper values")
                .extracting("state", "available")
                .containsExactly(BookState.USED, true);
    }

    @Test
    public void updateLoadDateAndReturnDateAndReturnStatePostconditionsOK() {
        BookCopy bookCopy = TestInstanceBuilder.withId(TestInstanceBuilder.emptyBookCopy(), "bookId");
        final Loan loan = TestInstanceBuilder.fullLoan("loanId", null, bookCopy,
                TestInstanceBuilder.parse("2021-12-12"), BookState.GOOD,
                TestInstanceBuilder.parse("2021-12-13"), BookState.USED);
        bookCopy = TestInstanceBuilder.withLoans(bookCopy, loan);
        bookCopy.setAvailable(true);
        bookCopy.setState(BookState.USED);
        given(this.loanRepository.findById("loanId")).willReturn(Optional.of(loan));
        given(this.loanRepository.save(Mockito.any())).willAnswer(AdditionalAnswers.returnsFirstArg());
        given(this.bookCopyRepository.save(Mockito.any())).willAnswer(AdditionalAnswers.returnsFirstArg());

        Loan inputLoan = TestInstanceBuilder.fullLoan("loanId", null, null,
                TestInstanceBuilder.parse("2021-12-10"), BookState.NEW,
                TestInstanceBuilder.parse("2021-12-13"), BookState.GOOD);

        final Loan updatedLoan = this.testedService.updateLoan(inputLoan);
        Mockito.verify(this.loanRepository, Mockito.times(1)).findById(Mockito.any());
        Mockito.verify(this.loanRepository, Mockito.times(1)).save(Mockito.any());
        Mockito.verify(this.bookCopyRepository, Mockito.times(1)).save(Mockito.any());

        assertThat(updatedLoan).as("instance returned is the good one").isSameAs(loan);
        assertThat(updatedLoan).as("instance returned contains proper props")
                .extracting("loanDateTime", "initialState", "returnDateTime", "returnState")
                .containsExactly(TestInstanceBuilder.parse("2021-12-10"), BookState.GOOD,
                        TestInstanceBuilder.parse("2021-12-13"), BookState.GOOD);
        assertThat(bookCopy).as("book copy updated with proper values")
                .extracting("state", "available")
                .containsExactly(BookState.GOOD, true);
    }

    @Test
    public void updateLoadAndresetReturnPostconditionsOK() {
        BookCopy bookCopy = TestInstanceBuilder.withId(TestInstanceBuilder.emptyBookCopy(), "bookId");
        final Loan loan = TestInstanceBuilder.fullLoan("loanId", null, bookCopy,
                TestInstanceBuilder.parse("2021-12-14"), BookState.GOOD,
                TestInstanceBuilder.parse("2021-12-17"), BookState.USED);
        bookCopy = TestInstanceBuilder.withLoans(bookCopy, loan);
        bookCopy.setAvailable(true);
        bookCopy.setState(BookState.USED);
        given(this.loanRepository.findById("loanId")).willReturn(Optional.of(loan));
        given(this.loanRepository.save(Mockito.any())).willAnswer(AdditionalAnswers.returnsFirstArg());
        given(this.bookCopyRepository.save(Mockito.any())).willAnswer(AdditionalAnswers.returnsFirstArg());

        Loan inputLoan = TestInstanceBuilder.fullLoan("loanId", null, null,
                TestInstanceBuilder.parse("2021-12-12"), null,
                null, null);

        final Loan updatedLoan = this.testedService.updateLoan(inputLoan);
        Mockito.verify(this.loanRepository, Mockito.times(1)).findById(Mockito.any());
        Mockito.verify(this.loanRepository, Mockito.times(1)).save(Mockito.any());
        Mockito.verify(this.bookCopyRepository, Mockito.times(1)).save(Mockito.any());

        assertThat(updatedLoan).as("instance returned is the good one").isSameAs(loan);
        assertThat(updatedLoan).as("instance returned contains proper props")
                .extracting("loanDateTime", "initialState", "returnDateTime", "returnState")
                .containsExactly(TestInstanceBuilder.parse("2021-12-12"), BookState.GOOD,
                        null, null);
        assertThat(bookCopy).as("book copy updated with proper values")
                .extracting("state", "available")
                .containsExactly(BookState.GOOD, false);
    }

    @Test
    public void unmanagedCasPostconditionsDoNothing() {
        BookCopy bookCopy = TestInstanceBuilder.withId(TestInstanceBuilder.emptyBookCopy(), "bookId");
        final Loan loan = TestInstanceBuilder.fullLoan("loanId", null, bookCopy,
                TestInstanceBuilder.parse("2021-12-14"), BookState.GOOD,
                TestInstanceBuilder.parse("2021-12-17"), BookState.USED);
        bookCopy = TestInstanceBuilder.withLoans(bookCopy, loan);
        bookCopy.setAvailable(true);
        bookCopy.setState(BookState.USED);
        given(this.loanRepository.findById("loanId")).willReturn(Optional.of(loan));

        // test: inputLoan.loanDate == null && loan.returnDate != null && inputLoan.returnDate.equals(loan.returnDate) && inputLoan.returnState == null
        Loan inputLoan = TestInstanceBuilder.fullLoan("loanId", null, null,
                null, null,
                TestInstanceBuilder.parse("2021-12-17"), null);
        this.testedService.updateLoan(inputLoan);

        // test: inputLoan.loanDate == null && loan.returnDate != null && inputLoan.returnDate.equals(loan.returnDate) && inputLoan.returnState.equals(loan.returnState)
        inputLoan = TestInstanceBuilder.fullLoan("loanId", null, null,
                null, null,
                TestInstanceBuilder.parse("2021-12-17"), BookState.USED);
        this.testedService.updateLoan(inputLoan);

        // test: inputLoan.loanDate == null && inputLoan.returnDate == null && inputLoan.returnState.equals(loan.returnState)
        inputLoan = TestInstanceBuilder.fullLoan("loanId", null, null,
                null, null,
                null, BookState.USED);
        this.testedService.updateLoan(inputLoan);

        Mockito.verify(this.loanRepository, Mockito.times(3)).findById(Mockito.any());
        Mockito.verify(this.loanRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(this.bookCopyRepository, Mockito.never()).save(Mockito.any());
    }

}
