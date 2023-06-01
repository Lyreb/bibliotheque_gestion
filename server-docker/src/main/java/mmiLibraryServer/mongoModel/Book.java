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
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotBlank;
import mmiLibraryServer.mongoModel.views.BookViews;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

/**
 *
 * @author Rémi Venant
 */
@Document(collection = "books")
public class Book implements Serializable {

    @JsonView(BookViews.Normal.class)
    @Id
    private String id;

    @JsonView(BookViews.Normal.class)
    @NotBlank
    @Indexed(unique = true)
    private String isbn;

    @JsonView(BookViews.Normal.class)
    @NotBlank
    @TextIndexed(weight = 1.0F)
    private String title;

    @JsonView(BookViews.Normal.class)
    private String editor;

    @JsonView(BookViews.Normal.class)
    private Integer numOfPages;

    @JsonView(BookViews.Normal.class)
    private Integer publicationYear;

    @JsonView(BookViews.Normal.class)
    private List<Author> authors = new ArrayList<>();

    @JsonView(BookViews.Normal.class)
    @DocumentReference
    private List<BookCategory> categories = new ArrayList<>();

    @JsonView(BookViews.WithCopies.class)
    @ReadOnlyProperty
    @DocumentReference(lookup = "{'book':?#{#self._id} }", lazy = true)
    private List<BookCopy> copies;

    protected Book() {
    }

    public Book(String isbn, String title, String editor, Integer numOfPages,
            Integer publicationYear, List<Author> authors, List<BookCategory> categories) {
        this.isbn = isbn;
        this.title = title;
        this.editor = editor;
        this.numOfPages = numOfPages;
        this.publicationYear = publicationYear;
        if (authors != null) {
            this.authors = authors;
        }
        if (categories != null) {
            this.categories = categories;
        }
        this.copies = List.of();
    }

    public Book(String isbn, String title, String editor, Integer numOfPages,
            Integer publicationYear) {
        this(isbn, title, editor, numOfPages, publicationYear, null, null);
    }

    public String getId() {
        return id;
    }

    protected void setId(String id) {
        this.id = id;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getEditor() {
        return editor;
    }

    public void setEditor(String editor) {
        this.editor = editor;
    }

    public Integer getNumOfPages() {
        return numOfPages;
    }

    public void setNumOfPages(Integer numOfPages) {
        this.numOfPages = numOfPages;
    }

    public Integer getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(Integer publicationYear) {
        this.publicationYear = publicationYear;
    }

    public List<Author> getAuthors() {
        return authors;
    }

    public void setAuthors(List<Author> authors) {
        this.authors = authors;
    }

    public List<BookCategory> getCategories() {
        return categories;
    }

    public void setCategories(List<BookCategory> categories) {
        this.categories = categories;
    }

    public List<BookCopy> getCopies() {
        return copies;
    }

    protected void setCopies(List<BookCopy> copies) {
        this.copies = copies;
    }

    @Override
    public String toString() {
        return "Book{" + "id=" + id + ", isbn=" + isbn + ", title=" + title + ", editor=" + editor + ", numOfPages=" + numOfPages + ", publicationYear=" + publicationYear + '}';
    }

}
