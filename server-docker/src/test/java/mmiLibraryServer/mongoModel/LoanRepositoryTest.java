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
import java.util.List;
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
public class LoanRepositoryTest {

    private static final Log LOG = LogFactory.getLog(LoanRepositoryTest.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private LoanRepository testedRepo;

    private Book book1;
    private Book book2;
    private BookCopy bookCopy1;
    private BookCopy bookCopy2;
    private BookCopy bookCopy3;
    private Member member1;
    private Member member2;

    public LoanRepositoryTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
        this.book1 = this.mongoTemplate.save(new Book("bookIsbn1", "bookTitle1", "bookEditor1", 100, 2021));
        this.book2 = this.mongoTemplate.save(new Book("bookIsbn2", "bookTitle2", "bookEditor2", 100, 2021));
        this.bookCopy1 = this.mongoTemplate.save(new BookCopy(book1, BookState.NEW));
        this.bookCopy2 = this.mongoTemplate.save(new BookCopy(book1, BookState.GOOD));
        this.bookCopy3 = this.mongoTemplate.save(new BookCopy(book2, BookState.NEW));
        this.member1 = this.mongoTemplate.save(new Member("Bon", "Jean",
                LocalDate.parse("1990-12-20", DateTimeFormatter.ISO_DATE)));
        this.member2 = this.mongoTemplate.save(new Member("Bonne", "Jeanne",
                LocalDate.parse("1995-01-20", DateTimeFormatter.ISO_DATE)));
    }

    @AfterEach
    public void tearDown() {
        this.mongoTemplate.remove(new BasicQuery("{}"), Book.class);
        this.mongoTemplate.remove(new BasicQuery("{}"), BookCopy.class);
        this.mongoTemplate.remove(new BasicQuery("{}"), Member.class);
        this.mongoTemplate.remove(new BasicQuery("{}"), Loan.class);
    }

    @Test
    public void repositoryShouldBeEmpty() {
        assertThat(this.testedRepo.findAll()).isEmpty();
    }

    @Test
    public void saveThenFindOk() {
        this.testedRepo.saveAll(List.of(
                new Loan(member1, bookCopy1, TestInstanceBuilder.now(), BookState.NEW),
                new Loan(member1, bookCopy2, TestInstanceBuilder.now(), BookState.NEW),
                new Loan(member2, bookCopy3, TestInstanceBuilder.now(), BookState.NEW)
        ));
        assertThat(this.testedRepo.findAll()).hasSize(3);
    }

    @Test
    public void findByOrderByLoanDateDescOk() {
        Loan l1 = this.testedRepo.save(new Loan(member1, bookCopy1, LocalDateTime.parse("2021-12-10T00:00:00"), BookState.NEW));
        Loan l2 = this.testedRepo.save(new Loan(member1, bookCopy2, LocalDateTime.parse("2021-12-15T00:00:00"), BookState.NEW));
        Loan l3 = this.testedRepo.save(new Loan(member2, bookCopy3, LocalDateTime.parse("2022-01-15T00:00:00"), BookState.NEW));
        Loan l4 = this.testedRepo.save(new Loan(member2, bookCopy1, LocalDateTime.parse("2021-12-14T00:00:00"), BookState.NEW));
        List<Loan> loans = this.testedRepo.findByOrderByLoanDateTimeDesc();
        assertThat(loans).map(Loan::getId).containsExactly(l3.getId(), l2.getId(), l4.getId(), l1.getId());
    }

    @Test
    public void findByBookCopyOrderByLoanDateDescOk() {
        Loan l1 = this.testedRepo.save(new Loan(member1, bookCopy1, LocalDateTime.parse("2021-12-10T00:00:00"), BookState.NEW));
        Loan l2 = this.testedRepo.save(new Loan(member1, bookCopy2, LocalDateTime.parse("2021-12-15T00:00:00"), BookState.NEW));
        Loan l3 = this.testedRepo.save(new Loan(member2, bookCopy3, TestInstanceBuilder.now(), BookState.NEW));
        Loan l4 = this.testedRepo.save(new Loan(member2, bookCopy1, LocalDateTime.parse("2021-12-14T00:00:00"), BookState.NEW));
        List<Loan> loans = this.testedRepo.findByBookCopyOrderByLoanDateTimeDesc(this.bookCopy1);
        assertThat(loans).map(Loan::getId).containsExactly(l4.getId(), l1.getId());
    }

    @Test
    public void findByBookCopyInOrderByLoanDateDescOk() {
        Loan l1 = this.testedRepo.save(new Loan(member1, bookCopy1, LocalDateTime.parse("2021-12-10T00:00:00"), BookState.NEW));
        Loan l2 = this.testedRepo.save(new Loan(member1, bookCopy2, LocalDateTime.parse("2021-12-15T00:00:00"), BookState.NEW));
        Loan l3 = this.testedRepo.save(new Loan(member2, bookCopy3, TestInstanceBuilder.now(), BookState.NEW));
        Book myBook = this.mongoTemplate.findById(this.book1.getId(), Book.class);
        assert myBook != null;
        List<Loan> loans = this.testedRepo.findByBookCopyInOrderByLoanDateTimeDesc(myBook.getCopies());
        assertThat(loans).map(Loan::getId).containsExactly(l2.getId(), l1.getId());
    }
}
