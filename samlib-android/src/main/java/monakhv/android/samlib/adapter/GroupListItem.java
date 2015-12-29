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

/**
 * Class to represent GroupBook into UI List
 * Created by monakhv on 28.12.15.
 */
public class GroupListItem implements ParentListItem {
    public static final GroupListItem BLIND=new GroupListItem("");
    public static final List<GroupListItem> EMPTY;

    static {
        EMPTY = new ArrayList<>();
    }
    private String name;
    private GroupBook mGroupBook;
    private boolean  initiallyExpanded;

    List<Book> mChildItemList;

    GroupListItem(GroupBook groupBook){
        mGroupBook=groupBook;
        name=mGroupBook.getName();
        initiallyExpanded=false;
    }

    private GroupListItem(String name){
        this.name=name;
        initiallyExpanded=true;

    }

    public String getName() {
        return name;
    }

    public GroupBook getGroupBook() {
        return mGroupBook;
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
