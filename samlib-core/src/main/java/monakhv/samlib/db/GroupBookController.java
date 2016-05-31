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
import monakhv.samlib.db.entity.Book;
import monakhv.samlib.db.entity.GroupBook;
import monakhv.samlib.db.entity.SamLibConfig;
import monakhv.samlib.log.Log;

import java.sql.SQLException;

import java.util.List;

/**
 * Class to deal with GroupBook entity class
 * <p>
 * Created by Dmitry Monakhov on 24.12.15.
 */
public class GroupBookController {
    private static final String DEBUG_TAG = "GroupBookController";
    private Dao<GroupBook, Integer> mGroupDao;

    GroupBookController(DaoBuilder sql) {
        mGroupDao = sql.getGroupBookDao();
    }

    void operate(Author author) {

        for (GroupBook groupBook : author.getGroupBooks()) {
            //Log.i(DEBUG_TAG, "Group: >" + groupBook.getName() + "< Operation: " + groupBook.getSqlOperation().name());
            switch (groupBook.getSqlOperation()) {
                case DELETE:
                    delete(groupBook);
                    break;
                case UPDATE:
                    groupBook.setAuthor(author);
                    update(groupBook);
                    break;
                case INSERT:
                    groupBook.setAuthor(author);
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
            res = mGroupDao.create(groupBook);
        } catch (SQLException e) {
            Log.e(DEBUG_TAG, "insert: error insert ", e);
        }
        return res;
    }

    private int delete(GroupBook groupBook) {
        try {
            return mGroupDao.delete(groupBook);
        } catch (SQLException e) {
            Log.e(DEBUG_TAG, "delete: delete error", e);
            return -1;
        }

    }

    int update(GroupBook groupBook) {
        try {
            return mGroupDao.update(groupBook);
        } catch (SQLException e) {
            Log.e(DEBUG_TAG, "update: update error", e);

            return -1;
        }
    }

    /**
     * Get virtual group which contains all books of the Author
     *
     * @param author the Author
     * @return GroupBook object
     */
    public GroupBook getAllGroup(Author author) {
        GroupBook groupBook = new GroupBook();
        groupBook.setAuthor(author);
        groupBook.setId(SamLibConfig.GROUP_ID_ALL);
        return groupBook;
    }

    public GroupBook getByBook(Book book) {
        GroupBook groupBook;
        if (book.getGroupBook() == null) {
            return getAllGroup(book.getAuthor());
        }
        try {
            groupBook = mGroupDao.queryForId(book.getGroupBook().getId());
        } catch (SQLException e) {
            Log.e(DEBUG_TAG, "getByBook: not found uri: " + book.getUri(), e);
            return null;
        }
        if (groupBook == null) {
            return getAllGroup(book.getAuthor());
        }
        return groupBook;
    }

    public GroupBook getById(long id) {
        Integer dd = (int) id;
        GroupBook a;
        try {
            a = mGroupDao.queryForId(dd);
        } catch (SQLException e) {
            Log.e(DEBUG_TAG, "getById - Error", e);
            return null;
        }

        return a;
    }


    /**
     * get List of Group for given Author
     *
     * @param author Author object
     * @return List of Group
     */
    public List<GroupBook> getByAuthor(Author author) {
        QueryBuilder<GroupBook, Integer> qb = mGroupDao.queryBuilder();
        qb.orderBy(SQLController.COL_GROUP_NEW_NUMBER, false);
        qb.orderBy(SQLController.COL_GROUP_IS_HIDDEN, true);
        try {
            qb.where().eq(SQLController.COL_BOOK_AUTHOR_ID, author);
            return mGroupDao.query(qb.prepare());
        } catch (SQLException e) {
            Log.e(DEBUG_TAG, "getByAuthor error ", e);
            return null;
        }

    }

    /**
     * get List of Group for given Author where there are new books
     *
     * @param author Author object
     * @return List of Group
     */
    public List<GroupBook> getByAuthorNew(Author author) {
        QueryBuilder<GroupBook, Integer> qb = mGroupDao.queryBuilder();
        qb.orderBy(SQLController.COL_GROUP_NEW_NUMBER, false);
        qb.orderBy(SQLController.COL_GROUP_IS_HIDDEN, true);
        try {
            qb.where()
                    .eq(SQLController.COL_BOOK_AUTHOR_ID, author)
                    .and()
                    .gt(SQLController.COL_GROUP_NEW_NUMBER, 0);


            return mGroupDao.query(qb.prepare());
        } catch (SQLException e) {
            Log.e(DEBUG_TAG, "getByAuthor error ", e);
            return null;
        }

    }

    GroupBook getByAuthorAndName(Author author, String name) {

        QueryBuilder<GroupBook, Integer> qb = mGroupDao.queryBuilder();
        List<GroupBook> res;
        try {
            qb.where().eq(SQLController.COL_BOOK_AUTHOR_ID, author)
                    .and().eq(SQLController.COL_NAME, name);
            res = mGroupDao.query(qb.prepare());
        } catch (SQLException e) {
            Log.e(DEBUG_TAG, "getByAuthorAndName: query error", e);
            return null;
        }

        if (res.size() != 1) {
            Log.e(DEBUG_TAG, "getByAuthorAndName: result number error " + res.size() + "  name >" + name + "<");
            return null;
        }
        return res.get(0);

    }


}
