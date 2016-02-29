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

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.bignerdranch.expandablerecyclerview.Adapter.ExpandableRecyclerAdapter;
import com.bignerdranch.expandablerecyclerview.Model.ParentListItem;
import com.bignerdranch.expandablerecyclerview.Model.ParentWrapper;
import monakhv.android.samlib.R;
import monakhv.android.samlib.data.SettingsHelper;
import monakhv.samlib.db.entity.Book;
import monakhv.samlib.db.entity.GroupBook;
import monakhv.samlib.db.entity.SamLibConfig;
import monakhv.samlib.log.Log;


import java.util.List;

/**
 * Base on this
 * https://github.com/bignerdranch/expandable-recycler-view
 * https://www.bignerdranch.com/blog/expand-a-recyclerview-in-four-steps/
 * <p>
 * Created by monakhv on 28.12.15.
 */
public class BookExpandableAdapter extends ExpandableRecyclerAdapter<GroupViewHolder, BookViewHolder> {

    public interface CallBack {
        void makeBookNewFlip(Book book);

        void makeGroupNewFlip(GroupBook groupBook);
    }

    private static final String DEBUG_TAG = "BookExpandableAdapter";
    public static final int NOT_SELECTED = -1;
    private int selected = NOT_SELECTED;
    public static final int TYPE_PARENT = 0;
    private static final int TYPE_CHILD = 1;


    private final LayoutInflater mInflater;
    private final SettingsHelper mSettingsHelper;
    private final int maxGroupId;
    private long author_id;
    private Context mContext;
    protected CallBack mCallBack;

    public BookExpandableAdapter(@NonNull List<? extends ParentListItem> parentItemList, int maxGroupId, Activity context, CallBack callBack, SettingsHelper settingsHelper) {
        super(parentItemList);
        this.maxGroupId = maxGroupId;
        mInflater = LayoutInflater.from(context);
        mCallBack = callBack;
        mContext = context;
        mSettingsHelper = settingsHelper;
        setHasStableIds(true);
    }

    public void setAuthor_id(long author_id) {
        this.author_id = author_id;
    }


    @Override
    public GroupViewHolder onCreateParentViewHolder(ViewGroup viewGroup) {
        View v = mInflater.inflate(R.layout.group_row, viewGroup, false);
        return new GroupViewHolder(v, this);
    }

    @Override
    public void onBindParentViewHolder(GroupViewHolder groupViewHolder, int position, ParentListItem parentListItem) {
        GroupListItem gi = (GroupListItem) parentListItem;
        groupViewHolder.groupTitle.setText(gi.getName());

        if (gi.getId() == -1 && author_id != SamLibConfig.SELECTED_BOOK_ID) {
            groupViewHolder.groupTitle.setText(mContext.getString(R.string.group_book_all));
        }


        if (gi.newNumber == 0) {
            groupViewHolder.groupTitle.setTypeface(Typeface.DEFAULT);
            groupViewHolder.bookNumber.setText(mContext.getString(R.string.group_book_number) + " " + gi.getChildItemList().size());
            groupViewHolder.newIcon.setImageDrawable(groupViewHolder.oldGroupImage);
            groupViewHolder.newIcon.setTag(0);

        } else {
            groupViewHolder.groupTitle.setTypeface(Typeface.DEFAULT_BOLD);
            groupViewHolder.newIcon.setImageDrawable(groupViewHolder.newGroupImage);
            groupViewHolder.newIcon.setTag(1);

            groupViewHolder.bookNumber.setText(mContext.getString(R.string.group_book_number) + " " + gi.getChildItemList().size()
                    + " "
                    + mContext.getString(R.string.group_book_number_new)
                    + " "
                    + gi.newNumber);
        }


        if (gi.hidden) {
            groupViewHolder.groupTitle.setAlpha(0.5f);
        } else {
            groupViewHolder.groupTitle.setAlpha(1.f);
        }

    }


    @Override
    public BookViewHolder onCreateChildViewHolder(ViewGroup viewGroup) {
        View v = mInflater.inflate(R.layout.book_row_anim, viewGroup, false);
        return new BookViewHolder(v, this);

    }

    public void flipCollapse(int position) {
        ParentWrapper parentWrapper = (ParentWrapper) mItemList.get(position);

        if (parentWrapper.isExpanded()) {
            collapseParent(parentWrapper.getParentListItem());
        } else {
            expandParent(parentWrapper.getParentListItem());
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBindChildViewHolder(BookViewHolder holder, final int position, Object o) {

        final Book book = (Book) o;
        holder.bookTitle.setText(Html.fromHtml(book.getTitle()));

        try {
            holder.bookDesc.setText(Html.fromHtml(book.getDescription()));
        } catch (Exception ex) {//This is because of old book scheme where Description could be null
            holder.bookDesc.setText("");
        }


        holder.bookAuthorName.setText(book.getAuthorName());
        if (author_id != SamLibConfig.SELECTED_BOOK_ID) {
            holder.bookAuthorName.setVisibility(View.GONE);

        } else {
            holder.bookAuthorName.setVisibility(View.VISIBLE);
        }

        if (book.isIsNew() && book.getDelta() != 0) {
            long delta = book.getDelta();
            String str;
            if (delta < 0) {
                str = "" + book.getSize() + "K (" + delta + "K)";
            } else {
                str = "" + book.getSize() + "K (+" + delta + "K)";
            }

            holder.bookSize.setText(str);
        } else {
            holder.bookSize.setText(book.getSize() + "K");
        }

        holder.bookForm.setText(book.getForm());
        if (book.isIsNew()) {
            holder.bookTitle.setTypeface(Typeface.DEFAULT_BOLD);
            holder.flipIcon.setImageDrawable(holder.openBook);
            holder.flipIcon.setTag(1);

        } else {
            holder.bookTitle.setTypeface(Typeface.DEFAULT);
            holder.flipIcon.setImageDrawable(holder.closeBook);
            holder.flipIcon.setTag(0);
        }
        holder.itemView.setActivated(position == getSelectedPosition());


        if (book.isSelected()) {
            holder.starIcon.setImageResource(mSettingsHelper.getSelectedIcon());
            holder.starIcon.setVisibility(View.VISIBLE);
        } else {
            holder.starIcon.setImageResource(R.drawable.rating_not_important);
            holder.starIcon.setVisibility(View.GONE);
        }

        if (book.isPreserve()) {
            holder.lockIcon.setImageResource(mSettingsHelper.getLockIcon());
            holder.lockIcon.setVisibility(View.VISIBLE);
        } else {
            holder.lockIcon.setImageResource(R.drawable.rating_not_important);
            holder.lockIcon.setVisibility(View.GONE);
        }

    }

    @Override
    public long getItemId(int position) {
        int iType = getItemViewType(position);
        Object o = mItemList.get(position);
        if (iType == TYPE_PARENT) {
            ParentWrapper pw = (ParentWrapper) o;
            ParentListItem parentListItem = pw.getParentListItem();
            GroupListItem gi = (GroupListItem) parentListItem;
            return gi.getId();
        }
        if (iType == TYPE_CHILD) {
            Book book = (Book) o;
            return maxGroupId + book.getId();
        }
        throw new IllegalStateException("getItemId: Incorrect ViewType found");
    }

    /**
     * update whole Book group
     * @param groupListItem parentItem
     * @param sort sort order currently not used
     */
    public void updateData(GroupListItem groupListItem, int sort) {

        //TODO: group Move is not implemented yet
        if (groupListItem.getId() <0) {//Null Group
            Log.d(DEBUG_TAG, "updateData1: change parent: 0!");
            ParentListItem parentListItem = getParentItemList().get(0);
            GroupListItem gi = (GroupListItem) parentListItem;
            gi.newNumber =groupListItem.newNumber;
            gi.setChildItemList(groupListItem.getChildItemList());
            notifyParentItemChanged(0);
        } else {
            int parentListItemCount = getParentItemList().size();
            for (int i = 0; i < parentListItemCount; i++) {
                ParentListItem parentListItem = getParentItemList().get(i);
                GroupListItem gi = (GroupListItem) parentListItem;

                if (gi.getId() == groupListItem.getId()) {
                    gi.setChildItemList(groupListItem.getChildItemList());
                    gi.newNumber = groupListItem.newNumber;
                    Log.d(DEBUG_TAG, "updateData1: change parent: " + i);
                    notifyParentItemChanged(i);
                    return;
                }
            }

        }


    }


    /**
     * Update date using new Book and group Objects
     *
     * @param book  Book
     * @param group Group
     * @param sort  new position of Book in the group
     */
    public void updateData(Book book, GroupBook group, int sort) {
        int parentListItemCount = getParentItemList().size();
        Log.d(DEBUG_TAG, "updateData2: parent list size:  " + parentListItemCount);
        ParentListItem parentListItem;

        int iParent=group.getId();


        for (int i = 0; i < parentListItemCount; i++) {//begin parent item cycle
            parentListItem = getParentItemList().get(i);
            GroupListItem gi = (GroupListItem) parentListItem;
            if (gi.getId() == iParent) {//parent found if


                gi.newNumber=group.getNewNumber();
                gi.setGroupBook(group);
                int idx = gi.getChildItemList().indexOf(book);
                Log.d(DEBUG_TAG, "updateData2: parent item found at " +i+  " id: " + iParent+"  book index: "+idx+"   child list size: "+gi.getChildItemList().size());
                if (idx != -1) {
                    if (sort == -1) {
                        gi.getChildItemList().remove(idx);
                        notifyChildItemRemoved(i,idx);
                        //notifyParentItemChanged(i);
                    } else {
                        gi.getChildItemList().remove(idx);
                        gi.getChildItemList().add(sort, book);

                        notifyChildItemMoved(i, idx, sort);
                        notifyParentItemChanged(i);
                    }
                    Log.d(DEBUG_TAG, "updateData2: update parent: " + i + "  update child: " + idx + " -- " + getParentWrapperIndex(i));
                    return;
                }
                else {
                    for (Book b : gi.getChildItemList()){
                        Log.d(DEBUG_TAG,"updateData2: "+b.getId()+" - "+b.getUri());
                    }
                }
            }//parent found if

        }//end parent item cycle
        Log.w(DEBUG_TAG, "updateData2: No book found to update!");

    }


    /**
     * Change selection position
     * make notification by default
     *
     * @param position new selected item position
     */
    public void toggleSelection(int position) {
        toggleSelection(position, true);
    }

    /**
     * Change selected element position
     *
     * @param position new selection position
     * @param notified whether make change item notification or not
     */
    private void toggleSelection(int position, boolean notified) {
        if (position > 0 && getItemViewType(position) == 0) {
            return;//ignore for parent type
        }
        if (position == selected) {
            return;//selection is not changed - ignore it
        }

        int old_selection = selected;//preserve old selection position
        selected = position;//new position

        if (old_selection != NOT_SELECTED && notified) {
            notifyItemChanged(old_selection);//clean up old selection
        }
        if (selected != NOT_SELECTED && notified) {
            notifyItemChanged(selected);//make new selection
        }
    }

    public void cleanSelection() {
        toggleSelection(NOT_SELECTED);
    }


    public int getSelectedPosition() {
        return selected;
    }

    public Book getSelected() {
        int pos = getSelectedPosition();
        Log.d(DEBUG_TAG, "getSelected: position = " + pos);
        if (pos == NOT_SELECTED) {
            Log.e(DEBUG_TAG, "getSelected: position is NOT_SELECTED");
            return null;
        }
        if (mItemList == null) {
            Log.e(DEBUG_TAG, "getSelected: itemList is null");
            return null;
        }

        Object o = mItemList.get(pos);
        if (o instanceof Book) {
            return (Book) o;
        } else {
            return null;
        }

    }

    /**
     * Return the book for current position
     *
     * @param position the position for Book
     * @return book or NULL if the position is not for Book object
     */
    public Book getBook(int position) {
        if (position < 0) {
            return null;
        }
        Object o = mItemList.get(position);

        if (!(o instanceof Book)) {
            Log.e(DEBUG_TAG, "getBook wrong object type");
            return null;
        }
        return (Book) o;
    }

    /**
     * Make flip read of the Book  or nothing if the position is not For Book
     *
     * @param position the position for Book
     */
    public void makeRead(int position) {
        Book book = getBook(position);
        if (book != null) {
            mCallBack.makeBookNewFlip(book);
        }
    }


    public void makeAllRead(int position) {
        Object o = mItemList.get(position);

        if (o instanceof Book) {
            Book book = (Book) o;
            if (!book.isIsNew()) {
                return;
            }
            mCallBack.makeBookNewFlip(book);
        }
        if (o instanceof ParentWrapper) {
            Log.d(DEBUG_TAG, "makeAllRead: parent wrapper");
            final ParentWrapper parentWrapper = (ParentWrapper) o;
            final ParentListItem parentListItem = parentWrapper.getParentListItem();
            final GroupListItem groupListItem = (GroupListItem) parentListItem;
            if (groupListItem.newNumber == 0) {
                Log.d(DEBUG_TAG, "makeAllRead: nothing to clean exiting");
                return;
            }
            Log.d(DEBUG_TAG, "makeAllRead: call clean group: " + groupListItem.getName());
            mCallBack.makeGroupNewFlip(groupListItem.getGroupBook());
        }
    }

    private int getParentWrapperIndex(int parentIndex) {
        int parentCount = 0;
        int listItemCount = mItemList.size();
        for (int i = 0; i < listItemCount; i++) {
            if (mItemList.get(i) instanceof ParentWrapper) {
                parentCount++;

                if (parentCount > parentIndex) {
                    return i;
                }
            }
        }

        return -1;
    }


}
