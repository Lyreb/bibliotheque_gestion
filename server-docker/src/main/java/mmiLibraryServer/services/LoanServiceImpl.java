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
import mmiLibraryServer.mongoModel.BookCategory;
import mmiLibraryServer.mongoModel.BookCopy;
import mmiLibraryServer.mongoModel.BookCopyRepository;
import mmiLibraryServer.mongoModel.BookState;
import mmiLibraryServer.mongoModel.Loan;
import mmiLibraryServer.mongoModel.LoanRepository;
import mmiLibraryServer.mongoModel.Member;
import mmiLibraryServer.services.exceptions.LoanImpossibleException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Rémi Venant
 */
@Service
public class LoanServiceImpl implements LoanService {

    private static final Log LOG = LogFactory.getLog(LoanServiceImpl.class);

    private final LoanRepository loanRepository;

    private final BookCopyRepository bookCopyRepository;

    @Autowired
    public LoanServiceImpl(LoanRepository loanRepository, BookCopyRepository bookCopyRepository) {
        this.loanRepository = loanRepository;
        this.bookCopyRepository = bookCopyRepository;
    }

    @Override
    public List<Loan> getLoans() {
        return this.loanRepository.findByOrderByLoanDateTimeDesc();
    }

    @Override
    public Loan createLoan(Member member, BookCopy bookCopy, LocalDateTime givenLoanDateTime) throws LoanImpossibleException {
        // Global preconditions
        if (member == null || bookCopy == null) {
            throw new IllegalArgumentException("Missing member or bookCopy to create loan.");
        }
        // Preconditions check
        // member.isMinor == true ?=> book is not for adult
        if (member.isMinor()) {
            boolean isBookForAdult = bookCopy.getBook().getCategories().stream().anyMatch(BookCategory::isAdultOnly);
            if (isBookForAdult) {
                throw new LoanImpossibleException("Member is minor while book is for adult.");
            }
        }
        // bookCopy.isAvailable() == true && !bookCopy.isRemove()
        if (!bookCopy.isAvailable() || bookCopy.isRemoved()) {
            throw new LoanImpossibleException("Book copy is not available or has been removed.");
        }
        // givenLoanDateTime != null && mostRecentLoan != null ?=> mostRecentLoan.returnDateTime < givenLoanDateTime
        if (givenLoanDateTime != null) {
            final Loan mostRecentLoan = this.getMostRecentLoan(bookCopy);
            if (mostRecentLoan != null && !mostRecentLoan.getReturnDateTime().isBefore(givenLoanDateTime)) {
                throw new IllegalArgumentException("Cannot create a loan with a dateTime before the returnTime of a previous loan");
            }
        }
        //Create and saved loan
        LocalDateTime loanDateTime = givenLoanDateTime == null ? LoanServiceImpl.now() : givenLoanDateTime;
        Loan loan = this.loanRepository.save(new Loan(member, bookCopy, loanDateTime, bookCopy.getState()));
        // update book copy availability
        bookCopy.setAvailable(false);
        this.bookCopyRepository.save(bookCopy);
        return loan;
    }

    @Override
    public Loan getLoanById(String loanId) {
        if (loanId == null) {
            throw new IllegalArgumentException("Missing loan id.");
        }
        try {
            return this.loanRepository.findById(loanId).get();
        } catch (NoSuchElementException ex) {
            throw new NoSuchElementException("Unknown loan.");
        }
    }

    @Override
    public Loan updateLoan(Loan inputLoan) {
        // Global preconditions : loan cannot be null, loan id cannot be null, loan must exist
        if (inputLoan == null) {
            throw new IllegalArgumentException("Missing loan to update.");
        }
        final Loan loan = this.loanRepository.findById(inputLoan.getId()).get();
        // According to the combinaison of given inputLoan properties, apply particular update
        int updatedEntity = NOTHING_UPDATED;
        // update loan date ?
        if (inputLoan.getLoanDateTime() != null
                && !inputLoan.getLoanDateTime().isEqual(loan.getLoanDateTime())) {
            updatedEntity |= this.updateLoanDate(loan, inputLoan.getLoanDateTime());
        }
        // set return date and state ?
        if (loan.getReturnDateTime() == null && inputLoan.getReturnDateTime() != null
                && inputLoan.getReturnState() != null) {
            updatedEntity |= this.setReturnDateAndState(loan, inputLoan.getReturnDateTime(),
                    inputLoan.getReturnState());
        } else if (loan.getReturnDateTime() != null && inputLoan.getReturnDateTime() == null
                && inputLoan.getReturnState() == null) { // reset return date and state ?
            updatedEntity |= this.resetReturnDateAndState(loan);
        } else if (loan.getReturnDateTime() != null) { // update return date and/or state ?
            if (inputLoan.getReturnDateTime() != null && !inputLoan.getReturnDateTime().isEqual(loan.getReturnDateTime())) {
                updatedEntity |= this.updateReturnDate(loan, inputLoan.getReturnDateTime());
            }
            if (inputLoan.getReturnState() != null && inputLoan.getReturnState() != loan.getReturnState()) {
                updatedEntity |= this.updateReturnState(loan, inputLoan.getReturnState());
            }
        }
        // According to the change, save loan or book or both
        Loan updatedLoan = loan;
        if ((updatedEntity & LOAN_UPDATED) != 0) {
            updatedLoan = this.loanRepository.save(loan);
        }
        if ((updatedEntity & BOOK_UPDATED) != 0) {
            this.bookCopyRepository.save(loan.getBookCopy());
        }
        return updatedLoan;
    }

    @Override
    public void deleteLoanById(String loanId) {
        Loan loan = this.loanRepository.findById(loanId)
                .orElseThrow(() -> new NoSuchElementException("Unknown loan to delete."));
        if (loan.getReturnDateTime() != null) {
            throw new IllegalArgumentException("Cannot delete already returned loan.");
        }
        final BookCopy bookCopy = loan.getBookCopy();
        this.loanRepository.deleteById(loanId);
        bookCopy.setAvailable(true);
        this.bookCopyRepository.save(bookCopy);
    }

    private int updateLoanDate(Loan loan, LocalDateTime newLoanDate) {
        assert newLoanDate != null;
        // Preconditions check
        // loan.returnDate != null ?=> loan.returnDate > newLoanDate
        if (loan.getReturnDateTime() != null && !loan.getReturnDateTime().isAfter(newLoanDate)) {
            throw new IllegalArgumentException("Cannot update loan dateTime with a value after the return date of the loan.");
        }
        // mostRecentPreviousLoan != null ?=> mostRecentPreviousLoan.returnDate < newLoanDate
        final Loan mostRecentPreviousLoan = this.getMostRecentPreviousLoan(loan);
        if (mostRecentPreviousLoan != null && !mostRecentPreviousLoan.getReturnDateTime().isBefore(newLoanDate)) {
            throw new IllegalArgumentException("Cannot update loan dateTime with a value before the return date of the previous loan.");
        }
        // Apply update
        loan.setLoanDateTime(newLoanDate);
        return LOAN_UPDATED;
    }

    private int setReturnDateAndState(Loan loan, LocalDateTime returnDate, BookState returnState) {
        assert loan.getReturnDateTime() == null;
        assert returnDate != null && returnState != null;
        // Preconditions check
        // loan.loanDateTime < returnDate
        if (!loan.getLoanDateTime().isBefore(returnDate)) {
            throw new IllegalArgumentException("Cannot set loan returnDate with a value before the date of the loan.");
        }
        // returnState != NEW && loan.initialState >= returnState
        if (returnState == BookState.NEW || compareBookStates(loan.getInitialState(), returnState) < 0) {
            throw new IllegalArgumentException("Cannot set loan returnState with a value NEW or a value > loan.initialState.");
        }
        // Apply update
        loan.setReturnDateTime(returnDate);
        loan.setReturnState(returnState);
        loan.getBookCopy().setAvailable(true);
        loan.getBookCopy().setState(returnState);
        return LOAN_UPDATED | BOOK_UPDATED;
    }

    private int updateReturnDate(Loan loan, LocalDateTime newReturnDate) {
        assert loan.getReturnDateTime() != null && !loan.getReturnDateTime().isEqual(newReturnDate);
        assert newReturnDate != null;
        // Preconditions check
        // loan.loanDateTime < newReturnDateTime
        if (!loan.getLoanDateTime().isBefore(newReturnDate)) {
            throw new IllegalArgumentException("Cannot update loan returnDate with a value before the date of the loan.");
        }
        // mostAncientNextLoan != null ?=> getMostAncientNextLoan.loanDateTime > newReturnDate
        final Loan mostAncientNextLoan = this.getMostAncientNextLoan(loan);
        if (mostAncientNextLoan != null && !mostAncientNextLoan.getLoanDateTime().isAfter(newReturnDate)) {
            throw new IllegalArgumentException("Cannot update loan returnDate with a value after the date of the next loan.");
        }
        // Apply update
        loan.setReturnDateTime(newReturnDate);
        return LOAN_UPDATED;
    }

    private int updateReturnState(Loan loan, BookState newReturnState) {
        assert loan.getReturnDateTime() != null && loan.getReturnState() != newReturnState;
        assert newReturnState != null;
        // Preconditions check
        // loan.initialState >= newReturnState
        if (compareBookStates(loan.getInitialState(), newReturnState) < 0) {
            throw new IllegalArgumentException("Cannot update loan returnState with a value higher the initial state of the loan.");
        }
        // mostAncientNextLoan != null ?=> mostAncientNextLoan.initialState <= newReturnState
        final Loan mostAncientNextLoan = this.getMostAncientNextLoan(loan);
        if (mostAncientNextLoan != null && compareBookStates(mostAncientNextLoan.getInitialState(), newReturnState) > 0) {
            throw new IllegalArgumentException("Cannot update loan returnState with a value higher the initial state of the next loan.");
        }
        // Apply update
        loan.setReturnState(newReturnState);
        if (mostAncientNextLoan == null) {
            // if updated loan is most recent, update book state also.
            loan.getBookCopy().setState(newReturnState);
            return LOAN_UPDATED | BOOK_UPDATED;
        } else {
            return LOAN_UPDATED;
        }
    }

    private int resetReturnDateAndState(Loan loan) {
        assert loan.getReturnDateTime() != null;
        // Preconditions check
        // mostAncientNextLoan == null
        if (this.getMostAncientNextLoan(loan) != null) {
            throw new IllegalArgumentException("Cannot reset loan return with an existing next loan.");
        }
        // Apply update
        loan.setReturnDateTime(null);
        loan.setReturnState(null);
        loan.getBookCopy().setAvailable(false);
        loan.getBookCopy().setState(loan.getInitialState());
        return LOAN_UPDATED | BOOK_UPDATED;
    }

    private Loan getMostRecentPreviousLoan(Loan loan) {
        return loan.getBookCopy().getLoans().stream()
                .filter((l) -> l.getLoanDateTime().isBefore(loan.getLoanDateTime()))
                .reduce(null, (l1, l2) -> {
                    if(l1 == null) return l2;
                    if(l2 == null) return l1;
                    return l1.getLoanDateTime().isBefore(l2.getLoanDateTime()) ? l2 : l1;
                });
    }

    private Loan getMostAncientNextLoan(Loan loan) {
        return loan.getBookCopy().getLoans().stream()
                .filter((l) -> l.getLoanDateTime().isAfter(loan.getLoanDateTime()))
                .reduce(null, (l1, l2) -> {
                    if(l1 == null) return l2;
                    if(l2 == null) return l1;
                    return l1.getLoanDateTime().isBefore(l2.getLoanDateTime()) ? l1 : l2;
                });
    }
    
    private Loan getMostRecentLoan(BookCopy bookCopy) {
        return bookCopy.getLoans().stream()
                .reduce(null, (l1, l2) -> {
                    if(l1 == null) return l2;
                    if(l2 == null) return l1;
                    return l1.getLoanDateTime().isBefore(l2.getLoanDateTime()) ? l2 : l1;
                });
    }

    private static int compareBookStates(BookState bs1, BookState bs2) {
        final int bs1Val = bs1 == null ? -1 : bs1.getValue();
        final int bs2Val = bs2 == null ? -1 : bs2.getValue();
        return Integer.compare(bs1Val, bs2Val);
    }

    private static LocalDateTime now() {
        return LocalDateTime.now().withNano(0);
    }

    private static final int NOTHING_UPDATED = 0;
    private static final int LOAN_UPDATED = 1;
    private static final int BOOK_UPDATED = 2;
}
