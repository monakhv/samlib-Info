package monakhv.samlib.desk.sql;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import monakhv.samlib.db.AbstractController;
import monakhv.samlib.db.SQLController;
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

    public TagController(SQLController sql){
        DaoController daoCtl = DaoController.getInstance(sql);
        tagDao = daoCtl.getTagDao();
    }
    @Override
    public int update(Tag tag) {
        return 0;
    }

    @Override
    public long insert(Tag tag) {
        return 0;
    }

    @Override
    public int delete(Tag tag) {
        return 0;
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

    @Override
    public Tag getById(long id) {
        return null;
    }
}
