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
public class LoanServiceDeleteTest {

    private static final Log LOG = LogFactory.getLog(LoanServiceDeleteTest.class);

    private AutoCloseable mocks;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private BookCopyRepository bookCopyRepository;

    @InjectMocks
    private LoanServiceImpl testedService;

    public LoanServiceDeleteTest() {
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
    public void deleteLoanByIdGlobalPreconditionsThrow() {
        // loanId != null
        given(this.loanRepository.findById(null)).willThrow(IllegalArgumentException.class);
        assertThatThrownBy(()
                -> this.testedService.deleteLoanById(null))
                .as("loanId != null")
                .isInstanceOf(IllegalArgumentException.class);
        // loan exists
        given(this.loanRepository.findById("loanId")).willReturn(Optional.empty());
        assertThatThrownBy(()
                -> this.testedService.deleteLoanById("loanId"))
                .as("loan exists")
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    public void deleteLoanByIdNotReturnedLoanPreconditionsThrow() {
        BookCopy bookCopy = TestInstanceBuilder.withId(TestInstanceBuilder.emptyBookCopy(), "bookId");
        final Loan loan = TestInstanceBuilder.fullLoan("loanId", null, bookCopy,
                TestInstanceBuilder.parse("2021-12-14"), BookState.NEW,
                TestInstanceBuilder.parse("2021-12-17"), BookState.GOOD);
        bookCopy = TestInstanceBuilder.withLoans(bookCopy, loan);
        given(this.loanRepository.findById("loanId")).willReturn(Optional.of(loan));
        assertThatThrownBy(()
                -> this.testedService.deleteLoanById("loanId"))
                .as("loan not return")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot delete already returned loan.");
    }

    @Test
    public void deleteLoanByIdOk() {
        BookCopy bookCopy = TestInstanceBuilder.withId(TestInstanceBuilder.emptyBookCopy(), "bookId");
        final Loan loan = TestInstanceBuilder.fullLoan("loanId", null, bookCopy,
                TestInstanceBuilder.parse("2021-12-14"), BookState.GOOD,
                null, null);
        bookCopy = TestInstanceBuilder.withLoans(bookCopy, loan);
        bookCopy.setAvailable(false);
        bookCopy.setState(BookState.GOOD);
        given(this.loanRepository.findById("loanId")).willReturn(Optional.of(loan));
        given(this.bookCopyRepository.save(Mockito.any())).willAnswer(AdditionalAnswers.returnsFirstArg());

        this.testedService.deleteLoanById("loanId");

        Mockito.verify(this.loanRepository, Mockito.times(1)).findById(Mockito.any());
        Mockito.verify(this.loanRepository, Mockito.times(1)).deleteById(Mockito.any());
        Mockito.verify(this.bookCopyRepository, Mockito.times(1)).save(Mockito.any());

        assertThat(bookCopy).as("book copy updated with proper values")
                .extracting("state", "available")
                .containsExactly(BookState.GOOD, true);
    }
}
