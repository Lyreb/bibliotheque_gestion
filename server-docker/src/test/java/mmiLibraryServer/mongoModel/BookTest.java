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

import com.mongodb.client.MongoCursor;
import java.util.List;
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
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ActiveProfiles;

/**
 *
 * @author Rémi Venant
 */
@DataMongoTest
@Import(MongoConfiguration.class)
@ActiveProfiles("mongo-test")
public class BookTest {

    private static final Log LOG = LogFactory.getLog(BookTest.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    private BookCategory cat1;
    private BookCategory cat2;

    public BookTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
        this.cat1 = this.mongoTemplate.save(new BookCategory("codeCat1", "nameCat1", true));
        this.cat2 = this.mongoTemplate.save(new BookCategory("codeCat2", "nameCat2", false));
    }

    @AfterEach
    public void tearDown() {
        //Remove all document without dropping collection to avoid index dropping
        this.mongoTemplate.remove(new BasicQuery("{}"), Book.class);
        this.mongoTemplate.remove(new BasicQuery("{}"), BookCategory.class);
    }

    @Test
    public void bookMustHaveAnISBN() {
        assertThatThrownBy(()
                -> this.mongoTemplate.save(new Book(null, "title", "editor", 100, 2021, null, null)))
                .isInstanceOf(ConstraintViolationException.class);
        assertThatThrownBy(()
                -> this.mongoTemplate.save(new Book("", "title", "editor", 100, 2021, null, null)))
                .isInstanceOf(ConstraintViolationException.class);
        assertThatThrownBy(()
                -> this.mongoTemplate.save(new Book("   ", "title", "editor", 100, 2021, null, null)))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void bookMustHaveATitle() {
        assertThatThrownBy(()
                -> this.mongoTemplate.save(new Book("isbn", null, "editor", 100, 2021, null, null)))
                .isInstanceOf(ConstraintViolationException.class);
        assertThatThrownBy(()
                -> this.mongoTemplate.save(new Book("isbn", "", "editor", 100, 2021, null, null)))
                .isInstanceOf(ConstraintViolationException.class);
        assertThatThrownBy(()
                -> this.mongoTemplate.save(new Book("isbn", "    ", "editor", 100, 2021, null, null)))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void bookNullableFields() {
        assertThat(this.mongoTemplate.save(new Book("isbn", "title", null, null, null, null, null)))
                .isNotNull();
    }

    @Test
    public void bookProperlySaved() {
        String bookId = this.mongoTemplate.save(
                new Book("isbn", "title", "editor", 100, 2021, null, null)).getId();
        final Book book = this.mongoTemplate.findById(bookId, Book.class);
        assertThat(book).isNotNull();
        assert book != null;
        assertThat(book.getId()).isNotNull();
        assertThat(book.getIsbn()).isEqualTo("isbn");
        assertThat(book.getTitle()).isEqualTo("title");
        assertThat(book.getEditor()).isEqualTo("editor");
        assertThat(book.getNumOfPages()).isEqualTo(100);
        assertThat(book.getPublicationYear()).isEqualTo(2021);
        assertThat(book.getAuthors()).isEmpty();
        assertThat(book.getCategories()).isEmpty();
        assertThat(book.getCopies()).isEmpty();
    }

    @Test
    public void bookProperlySavedSimpleConstructor() {
        String bookId = this.mongoTemplate.save(
                new Book("isbn", "title", "editor", 100, 2021)).getId();
        final Book book = this.mongoTemplate.findById(bookId, Book.class);
        assertThat(book).isNotNull();
        assert book != null;
        assertThat(book.getId()).isNotNull();
        assertThat(book.getIsbn()).isEqualTo("isbn");
        assertThat(book.getTitle()).isEqualTo("title");
        assertThat(book.getEditor()).isEqualTo("editor");
        assertThat(book.getNumOfPages()).isEqualTo(100);
        assertThat(book.getPublicationYear()).isEqualTo(2021);
        assertThat(book.getAuthors()).isEmpty();
        assertThat(book.getCategories()).isEmpty();
        assertThat(book.getCopies()).isEmpty();
    }

    @Test
    public void bookAuthorAndCatProperlySaved() {
        String bookId = this.mongoTemplate.save(new Book("isbn2", "title", "editor", 100, 2021,
                List.of(new Author("autFname1", "authLname1"), new Author("autFname2", "authLname2")),
                List.of(this.cat1, this.cat2))).getId();
        final Book book = this.mongoTemplate.findById(bookId, Book.class);
        assertThat(book).isNotNull();
        assert book != null;
        assertThat(book.getAuthors()).containsExactlyInAnyOrder(
                new Author("autFname1", "authLname1"),
                new Author("autFname2", "authLname2"));
        assertThat(book.getCategories()).extracting("id")
                .containsExactlyInAnyOrder(this.cat1.getId(), this.cat2.getId());
    }

    @Test
    public void unknownPropertyAreNotSaved() {
        String bookId = this.mongoTemplate.save(new Book("isbn", "title", "editor", 100, 2021, null,
                List.of(this.cat1, new BookCategory("uCode", "uName", true)))).getId();
        assertThat(this.mongoTemplate.count(new BasicQuery("{}"), BookCategory.class)).isEqualTo(2);

        final Book book = this.mongoTemplate.findById(bookId, Book.class);
        assertThat(book).isNotNull();
        assert book != null;
        assertThat(book.getCategories()).extracting("id").containsExactlyInAnyOrder(this.cat1.getId());
    }

    @Test
    public void bookHasUniqueIsbn() {
        Book b = this.mongoTemplate.save(new Book("isbn", "title", "editor", 100, 2021, null, null));
        assertThatThrownBy(()
                -> this.mongoTemplate.save(new Book("isbn", "title2", "editor", 100, 2021, null, null)))
                .isInstanceOf(DuplicateKeyException.class);
    }

    public void bookProperlySavedDebug() {
        final Author a1 = new Author("fName_A1", "lName_A2");
        final Author a2 = new Author("fName_A2", "lName_A2");
        final BookCategory cat1 = this.mongoTemplate.save(new BookCategory("CAT1", "Category 1", false));
        final BookCategory cat2 = this.mongoTemplate.save(new BookCategory("CAT2", "Category 2", false));
        final Book book = this.mongoTemplate.save(new Book("isbn", "title", "editor", 50, 2000,
                List.of(a1, a2), List.of(cat1, cat2)));

        LOG.info("print raw document");
        final MongoCursor<Document> mc
                = this.mongoTemplate
                        .getCollection(this.mongoTemplate.getCollectionName(Book.class))
                        .find().limit(1).iterator();
        int nbDocFound = 0;
        while (mc.hasNext()) {
            Document doc = mc.next();
            nbDocFound++;
            LOG.info(doc.toJson());
        }
        assertThat(nbDocFound).as("1 document found").isEqualTo(1);
    }

    @Test
    public void bookCatCanBeFound() {
        final Author a1 = new Author("fName_A1", "lName_A2");
        final Author a2 = new Author("fName_A2", "lName_A2");
        final BookCategory cat1 = this.mongoTemplate.save(new BookCategory("CAT1", "Category 1", false));
        final BookCategory cat2 = this.mongoTemplate.save(new BookCategory("CAT2", "Category 2", false));
        final Book book = this.mongoTemplate.save(new Book("isbn", "title", "editor", 50, 2000,
                List.of(a1, a2), List.of(cat1, cat2)));

        Query query = Query.query(Criteria.where("categories").in(List.of(cat1, cat2)));
        List<Book> books = this.mongoTemplate.find(query, Book.class);
        assertThat(books).as("1 document found").map(Book::getId).containsExactly(book.getId());
    }
}
