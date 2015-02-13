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

import monakhv.samlib.db.AbstractController;
import monakhv.samlib.db.SQLController;
import monakhv.samlib.db.entity.Tag;

/**
 *
 * @author monakhv
 */
public class TagController  implements AbstractController<Tag> {
    private static final String DEBUG_TAG ="TagController" ;
    private Context context;
    
    public TagController(Context context) {
        this.context = context;
        
    }

    /**
     * Update tag object on DB - for change tag name only!!
     * 
     * @param t th tag to update
     * @return 
     */
    public int update(Tag t) {
        
        int i =0;//we do not add dublicate tags but we can modify itself
        if (getByName(t.getName()) == -1 || getByName(t.getName()) == t.getId()){
            i = context.getContentResolver().update(AuthorProvider.TAG_URI, tag2Content(t), SQLController.COL_ID + "=" + t.getId(), null);
        }
        
        
        return i;
    }

    /**
     * insert new tag into DB checking duplicate case
     * @param t new tag to add
     * @return 
     */
    public long insert(Tag t) {
        if (getByName(t.getName())   != -1 ){
            return 0;//we do not add dublicate tags
        }
       Uri uri = context.getContentResolver().insert(AuthorProvider.TAG_URI, tag2Content(t));
       return ContentUris.parseId(uri);
       
    }
    
    /**
     * Find tag by name 
     * @param name
     * @return tag id or -1 if no tag found
     */
    public int getByName(String name){
        int res =  -1;
        String ucs = name.toUpperCase();
        
        Cursor cursor = context.getContentResolver().query(AuthorProvider.TAG_URI, null, 
                SQLController.COL_TAG_UCNAME+"=\""+ucs+"\"", null, null);
        if (cursor.moveToNext()) {
            res = cursor.getInt(cursor.getColumnIndex(SQLController.COL_ID));
        }
        cursor.close();
        return res;
    }

    public int delete(Tag t) {
        return delete(t.getId());
    }

    public List<Tag> getAll() {
        List<Tag> res = new ArrayList<>();
        Cursor cursor=context.getContentResolver().query(AuthorProvider.TAG_URI,null,null,null,SQLController.COL_TAG_NAME);
        if (cursor == null){
            Log.e(DEBUG_TAG, "getAll: cursor is null");
            return res;
        }
        while (cursor.moveToNext()){
            res.add(cursor2Tag(cursor));
        }
        cursor.close();
        return res;
    }

    public Tag getById(long id) {
        Tag res = null;
        Uri singleUri = ContentUris.withAppendedId(AuthorProvider.TAG_URI, id);
        Cursor cursor = context.getContentResolver().query(singleUri, null, null, null, null);
        if (cursor.moveToNext()) {
            res = cursor2Tag(cursor);
        }
        cursor.close();
        return res;
    }
    
    private static ContentValues tag2Content(Tag tag ){
        ContentValues cv = new ContentValues();
        
        cv.put(SQLController.COL_TAG_NAME, tag.getName());
        cv.put(SQLController.COL_TAG_UCNAME,   tag.getUcName());        
     
        return cv;
    }

    private static Tag cursor2Tag(Cursor cursor){       
        Tag res = new Tag();        
        res.setName(cursor.getString(cursor.getColumnIndex(SQLController.COL_TAG_NAME)));
        res.setId(cursor.getInt(cursor.getColumnIndex(SQLController.COL_ID)));
        return res;
    }
    public int delete(int id) {
        Uri singleUri = ContentUris.withAppendedId(AuthorProvider.TAG_URI,id);
        
        context.getContentResolver().delete(AuthorProvider.T2A_URI, SQLController.COL_T2A_TAGID+"="+id, null);
        
        int res = context.getContentResolver().delete(singleUri, null, null);
        return res;
    }

   
}
