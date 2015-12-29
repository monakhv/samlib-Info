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

package monakhv.android.samlib.adapter;

import com.bignerdranch.expandablerecyclerview.Model.ParentListItem;
import monakhv.samlib.db.entity.Book;
import monakhv.samlib.db.entity.GroupBook;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to represent GroupBook into UI List
 * Created by monakhv on 28.12.15.
 */
public class GroupListItem implements ParentListItem {
    public static final GroupListItem BLIND=new GroupListItem();
    public static final List<GroupListItem> EMPTY;
    private static final Pattern PATTERN_HIDDEN = Pattern.compile("^@.*");

    static {
        EMPTY = new ArrayList<>();
    }
    private String name;
    private GroupBook mGroupBook;
    private boolean  initiallyExpanded;
    //TODO: move into GroupBook to persist to make possible order by it
    private boolean hidden=false;

    List<Book> mChildItemList;

    private GroupListItem(){
        name=null;
        initiallyExpanded=true;
    }

    GroupListItem(GroupBook groupBook){
        mGroupBook=groupBook;
        name=mGroupBook.getName();
        initiallyExpanded=false;
        Matcher m = PATTERN_HIDDEN.matcher(name);
        if (m.find()){
            hidden=true;
            name=name.replaceAll("@","");
        }
    }

    GroupListItem(String name){
        this.name=name;
        initiallyExpanded=true;

    }

    public String getName() {
        return name;
    }

    public GroupBook getGroupBook() {
        return mGroupBook;
    }

    public boolean isHidden() {
        return hidden;
    }

    @Override
    public List<Book> getChildItemList() {
        return mChildItemList;
    }

    @Override
    public boolean isInitiallyExpanded() {
        return initiallyExpanded;
    }


}
