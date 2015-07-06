package monakhv.samlib.db;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.DatabaseResults;
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
    private final Tag2AuthorController t2aCtl;
    private final TagController tagCtl;
    private final Dao<Author, Integer> dao;
    private final Dao<Tag2Author, Integer> t2aDao;



    public AuthorController(DaoBuilder sql) {

        dao = sql.getAuthorDao();
        t2aDao = sql.getT2aDao();
        this.bookCtl = new BookController(sql);
        this.t2aCtl=new Tag2AuthorController(sql);
        this.tagCtl = new TagController(sql);


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
     * @param author Author to persist
     * @return id
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
            book.setAuthor(a);
            bookCtl.insert(book);
        }
        return res;
    }

    /**
     * Delete author object from Data base
     * @param author Author to delete
     * @return id
     */
    @Override
    public int delete(Author author) {
        //Delete book of the author first
        bookCtl.deleteByAuthor(author);

        //Delete Tag2Author
        t2aCtl.deleteByAuthor(author);

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
        return getAll(SamLibConfig.TAG_AUTHOR_ALL, null);
    }

    public Author getByUrl(String url) {


        QueryBuilder<Author, Integer> statement = dao.queryBuilder();

        List<Author> rr=query(getPrepared(statement, SQLController.COL_URL, url));

        if (rr== null || rr.size() != 1) {

            return null;

        }
        return rr.get(0);
    }




    private List<Author> query(PreparedQuery<Author> prep){
        List<Author> rr;



        if (prep == null){
            Log.e(DEBUG_TAG,"query: prepare error");
        }
        try {
            rr=dao.query(prep);
        } catch (SQLException e) {
            Log.e(DEBUG_TAG, "query: query error");
            return null;
        }
        makeAuthorTags(rr);
        return rr;
    }
    /**
     *  Very general method to make SQL query like this
     *  <i>Select * from Author WHERE x=y</i>
     * @param cb QueryBuilder could be make sorted calls
     * @param ColumnName Column name for WHERE or null when select ALL items
     * @param object Object for WHERE =
     * @return preparedQuery
     */
    private PreparedQuery<Author> getPrepared(QueryBuilder<Author,Integer>  cb,String ColumnName, Object object){

        try {
            if (ColumnName != null) {
                if (ColumnName.equalsIgnoreCase(SQLController.COL_ID) && object instanceof QueryBuilder) {
                    Log.d(DEBUG_TAG,"PreparedQuery: running where IN query");
                    cb.where().in(ColumnName, (QueryBuilder<Tag2Author, Integer>) object);
                } else {
                    Log.d(DEBUG_TAG, "PreparedQuery: running where EQ query");
                    cb.where().eq(ColumnName, object);
                }
            }
            return  cb.prepare();
        }catch (SQLException e) {
            Log.e(DEBUG_TAG, "getPrepared  error: " + cb.toString(), e);
            return null;
        }


    }


    /**
     * Make human readable String with list of  all tags for the authors
     *
     * @param authors list of the authors
     */
    private void makeAuthorTags(List<Author> authors){
        for (Author author : authors ){
            setTags(author);
        }
    }
    public void setTags(Author author){
        if (author.getTag2Authors()==null){
            return;
        }
        int num = author.getTag2Authors().size();
        int i =1;
        StringBuilder sb = new StringBuilder();
        for (Tag2Author t2a : author.getTag2Authors()){
            sb.append(tagCtl.getById(t2a.getTag().getId()).getName());
            if (i<num){
                sb.append(", ");
            }
            ++i;
        }
        author.setAll_tags_name(sb.toString());
    }

    /**
     * the main method to query authors
     *
     * SamLibConfig.TAG_AUTHOR_ALL - all authors
     * SamLibConfig.TAG_AUTHOR_NEW - authors with new books
     *
     *
     * @param isel ALL, New or TAG-id
     * @param rowSort -- SQL order part statement - can be null
     * @return list of the authors
     */
    public List<Author> getAll(int isel, String rowSort){
        PreparedQuery<Author> prep = getPrepared(isel, rowSort);

        if (prep == null){
            Log.e(DEBUG_TAG, "getAll: prepare error");
            return null;
        }

        return query(prep);
    }

    public DatabaseResults getRowResults(int isel, String rowSort){
        PreparedQuery<Author> prep = getPrepared(isel, rowSort);
        if (prep == null){
            Log.e(DEBUG_TAG, "getRowResults: prepare error: iSel = " + isel + "   rowSort = " + rowSort);
            return null;
        }
        try {
            CloseableIterator<Author> iterator = dao.iterator(prep);
            return iterator.getRawResults();
        } catch (SQLException e) {
            Log.e(DEBUG_TAG, "getRowResults: get Results error");
            return null;
        }

    }

    private PreparedQuery<Author> getPrepared(int isel, String rowSort){
        QueryBuilder<Author, Integer> statement = dao.queryBuilder();
        if (rowSort != null){
            statement.orderByRaw(rowSort);
        }
        if (isel == SamLibConfig.TAG_AUTHOR_ALL){//return ALL Authors
            Log.d(DEBUG_TAG,"getPrepared: query ALL Authors");
            return getPrepared(statement, null, null);
        }
        if (isel == SamLibConfig.TAG_AUTHOR_NEW){//return Authors with new Books
            return getPrepared(statement, SQLController.COL_isnew, true);
        }
        Tag tag = tagCtl.getById(isel);
        if (tag == null){
            Log.e(DEBUG_TAG,"getPrepared: wrong tag: "+isel+"<");
            return null;
        }
        QueryBuilder<Tag2Author,Integer> t2aqb = t2aDao.queryBuilder();
        t2aqb.selectColumns(SQLController.COL_T2A_AUTHORID);

        try {
            t2aqb.where().eq(SQLController.COL_T2A_TAGID,tag);

        } catch (SQLException e) {
            Log.e(DEBUG_TAG,"getPrepared:  SQL Error");
            return null;
        }
        return getPrepared(statement, SQLController.COL_ID, t2aqb);
    }


    @Override
    public Author getById(long id) {
        Integer dd = (int) id;
        Author a;
        try {
            a= dao.queryForId(dd);
        } catch (SQLException e) {
            Log.e(DEBUG_TAG,"getById - Error",e);
            return null;
        }
        if (a != null){
            setTags(a);
        }
        return a;
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
     * @param author the Author
     * @param tags list of tags
     * @return true if  database sync was done
     */
    public boolean syncTags(Author author, List<Tag>tags){
        //TODO: modification of the list of tags into the Author Object
        return  t2aCtl.sync(author,tags);
    }

}
