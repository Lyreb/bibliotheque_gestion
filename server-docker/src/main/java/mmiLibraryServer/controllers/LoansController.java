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
import mmiLibraryServer.controllers.model.LoanCreationOrder;
import mmiLibraryServer.controllers.views.CompositeViews;
import mmiLibraryServer.mongoModel.Loan;
import mmiLibraryServer.mongoModel.views.LoanViews;
import mmiLibraryServer.services.BookService;
import mmiLibraryServer.services.LoanService;
import mmiLibraryServer.services.MemberService;
import mmiLibraryServer.services.exceptions.LoanImpossibleException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Rémi Venant
 */
@RestController
@RequestMapping("/api/v1/rest/loans")
public class LoansController {

    private static final Log LOG = LogFactory.getLog(LoansController.class);

    private final LoanService loanSvc;

    private final MemberService memberSvc;

    private final BookService bookSvc;

    @Autowired
    public LoansController(LoanService loanSvc, MemberService memberSvc,
            BookService bookSvc) {
        this.loanSvc = loanSvc;
        this.memberSvc = memberSvc;
        this.bookSvc = bookSvc;
    }

    /**
     * GET /loans. Get all loans, order by loan date in descending order
     *
     * @return loans.
     */
    @GetMapping
    @JsonView(LoanViews.Normal.class)
    public List<Loan> getLoans() {
        return this.loanSvc.getLoans();
    }

    /**
     * POST /loans. Create a loan
     *
     * @param loanCreationOrder the loan to create, using member and book copy ids
     * @return the created loan
     * @throws LoanImpossibleException if book copy is unavailable or the book is for adult while
     * the member is a minor
     */
    @PostMapping
    @JsonView(LoanViews.WithMemberAndBookCopy.class)
    public Loan createLoan(@RequestBody LoanCreationOrder loanCreationOrder) throws LoanImpossibleException {
        if (loanCreationOrder == null) {
            throw new IllegalArgumentException("Missing information to create loan.");
        }
        return this.loanSvc.createLoan(
                this.memberSvc.getMemberById(loanCreationOrder.getMemberId()),
                this.bookSvc.getBookCopyById(loanCreationOrder.getBookCopyId()),
                loanCreationOrder.getLoanDateTime());
    }

    /**
     * GET /loans/:loanId. Return a loan with its member and book copy
     *
     * @param loanId the loan id
     * @return the loan with its member and book copy
     */
    @GetMapping("{loanId}")
    @JsonView(CompositeViews.LoanWithEverythingAndBookCopyWithBook.class)
    public Loan getLoan(@PathVariable String loanId) {
        return this.loanSvc.getLoanById(loanId);
    }

    /**
     * PUT /loans/:loanId. Update a loan. Can only update loan date, return date and return state
     *
     * @param loanId the loan id
     * @param loanToUpdate the loan to update
     * @return the updated loan with its member and book copy
     */
    @PutMapping("{loanId}")
    @JsonView(CompositeViews.LoanWithEverythingAndBookCopyWithBook.class)
    public Loan updateLoan(@PathVariable String loanId, Loan loanToUpdate) {
        if (loanToUpdate == null) {
            throw new IllegalArgumentException("Missing information to update loan.");
        }
        if (!loanId.equals(loanToUpdate.getId())) {
            throw new IllegalArgumentException("Wrong loand id to update loan.");
        }
        return this.loanSvc.updateLoan(loanToUpdate);
    }

    /**
     * DELETE /loans/:loanId. Delete a loan
     *
     * @param loanId the loan id
     */
    @DeleteMapping("{loanId}")
    public void deleteLoan(@PathVariable String loanId) {
        this.loanSvc.deleteLoanById(loanId);
    }

}
