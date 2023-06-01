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
import mmiLibraryServer.mongoModel.BookCopy;
import mmiLibraryServer.mongoModel.Loan;
import mmiLibraryServer.mongoModel.Member;
import mmiLibraryServer.services.exceptions.LoanImpossibleException;

/**
 *
 * @author Rémi Venant
 */
public interface LoanService {

    /**
     * Get all loans
     *
     * @return the loans
     */
    List<Loan> getLoans();

    /**
     * Create a new loan. The initial state of the loan will be the current state of the book copy.
     * The book copy will be automatically set as not available.
     *
     * @param member the member who borrows the book copy. Cannot be null.
     * @param bookCopy the book copy. Cannot be null.
     * @param loanDateTime the datetime of the loan. Use today if loanDate is null
     * @return the created Loan
     * @throws IllegalArgumentException if either member of bookCopy is null
     * @throws LoanImpossibleException if the book copy is not available of the book is for adult
     * and the member is a minor.
     */
    Loan createLoan(Member member, BookCopy bookCopy, LocalDateTime loanDateTime) throws LoanImpossibleException;

    /**
     * Get a loan by its id.
     *
     * @param loanId the loan id.
     * @return the loan
     * @throws IllegalArgumentException if loanId is null.
     * @throws NoSuchElementException if the loan does not exist.
     */
    Loan getLoanById(String loanId);

    /**
     * Update a loan, either to correct the loan date or when the book copy is returned. If a book
     * copy is return, both loanDate and returnState must be provided.
     *
     * @param loan the loan information to update. It will only use loanDate, returnDate and
     * returnState.
     * @return the updated Loan
     * @throws IllegalArgumentException if returnDateTime is given but not returnState and vice
     * versa, or if returnState is NEW. TODO: Update comments
     * @throws NoSuchElementException if the loan does not exist.
     */
    Loan updateLoan(Loan loan);

    /**
     * Delete a loan by its id. Should only be used to cancel a loan
     *
     * @param loanId the loan id
     * @throws IllegalArgumentException if loanId is null.
     * @throws NoSuchElementException if the loan does not exist.
     */
    void deleteLoanById(String loanId);
}
