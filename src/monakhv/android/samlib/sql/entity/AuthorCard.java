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

package monakhv.android.samlib.sql.entity;

import java.io.Serializable;


/**
 * @author Dmitry Monakhov
 */
public class AuthorCard implements Serializable{
    private static final int ACARD_LINK         = 0;
    private static final int ACARD_NAME       = 1;
    private static final int ACARD_TITLE       = 2;
    private static final int ACARD_SIZE         = 4;
    private static final int ACARD_COUNT    = 7;        
    private static final int ACARD_DESCRIP  = 8;
    private int id;
    private String url;
    private String name;
    private String title;
    private String description;
    private int size;
    private int count;//number of book
    
    public AuthorCard() {
        
    }
    public AuthorCard(String string2parse){
        String str = string2parse+" |";
     
        String[] strs = str.split(SamLibConfig.SPLIT);
        
        url      = SamLibConfig.SLASH+strs[ACARD_LINK];
        name = strs[ACARD_NAME];
        title    = strs[ACARD_TITLE];
        
        description = strs[ACARD_DESCRIP];
        
        
        size    = toInt(strs[ACARD_SIZE]);
        count = toInt(strs[ACARD_COUNT]);
        
       
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (this.url != null ? this.url.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AuthorCard other = (AuthorCard) obj;
        return !((this.url == null) ? (other.url != null) : !this.url.equals(other.url));
    }
    

   

    private int toInt(String string) {
        int res;
        try {
            res = Integer.valueOf(string);
        }
        catch (NumberFormatException ex) {
            res = 0;
        }        
        return res;        
    }
    
    
    
    
    
}
