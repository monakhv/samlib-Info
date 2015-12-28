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
import monakhv.samlib.db.AuthorController;
import monakhv.samlib.db.entity.Author;
import monakhv.samlib.db.entity.Book;
import monakhv.samlib.db.entity.GroupBook;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by monakhv on 28.12.15.
 */
public class GroupListItem implements ParentListItem {
    private String name;
    private GroupBook mGroupBook;
    private boolean  initiallyExpanded;

    private List<Book> mChildItemList;

    private GroupListItem(GroupBook groupBook){
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

    public static List<GroupListItem> getGroupList(AuthorController sql, long author_id) {
        Author a = sql.getById(author_id);
        List<GroupListItem> res = new ArrayList<>();
        List<GroupBook> rr = sql.getGroupBookController().getByAuthor(a);

        if (rr == null || rr.isEmpty()){
            GroupListItem gi = new GroupListItem("");
            gi.mChildItemList = sql.getBookController().getBooksByAuthor(a);
            res.add(gi);

        }
        else {
            for (GroupBook groupBook: rr){
                GroupListItem gi = new GroupListItem(groupBook);
                gi.mChildItemList=sql.getBookController().getBookForGroup(a,groupBook);
                res.add(gi);
            }
        }
        return res;


    }

}
