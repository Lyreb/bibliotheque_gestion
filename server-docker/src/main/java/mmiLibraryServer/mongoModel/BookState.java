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

/**
 *
 * @author Rémi Venant
 */
public enum BookState {
    NEW(5), VERY_GOOD(4), GOOD(3), USED(2), BAD(1);

    private final int value;

    private BookState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static BookState fromValue(int value) {
        switch (value) {
            case 1:
                return BookState.BAD;
            case 2:
                return BookState.USED;
            case 3:
                return BookState.GOOD;
            case 4:
                return BookState.VERY_GOOD;
            case 5:
                return BookState.NEW;
            default:
                throw new IllegalArgumentException("Incorrect book state value");
        }
    }
}
