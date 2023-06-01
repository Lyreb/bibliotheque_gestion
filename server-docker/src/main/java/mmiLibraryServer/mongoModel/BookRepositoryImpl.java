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
import java.util.Set;
import java.util.stream.Collectors;
import mmiLibraryServer.mongoModel.utils.BookRequestFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;

/**
 *
 * @author Rémi Venant
 */
public class BookRepositoryImpl implements BookRepositoryCustom {

    private static final Log LOG = LogFactory.getLog(BookRepositoryImpl.class);

    private final MongoTemplate mongoTemplate;

    @Autowired
    public BookRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<Book> findAllByFilter(BookRequestFilter filter) {
        // Process queries with all filters except availability that must be processed after
        final Query query = this.filterToBookCriteria(filter);
        // Retrieve books that match the query
        List<Book> books = this.mongoTemplate.find(query, Book.class);
        // Handle availability filter
        final Boolean availableFilter = filter.getAvailableFilter();
        if (availableFilter != null && availableFilter == true) {
            // Retrieve the subset the bookid whose bookinstance are available and not remove
            // To improve performance we use projection here
            books = this.filterAvailableBooks(books);
        }
        // HAndle
        return books;
    }

    private Query filterToBookCriteria(BookRequestFilter filter) {
        if (filter == null) {
            return Query.query(new Criteria());
        }

        Criteria criteria = new Criteria();

        // Handle isb filter
        final String isbnFilter = Strings.isBlank(filter.getIsbnFilter())
                ? null : filter.getIsbnFilter().trim();
        if (isbnFilter != null) {
            criteria = criteria.and("isbn").is(isbnFilter);
        }

        // handle categories filter
        boolean catCodeFilterRequired = filter.getCategoryCodesFilter() != null && !filter.getCategoryCodesFilter().isEmpty();
        boolean childCompliantFilterRequired = filter.getChildCompliant() != null && filter.getChildCompliant() == true;
        if (catCodeFilterRequired && childCompliantFilterRequired) {
            List<BookCategory> requiredCategories = this.mongoTemplate.find(
                    Query.query(Criteria.where("code").in(filter.getCategoryCodesFilter())),
                    BookCategory.class);
            List<BookCategory> adultCategories = this.mongoTemplate.find(
                    Query.query(Criteria.where("adultOnly").is(true)),
                    BookCategory.class);
            criteria = criteria.andOperator(
                    Criteria.where("categories").in(requiredCategories),
                    Criteria.where("categories").nin(adultCategories)
            );
        } else if (catCodeFilterRequired) {
            List<BookCategory> requiredCategories = this.mongoTemplate.find(
                    Query.query(Criteria.where("code").in(filter.getCategoryCodesFilter())),
                    BookCategory.class);
            criteria = criteria.and("categories").in(requiredCategories);
        } else if (childCompliantFilterRequired) {
            List<BookCategory> adultCategories = this.mongoTemplate.find(
                    Query.query(Criteria.where("adultOnly").is(true)),
                    BookCategory.class);
            criteria = criteria.and("categories").nin(adultCategories);
        }

        final Integer nbPagesFilter = filter.getNbPages();
        if (nbPagesFilter != null && nbPagesFilter >= 0) {
            int minPages = Math.max(0, nbPagesFilter - 50);
            int maxPages = nbPagesFilter + 50;
            criteria = criteria.and("numOfPages").gte(minPages).lte(maxPages);
        }

        Query query;

        // handle title filter (require special TextQuery)
        final String titleAlikeFilter = Strings.isBlank(filter.getTitleAlikeFilter())
                ? null : filter.getTitleAlikeFilter().trim();
        if (titleAlikeFilter != null) {
            TextCriteria txtCriteria = Strings.isBlank(filter.getLanguage())
                    ? TextCriteria.forDefaultLanguage() : TextCriteria.forLanguage(filter.getLanguage());
            txtCriteria = txtCriteria
                    .caseSensitive(false)
                    .diacriticSensitive(false)
                    .matchingPhrase(titleAlikeFilter);
            //Create a text query with the txtCriteria then add criteria
            query = TextQuery.queryText(txtCriteria).addCriteria(criteria);
        } else {
            query = Query.query(criteria);
        }

        return query;
    }

    public List<Book> filterAvailableBooks(List<Book> books) {
        final Query bookCopiesQuery = Query.query(
                Criteria.where("book").in(books)
                        .and("available").is(true)
                        .and("removed").ne(true));
        bookCopiesQuery.fields().include("book");
        Set<String> bookIds = this.mongoTemplate
                .find(bookCopiesQuery, BookCopy.class)
                .stream().map((bc) -> bc.getBook().getId())
                .collect(Collectors.toSet());
        return books.stream()
                .filter((book) -> bookIds.contains(book.getId()))
                .collect(Collectors.toList());
    }
}
