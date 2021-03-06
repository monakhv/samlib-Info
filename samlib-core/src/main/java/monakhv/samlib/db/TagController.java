package monakhv.samlib.db;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

import com.j256.ormlite.support.DatabaseResults;
import monakhv.samlib.db.entity.Tag;
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
 * 30.06.15.
 */
public class TagController implements AbstractController<Tag> {
    private static final String DEBUG_TAG="TagController";
    private final Dao<Tag, Integer> tagDao;
    private final Tag2AuthorController t2aCtl;

    TagController(DaoBuilder sql){
        t2aCtl=new Tag2AuthorController(sql);
        tagDao = sql.getTagDao();
    }
    @Override
    public int update(Tag tag) {
        try {
            return tagDao.update(tag);
        } catch (SQLException e) {
            Log.e(DEBUG_TAG,"update Error",e);
            return 0;
        }

    }

    @Override
    public long insert(Tag tag) {
        if (getByName(tag.getName()) != null){
            return 0;//ignore Duplicate by name objects
        }
        try {
            return tagDao.create(tag);
        } catch (SQLException e) {
            Log.e(DEBUG_TAG,"insert Error",e);
            return 0;
        }
    }

    @Override
    public int delete(Tag tag) {
        //Delete Tag2Author
        t2aCtl.deleteByTag(tag);
        //Delete Tag
        int ires;
        try {
            ires = tagDao.delete(tag);
        } catch (SQLException e) {
            Log.e(DEBUG_TAG,"Delete Error!",e);
            return 0;
        }
        return ires;
    }

    @Override
    public List<Tag> getAll() {
        QueryBuilder<Tag,Integer> statement = tagDao.queryBuilder();
        statement.orderBy(SQLController.COL_TAG_NAME, true);
        try {
            return tagDao.query(statement.prepare());
        } catch (SQLException e) {
            Log.e(DEBUG_TAG, "All tags selection error", e);
            return null;
        }
    }
public DatabaseResults getRowResult(){
    QueryBuilder<Tag,Integer> statement = tagDao.queryBuilder();
    statement.orderBy(SQLController.COL_TAG_NAME, true);

    try {
        PreparedQuery<Tag> prepare = statement.prepare();
        CloseableIterator iterator = tagDao.iterator(prepare);
        return iterator.getRawResults();
    }catch (SQLException e) {
        Log.e(DEBUG_TAG,"getRowResult: error");
        return null;
    }

}
    @Override
    public Tag getById(long id) {
        Integer dd = (int) id;
        try {
            return tagDao.queryForId(dd);
        } catch (SQLException e) {
            Log.e(DEBUG_TAG,"getById - Error",e);
            return null;
        }
    }



    /**
     * Find tag by name
     * @param name Name of the TAG
     * @return tag  or null if no tag found
     */
    public Tag getByName(String name){

        String ucs = name.toUpperCase();
        QueryBuilder<Tag,Integer> statement = tagDao.queryBuilder();
        List<Tag> rr;

        try {
            statement.where().eq(SQLController.COL_TAG_UCNAME,ucs);
            rr = tagDao.query(statement.prepare());
        } catch (SQLException e) {
            Log.e(DEBUG_TAG,"Get by name Error!",e);
            return null;
        }


        if (rr.size() != 1){
            Log.e(DEBUG_TAG,"Get by name NOT Unique");
            return null;
        }

        return rr.get(0);


    }
}
