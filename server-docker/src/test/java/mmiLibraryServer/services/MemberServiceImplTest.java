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
package mmiLibraryServer.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import mmiLibraryServer.mongoModel.Loan;
import mmiLibraryServer.mongoModel.Member;
import mmiLibraryServer.mongoModel.MemberRepository;
import mmiLibraryServer.mongoModel.TestInstanceBuilder;
import mmiLibraryServer.services.exceptions.MemberWithUnreturnedLoanException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.BDDMockito.given;
import org.mockito.Mockito;

/**
 *
 * @author Rémi Venant
 */
@ExtendWith(MockitoExtension.class)
public class MemberServiceImplTest {

    private static final Log LOG = LogFactory.getLog(MemberServiceImplTest.class);

    private AutoCloseable mocks;

    @Mock
    private MemberRepository memberRepo;

    @InjectMocks
    private MemberServiceImpl testedService;

    private Map<String, Member> testMembersById;

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {

    }

    @BeforeEach
    public void setUp() {
        this.mocks = MockitoAnnotations.openMocks(this);
        this.testedService = new MemberServiceImpl(memberRepo);
        this.testMembersById = Map.of(
                "u1", new Member("name1", "fname1", LocalDate.parse("1990-12-20")),
                "u2", new Member("name2", "fname2", LocalDate.parse("1999-10-20")),
                "u3", new Member("name3", "fname3", LocalDate.parse("1987-09-11")));
    }

    @AfterEach
    public void tearDown() throws Exception {
        this.mocks.close();
    }

    @Test
    public void getMembersOk() {
        given(this.memberRepo.findAll()).willReturn(this.testMembersById.values());
        List<Member> members = this.testedService.getMembers();
        Mockito.verify(this.memberRepo).findAll();
    }

    @Test
    public void getMemberByIdThrowsOk() {
        assertThatThrownBy(()
                -> this.testedService.getMemberById(null))
                .isInstanceOf(IllegalArgumentException.class);
        given(this.memberRepo.findById("u4")).willThrow(NoSuchElementException.class);
        assertThatThrownBy(()
                -> this.testedService.getMemberById("u4"))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    public void getMemberByIdOk() {
        given(this.memberRepo.findById("u1")).willReturn(Optional.of(this.testMembersById.get("u1")));
        Member m = this.testedService.getMemberById("u1");
        Mockito.verify(this.memberRepo).findById("u1");
    }

    @Test
    public void createMemberThrowsOk() {
        assertThatThrownBy(()
                -> this.testedService.createMember(null))
                .isInstanceOf(IllegalArgumentException.class);
        final Member mWithId = TestInstanceBuilder.withId(new Member("mName", "mFirstname",
                LocalDate.now()), "idMember");
        assertThatThrownBy(()
                -> this.testedService.createMember(mWithId))
                .isInstanceOf(IllegalArgumentException.class);
        final Member mWithLoans = TestInstanceBuilder.withLoans(new Member("mName", "mFirstname",
                LocalDate.now()), List.of(TestInstanceBuilder.emptyLoan()));
        assertThatThrownBy(()
                -> this.testedService.createMember(mWithLoans))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void createMemberOk() {
        Member member = new Member("mName", "mFirstname", LocalDate.now());
        given(this.memberRepo.save(member)).willReturn(member);
        Member m = this.testedService.createMember(member);
        Mockito.verify(this.memberRepo).save(member);
    }

    @Test
    public void updateMemberThrowsOk() {
        assertThatThrownBy(()
                -> this.testedService.updateMember(null))
                .isInstanceOf(IllegalArgumentException.class);
        given(this.memberRepo.findById(null)).willThrow(IllegalArgumentException.class);
        final Member mWithNullId = TestInstanceBuilder.emptyMember();
        assertThatThrownBy(()
                -> this.testedService.updateMember(mWithNullId))
                .isInstanceOf(IllegalArgumentException.class);
        final Member mWithBadId = TestInstanceBuilder.withId(new Member("mName", "mFirstname",
                LocalDate.now()), "idMemberBad");
        given(this.memberRepo.findById(mWithBadId.getId())).willReturn(Optional.empty());
        assertThatThrownBy(()
                -> this.testedService.updateMember(mWithBadId))
                .isInstanceOf(NoSuchElementException.class);
        Mockito.verify(this.memberRepo, Mockito.times(1)).findById(mWithBadId.getId());
    }

    @Test
    public void updateMemberOk() {
        final Member mWithGoodId = TestInstanceBuilder.withId(new Member("mName", "mFirstname",
                LocalDate.now()), "idMemberGood");
        given(this.memberRepo.findById(mWithGoodId.getId())).willReturn(Optional.of(mWithGoodId));
        given(this.memberRepo.save(mWithGoodId)).willReturn(mWithGoodId);
        this.testedService.updateMember(mWithGoodId);
        Mockito.verify(this.memberRepo, Mockito.times(1)).findById(mWithGoodId.getId());
        Mockito.verify(this.memberRepo).save(Mockito.any());
    }

    @Test
    public void deleteteMemberByIdThrowsOk() {
        given(this.memberRepo.findById(null)).willThrow(IllegalArgumentException.class);
        assertThatThrownBy(()
                -> this.testedService.deleteMemberById(null))
                .isInstanceOf(IllegalArgumentException.class);
        given(this.memberRepo.findById("mid")).willReturn(Optional.empty());
        assertThatThrownBy(()
                -> this.testedService.deleteMemberById("mid"))
                .isInstanceOf(NoSuchElementException.class);
        Mockito.verify(this.memberRepo, Mockito.times(1)).findById("mid");
        Mockito.verify(this.memberRepo, Mockito.never()).deleteById(Mockito.any());
    }

    @Test
    public void deleteteMemberByIdThrowsMemberWithUnreturnedLoanExceptionOk() {
        Loan l1 = TestInstanceBuilder.emptyLoan();
        l1.setLoanDateTime(TestInstanceBuilder.now());
        l1.setReturnDateTime(TestInstanceBuilder.now());
        Loan l2 = TestInstanceBuilder.emptyLoan();
        l2.setLoanDateTime(TestInstanceBuilder.now());
        Member m = TestInstanceBuilder.withLoans(TestInstanceBuilder.emptyMember(), List.of(
                l1, l2
        ));
        given(this.memberRepo.findById("mid")).willReturn(Optional.of(m));
        assertThatThrownBy(()
                -> this.testedService.deleteMemberById("mid"))
                .isInstanceOf(MemberWithUnreturnedLoanException.class);
        Mockito.verify(this.memberRepo, Mockito.times(1)).findById("mid");
        Mockito.verify(this.memberRepo, Mockito.never()).deleteById(Mockito.any());
    }

    @Test
    public void deleteteMemberByIdOk() throws MemberWithUnreturnedLoanException {
        Loan l1 = TestInstanceBuilder.emptyLoan();
        l1.setLoanDateTime(TestInstanceBuilder.now());
        l1.setReturnDateTime(TestInstanceBuilder.now());
        Loan l2 = TestInstanceBuilder.emptyLoan();
        l2.setLoanDateTime(TestInstanceBuilder.now());
        l2.setReturnDateTime(TestInstanceBuilder.now());
        Member m = TestInstanceBuilder.withLoans(TestInstanceBuilder.emptyMember(), List.of(
                l1, l2
        ));
        given(this.memberRepo.findById("mid")).willReturn(Optional.of(m));
        this.testedService.deleteMemberById("mid");
        Mockito.verify(this.memberRepo).findById("mid");
        Mockito.verify(this.memberRepo).deleteById("mid");
    }
}
