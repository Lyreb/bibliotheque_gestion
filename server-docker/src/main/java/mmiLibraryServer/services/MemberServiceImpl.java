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

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import mmiLibraryServer.mongoModel.Member;
import mmiLibraryServer.mongoModel.MemberRepository;
import mmiLibraryServer.services.exceptions.MemberWithUnreturnedLoanException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Rémi Venant
 */
@Service
public class MemberServiceImpl implements MemberService {

    private static final Log LOG = LogFactory.getLog(MemberServiceImpl.class);

    private final MemberRepository memberRepo;

    @Autowired
    public MemberServiceImpl(MemberRepository memberRepo) {
        this.memberRepo = memberRepo;
    }

    @Override
    public List<Member> getMembers() {
        return StreamSupport.stream(this.memberRepo.findAll().spliterator(), false).collect(Collectors.toList());
    }

    @Override
    public Member getMemberById(String memberId) {
        if (memberId == null) {
            throw new IllegalArgumentException("Missing member id.");
        }
        try {
            return this.memberRepo.findById(memberId).get();
        } catch (NoSuchElementException ex) {
            throw new NoSuchElementException("Unknown member.");
        }
    }

    @Override
    public Member createMember(Member member) {
        if (member == null) {
            throw new IllegalArgumentException("Missing member to create a new member.");
        }
        if (member.getId() != null) {
            throw new IllegalArgumentException("A new member cannot have already an id.");
        }
        if (member.getLoans() != null && !member.getLoans().isEmpty()) {
            throw new IllegalArgumentException("A new member cannot have already any loan.");
        }
        return this.memberRepo.save(member);
    }

    @Override
    public Member updateMember(Member memberToUpload) {
        if (memberToUpload == null) {
            throw new IllegalArgumentException("Missing information to update a member.");
        }
        Member member = this.memberRepo.findById(memberToUpload.getId())
                .orElseThrow(() -> new NoSuchElementException("Unknown member to update."));
        if (!Strings.isBlank(memberToUpload.getName())) {
            member.setName(memberToUpload.getName());
        }
        if (!Strings.isBlank(memberToUpload.getFirstname())) {
            member.setFirstname(memberToUpload.getFirstname());
        }
        if (memberToUpload.getBirthday() != null) {
            member.setBirthday(memberToUpload.getBirthday());
        }
        return this.memberRepo.save(member);
    }

    @Override
    public void deleteMemberById(String memberId) throws MemberWithUnreturnedLoanException {
        Member memberToDelete = this.memberRepo.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("Unknown member to delete."));
        Boolean hasNotReturnedLoan = memberToDelete.getLoans().stream()
                .anyMatch((l) -> l.getReturnDateTime() == null);
        if (hasNotReturnedLoan) {
            throw new MemberWithUnreturnedLoanException("The Member has unreturned loan");
        }
        this.memberRepo.deleteById(memberId);
    }

}
