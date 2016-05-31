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

package monakhv.samlib.db.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import monakhv.samlib.db.SQLController;

import java.io.Serializable;

/**
 * Entity to store selected book
 * Created by monakhv on 24.12.15.
 */
@DatabaseTable(tableName = SQLController.TABLE_SELECTED_BOOK)
public class SelectedBook  implements Serializable {
    @DatabaseField(columnName = SQLController.COL_ID, generatedId = true)
    protected int id;
    @DatabaseField(columnName = SQLController.COL_BOOK_ID,foreign = true,canBeNull = false,unique = true)
    private Book mBook;

    public SelectedBook(){

    }

    public SelectedBook(Book book){
        mBook=book;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Book getBook() {
        return mBook;
    }

    public void setBook(Book book) {
        mBook = book;
    }
}
