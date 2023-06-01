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
public class BookCopyRepositoryTest {

    private static final Log LOG = LogFactory.getLog(BookCopyRepositoryTest.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private BookCopyRepository testedRepo;

    private Book book1;
    private Book book2;

    public BookCopyRepositoryTest() {
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
    }

    @AfterEach
    public void tearDown() {
        this.mongoTemplate.remove(new BasicQuery("{}"), BookCategory.class);
        this.mongoTemplate.remove(new BasicQuery("{}"), BookCopy.class);
        this.mongoTemplate.remove(new BasicQuery("{}"), Book.class);
    }

    @Test
    public void repositoryShouldBeEmpty() {
        assertThat(this.testedRepo.findAll()).isEmpty();
    }

    @Test
    public void saveThenFindOk() {
        this.testedRepo.saveAll(List.of(
                new BookCopy(this.book1, BookState.NEW),
                new BookCopy(this.book1, BookState.GOOD),
                new BookCopy(this.book1, BookState.GOOD)
        ));
        assertThat(this.testedRepo.findAll()).hasSize(3);
    }

}
