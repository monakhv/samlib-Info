package monakhv.android.samlib.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import monakhv.samlib.db.DaoBuilder;
import monakhv.samlib.db.SQLController;
import monakhv.samlib.db.entity.Author;
import monakhv.samlib.db.entity.Book;
import monakhv.samlib.db.entity.Tag;
import monakhv.samlib.db.entity.Tag2Author;
import monakhv.samlib.log.Log;

import java.sql.SQLException;

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
 * 02.07.15.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper implements DaoBuilder{
    private static final String DEBUG_TAG="DatabaseHelper";
    private Dao<Author,Integer> authorDao;
    private Dao<Book,Integer>   bookDao;
    private Dao<Tag,Integer>     tagDao;
    private Dao<Tag2Author, Integer>        t2aDao;
    public DatabaseHelper(Context context){
        super(context, SQLController.DB_NAME, null, SQLController.DB_VERSION);

    }
    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, Author.class);
            TableUtils.createTable(connectionSource, Book.class);
            TableUtils.createTable(connectionSource, Tag.class);
            TableUtils.createTable(connectionSource, Tag2Author.class);

            getAuthorDao();
            authorDao.executeRawNoArgs(SQLController.DB_IDX1);
            authorDao.executeRawNoArgs(SQLController.DB_IDX2);
            authorDao.executeRawNoArgs(SQLController.DB_IDX3);
            authorDao.executeRawNoArgs(SQLController.DB_IDX4);


        } catch (SQLException e) {
            e.printStackTrace();
        }


       // db.execSQL(SQLController.DB_CREATE_STATE);


    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {

    }

    public  Dao<Author,Integer> getAuthorDao() {
        if (authorDao == null){
            try {
                authorDao = getDao(Author.class);
            } catch (SQLException e) {
                Log.e(DEBUG_TAG,"Author DAO Error",e);
            }
        }
        return authorDao;
    }

    public Dao<Book, Integer> getBookDao() {
        if (bookDao == null){
            try {
                bookDao=getDao(Book.class);
            } catch (SQLException e) {
                Log.e(DEBUG_TAG,"Book DAO Error",e);
            }
        }
        return bookDao;
    }

    public Dao<Tag, Integer> getTagDao()  {
        if (tagDao == null) {
            try {
                tagDao = getDao(Tag.class);
            } catch (SQLException e) {
                Log.e(DEBUG_TAG, "Tag DAO Error", e);
            }
        }
        return tagDao;
    }
    public Dao<Tag2Author, Integer> getT2aDao() {
        if (t2aDao == null){
            try {
                t2aDao=getDao(Tag2Author.class);
            } catch (SQLException e) {
                Log.e(DEBUG_TAG, "Tag2Author DAO Error", e);
            }
        }
        return t2aDao;
    }


    private void upgradeSchema3To4(SQLiteDatabase db) {
        db.execSQL(SQLController.DB_CREATE_TAGS);
        db.execSQL(SQLController.DB_CREATE_TAG_TO_AUTHOR);
        db.execSQL(SQLController.DB_CREATE_STATE);

        db.execSQL(SQLController.DB_IDX3);
        db.execSQL(SQLController.DB_IDX4);
        db.execSQL(SQLController.DB_ALTER_BOOK1);
    }
    @Override
    public void close() {
        super.close();
    }
}
