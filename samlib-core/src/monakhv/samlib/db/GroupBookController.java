/*
 * Copyright 2015 Dmitry Monakhov.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package monakhv.samlib.db;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import monakhv.samlib.db.entity.Author;
import monakhv.samlib.db.entity.GroupBook;
import monakhv.samlib.log.Log;

import java.sql.SQLException;
import java.util.List;

/**
 * Class to deal with GroupBook entity class
 *
 * Created by monakhv on 24.12.15.
 */
public class GroupBookController {
    private static final String DEBUG_TAG="GroupBookController";
    private Dao<GroupBook,Integer> dao;
    public GroupBookController(DaoBuilder sql){
       dao =sql.getGroupBookDao();
    }

    public List<GroupBook> getByAuthor(Author author){
        QueryBuilder<GroupBook,Integer> qb=dao.queryBuilder();
        try {
            qb.where().eq(SQLController.COL_BOOK_AUTHOR_ID,author);
            return dao.query(qb.prepare());
        } catch (SQLException e) {
            Log.e(DEBUG_TAG,"getByAuthor error ",e);
            return null;
        }

    }
}
