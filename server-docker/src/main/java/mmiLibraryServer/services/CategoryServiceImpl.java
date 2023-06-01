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
import mmiLibraryServer.mongoModel.BookCategory;
import mmiLibraryServer.mongoModel.BookCategoryRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Rémi Venant
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    private static final Log LOG = LogFactory.getLog(CategoryServiceImpl.class);

    private final BookCategoryRepository bookCatRepo;

    @Autowired
    public CategoryServiceImpl(BookCategoryRepository bookCatRepo) {
        this.bookCatRepo = bookCatRepo;
    }

    @Override
    public List<BookCategory> getCategories() {
        return StreamSupport.stream(this.bookCatRepo.findAll().spliterator(), false).collect(Collectors.toList());
    }

    @Override
    public BookCategory getCategoryById(String categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("Missing category id.");
        }
        try {
            return this.bookCatRepo.findById(categoryId).get();
        } catch (NoSuchElementException ex) {
            throw new NoSuchElementException("Unknown category.");
        }
    }

    @Override
    public BookCategory createCategory(BookCategory category) {
        if (category == null) {
            throw new IllegalArgumentException("Missing information to create a new category.");
        }
        if (category.getId() != null) {
            throw new IllegalArgumentException("A new category cannot have already an id.");
        }
        return this.bookCatRepo.save(category);
    }

    @Override
    public BookCategory updateCategory(BookCategory category) {
        if (category == null || category.getId() == null) {
            throw new IllegalArgumentException("Missing information to update category.");
        }
        if (!this.bookCatRepo.existsById(category.getId())) {
            throw new NoSuchElementException("Unknown category to update.");
        }
        return this.bookCatRepo.save(category);
    }

    @Override
    public void deleteCategoryById(String categoryId) {
        if (categoryId == null || !this.bookCatRepo.existsById(categoryId)) {
            throw new IllegalArgumentException("Unknown category to delete.");
        }
        if (!this.bookCatRepo.existsById(categoryId)) {
            throw new NoSuchElementException("Unknown category to delete.");
        }
        this.bookCatRepo.deleteById(categoryId);
    }

}
