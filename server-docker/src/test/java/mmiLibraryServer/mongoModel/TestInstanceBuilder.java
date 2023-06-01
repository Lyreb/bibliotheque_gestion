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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Rémi Venant
 */
public class TestInstanceBuilder {

    public static Member emptyMember() {
        return new Member();
    }

    public static Member withMinorStatus(Member member) {
        LocalDate today = LocalDate.now();
        int ageYear = Member.MAJOR_YEAR_LIMIT / 2;
        LocalDate birthday = today.minusYears(ageYear);
        member.setBirthday(birthday);
        return member;
    }

    public static Member withMajorStatus(Member member) {
        LocalDate today = LocalDate.now();
        int ageYear = Member.MAJOR_YEAR_LIMIT * 2;
        LocalDate birthday = today.minusYears(ageYear);
        member.setBirthday(birthday);
        return member;
    }

    public static Member withLoans(Member member, List<Loan> loans) {
        member.setLoans(loans);
        return member;
    }

    public static Member withId(Member member, String id) {
        member.setId(id);
        return member;
    }

    public static BookCategory emptyCategory() {
        return new BookCategory();
    }

    public static Book emptyBook() {
        return new Book();
    }

    public static Book withCopies(Book book, List<BookCopy> copies) {
        book.setCopies(copies);
        return book;
    }

    public static Book withId(Book book, String id) {
        book.setId(id);
        return book;
    }

    public static BookCopy emptyBookCopy() {
        BookCopy bc = new BookCopy();
        bc.setRemoved(false);
        bc.setAvailable(true);
        bc.setLoans(new ArrayList<>());
        return bc;
    }

    public static BookCopy withRemovedAndAvailable(BookCopy bookCopy, boolean removed, boolean available) {
        bookCopy.setRemoved(removed);
        bookCopy.setAvailable(available);
        return bookCopy;
    }

    public static BookCopy withId(BookCopy bookCopy, String id) {
        bookCopy.setId(id);
        return bookCopy;
    }

    public static BookCopy withNoLoans(BookCopy bookCopy) {
        bookCopy.setLoans(new ArrayList<>());
        return bookCopy;
    }

    public static BookCopy withLoans(BookCopy bookCopy, Loan... loans) {
        bookCopy.setLoans(List.of(loans));
        return bookCopy;
    }

    public static BookCopy fullBookCopy(String id, Book book, BookState state, boolean removed,
            boolean available, List<Loan> loans) {
        final BookCopy bc = new BookCopy(book, state, removed, available);
        bc.setId(id);
        bc.setLoans(loans);
        return bc;
    }

    public static Loan emptyLoan() {
        return new Loan();
    }

    public static Loan fullLoan(String id, Member member, BookCopy bookCopy,
            LocalDateTime loanDateTime, BookState initialState, LocalDateTime returnDateTime,
            BookState returnState) {
        Loan l = new Loan(member, bookCopy, loanDateTime, initialState);
        l.setId(id);
        l.setReturnDateTime(returnDateTime);
        l.setReturnState(returnState);
        return l;
    }

    public static Loan withId(Loan loan, String id) {
        loan.setId(id);
        return loan;
    }

    public static Loan withInitialState(Loan loan, BookState state) {
        loan.setInitialState(state);
        return loan;
    }

    public static Loan withReturnState(Loan loan, BookState state) {
        loan.setReturnState(state);
        return loan;
    }

    public static LocalDateTime now() {
        return LocalDateTime.now().withNano(0);
    }

    public static LocalDateTime parse(String format) {
        String properFormat = format;
        if (properFormat.indexOf('T') < 0) {
            properFormat += "T00:00:00";
        }
        return LocalDateTime.parse(properFormat).withNano(0);
    }
}
