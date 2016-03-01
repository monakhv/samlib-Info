package monakhv.samlib.db;

import com.j256.ormlite.dao.Dao;
import monakhv.samlib.db.entity.*;

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
public interface DaoBuilder {
    Dao<Author, Integer> getAuthorDao();
    Dao<Book, Integer> getBookDao();
    Dao<Tag, Integer> getTagDao();
    Dao<Tag2Author, Integer> getT2aDao();
    Dao<SelectedBook, Integer> getSelectedBookDao();
    Dao<GroupBook, Integer> getGroupBookDao();
}
