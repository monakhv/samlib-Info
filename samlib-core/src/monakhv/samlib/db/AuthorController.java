package monakhv.samlib.db;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.stmt.QueryBuilder;
import monakhv.samlib.db.entity.*;
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
 * 2/13/15.
 */


/**
 * COL_ID+"  integer primary key autoincrement, "+
 * COL_NAME+" text, "+
 * COL_URL   +" text UNIQUE NOT NULL, "+
 * COL_isnew+" BOOLEAN DEFAULT '0' NOT NULL,"+
 * COL_mtime+" timestamp "+
 */
public class AuthorController implements AbstractController<Author> {
    private static final String DEBUG_TAG = "AuthorController";

    private final BookController bookCtl;
    private final Dao<Author, Integer> dao;
    private final Dao<Tag2Author, Integer> t2aDao;



    public AuthorController(DaoBuilder sql) {

        dao = sql.getAuthorDao();
        t2aDao = sql.getT2aDao();
        this.bookCtl = new BookController(sql);


    }

    /**
     * Make new Author object with zero-length Foreign Collection Field
     * @return new Author object
     */
    public Author getEmptyObject(){
        Author a = new Author();

        try {
            ForeignCollection<Book> books = dao.getEmptyForeignCollection(Author.COL_BOOKS);
            a.setBooks(books);
        } catch (SQLException e) {
            Log.e(DEBUG_TAG,"foreign collection error",e);
        }

        return a;
    }

    /**
     * Mark Author and all it's book as read
     *
     * @param a Author object
     * @return id of the Author
     */
    public int markRead(Author a) {
        a.setIsNew(false);
        int ires = update(a);
        List<Book> books = bookCtl.getAll(a,null);
        for (Book book : books) {
            if (book.isIsNew()) {
                book.setIsNew(false);
                bookCtl.update(book);
            }
        }

        return ires;

    }

    /**
     * Update Author object
     * @param author
     * @return
     */
    @Override
    public int update(Author author) {

        int res;

        try {
            res = dao.update(author);
        } catch (SQLException e) {
            Log.e(DEBUG_TAG, "can not update: ", e);
            return -1;
        }

        //Books of the author update
        BookCollection oldBooks = new BookCollection(bookCtl.getAll(author, null));//old books from BD
        for (Book book : author.getBooks()) {//Cycle on new Book list taken from Author object
            book.setAuthor(author);
            String url = book.getUri();
            Book oldb = oldBooks.take(url);

            if (oldb == null) {//insert
                bookCtl.insert(book);
            } else {//update
                if (book.isIsNew()) {//update
                    //TODO: we need save some parameters from the old book object
                    book.setId(oldb.getId());
                    book.setGroup_id(oldb.getGroup_id());
                    bookCtl.update(book);
                }
            }

        }
        for (Book bk : oldBooks.getLastBooks()) {
            bookCtl.delete(bk);
        }

        return res;
    }

    /**
     * Make persist new Author object
     * @param author
     * @return
     */
    @Override
    public long insert(Author author) {

        int res;

        try {
            res = dao.create(author);
        } catch (SQLException e) {
            Log.e(DEBUG_TAG, "can not insert: ", e);
            return -1;
        }

        //Insert book for the author
        Author a = getByUrl(author.getUrl());
        for (Book book : author.getBooks()) {
            book.setAuthorId(a.getId());
            bookCtl.insert(book);
        }
        return res;
    }

    /**
     * Delete author object from Data base
     * @param author Author to delete
     * @return
     */
    @Override
    public int delete(Author author) {
        //Delete book of the author first
        List<Book> books = bookCtl.getAll(author, null);

        for (Book book : books) {
            bookCtl.delete(book);
        }
        //Delete Author

        int res ;

        try {
            res = dao.delete(author);
        } catch (SQLException e) {
            Log.e(DEBUG_TAG, "can not delete: ", e);
            return -1;
        }

        return res;
    }


    @Override
    public List<Author> getAll() {
        return getAll(null);
    }

    public Author getByUrl(String url) {
        List<Author> rr;

        QueryBuilder<Author, Integer> statement = dao.queryBuilder();


        try {
            statement.where().eq(SQLController.COL_URL, url);
            rr = dao.query(statement.prepare());
        } catch (SQLException e) {
            Log.e(DEBUG_TAG, "can not find by URL: ", e);
            return null;
        }

        if (rr.size() != 1) {
            return null;

        }
        return rr.get(0);
    }

    /**
     * Get All Authors ordered by <b>order</b> param
     * Authors with new books are in the begin of the list
     * @param order Field used for sorting
     * @return list of the Authors
     */
    public List<Author> getAll(String order) {
        QueryBuilder<Author, Integer> statement = dao.queryBuilder();
        statement.orderBy(SQLController.COL_isnew, false);//new is first by default
        if (order != null) {
            Log.d(DEBUG_TAG, "Sort order is " + order);
            statement.orderBy(order, true);
        }
        List<Author> rr;

        try {
            rr = dao.query(statement.prepare());

        } catch (SQLException e) {
            Log.e(DEBUG_TAG, "select error: " + statement, e);
            return null;
        }
        return rr;
    }
    /**
     * Get All NEW Authors ordered by <b>order</b> param
     * @param order Field used for sorting
     * @return
     */
    public List<Author> getAllNew(String order) {
        QueryBuilder<Author, Integer> statement = dao.queryBuilder();
        statement.orderBy(SQLController.COL_isnew, false);//new is first by default
        if (order != null) {
            Log.d(DEBUG_TAG, "Sort order is " + order);
            statement.orderBy(order, true);
        }

        List<Author> rr;

        try {
            statement.where().eq(SQLController.COL_isnew,true);
            rr = dao.query(statement.prepare());

        } catch (SQLException e) {
            Log.e(DEBUG_TAG, "select error: " + statement, e);
            return null;
        }
        return rr;
    }

    /**
     * Get All Authors with given tag  ordered by <b>order</b> param
     *
     * @param order Field used for sorting
     * @param tag common tag for all authors to return
     * @return List of the Authors
     */
    public List<Author> getAll(String order,Tag tag) {

        QueryBuilder<Tag2Author,Integer> t2aqb = t2aDao.queryBuilder();
        t2aqb.selectColumns(SQLController.COL_T2A_AUTHORID);

        try {
            t2aqb.where().eq(SQLController.COL_T2A_TAGID,tag);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        QueryBuilder<Author, Integer> statement = dao.queryBuilder();
        statement.orderBy(SQLController.COL_isnew, false);//new is first by default
        if (order != null) {

            Log.d(DEBUG_TAG, "Sort order is " + order);
            statement.orderBy(order, true);
        }
        List<Author> rr;

        try {
            statement.where().in(SQLController.COL_ID, t2aqb);
            rr = dao.query(statement.prepare());

        } catch (SQLException e) {
            Log.e(DEBUG_TAG, "select error: " + statement, e);
            return null;
        }
        return rr;
    }

    @Override
    public Author getById(long id) {
        Integer dd = (int) id;
        try {
            return dao.queryForId(dd);
        } catch (SQLException e) {
            Log.e(DEBUG_TAG,"getById - Error",e);
            return null;
        }
    }


    public BookController getBookController() {
        return bookCtl;
    }


    /**
     * Set author new status according to the its book status
     *
     * @param a   Author object
     * @return true if the author has unread books
     */
    public boolean testMarkRead(Author a) {
        boolean rr = getReadStatus(a);
        a.setIsNew(rr);
        update(a);
        return false;
    }

     /**
     * Whether the author has unread books or not
     * @param a Author object
     * @return true if the  author has at least one unread book
     */
    public boolean getReadStatus(Author a){
        List<Book> books = bookCtl.getBooksByAuthor(a);
        for (Book book : books) {
            if (book.isIsNew()) {
                return true;
            }
        }
        return false;
    }


     /**
     * Making tags for the Author according to th given list
     *
     * @param a the Author
     * @param tags list of tag id
     */
//    public void syncTags(Author a, List<Integer> tags) {
//        for (Integer tag_id : tags) {//add new tag to the author
//            if (!a.getTags_id().contains(tag_id)) {
//                ContentValues cv = new ContentValues();
//                cv.put(SQLController.COL_T2A_AUTHORID, a.getId());
//                cv.put(SQLController.COL_T2A_TAGID, tag_id);
//                context.getContentResolver().insert(AuthorProvider.T2A_URI, cv);
//            }
//
//        }
//
//        for (Integer tag : a.getTags_id()){
//            if (! tags.contains(tag)){
//                context.getContentResolver().delete(AuthorProvider.T2A_URI, SQLController.COL_T2A_AUTHORID+ "="+a.getId()
//                        +" and "+SQLController.COL_T2A_TAGID+"="+tag.toString()
//                        , null);
//            }
//        }
//        Uri singleUri = ContentUris.withAppendedId(AuthorProvider.AUTHOR_URI, a.getId());
//        context.getContentResolver().notifyChange(singleUri, null);
//    }

}
