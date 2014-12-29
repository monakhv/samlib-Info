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

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import monakhv.android.samlib.data.SettingsHelper;
import monakhv.android.samlib.sql.entity.Author;
import monakhv.android.samlib.sql.entity.Book;

/**
 *
 * @author monakhv
 */
public class BookController implements AbstractController<Book> {

    private static final String DEBUG_TAG = "BookController";
    private final Context context;
    private final SettingsHelper settings;

    BookController(Context context) {
        this.context = context;
        settings = new SettingsHelper(context);
    }

    public int update(Book book) {

        int i = context.getContentResolver().update(AuthorProvider.BOOKS_URI, book2Content(book), SQLController.COL_ID + "=" + book.getId(), null);
        settings.log(DEBUG_TAG, "update: "+i);
        return i;
    }

    public long insert(Book book) {
        Uri uri = context.getContentResolver().insert(AuthorProvider.BOOKS_URI, book2Content(book));
        if (uri == null){
            settings.log(DEBUG_TAG, "insert: uri is NULL");
            Log.e(DEBUG_TAG, "insert: uri is NULL");
            return 0;
        }
        long id = ContentUris.parseId(uri);
        settings.log(DEBUG_TAG, "insert: "+id);
        return id;
    }

    /**
     * Get List of Books from DB for given Author
     *
     * @param a Author of books
     * @return List of books for the given author
     */
    public List<Book> getBooksByAuthor(Author a) {
        List<Book> books = new ArrayList<Book>();

        String author_id = (Long.valueOf(a.getId())).toString();
        String[] selectionArgs = {author_id};
        Cursor bc = context.getContentResolver().query(AuthorProvider.BOOKS_URI, null, AuthorDB.WHERE_AUTHOR_ID, selectionArgs, null);
        if (bc == null){
            Log.e(DEBUG_TAG,"getBooksByAuthor: cursor is null - "+a.getName());
            return books;
        }
        while (bc.moveToNext()) {
            books.add(cursor2Book(bc));
        }
        bc.close();
        return books;
    }

    public List<Book> getAll() {
        List<Book> res = new ArrayList<Book>();
        Cursor cursor = context.getContentResolver().query(AuthorProvider.BOOKS_URI, null, null, null, null);
        if (cursor == null){
            Log.e(DEBUG_TAG,"getAll: cursor is null");
            return null;
        }
        while (cursor.moveToNext()) {
            res.add(cursor2Book(cursor));
        }

        cursor.close();
        return res;
    }

    public Book getById(long id) {
        Book res = null;
        Uri singleUri = ContentUris.withAppendedId(AuthorProvider.BOOKS_URI, id);
        if (singleUri == null){
            settings.log(DEBUG_TAG, "getById: uri is NULL");
            Log.e(DEBUG_TAG, "getById: uri is NULL");
            return null;
        }
        Cursor cursor = context.getContentResolver().query(singleUri, null, null, null, null);
        if (cursor == null){
            settings.log(DEBUG_TAG, "getById: cursor is NULL");
            Log.e(DEBUG_TAG, "getById: cursor is NULL");
            return null;
        }
        if (cursor.moveToNext()) {
            res = cursor2Book(cursor);
        }
        cursor.close();
        return res;

    }

    public int delete(Book book) {
        Uri singleUri = ContentUris.withAppendedId(AuthorProvider.BOOKS_URI, book.getId());
        if (singleUri == null){
            settings.log(DEBUG_TAG, "delete: uri is NULL");
            Log.e(DEBUG_TAG, "delete: uri is NULL");
            return 0;
        }
        int res = context.getContentResolver().delete(singleUri, null, null);
        settings.log(DEBUG_TAG, "delete: "+res);
        return res;
    }

    public void markRead(Book book) {
        setNewStatus(book,false);
    }
    public void markUnRead(Book book) {
        setNewStatus(book,true);
    }

    public void setNewStatus(Book book, boolean status){
        book.setIsNew(status);
        int i = context.getContentResolver().update(AuthorProvider.BOOKS_URI, book2Content(book), SQLController.COL_ID + "=" + book.getId(), null);
        settings.log(DEBUG_TAG, "Book mark: "+i+"  status: "+status);
    }

    private static Book cursor2Book(Cursor cursor) {
        Book res = new Book();
        res.setId(cursor.getInt(cursor.getColumnIndex(SQLController.COL_ID)));
        res.setAuthorName(cursor.getString(cursor.getColumnIndex(SQLController.COL_BOOK_AUTHOR)));
        res.setTitle(cursor.getString(cursor.getColumnIndex(SQLController.COL_BOOK_TITLE)));
        res.setUri(cursor.getString(cursor.getColumnIndex(SQLController.COL_BOOK_LINK)));
        res.setDescription(cursor.getString(cursor.getColumnIndex(SQLController.COL_BOOK_DESCRIPTION)));
        res.setUpdateDate(cursor.getLong(cursor.getColumnIndex(SQLController.COL_BOOK_DATE)));
        res.setModifyTime(cursor.getLong(cursor.getColumnIndex(SQLController.COL_BOOK_MTIME)));
        res.setSize(cursor.getLong(cursor.getColumnIndex(SQLController.COL_BOOK_SIZE)));
        res.setForm(cursor.getString(cursor.getColumnIndex(SQLController.COL_BOOK_FORM)));
        res.setIsNew(cursor.getInt(cursor.getColumnIndex(SQLController.COL_isnew)) == 1);
        res.setAuthorId(cursor.getInt(cursor.getColumnIndex(SQLController.COL_BOOK_AUTHOR_ID)));
        res.setGroup_id(cursor.getInt(cursor.getColumnIndex(SQLController.COL_BOOK_GROUP_ID)));
        return res;
    }

    /**
     * Create content values to store new Book
     *
     * @param book Book object

     * @return ContentValues
     */
    private static ContentValues book2Content(Book book) {
        ContentValues cv = new ContentValues();

        cv.put(SQLController.COL_BOOK_AUTHOR, book.getAuthorName());
        cv.put(SQLController.COL_BOOK_DATE, book.getUpdateDate());
        cv.put(SQLController.COL_BOOK_DESCRIPTION, book.getDescription());
        cv.put(SQLController.COL_BOOK_FORM, book.getForm());
        cv.put(SQLController.COL_BOOK_ISNEW, book.isIsNew());
        cv.put(SQLController.COL_BOOK_LINK, book.getUri());
        cv.put(SQLController.COL_BOOK_MTIME, book.getModifyTime());
        cv.put(SQLController.COL_BOOK_SIZE, book.getSize());
        cv.put(SQLController.COL_BOOK_TITLE, book.getTitle());
        cv.put(SQLController.COL_BOOK_AUTHOR_ID, book.getAuthorId());
        cv.put(SQLController.COL_BOOK_GROUP_ID, book.getGroup_id());
        return cv;

    }
}
