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
package mmiLibraryServer.mongoModel;

import com.fasterxml.jackson.annotation.JsonView;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import mmiLibraryServer.mongoModel.views.LoanViews;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

/**
 *
 * @author Rémi Venant
 */
@Document(collection = "loans")
public class Loan {

    @JsonView(LoanViews.Normal.class)
    @Id
    private String id;

    @JsonView(LoanViews.WithMember.class)
    @NotNull
    @DocumentReference(lazy = true)
    private Member member;

    @JsonView(LoanViews.WithBookCopy.class)
    @NotNull
    @DocumentReference
    private BookCopy bookCopy;

    @JsonView(LoanViews.Normal.class)
    @NotNull
    private LocalDateTime loanDateTime;

    @JsonView(LoanViews.Normal.class)
    @NotNull
    private BookState initialState;

    @JsonView(LoanViews.Normal.class)
    private LocalDateTime returnDateTime;

    @JsonView(LoanViews.Normal.class)
    private BookState returnState;

    protected Loan() {
    }

    public Loan(Member member, BookCopy bookCopy, LocalDateTime loanDateTime, BookState initialState) {
        this.member = member;
        this.bookCopy = bookCopy;
        this.loanDateTime = loanDateTime;
        this.initialState = initialState;
    }

    public String getId() {
        return id;
    }

    protected void setId(String id) {
        this.id = id;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public BookCopy getBookCopy() {
        return bookCopy;
    }

    public void setBookCopy(BookCopy bookCopy) {
        this.bookCopy = bookCopy;
    }

    public LocalDateTime getLoanDateTime() {
        return loanDateTime;
    }

    public void setLoanDateTime(LocalDateTime loanDateTime) {
        this.loanDateTime = loanDateTime;
    }

    public BookState getInitialState() {
        return initialState;
    }

    public void setInitialState(BookState initialState) {
        this.initialState = initialState;
    }

    public LocalDateTime getReturnDateTime() {
        return returnDateTime;
    }

    public void setReturnDateTime(LocalDateTime returnDateTime) {
        this.returnDateTime = returnDateTime;
    }

    public BookState getReturnState() {
        return returnState;
    }

    public void setReturnState(BookState returnState) {
        this.returnState = returnState;
    }

    @Override
    public String toString() {
        return "Loan{" + "id=" + id + ", member=" + member + ", bookCopy=" + bookCopy + ", loanDate=" + loanDateTime + ", initialState=" + initialState + ", returnDate=" + returnDateTime + ", returnState=" + returnState + '}';
    }

}
