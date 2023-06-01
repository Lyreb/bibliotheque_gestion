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

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import java.util.NoSuchElementException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

/**
 *
 * @author Rémi Venant
 */
public class BookCategoryRepositoryImpl implements BookCategoryRepositoryCustom {

    private static final Log LOG = LogFactory.getLog(BookCategoryRepositoryImpl.class);

    private final MongoOperations mongoOps;

    @Autowired
    public BookCategoryRepositoryImpl(MongoOperations mongoOps) {
        this.mongoOps = mongoOps;
    }

    @Override
    public void deleteAll() {
        UpdateResult ur = this.mongoOps.updateMulti(Query.query(new Criteria()),
                new Update().unset("categories"), Book.class);
        LOG.debug(String.format("%d books update before remove bookCategory.", ur.getModifiedCount()));
        this.mongoOps.dropCollection(BookCategory.class);
    }

    @Override
    public void deleteAll(Iterable<? extends BookCategory> entities) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deleteAllById(Iterable<? extends String> ids) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void delete(BookCategory entity) {
        UpdateResult ur = this.mongoOps.updateMulti(
                Query.query(Criteria.where("categories").elemMatch(new Criteria().is(entity))),
                new Update().pull("categories", entity),
                Book.class);
        LOG.debug(String.format("%d books update before remove bookCategory.", ur.getModifiedCount()));
        DeleteResult dr = this.mongoOps.remove(entity);
        LOG.debug(String.format("%d bookCategory remove.", dr.getDeletedCount()));
    }

    @Override
    public void deleteById(String id) {
        BookCategory bc = this.mongoOps.findById(id, BookCategory.class);
        if (bc == null) {
            throw new NoSuchElementException("BookCategory not found");
        }
        this.delete(bc);
    }

}
