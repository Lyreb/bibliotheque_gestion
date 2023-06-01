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
package mmiLibraryServer.mongoModel.utils;

import java.util.List;
import java.util.stream.Collectors;
import mmiLibraryServer.mongoModel.BookCategory;

/**
 *
 * @author Rémi Venant
 */
public class BookRequestFilter {

    private String isbnFilter;

    private String language;

    private String titleAlikeFilter;

    private List<String> categoryCodesFilter;

    private Boolean childCompliant;

    private Boolean AvailableFilter;

    private Integer nbPages;

    public BookRequestFilter() {
    }

    public String getIsbnFilter() {
        return isbnFilter;
    }

    public void setIsbnFilter(String isbnFilter) {
        this.isbnFilter = isbnFilter;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTitleAlikeFilter() {
        return titleAlikeFilter;
    }

    public void setTitleAlikeFilter(String titleAlikeFilter) {
        this.titleAlikeFilter = titleAlikeFilter;
    }

    public List<String> getCategoryCodesFilter() {
        return categoryCodesFilter;
    }

    public void setCategoryCodesFilter(List<String> categoryCodesFilter) {
        this.categoryCodesFilter = categoryCodesFilter;
    }

    public Boolean getChildCompliant() {
        return childCompliant;
    }

    public void setChildCompliant(Boolean childCompliant) {
        this.childCompliant = childCompliant;
    }

    public Boolean getAvailableFilter() {
        return AvailableFilter;
    }

    public void setAvailableFilter(Boolean AvailableFilter) {
        this.AvailableFilter = AvailableFilter;
    }

    public Integer getNbPages() {
        return nbPages;
    }

    public void setNbPages(Integer nbPages) {
        this.nbPages = nbPages;
    }

    public static class Builder {

        private BookRequestFilter filter = new BookRequestFilter();

        public BookRequestFilter build() {
            return this.filter;
        }

        public Builder withIsbn(String isbn) {
            this.filter.setIsbnFilter(isbn);
            return this;
        }

        public Builder withTitleAlike(String title, String language) {
            this.filter.setTitleAlikeFilter(title);
            this.filter.setLanguage(language);
            return this;
        }

        public Builder withCategories(List<String> catCodes) {
            this.filter.setCategoryCodesFilter(catCodes);
            return this;
        }

        public Builder withCategories(String... catCodes) {
            this.filter.setCategoryCodesFilter(List.of(catCodes));
            return this;
        }

        public Builder withCategories(BookCategory... categories) {
            this.filter.setCategoryCodesFilter(List.of(categories)
                    .stream().map(BookCategory::getCode).collect(Collectors.toList()));
            return this;
        }

        public Builder withChildCompliancy() {
            this.filter.setChildCompliant(true);
            return this;
        }

        public Builder withAvailability() {
            this.filter.setAvailableFilter(true);
            return this;
        }

        public Builder withNumberOfPages(Integer nbPages) {
            this.filter.setNbPages(nbPages);
            return this;
        }
    }

    public static Builder getBuilder() {
        return new Builder();
    }
}
