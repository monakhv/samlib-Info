package monakhv.samlib.desk.gui;

import monakhv.samlib.db.AuthorController;
import monakhv.samlib.db.DaoBuilder;
import monakhv.samlib.db.TagController;
import monakhv.samlib.db.entity.Author;
import monakhv.samlib.db.entity.Tag;
import monakhv.samlib.db.entity.Tag2Author;
import monakhv.samlib.log.Log;

import javax.swing.*;
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
 * 02.07.15.
 */
public class AuthorTags {
    private static final String DEBUG_TAG="AuthorTags";
    private List<Tag> allTags;

    private TagController tagCtl;
    private DaoBuilder sql;
    public AuthorTags(DaoBuilder sql){
        this.sql = sql;


        tagCtl = new TagController(sql);
        allTags = tagCtl.getAll();


    }

    JPanel getPanel(Author author){
        JPanel panel = new JPanel();

        for (Tag2Author t2a : author.getTag2Authors()){
            int tagId = t2a.getTag().getId();
            Log.i(DEBUG_TAG,"Author: "+author.getName()+" - "+tagCtl.getById(tagId).getName());
        }

        return panel;
    }
}
