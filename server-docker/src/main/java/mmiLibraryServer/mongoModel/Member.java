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

import com.fasterxml.jackson.annotation.JsonView;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import mmiLibraryServer.mongoModel.views.MemberViews;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

/**
 *
 * @author Rémi Venant
 */
@Document(collection = "members")
public class Member implements Serializable {

    public static final int MAJOR_YEAR_LIMIT = 18;

    @JsonView(MemberViews.Normal.class)
    @Id
    private String id;

    @JsonView(MemberViews.Normal.class)
    @NotBlank
    private String name;

    @JsonView(MemberViews.Normal.class)
    @NotBlank
    private String firstname;

    @JsonView(MemberViews.Normal.class)
    @NotNull
    private LocalDate birthday;

    @JsonView(MemberViews.WithLoans.class)
    @ReadOnlyProperty
    @DocumentReference(lookup = "{'member':?#{#self._id} }", lazy = true, sort = "{loanDate:-1}")
    private List<Loan> loans;

    protected Member() {
    }

    public Member(String name, String firstname, LocalDate birthday) {
        this.name = name;
        this.firstname = firstname;
        this.birthday = birthday;
        this.loans = List.of();
    }

    public String getId() {
        return id;
    }

    protected void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public List<Loan> getLoans() {
        return loans;
    }

    protected void setLoans(List<Loan> loans) {
        this.loans = loans;
    }

    public boolean isMinor() {
        Period p = Period.between(this.birthday, LocalDate.now());
        return p.getYears() < Member.MAJOR_YEAR_LIMIT;
    }

    @Override
    public String toString() {
        return "Member{" + "id=" + id + ", name=" + name + ", firstname=" + firstname + ", birthday=" + birthday + '}';
    }

}
