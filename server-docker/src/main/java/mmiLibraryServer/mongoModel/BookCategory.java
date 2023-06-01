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
import javax.validation.constraints.NotBlank;
import mmiLibraryServer.mongoModel.views.DefaultView;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 *
 * @author Rémi Venant
 */
@Document(collection = "bookCategories")
public class BookCategory implements Serializable {

    @JsonView(DefaultView.Default.class)
    @Id
    private String id;

    @JsonView(DefaultView.Default.class)
    @NotBlank
    @Indexed(unique = true)
    private String code;

    @JsonView(DefaultView.Default.class)
    @NotBlank
    private String name;

    @JsonView(DefaultView.Default.class)
    private boolean adultOnly;

    protected BookCategory() {
    }

    public BookCategory(String code, String name, boolean adultOnly) {
        this.code = code;
        this.name = name;
        this.adultOnly = adultOnly;
    }

    public String getId() {
        return id;
    }

    protected void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAdultOnly() {
        return adultOnly;
    }

    public void setAdultOnly(boolean adultOnly) {
        this.adultOnly = adultOnly;
    }

    @Override
    public String toString() {
        return "BookCategory{" + "id=" + id + ", code=" + code + ", name=" + name + ", adultOnly=" + adultOnly + '}';
    }

}
