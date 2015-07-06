package monakhv.samlib.db;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.DatabaseResults;
import monakhv.samlib.db.entity.Author;
import monakhv.samlib.db.entity.Book;
import monakhv.samlib.log.Log;


import java.sql.SQLException;
import java.util.List;


/*
 * Copyright 2015  Dmitry Monakhov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 *
 * 2/16/15.
 */

/**
 * COL_ID+"  integer primary key autoincrement, "+
 * COL_BOOK_LINK +" text,"+
 * COL_BOOK_AUTHOR                +" text,"+
 * COL_BOOK_TITLE                     +" text,"+
 * COL_BOOK_FORM                     +" text,"+
 * COL_BOOK_SIZE                        +" INTEGER,"+
 * COL_BOOK_GROUP_ID             +" INTEGER,"+
 * COL_BOOK_DATE                      +" timestamp,"+//from the samlib
 * COL_BOOK_DESCRIPTION        +" text,"+
 * COL_BOOK_AUTHOR_ID            +" INTEGER NOT NULL,"+
 * COL_BOOK_MTIME                    +" timestamp, "+//updated in the db
 * COL_BOOK_ISNEW                    +" BOOLEAN DEFAULT '0' NOT NULL"+
 */
public class BookController implements AbstractController<Book> {
    private static final String DEBUG_TAG = "BookController";

    private final Dao<Book, Integer> dao;

    public BookController(DaoBuilder sql) {

        dao = sql.getBookDao();

    }

    /**
     * Update book into database
     *
     * @param book The object to update
     * @return id
     */
    @Override
    public int update(Book book) {

        int res;

        try {
            res = dao.update(book);
        } catch (SQLException e) {
            Log.e(DEBUG_TAG, "can not update: ", e);
            return -1;

        }
        return res;

    }

    /**
     * Insert new Book object into Database
     *
     * @param book object to insert
     * @return id
     */
    @Override
    public long insert(Book book) {
        int res;
        try {
            res = dao.create(book);
        } catch (SQLException e) {
            Log.e(DEBUG_TAG, "can not insert: ", e);
            return -1;
        }
        return res;
    }

    /**
     * Delete Book object
     *
     * @param book objecr to delete
     * @return id
     */
    @Override
    public int delete(Book book) {
        int res;
        try {
            res = dao.delete(book);
        } catch (SQLException e) {
            Log.e(DEBUG_TAG, "can not delete: ", e);
            return -1;
        }

        return res;
    }

    /**
     * Delete all book of the Author
     *
     * @param author The Author whose books to be deleted
     */
    void deleteByAuthor(Author author) {
        DeleteBuilder<Book, Integer> deleteBuilder = dao.deleteBuilder();
        try {
            deleteBuilder.where().eq(SQLController.COL_BOOK_AUTHOR_ID, author);
            deleteBuilder.delete();
        } catch (SQLException e) {
            Log.e(DEBUG_TAG, "Delete all Author book error", e);
        }

    }

    @Override
    public List<Book> getAll() {
        return null;
    }

    public List<Book> getAll(Author author, String order) {
        List<Book> res;
        QueryBuilder<Book, Integer> qb = dao.queryBuilder();
        qb.orderBy(SQLController.COL_BOOK_ISNEW, false);//new is first by default

        if (order != null) {
            qb.orderByRaw(order);
        }

        try {

            res = dao.query(getPrepared(qb, author));
        } catch (SQLException e) {
            Log.e(DEBUG_TAG, "Query error: ", e);
            return null;
        }

        return res;
    }

    private PreparedQuery<Book> getPrepared(QueryBuilder<Book, Integer> qb , Author author) throws SQLException {

        qb.where().eq(SQLController.COL_BOOK_AUTHOR_ID, author);

        return qb.prepare();

    }

    public DatabaseResults getRowResult(Author author, String order) {

        QueryBuilder<Book, Integer> qb = dao.queryBuilder();
        qb.orderBy(SQLController.COL_BOOK_ISNEW, false);//new is first by default

        if (order != null) {
            qb.orderByRaw(order);
        }
        try {
            PreparedQuery<Book> prep = getPrepared(qb,author);
            CloseableIterator<Book> iterator = dao.iterator(prep);
            return iterator.getRawResults();

        } catch (SQLException e) {
            Log.e(DEBUG_TAG,"getRowResult: error",e);
            return null;
        }

    }

    @Override
    public Book getById(long id) {
        Integer dd = (int) id;
        try {
            return dao.queryForId(dd);
        } catch (SQLException e) {
            return null;
        }

    }

    public List<Book> getBooksByAuthor(Author a) {
        return getAll(a, null);
    }

    public void markRead(Book book) {
        book.setIsNew(false);
        update(book);
    }

    public void markUnRead(Book book) {
        book.setIsNew(true);
        update(book);
    }
}
