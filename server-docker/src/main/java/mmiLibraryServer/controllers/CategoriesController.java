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

import java.util.List;
import mmiLibraryServer.mongoModel.BookCategory;
import mmiLibraryServer.services.CategoryService;
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
@RequestMapping("/api/v1/rest/categories")
public class CategoriesController {

    private static final Log LOG = LogFactory.getLog(CategoriesController.class);

    private final CategoryService categorySvc;

    @Autowired
    public CategoriesController(CategoryService categorySvc) {
        this.categorySvc = categorySvc;
    }

    /**
     * GET /categories. Get all the categories.
     *
     * @return the categories
     */
    @GetMapping
    public List<BookCategory> getCategories() {
        return this.categorySvc.getCategories();
    }

    /**
     * POST /categories. Create a category.
     *
     * @param categoryToCreate the category to create
     * @return the created category
     */
    @PostMapping
    public BookCategory createCategory(@RequestBody BookCategory categoryToCreate) {
        return this.categorySvc.createCategory(categoryToCreate);
    }

    /**
     * GET /categories/:catId. Get a category.
     *
     * @param catId the category id
     * @return the category
     */
    @GetMapping("{catId}")
    public BookCategory getCategory(@PathVariable String catId) {
        return this.categorySvc.getCategoryById(catId);
    }

    /**
     * PUT /categories/:catId. Update a category.
     *
     * @param catId the category id
     * @param categoryToUpdate the category to update
     * @return the updated category
     */
    @PutMapping("{catId}")
    public BookCategory updateCategory(@PathVariable String catId,
            @RequestBody BookCategory categoryToUpdate) {
        if (categoryToUpdate == null) {
            throw new IllegalArgumentException("Miising category information.");
        }
        if (!catId.equals(categoryToUpdate.getId())) {
            throw new IllegalArgumentException("Wrong category id to update.");
        }
        return this.categorySvc.updateCategory(categoryToUpdate);
    }

    /**
     * DELETE /categories/:catId. Delete a category.
     *
     * @param catId the category id
     */
    @DeleteMapping("{catId}")
    public void deleteMember(@PathVariable String catId) {
        this.categorySvc.deleteCategoryById(catId);
    }

}
