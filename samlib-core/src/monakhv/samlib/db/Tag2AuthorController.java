package monakhv.samlib.db;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import monakhv.samlib.db.entity.Author;
import monakhv.samlib.db.entity.Tag;
import monakhv.samlib.db.entity.Tag2Author;
import monakhv.samlib.log.Log;

import java.sql.SQLException;
import java.util.ArrayList;
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
 * 03.07.15.
 */
public class Tag2AuthorController {
    private static final String DEBUG_TAG="Tag2AuthorController";

    private final Dao<Tag2Author, Integer> t2aDao;
    Tag2AuthorController(DaoBuilder sql){
        t2aDao=sql.getT2aDao();
    }

    /**
     * Delete All T2A object for the Author
     * @param author Author whose Tag2Author will be deleted
     */
    void deleteByAuthor(Author author){
        DeleteBuilder<Tag2Author,Integer> deleteBuilder = t2aDao.deleteBuilder();
        try {
            deleteBuilder.where().eq(SQLController.COL_T2A_AUTHORID,author);
            deleteBuilder.delete();
        } catch (SQLException e) {
            Log.e(DEBUG_TAG,"DeleteByAuthor Error",e);
        }
    }
    /**
     * Delete All T2A object for the Author
     * @param tag Author whose Tags will be deleted
     */
    void deleteByTag(Tag tag){
        DeleteBuilder<Tag2Author,Integer> deleteBuilder = t2aDao.deleteBuilder();
        try {
            deleteBuilder.where().eq(SQLController.COL_T2A_TAGID,tag);
            deleteBuilder.delete();
        } catch (SQLException e) {
            Log.e(DEBUG_TAG,"DeleteByTag Error",e);
        }
    }
    boolean sync(Author author,List<Tag> tags){
        boolean res = false;
        List<Tag2Author> t2as= queryByAuthor(author);

        if (t2as == null){
            return false;
        }
        List<Tag> tgs = new ArrayList<>();
        for (Tag2Author t2a: t2as){
            if (! tags.contains(t2a.getTag())){//delete t2a
                try {
                    t2aDao.delete(t2a);
                    res = true;
                } catch (SQLException e) {
                    Log.e(DEBUG_TAG,"Error delete t2a",e);
                    return false;
                }
            }
            else {
                tgs.add(t2a.getTag());
            }
        }

        for (Tag tag : tags){
            if (! tgs.contains(tag)){//add new t2a

                Tag2Author t2a = new Tag2Author(author,tag);

                try {
                    t2aDao.create(t2a);
                    res = true;
                } catch (SQLException e) {
                    Log.e(DEBUG_TAG,"Error create t2a",e);
                    return false;
                }
            }
        }

        return res;
    }

    private List<Tag2Author> queryByAuthor(Author author){
        List<Tag2Author> t2as;
        QueryBuilder<Tag2Author,Integer> queryBuilder = t2aDao.queryBuilder();
        try {
            queryBuilder.where().eq(SQLController.COL_T2A_AUTHORID,author);
            t2as=queryBuilder.query();
        } catch (SQLException e) {
            Log.e(DEBUG_TAG,"Query error",e);
            return null;
        }
        return t2as;
    }

}
