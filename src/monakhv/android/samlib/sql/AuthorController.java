/*
 * Copyright 2013 Dmitry Monakhov.
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
 */
package monakhv.android.samlib.sql;

import android.app.backup.BackupManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import monakhv.android.samlib.sql.entity.Author;
import monakhv.android.samlib.sql.entity.Book;
import monakhv.android.samlib.sql.entity.BookCollection;

/**
 * All DB manipulations must be done via this class
 *
 * @author monakhv
 */
public class AuthorController implements AbstractController<Author> {

    private static final String DEBUG_TAG = "AuthorController";
    private final BookController bkCtr;
    private final Context context;

    public AuthorController(Context context) {
        this.context = context;
        bkCtr = new BookController(context);
    }

    public BookController getBookController() {
        return bkCtr;
    }

     public List<Author> getAll() {
        return getAll(null,null);
    }
    
    /**
     * Return all authors from Data Base
     *
     * @param selection Select string
     * @param order Order by string
     * @return List of Author object - th eresult
     */
    public List<Author> getAll(String selection, String order) {
        List<Author> res = new ArrayList<Author>();
        Cursor cursor = context.getContentResolver().query(AuthorProvider.AUTHOR_URI, null, selection, null, order);
        if (cursor == null){
            Log.e(DEBUG_TAG,"getAll: cursor is null");
            return res;
        }
        while ( cursor.moveToNext()) {
            res.add(Cursor2Author(cursor));
        }

        cursor.close();
        return res;
    }
    /**
     * Test whether the selection is empty or not
     *
     * @param selection selection string
     * @return true if no author is found
     */
    public boolean isEmpty(String selection){        
        Cursor cursor = context.getContentResolver().query(AuthorProvider.AUTHOR_URI, null, selection, null, null);
        if (cursor == null){
            return true;
        }
        boolean res = cursor.moveToNext();
        cursor.close();
        return !res;
    }
   

    /**
     * Update Author in DB id is constant
     *
     * @param a Author object
     * @return Author id
     */
    public int update(Author a) {

        int i = context.getContentResolver().update(AuthorProvider.AUTHOR_URI, author2Content(a), SQLController.COL_ID + "=" + a.getId(), null);

        Log.d(DEBUG_TAG, "Author id " + a.getId());
        BookCollection oldBooks = new BookCollection(bkCtr.getBooksByAuthor(a));


        for (Book book : a.getBooks()) {//Cycle on new Book list
            book.setAuthorId(a.getId());
            String url = book.getUri();
            Book oldb = oldBooks.take(url);

            if (oldb == null) {//insert                
                bkCtr.insert(book);
            } else {//update                
                if (book.isIsNew()) {//update
                    //TODO: we need save some paramiters from the old book object
                    book.setId(oldb.getId());
                    book.setGroup_id(oldb.getGroup_id());
                    bkCtr.update(book);
                }
            }

        }
        for (Book bk : oldBooks.getLastBooks()) {
            bkCtr.delete(bk);
        }

        return i;

    }

    /**
     * Add new entry to Author table
     *
     * @param a object to add
     * @return  Author - id
     */
    public long insert(Author a) {

        Uri uri = context.getContentResolver().insert(AuthorProvider.AUTHOR_URI, author2Content(a));
        if (uri == null){
            Log.e(DEBUG_TAG, "insert: uri is NULL");
            return 0;
        }
        BackupManager bmr = new BackupManager(context);
        bmr.dataChanged();
        long id = ContentUris.parseId(uri);
        for (Book book : a.getBooks()) {
            book.setAuthorId(id);
            bkCtr.insert(book);
        }
        return id;
    }

    /**
     * Delete Single Author object
     *
     * @param a Author to delete
     * @return  Author - id
     */
    public int delete(Author a) {
        Uri singleUri = ContentUris.withAppendedId(AuthorProvider.AUTHOR_URI, a.getId());
        if (singleUri == null){

            Log.e(DEBUG_TAG, "delete: uri is NULL");
            return 0;
        }

        List<Book> books = bkCtr.getBooksByAuthor(a);

        for (Book book : books) {
            bkCtr.delete(book);
        }
        BackupManager bmr = new BackupManager(context);
        bmr.dataChanged();
        
        context.getContentResolver().delete(AuthorProvider.T2A_URI, SQLController.COL_T2A_AUTHORID+"="+a.getId(), null);
        return  context.getContentResolver().delete(singleUri, null, null);
    }

    /**
     * Mark Author and all it's book as read
     *
     * @param a Author object
     * @return id of the Author
     */
    public int markRead(Author a) {
        a.setIsNew(false);
        int i = context.getContentResolver().update(AuthorProvider.AUTHOR_URI, author2Content(a), SQLController.COL_ID + "=" + a.getId(), null);
        List<Book> books = bkCtr.getBooksByAuthor(a);
        for (Book book : books) {
            if (book.isIsNew()) {
                book.setIsNew(false);
                bkCtr.update(book);
            }
        }

        return i;

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
        context.getContentResolver().update(AuthorProvider.AUTHOR_URI, author2Content(a), SQLController.COL_ID + "=" + a.getId(), null);
        return false;

    }

    /**
     * Whether th author has unread books or not
     * @param a Author object
     * @return true if the  author has at least one unread book
     */
    public boolean getReadStatus(Author a){
        List<Book> books = bkCtr.getBooksByAuthor(a);
        for (Book book : books) {
            if (book.isIsNew()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Find Author in DB using its URL string
     *
     * @param url URL String to find Author by
     * @return Author object or null if not found
     */
    public Author getByUrl(String url) {
        Author res = null;
        String[] selectionArgs = {url};
        Cursor cursor = context.getContentResolver().query(AuthorProvider.AUTHOR_URI, null, AuthorDB.WHERE_URL, selectionArgs, null);
        if (cursor == null){
            Log.e(DEBUG_TAG,"getByUrl: cuirsor is null - "+url);
            return null;
        }
        if (cursor.moveToNext()) {
            res = Cursor2Author(cursor);
        }

        cursor.close();
        return res;
    }

    /**
     * Find Author object into DB using id as parameter
     *
     * @param id Author id
     * @return  Author object or null if not found
     */
    public Author getById(long id) {
        Author res = null;
        Uri singleUri = ContentUris.withAppendedId(AuthorProvider.AUTHOR_URI, id);
        if (singleUri == null){

            Log.e(DEBUG_TAG, "getById: uri is NULL");
            return null;
        }
        Cursor cursor = context.getContentResolver().query(singleUri, null, null, null, null);
        if (cursor == null){
            return null;
        }
        if (cursor.moveToNext()) {
            res = Cursor2Author(cursor);
        }
        cursor.close();
        return res;
    }
    
    public Author getByBook(Book book){
        return getById(book.getAuthorId());
    }

    /**
     * Making tags for the Author according to th given list
     *
     * @param a the Author
     * @param tags list of tag id
     */
    public void syncTags(Author a, List<Integer> tags) {
        for (Integer tag_id : tags) {//add new tag to the author
            if (!a.getTags_id().contains(tag_id)) {
                ContentValues cv = new ContentValues();
                cv.put(SQLController.COL_T2A_AUTHORID, a.getId());
                cv.put(SQLController.COL_T2A_TAGID, tag_id);
                context.getContentResolver().insert(AuthorProvider.T2A_URI, cv);
            }

        }
        
        for (Integer tag : a.getTags_id()){
            if (! tags.contains(tag)){
                context.getContentResolver().delete(AuthorProvider.T2A_URI, SQLController.COL_T2A_AUTHORID+ "="+a.getId()
                        +" and "+SQLController.COL_T2A_TAGID+"="+tag.toString()
                        , null);
            }
        }
        Uri singleUri = ContentUris.withAppendedId(AuthorProvider.AUTHOR_URI, a.getId());
        context.getContentResolver().notifyChange(singleUri, null);
    }

    /**
     * Create ContentValues object to store new Author object to SQL Store
     *
     * @param a object to store
     * @return object can be used in Content insert methods
     */
    private static ContentValues author2Content(Author a) {
        ContentValues cv = new ContentValues();

        cv.put(SQLController.COL_NAME, a.getName());
        cv.put(SQLController.COL_URL, a.getUrl());
        cv.put(SQLController.COL_mtime, a.getUpdateDate());
        cv.put(SQLController.COL_isnew, a.isIsNew());

        return cv;
    }

    private Author Cursor2Author(Cursor cursor) {

        Author a = new Author();
        a.setId(cursor.getInt(cursor.getColumnIndex(SQLController.COL_ID)));
        a.setName(cursor.getString(cursor.getColumnIndex(SQLController.COL_NAME)));
        a.setUrl(cursor.getString(cursor.getColumnIndex(SQLController.COL_URL)));        

        a.setUpdateDate(cursor.getLong(cursor.getColumnIndex(SQLController.COL_mtime)));
        a.setIsNew(cursor.getInt(cursor.getColumnIndex(SQLController.COL_isnew)) == 1);

        //Populate List of Books
        List<Book> books = bkCtr.getBooksByAuthor(a);
        a.setBooks(books);


        String all_tag_names = cursor.getString(cursor.getColumnIndex(SQLController.COL_TGNAMES));

        if (all_tag_names != null) {
            String[] names = all_tag_names.split(",");
            a.setTags_name(Arrays.asList(names));
        }

        String all_tag_ids = cursor.getString(cursor.getColumnIndex(SQLController.COL_TGIDS));

        if (all_tag_ids != null) {
            String[] ids = all_tag_ids.split(",");

            List<Integer> res = new ArrayList<Integer>();
            for (String s : ids) {
                res.add(Integer.valueOf(s));
            }

            a.setTags_id(res);
        }


        return a;
    }

    public static Author Cursor2Author(Context applicationContext, Cursor cursor) {
        AuthorController ctl = new AuthorController(applicationContext);
        return ctl.Cursor2Author(cursor);
    }
}
