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
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ActiveProfiles;

/**
 *
 * @author Rémi Venant
 */
@DataMongoTest
@Import(MongoConfiguration.class)
@ActiveProfiles("mongo-test")
public class BookCategoryRepositoryTest {

    private static final Log LOG = LogFactory.getLog(BookCategoryRepositoryTest.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private BookCategoryRepository testedRepo;

    public BookCategoryRepositoryTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
        this.mongoTemplate.remove(new BasicQuery("{}"), BookCategory.class);
    }

    @Test
    public void repositoryShouldBeEmpty() {
        assertThat(this.testedRepo.findAll()).isEmpty();
    }

    @Test
    public void saveThenFindOk() {
        this.testedRepo.saveAll(List.of(
                new BookCategory("code1", "name1", true),
                new BookCategory("code2", "name2", true),
                new BookCategory("code3", "name3", true)
        ));
        assertThat(this.testedRepo.findAll()).hasSize(3);
    }

    @Test
    public void deleteBookCatOk() {
        BookCategory bc1 = this.mongoTemplate.save(new BookCategory("CAT1", "Category 1", false));
        BookCategory bc2 = this.mongoTemplate.save(new BookCategory("CAT2", "Category 2", false));
        BookCategory bc3 = this.mongoTemplate.save(new BookCategory("CAT3", "Category 3", false));
        Book b1 = this.mongoTemplate.save(new Book("bookIsbn1", "bookTitle1", "bookEditor1", 100, 2021,
                List.of(), List.of(bc1, bc2)));
        Book b2 = this.mongoTemplate.save(new Book("bookIsbn2", "bookTitle2", "bookEditor2", 100, 2021,
                List.of(), List.of(bc1, bc3)));
        Book b3 = this.mongoTemplate.save(new Book("bookIsbn3", "bookTitle3", "bookEditor3", 100, 2021,
                List.of(), List.of(bc2, bc3)));

        this.testedRepo.delete(bc2);

        assertThat(this.mongoTemplate.find(new Query(), BookCategory.class)).as("Book cats changed")
                .map(BookCategory::getId).containsExactlyInAnyOrder(bc1.getId(), bc3.getId());
        assertThat(this.mongoTemplate.count(new Query(), Book.class)).as("Still 3 books").isEqualTo(3);

        Book updatedBook = this.mongoTemplate.findById(b1.getId(), Book.class);
        assert updatedBook != null;
        assertThat(updatedBook.getCategories()).as("Book categories of b1 changed")
                .map(BookCategory::getId).containsExactlyInAnyOrder(bc1.getId());

        updatedBook = this.mongoTemplate.findById(b2.getId(), Book.class);
        assert updatedBook != null;
        assertThat(updatedBook.getCategories()).as("Book categories of b2 changed")
                .map(BookCategory::getId).containsExactlyInAnyOrder(bc1.getId(), bc3.getId());

        updatedBook = this.mongoTemplate.findById(b3.getId(), Book.class);
        assert updatedBook != null;
        assertThat(updatedBook.getCategories()).as("Book categories of b3 changed")
                .map(BookCategory::getId).containsExactlyInAnyOrder(bc3.getId());
    }
}
