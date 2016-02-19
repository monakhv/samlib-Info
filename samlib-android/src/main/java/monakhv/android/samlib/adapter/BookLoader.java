package monakhv.android.samlib.adapter;

import android.content.Context;
import android.util.Log;
import monakhv.android.samlib.R;
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
@SuppressWarnings("FieldCanBeLocal")
public class BookLoader extends AbstractLoader<GroupListItem> {
    private final String DEBUG_TAG = "BookLoader";

    private final AuthorController mAuthorController;
    private final long id;
    private final String order;
    private final Context mContext;
    private int maxGroupId = -1;

    public BookLoader(final Context context, final AuthorController authorController, long id, String order) {
        super(context);
        mContext = context;
        this.id = id;
        this.order = order;
        mAuthorController = authorController;
    }

    @Override
    public List<GroupListItem> loadInBackground() {

        List<GroupListItem> res = new ArrayList<>();
        GroupListItem gr = new GroupListItem();

        if (id == SamLibConfig.SELECTED_BOOK_ID) {//load selected book

            gr.mChildItemList = mAuthorController.getBookController().getSelected(order);
            gr.setName(null);
            res.add(gr);
            return res;
        } else {//load book for the author
            Author a = mAuthorController.getById(id);

            if (a == null) {//no author found display empty screen
                Log.e(DEBUG_TAG, "loadInBackground: author is not defined");
                return GroupListItem.EMPTY;
            }
//            GroupListItem newGrp = new GroupListItem(mContext.getString(R.string.group_book_new));
//            newGrp.mChildItemList=mAuthorController.getBookController().getAllNew(a, order);
//            newGrp.newNumber=newGrp.mChildItemList.size();
//            res.add(newGrp);

            List<GroupBook> rr = mAuthorController.getGroupBookController().getByAuthor(a);

            if (rr.isEmpty()) {//No groups found group all books into single group

                gr.mChildItemList = mAuthorController.getBookController().getAll(a, order);
                gr.setName(mContext.getString(R.string.group_book_all));
                gr.newNumber = mAuthorController.getBookController().getAllNew(a, order).size();
                res.add(gr);
            } else {
                for (GroupBook groupBook : rr) {
                    mAuthorController.getBookController().getBookForGroup(groupBook, order);
                    GroupListItem grr = new GroupListItem(groupBook);
                    res.add(grr);
                    if (groupBook.getId() > maxGroupId) {
                        maxGroupId = groupBook.getId();
                    }
                }
            }
            return res;
        }
    }

    public int getMaxGroupId() {
        return maxGroupId;
    }
}
