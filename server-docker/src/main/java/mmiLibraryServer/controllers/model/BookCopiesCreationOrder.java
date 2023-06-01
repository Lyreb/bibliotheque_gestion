/*
 * Copyright (C) 2021 IUT Laval - Le Mans Université.
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
package mmiLibraryServer.controllers.model;

import mmiLibraryServer.mongoModel.BookState;

/**
 *
 * @author Rémi Venant
 */
public class BookCopiesCreationOrder {

    private Integer numCopies;

    private BookState initialState;

    public BookCopiesCreationOrder() {
    }

    public Integer getNumCopies() {
        return numCopies;
    }

    public void setNumCopies(Integer numCopies) {
        this.numCopies = numCopies;
    }

    public BookState getInitialState() {
        return initialState;
    }

    public void setInitialState(BookState initialState) {
        this.initialState = initialState;
    }

}
