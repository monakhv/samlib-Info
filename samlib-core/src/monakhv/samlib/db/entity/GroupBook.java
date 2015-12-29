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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Entity to Store Book group
 * Created by monakhv on 24.12.15.
 */
@DatabaseTable(tableName = SQLController.TABLE_GROUP_BOOK)
public class GroupBook  implements Serializable{
    private static final Pattern PATTERN_HIDDEN = Pattern.compile("^@(.*)");
    @DatabaseField(columnName = SQLController.COL_ID, generatedId = true)
    protected int id;
    @DatabaseField(columnName = SQLController.COL_GROUP_AUTHOR_ID,foreign = true,canBeNull = false)
    private Author author;
    @DatabaseField(columnName = SQLController.COL_GROUP_NAME)
    protected String name;
    @DatabaseField(columnName = SQLController.COL_GROUP_DISPLAY_NAME)
    protected String displayName;
    @DatabaseField(columnName = SQLController.COL_GROUP_isnew)
    protected boolean isNew = false;
    @DatabaseField(columnName = SQLController.COL_GROUP_IS_HIDDEN)
    protected boolean hidden = false;
    SqlOperation mSqlOperation;


    public GroupBook(){
        mSqlOperation=SqlOperation.DELETE;
        isNew=false;
    }

    public GroupBook(Author author, String name){
        this();
        mSqlOperation=SqlOperation.INSERT;
        this.author=author;
        this.name=name;
        Matcher m = PATTERN_HIDDEN.matcher(name);
        if (m.find()){
            hidden=true;
            displayName=m.group(1);
        }
        else {
            displayName=name;
        }

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isHidden() {
        return hidden;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GroupBook groupBook = (GroupBook) o;

        return name != null ? name.equals(groupBook.name) : groupBook.name == null;

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    public SqlOperation getSqlOperation() {
        return mSqlOperation;
    }


}
