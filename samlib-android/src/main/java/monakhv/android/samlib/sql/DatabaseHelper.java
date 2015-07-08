package monakhv.android.samlib.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            Log.e(DEBUG_TAG,"Can not create the Schema");
        }


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        getAuthorDao();

        try {
            if (oldVersion == 4 && newVersion == 7) {
                upgradeSchema4To5(db);
                upgradeSchema5To6(db);
                upgradeSchema6To7(db);
            }
            if (oldVersion == 5 && newVersion == 7) {
                upgradeSchema5To6(db);
                upgradeSchema6To7(db);
            }
            if (oldVersion == 6 && newVersion == 7) {
                upgradeSchema6To7(db);
            }

        }catch (SQLException e) {
            Log.e(DEBUG_TAG,"Can not UPGRADE the Schema");
        }


    }

    /**
     * Schema update to version 5
     * Remove samlib URL
     *
     * @param db
     */
    private void upgradeSchema4To5(SQLiteDatabase db) throws SQLException {

        android.util.Log.d("upgradeSchema4To5", "Begin upgrade schema 4->5");
        String[] columns = {SQLController.COL_ID, SQLController.COL_URL};
        QueryBuilder<Author,Integer> qb = authorDao.queryBuilder();
        List<Author> aa = authorDao.query(qb.prepare());
        for (Author a : aa){

            String url = a.getUrl();
            android.util.Log.d("upgradeSchema4To5", "Change url: " + url);
            url = url.replaceAll("http://samlib.ru", "");
            android.util.Log.d("upgradeSchema4To5", "To url: " + url);
            a.setUrl(url);
            authorDao.update(a);

        }


        android.util.Log.d("upgradeSchema4To5", "End upgrade schema 4->5");
    }
    private void upgradeSchema5To6(SQLiteDatabase db) throws SQLException {

        authorDao.executeRawNoArgs(SQLController.ALTER6_1);
    }
    private void upgradeSchema6To7(SQLiteDatabase db) throws SQLException {
        authorDao.executeRawNoArgs(SQLController.ALTER7_1);
        QueryBuilder<Author,Integer> qb = authorDao.queryBuilder();
        List<Author> aa = authorDao.query(qb.prepare());
        for (Author a: aa){
            List<Integer> tagIds = a.getTagIds();
            List<String> tagNames = new ArrayList<>();
            for (Integer tagId : tagIds){
                Tag tag=tagDao.queryForId(tagId);
                if (tag != null){
                    tagNames.add(tag.getName());
                }
            }
            a.setAll_tags_name(tagNames);
            authorDao.update(a);
        }
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



    @Override
    public void close() {
        super.close();
    }
}
