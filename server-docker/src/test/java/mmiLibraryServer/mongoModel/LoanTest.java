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
import java.time.format.DateTimeFormatter;
import javax.validation.ConstraintViolationException;
import mmiLibraryServer.configuration.MongoConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.test.context.ActiveProfiles;

/**
 *
 * @author Rémi Venant
 */
@DataMongoTest
@Import(MongoConfiguration.class)
@ActiveProfiles("mongo-test")
public class LoanTest {

    private static final Log LOG = LogFactory.getLog(LoanTest.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    private Book book1;
    private BookCopy bookCopy1;
    private Member member1;

    public LoanTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
        this.book1 = this.mongoTemplate.save(new Book("isbn", "title", "editor", 100, 2021,
                null, null));
        this.bookCopy1 = this.mongoTemplate.save(new BookCopy(book1, BookState.NEW));
        this.member1 = this.mongoTemplate.save(new Member("Bon", "Jean",
                LocalDate.parse("1990-12-20", DateTimeFormatter.ISO_DATE)));
    }

    @AfterEach
    public void tearDown() {
        //Remove all document without dropping collection to avoid index dropping
        this.mongoTemplate.remove(new BasicQuery("{}"), Book.class);
        this.mongoTemplate.remove(new BasicQuery("{}"), BookCopy.class);
        this.mongoTemplate.remove(new BasicQuery("{}"), Member.class);
        this.mongoTemplate.remove(new BasicQuery("{}"), Loan.class);
    }

    @Test
    public void loanMustHaveAMember() {
        assertThatThrownBy(()
                -> this.mongoTemplate.save(new Loan(null, bookCopy1, TestInstanceBuilder.now(), BookState.NEW)))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void loanMustHaveACopy() {
        assertThatThrownBy(()
                -> this.mongoTemplate.save(new Loan(member1, null, TestInstanceBuilder.now(), BookState.NEW)))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void loanMustHaveALoanDate() {
        assertThatThrownBy(()
                -> this.mongoTemplate.save(new Loan(member1, bookCopy1, null, BookState.NEW)))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void loanMustHaveAnInitialState() {
        assertThatThrownBy(()
                -> this.mongoTemplate.save(new Loan(member1, bookCopy1, TestInstanceBuilder.now(), null)))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void loanCanHaveNullProperties() {
        assertThat(this.mongoTemplate.save(new Loan(member1, bookCopy1, TestInstanceBuilder.now(), BookState.NEW)))
                .isNotNull();
    }

    @Test
    public void loanIsProperlySaved() {
        LocalDateTime today = TestInstanceBuilder.now();
        String loanId = this.mongoTemplate.save(new Loan(member1, bookCopy1, today, BookState.NEW)).getId();
        final Loan loan = this.mongoTemplate.findById(loanId, Loan.class);
        assertThat(loan).isNotNull();
        assert loan != null;
        assertThat(loan.getId()).isNotNull();
        assertThat(loan.getMember()).extracting("id").isEqualTo(this.member1.getId());
        assertThat(loan.getBookCopy()).extracting("id").isEqualTo(this.bookCopy1.getId());
        assertThat(loan.getLoanDateTime()).isEqualTo(today);
        assertThat(loan.getInitialState()).isEqualTo(BookState.NEW);
        assertThat(loan.getReturnDateTime()).isNull();
        assertThat(loan.getReturnState()).isNull();

        LocalDateTime tomorrow = today.plusDays(1);
        loan.setReturnDateTime(tomorrow);
        loan.setReturnState(BookState.VERY_GOOD);
        this.mongoTemplate.save(loan);
        final Loan updatedLoan = this.mongoTemplate.findById(loanId, Loan.class);
        assert updatedLoan != null;
        assertThat(updatedLoan.getReturnDateTime()).isEqualTo(tomorrow);
        assertThat(updatedLoan.getReturnState()).isEqualTo(BookState.VERY_GOOD);
    }
}
