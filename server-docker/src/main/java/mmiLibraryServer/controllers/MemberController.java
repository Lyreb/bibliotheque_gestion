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
package mmiLibraryServer.controllers;

import com.fasterxml.jackson.annotation.JsonView;
import java.util.List;
import mmiLibraryServer.controllers.views.CompositeViews;
import mmiLibraryServer.mongoModel.Member;
import mmiLibraryServer.mongoModel.views.MemberViews;
import mmiLibraryServer.services.MemberService;
import mmiLibraryServer.services.exceptions.MemberWithUnreturnedLoanException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Rémi Venant
 */
@RestController
@RequestMapping("/api/v1/rest/members")
public class MemberController {

    private static final Log LOG = LogFactory.getLog(MemberController.class);

    private MemberService memberSvc;

    @Autowired
    public MemberController(MemberService memberSvc) {
        this.memberSvc = memberSvc;
    }

    /**
     * GET /members. Get all the members.
     *
     * @return the members
     */
    @GetMapping
    @JsonView(MemberViews.Normal.class)
    public List<Member> getMembers() {
        return this.memberSvc.getMembers();
    }

    /**
     * POST /members. Create a member.
     *
     * @param memberToCreate the member to create
     * @return
     */
    @PostMapping
    @JsonView(MemberViews.Normal.class)
    public Member createMember(@RequestBody Member memberToCreate) {
        if (memberToCreate == null) {
            throw new IllegalArgumentException("Missing information to create member.");
        }
        return this.memberSvc.createMember(memberToCreate);
    }

    /**
     * GET /member/:memberId. Get a member.
     *
     * @param memberId the member id
     * @return the created member
     */
    @GetMapping("{memberId}")
    @JsonView(CompositeViews.MemberWithLoansWithBookCopy.class)
    public Member getMember(@PathVariable String memberId) {
        return this.memberSvc.getMemberById(memberId);
    }

    /**
     * PUT /member/:memberId. Update a member.
     *
     * @param memberId the member id
     * @param memberToUpdate the member to update
     * @return the updated member
     */
    @PutMapping("{memberId}")
    @JsonView(CompositeViews.MemberWithLoansWithBookCopy.class)
    public Member updateMember(@PathVariable String memberId,
            @RequestBody Member memberToUpdate) {
        if (memberToUpdate == null) {
            throw new IllegalArgumentException("Miising information to update member.");
        }
        if (!memberId.equals(memberToUpdate.getId())) {
            throw new IllegalArgumentException("Wrong member id to update member.");
        }
        return this.memberSvc.updateMember(memberToUpdate);
    }

    /**
     * DELETE /member/:memberId. Delete a member.(will keep its loan)
     *
     * @param memberId the member id
     * @throws MemberWithUnreturnedLoanException Member has loan unreturned (409)
     */
    @DeleteMapping("{memberId}")
    public void deleteMember(@PathVariable String memberId) throws MemberWithUnreturnedLoanException {
        this.memberSvc.deleteMemberById(memberId);
    }

}
