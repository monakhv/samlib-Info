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

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import monakhv.samlib.db.SQLController;

/**
 *
 * @author monakhv
 */
public class AuthorProvider extends ContentProvider {
    private static final String DEBUG_TAG="AuthorProvider";
    private AuthorDB mDB;
    private static final String AUTHORITY = "monakhv.android.samlib.sql.AuthorProvider";
    private static final String AUTHORS_BASE_PATH = SQLController.TABLE_AUTHOR;
    private static final String BOOKS_BASE_PATH = SQLController.TABLE_BOOKS;
    private static final String TAG_BASE_PATH = SQLController.TABLE_TAGS;
    
    private static final String T2A_BASE_PATH = SQLController.TABLE_T2A;
    
    public static final int AUTHORS = 100;//marker to get all authors
    public static final int AUTHOR_ID = 110;//marker to get author by ID
    public static final int AUTHORS_TAG = 120;//marker to get all authors for given tag 
    public static final int BOOKS = 200;//marker to get all books
    public static final int BOOK_ID = 210;//marker to get book by ID
    public static final int TAGS = 300;//marker to get all tags
    public static final int TAG_ID = 310;//marker to get tag by ID
    
    public static final int T2AS    = 400;//marker to get all tags
    public static final int T2A_ID = 410;//marker to get tag by ID
    
    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/samlib-monk";//Type for all table
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/samlib-monk";//Type for single Author
    public static final String BOOK_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/samlib-monk-book";//Type for all book
    public static final String BOOK_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/samlib-monk-book";//Type for single Book
    public static final String TAG_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/samlib-monk-tag";//Type for all tags
    public static final String TAG_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/samlib-monk-tag";//Type for single tag
    public static final String T2A_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/samlib-monk-t2a";//Type for all tags
    public static final String T2A_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/samlib-monk-t2a";//Type for single tag
    
    private static final UriMatcher sURIMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, AUTHORS_BASE_PATH, AUTHORS);
        sURIMatcher.addURI(AUTHORITY, AUTHORS_BASE_PATH + "/#", AUTHOR_ID);
        sURIMatcher.addURI(AUTHORITY, BOOKS_BASE_PATH, BOOKS);
        sURIMatcher.addURI(AUTHORITY, BOOKS_BASE_PATH + "/#", BOOK_ID);

        sURIMatcher.addURI(AUTHORITY, TAG_BASE_PATH, TAGS);
        sURIMatcher.addURI(AUTHORITY, TAG_BASE_PATH + "/#", TAG_ID);
        sURIMatcher.addURI(AUTHORITY, AUTHORS_BASE_PATH + "/" + TAG_BASE_PATH + "/#", AUTHORS_TAG);
        
        sURIMatcher.addURI(AUTHORITY, T2A_BASE_PATH, T2AS);
        sURIMatcher.addURI(AUTHORITY, T2A_BASE_PATH + "/#", T2A_ID);
    }
    public static final Uri AUTHOR_URI = Uri.parse("content://" + AUTHORITY
            + "/" + AUTHORS_BASE_PATH);//Content URI for AUTHORS
    public static final Uri BOOKS_URI = Uri.parse("content://" + AUTHORITY
            + "/" + BOOKS_BASE_PATH);//Content URI for BOOKS
    
    public static final Uri TAG_URI = Uri.parse("content://" + AUTHORITY
            + "/" + TAG_BASE_PATH);//Content URI for TAGS
    public static final Uri AUTHOR_TAG_URI = Uri.parse("content://" + AUTHORITY
            + "/" + AUTHORS_BASE_PATH+ "/" + TAG_BASE_PATH);//Content URI for AUTHORS of tag
    
    public static final Uri T2A_URI = Uri.parse("content://" + AUTHORITY
            + "/" + T2A_BASE_PATH);//Content URI for AUTHORS
    
    @Override
    public boolean onCreate() {
        mDB = new AuthorDB(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        Cursor cursor;
        String select_authors  = SQLController.SELECT_AUTHOR_WITH_TAGS;
        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case T2AS:
                queryBuilder.setTables(SQLController.TABLE_T2A);
                break;
            case T2A_ID:
                queryBuilder.setTables(SQLController.TABLE_T2A);
                queryBuilder.appendWhere(AuthorDB.ID + "="
                        + uri.getLastPathSegment());
                break;
            case AUTHOR_ID:
//                queryBuilder.setTables(SQLController.TABLE_AUTHOR);
//                queryBuilder.appendWhere(AuthorDB.ID + "="
//                        + uri.getLastPathSegment());
//                break;
                
                if (! TextUtils.isEmpty(sortOrder)){
                    select_authors  +=" ORDER BY "+sortOrder;
                }
                
                
                if (TextUtils.isEmpty(selection)){
                    select_authors=select_authors.replaceAll(SQLController.WHERE_PAT, "where  Author._id = "+ uri.getLastPathSegment());
                    cursor = mDB.getReadableDatabase().rawQuery(select_authors, null);
                }
                else {
                    select_authors=select_authors.replaceAll(SQLController.WHERE_PAT, "where Author._id = "  +uri.getLastPathSegment()+"  and "+selection);
                    cursor = mDB.getReadableDatabase().rawQuery(select_authors, selectionArgs);
                }
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            case AUTHORS:             
                
                if (! TextUtils.isEmpty(sortOrder)){
                    select_authors  +=" ORDER BY "+sortOrder;
                }
                
                if (TextUtils.isEmpty(selection)){
                    select_authors=select_authors.replaceAll(SQLController.WHERE_PAT, "");
                    cursor = mDB.getReadableDatabase().rawQuery(select_authors, null);                    
                }
                else {
                    select_authors=select_authors.replaceAll(SQLController.WHERE_PAT, "where "+selection);
                    cursor = mDB.getReadableDatabase().rawQuery(select_authors, selectionArgs);
                }
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
                //queryBuilder.setTables(SQLController.TABLE_AUTHOR);
                //break;
            case BOOK_ID:
                queryBuilder.setTables(SQLController.TABLE_BOOKS);
                queryBuilder.appendWhere(AuthorDB.ID + "="
                        + uri.getLastPathSegment());
                break;
            case BOOKS:
                queryBuilder.setTables(SQLController.TABLE_BOOKS);
                break;
            case TAGS:
                queryBuilder.setTables(SQLController.TABLE_TAGS);
                break;
            case TAG_ID:
                queryBuilder.setTables(SQLController.TABLE_TAGS);
                queryBuilder.appendWhere(AuthorDB.ID + "="
                        + uri.getLastPathSegment());
                break;
            case AUTHORS_TAG:
                queryBuilder.setTables(SQLController.TABLE_AUTHOR+", "+SQLController.TABLE_T2A);
                
                queryBuilder.appendWhere(
                        SQLController.TABLE_T2A+"."+SQLController.COL_T2A_AUTHORID+"="+SQLController.TABLE_AUTHOR+"."+AuthorDB.ID
                        +" AND "+SQLController.TABLE_T2A+"."+SQLController.COL_T2A_TAGID + "="
                        + uri.getLastPathSegment());
                break;
            default:
                Log.e(DEBUG_TAG, "Wrong uri type: "+uriType);
                throw new IllegalArgumentException("Unknown URI");
        }

        cursor = queryBuilder.query(mDB.getReadableDatabase(),
                projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case AUTHORS:
                return CONTENT_TYPE;
            case AUTHOR_ID:
                return CONTENT_ITEM_TYPE;
            case BOOKS:
                return BOOK_CONTENT_TYPE;
            case BOOK_ID:
                return BOOK_CONTENT_ITEM_TYPE;
            case TAGS:
                return TAG_CONTENT_TYPE;
            case TAG_ID:
                return TAG_CONTENT_ITEM_TYPE;
            case AUTHORS_TAG:
                return CONTENT_TYPE;
            case T2AS:
                return T2A_CONTENT_TYPE;
            case T2A_ID:
                return T2A_CONTENT_ITEM_TYPE;
            default:
                return null;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);

        SQLiteDatabase sqlDB = mDB.getWritableDatabase();
        long newID = -1;
        switch (uriType) {
            case AUTHORS:
                newID = sqlDB.insert(SQLController.TABLE_AUTHOR, null, values);
                break;
            case BOOKS:
                newID = sqlDB.insert(SQLController.TABLE_BOOKS, null, values);
                break;
            case TAGS:
                newID = sqlDB.insert(SQLController.TABLE_TAGS, null, values);
                break;
            case T2AS:
                newID = sqlDB.insert(SQLController.TABLE_T2A, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown or Invalid URI " + uri);

        }

        if (newID > 0) {
            Uri newUri = ContentUris.withAppendedId(uri, newID);
            getContext().getContentResolver().notifyChange(uri, null);
            return newUri;
        } else {
            throw new SQLException("Failed to insert row into " + uri);
        }
    }

    /**
     * Standard implementation 
     * 
     * Books cleanup moved into controller
     * @param uri
     * @param selection
     * @param selectionArgs
     * @return 
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = mDB.getWritableDatabase();
        int rowsAffected = 0;
        String id;
        switch (uriType) {
            case T2AS:
                rowsAffected = sqlDB.delete(SQLController.TABLE_T2A,
                        selection, selectionArgs);
                break;
            case T2A_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsAffected = sqlDB.delete(SQLController.TABLE_T2A,
                            AuthorDB.ID + "=" + id, null);                    
                }
                else {
                    rowsAffected = sqlDB.delete(SQLController.TABLE_T2A,
                            selection + " and " + AuthorDB.ID + "=" + id,
                            selectionArgs);
                }
                break;
            case AUTHORS:
                rowsAffected = sqlDB.delete(SQLController.TABLE_AUTHOR,
                        selection, selectionArgs);
                break;
            case AUTHOR_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsAffected = sqlDB.delete(SQLController.TABLE_AUTHOR,
                            AuthorDB.ID + "=" + id, null);
                } else {
                    rowsAffected = sqlDB.delete(SQLController.TABLE_AUTHOR,
                            selection + " and " + AuthorDB.ID + "=" + id,
                            selectionArgs);
                }
                break;
            case BOOKS:
                rowsAffected = sqlDB.delete(SQLController.TABLE_BOOKS,
                        selection, selectionArgs);
                break;
            case BOOK_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsAffected = sqlDB.delete(SQLController.TABLE_BOOKS,
                            AuthorDB.ID + "=" + id, null);
                } else {
                    rowsAffected = sqlDB.delete(SQLController.TABLE_BOOKS,
                            selection + " and " + AuthorDB.ID + "=" + id,
                            selectionArgs);
                }
                break;
            case TAGS:
                rowsAffected = sqlDB.delete(SQLController.TABLE_TAGS,
                        selection, selectionArgs);
                //put heare TAG2Author clean up                
                break;
            case TAG_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsAffected = sqlDB.delete(SQLController.TABLE_TAGS,
                            AuthorDB.ID + "=" + id, null);
                }
                else {
                    rowsAffected = sqlDB.delete(SQLController.TABLE_TAGS,
                            selection + " and " + AuthorDB.ID + "=" + id,
                            selectionArgs);
                }
                //making member clean up
                sqlDB.delete(SQLController.TABLE_T2A,SQLController.COL_T2A_TAGID+"="+id,null);
                //notifyChange or redrow
                break;
                        
            default:
                throw new IllegalArgumentException("Unknown or Invalid URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsAffected;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = mDB.getWritableDatabase();

        int rowsAffected;
        String id;
        StringBuilder modSelection;
        switch (uriType) {
            case AUTHOR_ID:
                id = uri.getLastPathSegment();
                modSelection = new StringBuilder(AuthorDB.ID + "=" + id);

                if (!TextUtils.isEmpty(selection)) {
                    modSelection.append(" AND ").append(selection);
                }

                rowsAffected = sqlDB.update(SQLController.TABLE_AUTHOR,
                        values, modSelection.toString(), null);
                break;
            case AUTHORS:
                rowsAffected = sqlDB.update(SQLController.TABLE_AUTHOR,
                        values, selection, selectionArgs);
                break;
            case BOOKS:
                rowsAffected = sqlDB.update(SQLController.TABLE_BOOKS,
                        values, selection, selectionArgs);
                break;
            case BOOK_ID:
                id = uri.getLastPathSegment();
                modSelection = new StringBuilder(AuthorDB.ID + "=" + id);

                if (!TextUtils.isEmpty(selection)) {
                    modSelection.append(" AND ").append(selection);
                }

                rowsAffected = sqlDB.update(SQLController.TABLE_BOOKS,
                        values, modSelection.toString(), null);
                break;
            case TAGS:
                rowsAffected = sqlDB.update(SQLController.TABLE_TAGS,
                        values, selection, selectionArgs);
                break;
            case TAG_ID:
                id = uri.getLastPathSegment();
                modSelection = new StringBuilder(AuthorDB.ID + "=" + id);
                
                if (!TextUtils.isEmpty(selection)) {
                    modSelection.append(" AND ").append(selection);
                }

                rowsAffected = sqlDB.update(SQLController.TABLE_TAGS,
                        values, modSelection.toString(), null);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI");
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsAffected;
    }
}
