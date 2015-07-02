package monakhv.samlib.db;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import monakhv.samlib.db.AbstractController;
import monakhv.samlib.db.DaoBuilder;
import monakhv.samlib.db.SQLController;
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
 COL_BOOK_LINK +" text,"+
 COL_BOOK_AUTHOR                +" text,"+
 COL_BOOK_TITLE                     +" text,"+
 COL_BOOK_FORM                     +" text,"+
 COL_BOOK_SIZE                        +" INTEGER,"+
 COL_BOOK_GROUP_ID             +" INTEGER,"+
 COL_BOOK_DATE                      +" timestamp,"+//from the samlib
 COL_BOOK_DESCRIPTION        +" text,"+
 COL_BOOK_AUTHOR_ID            +" INTEGER NOT NULL,"+
 COL_BOOK_MTIME                    +" timestamp, "+//updated in the db
 COL_BOOK_ISNEW                    +" BOOLEAN DEFAULT '0' NOT NULL"+
 */
public class BookController  implements AbstractController<Book> {
    private static final String DEBUG_TAG="BookController";

    private final Dao<Book,Integer> dao;

    public BookController(DaoBuilder sql) {

        dao = sql.getBookDao();

    }
    @Override
    public int update(Book book) {

        int res;

        try {
            res=dao.update(book);
        } catch (SQLException e) {
            Log.e(DEBUG_TAG,"can not update: ",e);
            return -1;

        }
        return res;

    }

    @Override
    public long insert(Book book) {
        int res;
        try {
            res=dao.create(book);
        } catch (SQLException e) {
            Log.e(DEBUG_TAG, "can not insert: " , e);
            return -1;
        }
        return res;
    }

    @Override
    public int delete(Book book) {
        int res;
        try {
            res = dao.delete(book);
        } catch (SQLException e) {
            Log.e(DEBUG_TAG, "can not delete: "  , e);
            return -1;
        }

        return res;
    }

    @Override
    public List<Book> getAll() {
        return null;
    }
    public List<Book> getAll(Author author, String order) {
        List<Book> res ;
        QueryBuilder<Book,Integer> qb = dao.queryBuilder();
        qb.orderBy(SQLController.COL_BOOK_ISNEW, false);//new is first by default

        try {
            qb.where().eq(SQLController.COL_BOOK_AUTHOR_ID,author);
            if (order != null){
                qb.orderBy(order,false);
            }

            res = dao.query(qb.prepare());
        } catch (SQLException e) {
            Log.e(DEBUG_TAG, "Query error: " , e);
            return null;
        }

 return res;
    }

    @Override
    public Book getById(long id) {
        return null;
    }

}
