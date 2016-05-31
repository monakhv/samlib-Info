/*
 * Copyright 2013 Dmitry Monakhov.
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
 */
package monakhv.samlib.db.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import monakhv.samlib.db.SQLController;

import java.io.Serializable;

/**
 *
 * @author monakhv
 */
@DatabaseTable(tableName = SQLController.TABLE_TAGS)
public class Tag  implements Serializable{
    @DatabaseField(columnName = SQLController.COL_ID,generatedId = true)
    private int id;
    @DatabaseField(columnName = SQLController.COL_TAG_NAME, canBeNull = false)
    private String name;
    @DatabaseField(columnName = SQLController.COL_TAG_UCNAME, canBeNull = false)
    private String ucName;

    public Tag(){
        
    }
    public Tag(String name){
        this();
        pSetName(name);
    }
    
    private void pSetName(String name){
        
        String ss = name.replaceAll(",", "");
        this.name = ss;
        this.ucName = ss.toUpperCase();
    }
            
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {        
        pSetName(name);
        
    }

    public String getUcName() {
        return ucName;
    }

    public void setUcName(String ucName) {
        this.ucName = ucName;
    }
    
}
