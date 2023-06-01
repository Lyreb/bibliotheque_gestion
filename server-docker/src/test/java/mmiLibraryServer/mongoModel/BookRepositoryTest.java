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
import mmiLibraryServer.mongoModel.utils.BookRequestFilter;
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
public class BookRepositoryTest {

    private static final Log LOG = LogFactory.getLog(BookRepositoryTest.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private BookRepository testedRepo;

    @Autowired
    private BookCategoryRepository bookCategoryRepo;

    @Autowired
    private BookCopyRepository bookCopyRepo;

    public BookRepositoryTest() {
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
                new Book("isbn1", "title1", "editor1", 100, 2021),
                new Book("isbn2", "title2", "editor2", 100, 2021),
                new Book("isbn3", "title3", "editor3", 100, 2021)
        ));
        assertThat(this.testedRepo.findAll()).hasSize(3);
    }

    @Test
    public void booksFilterOk() {
        //1 isbn précis : I / NI
        final String expectedIsbn = "A good isbn";

        //2 titre plus ou moins proches et 2 autres éloignés: T CT1 CT2 FT
        final String expectedTitle = "good book";
        final String closeTitle1 = "A good book title";
        final String farTitle1 = "good goody book";
        final String farTitle2 = "George Motz and the burger";

        //3 catégorie dont 1 pour adultes/ 6 livre avec 3 couples différent : C1 C2 C3
        final BookCategory cat1 = this.bookCategoryRepo.save(new BookCategory("CAT1", "Category 1", false));
        final BookCategory cat2 = this.bookCategoryRepo.save(new BookCategory("CAT2", "Category 2", false));
        final BookCategory cat3 = this.bookCategoryRepo.save(new BookCategory("CAT3", "Category 3", true));

        //des livres availables d'autres non : A / NA
        //2 livre avec pages : P1 ~100 (entre 50 et 150), P2 ~ 200 (entre 150 et 250), P3 ~ 300 (entre 250 et 350)
        // livre avec auteurs A1, A2, A3
        final Author a1 = new Author("fName_A1", "lName_A2");
        final Author a2 = new Author("fName_A2", "lName_A2");
        final Author a3 = new Author("fName_A3", "lName_A3");

        final Book b_I_T_C12_A_P1_A1 = this.testedRepo.save(new Book(expectedIsbn, expectedTitle, "editor1", 60, 2000,
                List.of(a1), List.of(cat1, cat2)));
        this.buildBookCopies(b_I_T_C12_A_P1_A1, 2, 0, 3);

        final Book b_NI_CT1_C12_NA_P2_A2 = this.testedRepo.save(new Book("isbn1", closeTitle1, "editor2", 160, 2010,
                List.of(a1), List.of(cat1, cat2)));
        this.buildBookCopies(b_NI_CT1_C12_NA_P2_A2, 0, 2, 3);

        final Book b_NI_FT1_C13_A_P3_A3 = this.testedRepo.save(new Book("isbn2", farTitle1, "editor1", 300, 2020,
                List.of(a3), List.of(cat1, cat3)));
        this.buildBookCopies(b_NI_FT1_C13_A_P3_A3, 2, 0, 0);

        final Book b_NI_FT2_C13_NA_P1_A12 = this.testedRepo.save(new Book("isbn3", farTitle2, "editor2", 149, 2000,
                List.of(a1, a2), List.of(cat1, cat3)));
        this.buildBookCopies(b_NI_FT2_C13_NA_P1_A12, 0, 1, 0);

        final Book b_NI_CT1_C23_A_P2_A13 = this.testedRepo.save(new Book("isbn4", closeTitle1, "editor1", 151, 2010,
                List.of(a1, a3), List.of(cat3, cat2)));
        this.buildBookCopies(b_NI_CT1_C23_A_P2_A13, 1, 0, 0);

        final Book b_NI_FT1_C23_NA_P3_A23 = this.testedRepo.save(new Book("isbn5", farTitle1, "editor2", 350, 2020,
                List.of(a2, a3), List.of(cat2, cat3)));
        this.buildBookCopies(b_NI_FT1_C23_NA_P3_A23, 0, 0, 3);

        List<Book> books;

        // Recherche avec 1 filtre : livre avec le bon isbn
        LOG.info("Test 1 filtre: bon isbn");
        books = this.testedRepo.findAllByFilter(BookRequestFilter.getBuilder()
                .withIsbn(expectedIsbn)
                .build());
        assertThat(books).as("1 filtre: bon isbn")
                .map(Book::getId).containsExactlyInAnyOrder(b_I_T_C12_A_P1_A1.getId());

        // Recherche avec 1 filtre : livres avec titre proche
        LOG.info("Test 1 filtre: titre proche");
        books = this.testedRepo.findAllByFilter(BookRequestFilter.getBuilder()
                .withTitleAlike(expectedTitle, "en")
                .build());
        // On utilise ici contains : on pourrait avoir d'autre elements
        assertThat(books).as("1 filtre: titre proche")
                .map(Book::getId).contains(b_I_T_C12_A_P1_A1.getId(), b_NI_CT1_C12_NA_P2_A2.getId(),
                b_NI_CT1_C23_A_P2_A13.getId());

        // Recherche avec 1 filtre : livres avec category 2
        LOG.info("Test 1 filtre: catégorie 2");
        books = this.testedRepo.findAllByFilter(BookRequestFilter.getBuilder()
                .withCategories(cat2)
                .build());
        assertThat(books).as("1 filtre: catégorie 2")
                .map(Book::getId).containsExactlyInAnyOrder(b_I_T_C12_A_P1_A1.getId(), b_NI_CT1_C12_NA_P2_A2.getId(),
                b_NI_CT1_C23_A_P2_A13.getId(), b_NI_FT1_C23_NA_P3_A23.getId());

        // Recherche avec 1 filtre : livres avec category 2 ou 3
        LOG.info("Test 1 filtre: catégories 2 ou 3");
        books = this.testedRepo.findAllByFilter(BookRequestFilter.getBuilder()
                .withCategories(cat2, cat3)
                .build());
        assertThat(books).as("1 filtre: catégories 2 ou 3")
                .map(Book::getId).containsExactlyInAnyOrder(b_I_T_C12_A_P1_A1.getId(), b_NI_CT1_C12_NA_P2_A2.getId(),
                b_NI_FT1_C13_A_P3_A3.getId(), b_NI_FT2_C13_NA_P1_A12.getId(),
                b_NI_CT1_C23_A_P2_A13.getId(), b_NI_FT1_C23_NA_P3_A23.getId());

        // Recherche avec 1 filtre : Livres pour enfants
        LOG.info("Test 1 filtre: pour enfants");
        books = this.testedRepo.findAllByFilter(BookRequestFilter.getBuilder()
                .withChildCompliancy()
                .build());
        assertThat(books).as("1 filtre: pour enfants")
                .map(Book::getId).containsExactlyInAnyOrder(b_I_T_C12_A_P1_A1.getId(), b_NI_CT1_C12_NA_P2_A2.getId());

        // Recherche avec 1 filtre : Livres disponibles
        LOG.info("Test 1 filtre: disponibles");
        books = this.testedRepo.findAllByFilter(BookRequestFilter.getBuilder()
                .withAvailability()
                .build());
        assertThat(books).as("1 filtre: disponibles")
                .map(Book::getId).containsExactlyInAnyOrder(b_I_T_C12_A_P1_A1.getId(), b_NI_FT1_C13_A_P3_A3.getId(),
                b_NI_CT1_C23_A_P2_A13.getId());

        // Recherche avec 1 filtre : livres avec nb pages P2 200±50
        LOG.info("Test 1 filtre: avec nb pages 200±50");
        books = this.testedRepo.findAllByFilter(BookRequestFilter.getBuilder()
                .withNumberOfPages(200)
                .build());
        assertThat(books).as("1 filtre: avec nb pages 200±50")
                .map(Book::getId).containsExactlyInAnyOrder(b_NI_CT1_C12_NA_P2_A2.getId(), b_NI_CT1_C23_A_P2_A13.getId());

        // Recherche avec 3 filtres : b_NI_CT1_C12_NA_P2_A2 b_NI_CT1_C23_A_P2_A13
        LOG.info("Test 3 filtres: avec titre, cat C2, pour enfant, nb pages 200+50");
        books = this.testedRepo.findAllByFilter(BookRequestFilter.getBuilder()
                .withTitleAlike(expectedTitle, null)
                .withCategories(cat2)
                .withNumberOfPages(200)
                .build());
        assertThat(books).as("3 filtres: avec titre, cat C2, pour enfant, nb pages 200+50")
                .map(Book::getId).containsExactlyInAnyOrder(b_NI_CT1_C12_NA_P2_A2.getId(), b_NI_CT1_C23_A_P2_A13.getId());

        // Recherche avec 3 filtres : b_I_T_C12_A_P1_A1, b_NI_CT1_C12_NA_P2_A2
        LOG.info("Test 3 filtres: avec titre, cat C2, pour enfants");
        books = this.testedRepo.findAllByFilter(BookRequestFilter.getBuilder()
                .withTitleAlike(expectedTitle, null)
                .withCategories(cat2)
                .withChildCompliancy()
                .build());
        assertThat(books).as("3 filtres: avec titre, cat C2, pour enfants")
                .map(Book::getId).containsExactlyInAnyOrder(b_I_T_C12_A_P1_A1.getId(), b_NI_CT1_C12_NA_P2_A2.getId());

        // Recherche avec TOUS filtres :  b_I_T_C12_A_P1_A1
        LOG.info("Test TOUS filtres");
        books = this.testedRepo.findAllByFilter(BookRequestFilter.getBuilder()
                .withIsbn(expectedIsbn)
                .withTitleAlike(expectedTitle, "en")
                .withCategories(cat1, cat2)
                .withChildCompliancy()
                .withAvailability()
                .withNumberOfPages(100)
                .build());
        assertThat(books).as("TOUS filtres")
                .map(Book::getId).containsExactlyInAnyOrder(b_I_T_C12_A_P1_A1.getId());
    }

    private void buildBookCopies(Book book, int nbAvailablesNotRemoved, int nbAvailablesRemoved, int nbNotAvailables) {
        for (int i = 0; i < nbAvailablesNotRemoved; i++) {
            this.bookCopyRepo.save(new BookCopy(book, BookState.NEW, false, true));
        }

        for (int i = 0; i < nbAvailablesRemoved; i++) {
            this.bookCopyRepo.save(new BookCopy(book, BookState.NEW, true, true));
        }

        for (int i = 0; i < nbNotAvailables; i++) {
            this.bookCopyRepo.save(new BookCopy(book, BookState.NEW, false, false));
        }

    }
}
