package monakhv.samlib.desk.gui;

import monakhv.samlib.db.entity.SamLibConfig;
import monakhv.samlib.db.entity.Tag;

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
 * 30.06.15.
 */

public class TagComboItem implements Serializable{
    public  static final TagComboItem ALL= new TagComboItem(SamLibConfig.TAG_AUTHOR_ALL ,"ALL");
    public  static final TagComboItem NEW= new TagComboItem(SamLibConfig.TAG_AUTHOR_NEW ,"NEW");
    private int id;
    private String tittle;
    private Tag tag=null;

    private TagComboItem(int id, String title){
        this.id=id;
        this.tittle= title;
    }
    public TagComboItem(Tag tag){
        this.tag=tag;
        this.id=tag.getId();
        this.tittle=tag.getName();
    }

    @Override
    public String toString() {
        return tittle;
    }

    public int getId(){
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TagComboItem tagComboItem = (TagComboItem) o;

        return id == tagComboItem.id;

    }

    @Override
    public int hashCode() {
        return id;
    }

    public Tag getTag() {
        return tag;
    }
}
