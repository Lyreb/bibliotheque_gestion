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
public class BookCopyTest {

    private static final Log LOG = LogFactory.getLog(BookCopyTest.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    private Book book1;

    public BookCopyTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
        this.book1 = this.mongoTemplate.save(new Book("bookIsbn", "bookTitle", "bookEditor", 100, 2021, null, null));
    }

    @AfterEach
    public void tearDown() {
        this.mongoTemplate.remove(new BasicQuery("{}"), Book.class);
        this.mongoTemplate.remove(new BasicQuery("{}"), BookCopy.class);
    }

    @Test
    public void bookCopyMustHaveABook() {
        assertThatThrownBy(()
                -> this.mongoTemplate.save(new BookCopy(null, BookState.GOOD)))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void bookCopyMustHaveAState() {
        assertThatThrownBy(()
                -> this.mongoTemplate.save(new BookCopy(this.book1, null)))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void bookCopyIsProperlySaved() {
        String bcId = this.mongoTemplate.save(new BookCopy(this.book1, BookState.USED)).getId();
        final BookCopy bookCopy = this.mongoTemplate.findById(bcId, BookCopy.class);
        assertThat(bookCopy).isNotNull();
        assert bookCopy != null;
        assertThat(bookCopy.getId()).isNotNull();
        assertThat(bookCopy.getBook()).extracting("id").isEqualTo(this.book1.getId());
        assertThat(bookCopy.getState()).isEqualTo(BookState.USED);
        assertThat(bookCopy.isRemoved()).isFalse();
        assertThat(bookCopy.isAvailable()).isTrue();
        assertThat(bookCopy.getLoans()).isEmpty();
    }

    @Test
    public void bookCopyIsInBook() {
        BookCopy bc = this.mongoTemplate.save(new BookCopy(this.book1, BookState.USED));
        final Book book = this.mongoTemplate.findById(this.book1.getId(), Book.class);
        assertThat(book).isNotNull();
        assert book != null;
        assertThat(book.getCopies()).hasSize(1).element(0).extracting("id").isEqualTo(bc.getId());
    }
}
