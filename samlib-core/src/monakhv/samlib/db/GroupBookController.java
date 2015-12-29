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

import java.util.ArrayList;
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

    public void operate(List<GroupBook> groups) {

        for (GroupBook groupBook : groups){
            Log.i(DEBUG_TAG,"Group: >"+groupBook.getName()+"< Operation: "+groupBook.getSqlOperation().name());
            switch (groupBook.getSqlOperation()) {
                case DELETE:
                    List<GroupBook> gg = new ArrayList<>();
                    gg.add(groupBook);
                    delete(gg);
                    break;
                case UPDATE:
                    break;
                case INSERT:
                    insert(groupBook);
                    break;
                case NONE:
                    break;
            }

        }
    }

    private int insert(GroupBook groupBook) {
        int res = -1;
        try {
            res = dao.create(groupBook);
        } catch (SQLException e) {
            Log.e(DEBUG_TAG,"insert: error insert ",e);
        }
        return res;
    }

    public void delete(List<GroupBook> groupBooks) {
        try {
            dao.delete(groupBooks);
        } catch (SQLException e) {
            Log.e(DEBUG_TAG,"delete: delete error",e);
        }

    }

    /**
     *  get List of Group for given Author
     * @param author Author object
     * @return List of Group
     */
    public List<GroupBook> getByAuthor(Author author){
        QueryBuilder<GroupBook,Integer> qb=dao.queryBuilder();
        qb.orderBy(SQLController.COL_GROUP_IS_HIDDEN,true);
        try {
            qb.where().eq(SQLController.COL_BOOK_AUTHOR_ID,author);
            return dao.query(qb.prepare());
        } catch (SQLException e) {
            Log.e(DEBUG_TAG,"getByAuthor error ",e);
            return null;
        }

    }

    public  GroupBook getByAuthorAndName(Author author, String name){

        QueryBuilder<GroupBook,Integer> qb=dao.queryBuilder();
        List<GroupBook> res;
        try {
            qb.where().eq(SQLController.COL_BOOK_AUTHOR_ID,author)
            .and().eq(SQLController.COL_NAME,name);
            res = dao.query(qb.prepare());
        } catch (SQLException e) {
            Log.e(DEBUG_TAG,"getByAuthorAndName: query error",e);
            return null;
        }

        if (res.size() != 1){
            Log.e(DEBUG_TAG,"getByAuthorAndName: result number error "+res.size()+"name >"+name+"<");
            return null;
        }
        return res.get(0);

    }
}
