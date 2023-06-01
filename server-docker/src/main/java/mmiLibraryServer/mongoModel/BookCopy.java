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
import java.io.Serializable;
import java.util.List;
import javax.validation.constraints.NotNull;
import mmiLibraryServer.mongoModel.views.BookCopyViews;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

/**
 *
 * @author Rémi Venant
 */
@Document(collection = "bookCopies")
public class BookCopy implements Serializable {

    @JsonView(BookCopyViews.Normal.class)
    @Id
    private String id;

    @JsonView(BookCopyViews.WithBook.class)
    @NotNull
    @DocumentReference(lazy = true)
    private Book book;

    @JsonView(BookCopyViews.Normal.class)
    @NotNull
    private BookState state;

    @JsonView(BookCopyViews.Normal.class)
    @NotNull
    private Boolean removed;

    @JsonView(BookCopyViews.Normal.class)
    @NotNull
    private Boolean available;

    @JsonView(BookCopyViews.WithLoans.class)
    @ReadOnlyProperty
    @DocumentReference(lookup = "{'bookCopy':?#{#self._id} }", lazy = true, sort = "{loanDate:-1}")
    private List<Loan> loans;

    protected BookCopy() {
    }

    protected BookCopy(Book book, BookState state, Boolean removed, Boolean available) {
        this.book = book;
        this.state = state;
        this.removed = removed;
        this.available = available;
        this.loans = List.of();
    }

    public BookCopy(Book book, BookState state) {
        this(book, state, false, true);
    }

    public String getId() {
        return id;
    }

    protected void setId(String id) {
        this.id = id;
    }

    public Book getBook() {
        return book;
    }

    public BookState getState() {
        return state;
    }

    public void setState(BookState state) {
        this.state = state;
    }

    public Boolean isRemoved() {
        return removed;
    }

    public void setRemoved(Boolean removed) {
        this.removed = removed;
    }

    public Boolean isAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public List<Loan> getLoans() {
        return loans;
    }

    protected void setLoans(List<Loan> loans) {
        this.loans = loans;
    }

    @Override
    public String toString() {
        return "BookCopy{" + "id=" + id + ", state=" + state + ", removed=" + removed + '}';
    }

}
