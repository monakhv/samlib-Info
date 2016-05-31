package monakhv.samlib.desk.sql;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import monakhv.samlib.db.DaoBuilder;
import monakhv.samlib.db.SQLController;
import monakhv.samlib.db.entity.*;
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
 * 10.06.15.
 */
public class DaoController implements DaoBuilder{
    private static DaoController instance;

    private Dao<Author, Integer>    authorDao;
	private Dao<Book, Integer>      bookDao;
    private Dao<Tag, Integer>        tagDao;
    private Dao<Tag2Author, Integer>        t2aDao;
    private Dao<SelectedBook, Integer> selectedBookDao;
    private Dao<GroupBook, Integer> groupBookDao;

    private DaoController(SQLController sql)  {

        JdbcConnectionSource jdbc ;
        try {
            jdbc = new JdbcConnectionSource(sql.getDbUrl());
            authorDao   = DaoManager.createDao(jdbc,Author.class);
            bookDao     = DaoManager.createDao(jdbc,Book.class);

            tagDao   = DaoManager.createDao(jdbc,Tag.class);
            t2aDao     = DaoManager.createDao(jdbc,Tag2Author.class);

            selectedBookDao   = DaoManager.createDao(jdbc,SelectedBook.class);
            groupBookDao     = DaoManager.createDao(jdbc,GroupBook.class);

        } catch (SQLException e) {
            Log.e("DAOController:","SQL Error",e);
        }


    }

    public Dao<Author, Integer> getAuthorDao() {
        return authorDao;
    }

    public Dao<Book, Integer> getBookDao() {
        return bookDao;
    }

    public Dao<Tag, Integer> getTagDao() {
        return tagDao;
    }

    public Dao<Tag2Author, Integer> getT2aDao() {
        return t2aDao;
    }

    @Override
    public Dao<SelectedBook, Integer> getSelectedBookDao() {
        return selectedBookDao;
    }

    @Override
    public Dao<GroupBook, Integer> getGroupBookDao() {
        return groupBookDao;
    }

    public static DaoController getInstance(SQLController sql)  {
        if (instance == null){
            instance = new DaoController(sql);
        }
        return instance;
    }
}
