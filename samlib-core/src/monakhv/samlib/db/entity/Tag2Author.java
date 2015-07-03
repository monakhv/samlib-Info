package monakhv.samlib.db.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import monakhv.samlib.db.SQLController;

import java.io.Serializable;

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
 * 09.06.15.
 */
@DatabaseTable( tableName = SQLController.TABLE_T2A)
public class Tag2Author implements Serializable {
    @DatabaseField(columnName = SQLController.COL_ID, generatedId = true)
    private int id;
    @DatabaseField(foreign = true,columnName = SQLController.COL_T2A_AUTHORID,canBeNull = false)
    private Author author;
    @DatabaseField(foreign = true,columnName = SQLController.COL_T2A_TAGID, canBeNull = false)
    private Tag tag;


    public Tag2Author(){

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }
}
