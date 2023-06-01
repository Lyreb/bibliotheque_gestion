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
import java.time.format.DateTimeFormatter;
import javax.validation.ConstraintViolationException;
import mmiLibraryServer.configuration.MongoConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.*;

/**
 *
 * @author Rémi Venant
 */
@DataMongoTest
@Import(MongoConfiguration.class)
@ActiveProfiles("mongo-test")
public class MemberTest {

    private static final Log LOG = LogFactory.getLog(MemberTest.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    private final LocalDate birthdayDateSample = LocalDate.parse("1990-12-20", DateTimeFormatter.ISO_DATE);

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    public MemberTest() {
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
        //Remove all document without dropping collection to avoid index dropping
        this.mongoTemplate.remove(new BasicQuery("{}"), Member.class);
    }

    @Test
    public void memberMustHaveAName() {
        assertThatThrownBy(()
                -> this.mongoTemplate.save(new Member(null, "Jean", this.birthdayDateSample)))
                .isInstanceOf(ConstraintViolationException.class);
        assertThatThrownBy(()
                -> this.mongoTemplate.save(new Member("", "Jean", this.birthdayDateSample)))
                .isInstanceOf(ConstraintViolationException.class);
        assertThatThrownBy(()
                -> this.mongoTemplate.save(new Member("   ", "Jean", this.birthdayDateSample)))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void memberMustHaveAFirstname() {
        assertThatThrownBy(()
                -> this.mongoTemplate.save(new Member("Bon", null, this.birthdayDateSample)))
                .isInstanceOf(ConstraintViolationException.class);
        assertThatThrownBy(()
                -> this.mongoTemplate.save(new Member("Bon", "", this.birthdayDateSample)))
                .isInstanceOf(ConstraintViolationException.class);
        assertThatThrownBy(()
                -> this.mongoTemplate.save(new Member("Bon", "    ", this.birthdayDateSample)))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void memberMustHaveABirthday() {
        assertThatThrownBy(()
                -> this.mongoTemplate.save(new Member("Bon", "Jean", null)))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void memberIsMinorOk() {
        Member major = TestInstanceBuilder.withMajorStatus(TestInstanceBuilder.emptyMember());
        Member minor = TestInstanceBuilder.withMinorStatus(TestInstanceBuilder.emptyMember());
        assertThat(major.isMinor()).isFalse();
        assertThat(minor.isMinor()).isTrue();
    }

    @Test
    public void memberProperlySaved() {
        String memberId = this.mongoTemplate.save(new Member("Bon", "Jean", this.birthdayDateSample)).getId();
        Member m = this.mongoTemplate.findById(memberId, Member.class);
        assertThat(m).isNotNull();
        assert m != null;
        assertThat(m.getName()).isEqualTo("Bon");
        assertThat(m.getFirstname()).isEqualTo("Jean");
        assertThat(m.getBirthday()).isEqualTo(this.birthdayDateSample);
        assertThat(m.getId()).isNotNull();
        assertThat(m.getLoans()).isEmpty();
    }

}
