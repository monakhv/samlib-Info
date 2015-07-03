package monakhv.samlib.db;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import monakhv.samlib.db.entity.Author;
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
}
