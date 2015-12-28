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

package monakhv.samlib.db;


import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import monakhv.samlib.db.entity.*;
import monakhv.samlib.log.Log;


import java.sql.SQLException;
import java.util.List;



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
    private final Tag2AuthorController t2aCtl;
    private final TagController tagCtl;
    private final GroupBookController grpCtl;
    private final Dao<Author, Integer> dao;
    private final Dao<Tag2Author, Integer> t2aDao;


    public AuthorController(DaoBuilder sql) {

        dao = sql.getAuthorDao();
        t2aDao = sql.getT2aDao();
        this.bookCtl = new BookController(sql);
        this.t2aCtl = new Tag2AuthorController(sql);
        this.tagCtl = new TagController(sql);
        grpCtl=new GroupBookController(sql);


    }


    /**
     * Mark Author and all it's book as read
     *
     * @param a Author object
     * @return id of the Author
     */
    public int markRead(Author a) {
        loadBooks(a);
        a.setIsNew(false);
        int ires = update(a);

        for (Book book : a.getBooks()) {
            if (book.isIsNew()) {
                book.setIsNew(false);
                bookCtl.update(book);
            }
        }

        return ires;

    }

    /**
     * Update Author object
     *
     * @param author Author to update
     * @return id
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
        if (!author.isBookLoaded()) {//books are not loaded since update the author only without books
            Log.i(DEBUG_TAG,"Books are not loaded exiting");
            return res ;
        }

        grpCtl.operate(author.getGroupBooks());
        bookCtl.operate(author);


        return res;
    }

    /**
     * Make persist new Author object
     *
     * @param author Author to persist
     * @return id
     */
    @Override
    public long insert(Author author) {

        try {
            dao.create(author);
        } catch (SQLException e) {
            Log.e(DEBUG_TAG, "can not insert: ", e);
            return -1;
        }

        //Insert book for the author
        Author a = getByUrl(author.getUrl());
        for (Book book : author.getBooks()) {
            book.setAuthor(a);
            bookCtl.insert(book);
        }
        return a.getId();
    }

    /**
     * Delete author object from Data base
     *
     * @param author Author to delete
     * @return id
     */
    @Override
    public int delete(Author author) {
        loadBooks(author);
        loadGroupBooks(author);
        //Delete book of the author first
        bookCtl.delete(author.getBooks());

        //Delete Tag2Author
        t2aCtl.deleteByAuthor(author);

        grpCtl.delete(author.getGroupBooks());

        //Delete Author

        int res;

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
        return getAll(SamLibConfig.TAG_AUTHOR_ALL, null);
    }

    public Author getByUrl(String url) {


        QueryBuilder<Author, Integer> statement = dao.queryBuilder();

        List<Author> rr = query(getPrepared(statement, SQLController.COL_URL, url));

        if (rr == null || rr.size() != 1) {
            return null;
        }
        return rr.get(0);
    }


    private List<Author> query(PreparedQuery<Author> prep) {
        List<Author> rr;


        if (prep == null) {
            Log.e(DEBUG_TAG, "query: prepare error");
        }
        try {
            rr = dao.query(prep);
        } catch (SQLException e) {
            Log.e(DEBUG_TAG, "query: query error");
            return null;
        }

        return rr;
    }

    /**
     * Very general method to make SQL query like this
     * <i>Select * from Author WHERE x=y</i>
     *
     * @param cb         QueryBuilder could be make sorted calls
     * @param ColumnName Column name for WHERE or null when select ALL items
     * @param object     Object for WHERE =
     * @return preparedQuery
     */
    private PreparedQuery<Author> getPrepared(QueryBuilder<Author, Integer> cb, String ColumnName, Object object) {

        try {
            if (ColumnName != null) {
                if (ColumnName.equalsIgnoreCase(SQLController.COL_ID) && object instanceof QueryBuilder) {
                    //Log.d(DEBUG_TAG, "PreparedQuery: running where IN query");
                    cb.where().in(ColumnName, (QueryBuilder<Tag2Author, Integer>) object);
                } else {
                    Log.d(DEBUG_TAG, "PreparedQuery: running where EQ query");
                    cb.where().eq(ColumnName, object);
                }
            }
            return cb.prepare();
        } catch (SQLException e) {
            Log.e(DEBUG_TAG, "getPrepared  error: " + cb.toString(), e);
            return null;
        }


    }


    /**
     * Make human readable String with list of  all tags for the authors
     *
     * @param authors list of the authors
     */
    public void updateAuthorTags(List<Author> authors) {
        for (Author author : authors) {
            updateTags(author);
        }
    }

    public void updateAuthorTags() {
        updateAuthorTags(getAll());
    }

    public void updateTags(Author author) {

        if (author.getTag2Authors() == null) {
            Log.e(DEBUG_TAG, "updateTags: T2A Collection is NULL for Author " + author.getName());
            return;
        }
        int num = author.getTag2Authors().size();
        int i = 1;
        StringBuilder sb = new StringBuilder();
        //Log.d(DEBUG_TAG, "updateTags: author " + author.getName() + " has " + num + " tags");
        for (Tag2Author t2a : author.getTag2Authors()) {
            sb.append(tagCtl.getById(t2a.getTag().getId()).getName());
            if (i < num) {
                sb.append(", ");
            }
            ++i;
        }
        author.setAll_tags_name(sb.toString());
        update(author);
    }

    /**
     * the main method to query authors
     * <p/>
     * SamLibConfig.TAG_AUTHOR_ALL - all authors
     * SamLibConfig.TAG_AUTHOR_NEW - authors with new books
     *
     * @param iselectTag ALL, New or TAG-id
     * @param rowSort    -- SQL order part statement - can be null
     * @return list of the authors
     */
    public synchronized List<Author> getAll(int iselectTag, String rowSort) {
        PreparedQuery<Author> prep = getPrepared(iselectTag, rowSort);

        if (prep == null) {
            Log.e(DEBUG_TAG, "getAll: prepare error");
            return null;
        }

        return query(prep);
    }


    private PreparedQuery<Author> getPrepared(int isel, String rowSort) {
        QueryBuilder<Author, Integer> statement = dao.queryBuilder();
        if (rowSort != null) {
            statement.orderByRaw(rowSort);
        }
        if (isel == SamLibConfig.TAG_AUTHOR_ALL) {//return ALL Authors
            //Log.d(DEBUG_TAG, "getPrepared: query ALL Authors");
            return getPrepared(statement, null, null);
        }
        if (isel == SamLibConfig.TAG_AUTHOR_NEW) {//return Authors with new Books
            return getPrepared(statement, SQLController.COL_isnew, true);
        }
        Tag tag = tagCtl.getById(isel);
        if (tag == null) {
            Log.e(DEBUG_TAG, "getPrepared: wrong tag: " + isel + "<");
            return null;
        }
        QueryBuilder<Tag2Author, Integer> t2aqb = t2aDao.queryBuilder();
        t2aqb.selectColumns(SQLController.COL_T2A_AUTHORID);

        try {
            t2aqb.where().eq(SQLController.COL_T2A_TAGID, tag);

        } catch (SQLException e) {
            Log.e(DEBUG_TAG, "getPrepared:  SQL Error");
            return null;
        }
        return getPrepared(statement, SQLController.COL_ID, t2aqb);
    }


    @Override
    public Author getById(long id) {
        Integer dd = (int) id;
        Author a;
        try {
            a = dao.queryForId(dd);
        } catch (SQLException e) {
            Log.e(DEBUG_TAG, "getById - Error", e);
            return null;
        }

        return a;
    }


    public BookController getBookController() {
        return bookCtl;
    }

    public GroupBookController getGroupBookController(){
        return grpCtl;
    }


    /**
     * Set author new status according to the its book status
     *
     * @param a Author object
     * @return true if the author has unread books
     */
    public boolean testMarkRead(Author a) {
        boolean rr = getReadStatus(a);
        a.setIsNew(rr);
        a.setBookLoaded(false);
        update(a);
        return false;
    }

    /**
     * Whether the author has unread books or not
     *
     * @param a Author object
     * @return true if the  author has at least one unread book
     */
    public boolean getReadStatus(Author a) {
        loadBooks(a);

        for (Book book : a.getBooks()) {
            if (book.isIsNew()) {
                return true;
            }
        }
        return false;
    }


    /**
     * Making tags for the Author according to th given list
     *
     * @param author the Author
     * @param tags   list of tags
     * @return true if  database sync was done
     */
    public boolean syncTags(Author author, List<Tag> tags) {

        boolean bres = t2aCtl.sync(author, tags);
        author = getById(author.getId());
        if (bres) {
            //Log.d(DEBUG_TAG, "syncTags: making update for All_Tags_String for " + author.getName());
            updateTags(author);
        }
        return bres;
    }

    /**
     * Find books of the author and load them into object
     * @param a Author object to load books for
     */
    public void loadBooks(Author a) {
        a.setBooks(getBookController().getBooksByAuthor(a));
        a.setBookLoaded(true);
    }

    public void loadGroupBooks(Author a){
        a.setGroupBooks(grpCtl.getByAuthor(a));
    }

    /**
     * Ugly hack to cleanup database
     * DELETE from Book where author_id=0
     */
    public void cleanBooks(){
        try {
            dao.executeRawNoArgs("DELETE from "+SQLController.TABLE_BOOKS+"  where " + SQLController.COL_BOOK_AUTHOR_ID+" = 0");
        } catch (SQLException e) {
            Log.e(DEBUG_TAG,"Delete clean books: ",e);
        }
    }
}
