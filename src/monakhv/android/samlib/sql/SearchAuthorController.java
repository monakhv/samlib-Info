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
import java.util.ArrayList;
import java.util.List;
import monakhv.android.samlib.sql.entity.AuthorCard;

/**
 *
 * @author Dmitry Monakhov
 */
public class SearchAuthorController implements AbstractController<AuthorCard>{
    private final Context context;

    public SearchAuthorController(Context context) {
        this.context = context;
    }
    

    public int update(AuthorCard t) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public long insert(AuthorCard ac) {
        Uri uri = context.getContentResolver().insert(AuthorProvider.SEARCH_AUTHOR_URI, ac2Content(ac));
        long id = ContentUris.parseId(uri);
        
        return id;
        
    }

    public int delete(AuthorCard ac) {
        Uri singleUri = ContentUris.withAppendedId(AuthorProvider.SEARCH_AUTHOR_URI, ac.getId());
        int res = context.getContentResolver().delete(singleUri, null, null);
        
        return res;
        
    }

    public List<AuthorCard> getAll() {
        List<AuthorCard> res = new ArrayList<AuthorCard>();
        Cursor cursor = context.getContentResolver().query(AuthorProvider.SEARCH_AUTHOR_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            res.add(cursor2Ac(cursor));
        }

        cursor.close();
        return res;
        
    }

    public AuthorCard getById(long id) {
        AuthorCard res = null;
        Uri singleUri = ContentUris.withAppendedId(AuthorProvider.SEARCH_AUTHOR_URI, id);
        Cursor cursor = context.getContentResolver().query(singleUri, null, null, null, null);
        if (cursor.moveToNext()) {
            res = cursor2Ac(cursor);
        }
        cursor.close();
        return res;
        
    }

    private ContentValues ac2Content(AuthorCard ac) {
        ContentValues cv = new ContentValues();

        cv.put(SQLController.COL_AC_URL, ac.getUrl());
        cv.put(SQLController.COL_AC_NAME, ac.getName());
        cv.put(SQLController.COL_AC_TITLE, ac.getTitle());
        cv.put(SQLController.COL_AC_DESC, ac.getDescription());
        cv.put(SQLController.COL_AC_SIZE, ac.getSize());
        cv.put(SQLController.COL_AC_COUNT, ac.getCount());
        
        return cv;
        
    }

    private AuthorCard cursor2Ac(Cursor cursor) {
        AuthorCard res = new AuthorCard();
        res.setId(cursor.getInt(cursor.getColumnIndex(SQLController.COL_ID)));
        res.setUrl(cursor.getString(cursor.getColumnIndex(SQLController.COL_AC_URL)));
        res.setName(cursor.getString(cursor.getColumnIndex(SQLController.COL_AC_NAME)));
        res.setTitle(cursor.getString(cursor.getColumnIndex(SQLController.COL_AC_TITLE)));
        res.setDescription(cursor.getString(cursor.getColumnIndex(SQLController.COL_AC_DESC)));
        res.setSize(cursor.getInt(cursor.getColumnIndex(SQLController.COL_AC_SIZE)));
        res.setCount(cursor.getInt(cursor.getColumnIndex(SQLController.COL_AC_COUNT)));
        
        return res;
    }
    
}
