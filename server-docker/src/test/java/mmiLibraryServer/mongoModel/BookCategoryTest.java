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
import org.springframework.dao.DuplicateKeyException;
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
public class BookCategoryTest {

    private static final Log LOG = LogFactory.getLog(BookCategoryTest.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    public BookCategoryTest() {
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
    public void categoryMustHaveACode() {
        assertThatThrownBy(()
                -> this.mongoTemplate.save(new BookCategory(null, "catName", false)))
                .isInstanceOf(ConstraintViolationException.class);
        assertThatThrownBy(()
                -> this.mongoTemplate.save(new BookCategory("", "catName", false)))
                .isInstanceOf(ConstraintViolationException.class);
        assertThatThrownBy(()
                -> this.mongoTemplate.save(new BookCategory("   ", "catName", false)))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void categoryMustHaveAName() {
        assertThatThrownBy(()
                -> this.mongoTemplate.save(new BookCategory("catCode", null, false)))
                .isInstanceOf(ConstraintViolationException.class);
        assertThatThrownBy(()
                -> this.mongoTemplate.save(new BookCategory("catCode", "", false)))
                .isInstanceOf(ConstraintViolationException.class);
        assertThatThrownBy(()
                -> this.mongoTemplate.save(new BookCategory("catCode", "   ", false)))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void categoryProperlySaved() {
        String catId = this.mongoTemplate.save(new BookCategory("catCode", "catName", false)).getId();
        BookCategory cat = this.mongoTemplate.findById(catId, BookCategory.class);
        assertThat(cat).isNotNull();
        assert cat != null;
        assertThat(cat.getName()).isEqualTo("catName");
        assertThat(cat.getCode()).isEqualTo("catCode");
        assertThat(cat.isAdultOnly()).isFalse();
        assertThat(cat.getId()).isNotNull();
    }

    @Test
    public void categoryHasUniqueCode() {
        this.mongoTemplate.save(new BookCategory("catCode", "catName", false));
        assertThatThrownBy(()
                -> this.mongoTemplate.save(new BookCategory("catCode", "otherCateNAme", false)))
                .isInstanceOf(DuplicateKeyException.class);
    }
}
