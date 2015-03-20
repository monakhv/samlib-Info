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

import android.content.ContentValues;
import android.util.Log;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import monakhv.samlib.db.SQLController;
import monakhv.samlib.sql.entity.Book;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *
 * @author monakhv
 * Hide here all DB design
 * 
 */
public class AuthorDB extends SQLiteOpenHelper {
    public static final String ID = SQLController.COL_ID;
    public static final String WHERE_URL=SQLController.COL_URL+" = ?";
    public static final String WHERE_AUTHOR_ID=SQLController.COL_BOOK_AUTHOR_ID+" = ?";
    
    private final static String DEBUG_TAG="AuthorDB";

    public AuthorDB(Context context) {
        super(context, SQLController.DB_NAME, null, SQLController.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQLController.DB_CREATE_AUTHOR);
        db.execSQL(SQLController.DB_CREATE_BOOKS);
        db.execSQL(SQLController.DB_IDX1);
        db.execSQL(SQLController.DB_IDX2);
        //upgradeSchema3To4(db);
        
        db.execSQL(SQLController.DB_CREATE_TAGS);
        db.execSQL(SQLController.DB_CREATE_TAG_TO_AUTHOR);
        db.execSQL(SQLController.DB_CREATE_STATE);
        
        db.execSQL(SQLController.DB_IDX3);
        db.execSQL(SQLController.DB_IDX4);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(DEBUG_TAG,"Downgrade in progress");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {      
        
        if (oldVersion ==1 && newVersion ==5){
            upgradeSchema1To2(db);
            upgradeSchema2To3(db);
            upgradeSchema3To4(db);
            upgradeSchema4To5(db);
            
        }
        if (oldVersion ==2 && newVersion ==5){           
            upgradeSchema2To3(db);
            upgradeSchema3To4(db);
            upgradeSchema4To5(db);
        }
        if (oldVersion ==3 && newVersion ==5){                       
            upgradeSchema3To4(db);
            upgradeSchema4To5(db);
        }
        if (oldVersion ==4 && newVersion ==5){                       
           
            upgradeSchema4To5(db);
        }
        }
    private void upgradeSchema3To4(SQLiteDatabase db) {
        db.execSQL(SQLController.DB_CREATE_TAGS);
        db.execSQL(SQLController.DB_CREATE_TAG_TO_AUTHOR);
        db.execSQL(SQLController.DB_CREATE_STATE);
        
        db.execSQL(SQLController.DB_IDX3);
        db.execSQL(SQLController.DB_IDX4);
        db.execSQL(SQLController.DB_ALTER_BOOK1);
    }    
    
    private void upgradeSchema1To2(SQLiteDatabase db) {
        db.execSQL(SQLController.DB_CREATE_BOOKS);
        db.execSQL(SQLController.DB_IDX1);
        db.execSQL(SQLController.DB_IDX2);
        
        String [] columns = {SQLController.COL_ID,SQLController.COL_books};
        
        Cursor cursor = db.query(SQLController.TABLE_AUTHOR, columns, null, null, null, null, null);
        
      
        
        while(cursor.moveToNext()){
            byte [] data = cursor.getBlob(cursor.getColumnIndex(SQLController.COL_books));
            int author_id  = cursor.getInt(cursor.getColumnIndex(SQLController.COL_ID));
            List<Book> books = null;
            try {
                books = (List<Book>)deserializeObject(data);
            } catch (IOException ex) {
                Log.e(DEBUG_TAG, "deserialize error: ",ex);
            } catch (ClassNotFoundException ex) {
                Log.e(DEBUG_TAG, "deserialize error: ",ex);
            }
            
            for (Book book : books){
                ContentValues values = new ContentValues();
                values.put(SQLController.COL_BOOK_TITLE, book.getTitle());
                values.put(SQLController.COL_BOOK_AUTHOR, book.getAuthor());
                values.put(SQLController.COL_BOOK_SIZE, book.getSize());
                values.put(SQLController.COL_BOOK_LINK, book.getUri());
                values.put(SQLController.COL_BOOK_DATE, book.getUpdateDate());
                values.put(SQLController.COL_BOOK_AUTHOR_ID, author_id);
                values.put(SQLController.COL_BOOK_MTIME, Calendar.getInstance().getTime().getTime());
                db.insert(SQLController.TABLE_BOOKS, null, values);
                
            }
            
        }
        cursor.close();
        //db.execSQL(SQLController.ALTER2_1);
        db.execSQL(SQLController.ALTER2_2);
    }
    /**
     * Schema update to version 5 
     * Remove samlib URL
     * 
     * @param db 
     */
     private void upgradeSchema4To5(SQLiteDatabase db) {
        Log.d("upgradeSchema4To5", "Begin upgrade schema 4->5");
        String[] columns = {SQLController.COL_ID, SQLController.COL_URL};
        Map<Integer, String> data = new HashMap();
        Cursor cursor = db.query(SQLController.TABLE_AUTHOR, columns, null, null, null, null, null);
         while(cursor.moveToNext()){
             int    idx = cursor.getInt(cursor.getColumnIndex(SQLController.COL_ID));
             String url = cursor.getString(cursor.getColumnIndex(SQLController.COL_URL));
             Log.d("upgradeSchema4To5", "Change url: "+url);             
             url = url.replaceAll("http://samlib.ru", "");
             Log.d("upgradeSchema4To5", "To url: "+url);
             data.put(idx, url);
         }
         cursor.close();
         String where =SQLController.COL_ID +" = ?";
          for (Integer idx : data.keySet() ){
              ContentValues cv = new ContentValues();
              cv.put(SQLController.COL_URL, data.get(idx));
              db.update(SQLController.TABLE_AUTHOR, cv, where, new String [] {idx.toString()});
          }
          Log.d("upgradeSchema4To5", "End upgrade schema 4->5");
    }

    private void upgradeSchema2To3(SQLiteDatabase db){
        String [] columns = {SQLController.COL_ID,SQLController.COL_BOOK_DATE};
        Map<Integer,Long> data = new HashMap();
        Cursor cursor = db.query(SQLController.TABLE_BOOKS, columns, null, null, null, null, null);
        while(cursor.moveToNext()){
            long dd= cursor.getLong(cursor.getColumnIndex(SQLController.COL_BOOK_DATE));
            int    idx = cursor.getInt(cursor.getColumnIndex(SQLController.COL_ID));
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(dd);
            dd += cal.getTimeZone().getOffset(dd);
            data.put(idx, dd);
     }
        cursor.close();
      
        String where =SQLController.COL_ID +" = ?";
        
        for (Integer idx : data.keySet() ){
            Long dd = data.get(idx);
            ContentValues cv = new ContentValues();
            cv.put(SQLController.COL_BOOK_DATE, dd);
            db.update(SQLController.TABLE_BOOKS, cv, where, new String [] {idx.toString()});
        }
        
    }
    private static Object deserializeObject(byte[] b) throws IOException, ClassNotFoundException {

        Object object;
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(b));
            object = in.readObject();
        

        return object;

    }

    
}
