package monakhv.android.samlib.adapter;

import android.content.Context;
import android.util.Log;
import monakhv.android.samlib.sql.DatabaseHelper;
import monakhv.samlib.db.AuthorController;
import monakhv.samlib.db.entity.Author;
import monakhv.samlib.db.entity.GroupBook;
import monakhv.samlib.db.entity.SamLibConfig;

import java.util.ArrayList;
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
 * 23.07.15.
 */
public class BookLoader extends AbstractLoader<GroupListItem> {
    private final String DEBUG_TAG = "BookLoader";

    private final AuthorController authorController;
    private long id;
    private String order;

    public BookLoader(final Context context, final DatabaseHelper databaseHelper, long id, String order) {
        super(context);
        this.id = id;
        this.order = order;
        authorController = new AuthorController(databaseHelper);
    }

    @Override
    public List<GroupListItem> loadInBackground() {

        List<GroupListItem> res = new ArrayList<>();

        if (id == SamLibConfig.SELECTED_BOOK_ID) {
            GroupListItem gr = GroupListItem.BLIND;
            gr.mChildItemList = authorController.getBookController().getSelected(order);
            res.add(gr);
            return res;
        } else {
            Author a = authorController.getById(id);

            if (a == null) {
                Log.e(DEBUG_TAG, "loadInBackground: author is not defined");
                return GroupListItem.EMPTY;
            }
            List<GroupBook> rr = authorController.getGroupBookController().getByAuthor(a);

            if (rr.isEmpty()) {
                GroupListItem gr = GroupListItem.BLIND;
                gr.mChildItemList = authorController.getBookController().getAll(a, order);
                res.add(gr);
            } else {
                for (GroupBook groupBook : rr) {
                    GroupListItem gr = new GroupListItem(groupBook);
                    gr.mChildItemList = authorController.getBookController().getBookForGroup(a, groupBook, order);
                    res.add(gr);
                }
            }
            return res;
        }
    }


}
