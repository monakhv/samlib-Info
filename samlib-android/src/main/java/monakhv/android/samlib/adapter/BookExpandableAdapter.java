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
 * <p/>
 * Created by monakhv on 28.12.15.
 */
public class BookExpandableAdapter extends ExpandableRecyclerAdapter<GroupViewHolder, BookViewHolder> {

    public interface CallBack {
        void makeNewFlip(int id);
    }

    private static final String DEBUG_TAG = "BookExpandableAdapter";
    public static final int NOT_SELECTED = -1;
    private int selected = NOT_SELECTED;


    private final LayoutInflater mInflater;
    private final SettingsHelper mSettingsHelper;
    private long author_id;
    private Context mContext;
    protected CallBack mCallBack;

    public BookExpandableAdapter(@NonNull List<? extends ParentListItem> parentItemList, Activity context, CallBack callBack, SettingsHelper settingsHelper) {
        super(parentItemList);

        mInflater = LayoutInflater.from(context);
        mCallBack = callBack;
        mContext = context;
        mSettingsHelper = settingsHelper;
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



        if (gi.getName() == null || gi.getChildItemList().isEmpty()) {
            Log.i(DEBUG_TAG, "onBindParentViewHolder: empty for group " + gi.getName() + " size " + gi.getChildItemList().size());
            groupViewHolder.groupTitle.setVisibility(View.GONE);
            groupViewHolder.icon.setVisibility(View.GONE);
            groupViewHolder.bookNumber.setVisibility(View.GONE);
            groupViewHolder.rowLayout.setVisibility(View.GONE);
            groupViewHolder.rowLayout.setPadding(0, 0, 0, 0);

        } else {
            groupViewHolder.groupTitle.setText(gi.getName());
            if (gi.newNumber == 0) {
                groupViewHolder.groupTitle.setTypeface(Typeface.DEFAULT);
                groupViewHolder.bookNumber.setText(mContext.getString(R.string.group_book_number) + " " + gi.getChildItemList().size());
                groupViewHolder.newIcon.setImageDrawable(groupViewHolder.oldGroupImage);
                groupViewHolder.newIcon.setTag(0);
                        //.setData(groupViewHolder.oldGroupImage, groupViewHolder.newGroupImage, groupViewHolder.listener, false);
            } else {
                groupViewHolder.groupTitle.setTypeface(Typeface.DEFAULT_BOLD);
                groupViewHolder.newIcon.setImageDrawable(groupViewHolder.newGroupImage);
                groupViewHolder.newIcon.setTag(1);
                        //.setData(groupViewHolder.newGroupImage, groupViewHolder.oldGroupImage, groupViewHolder.listener, false);
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


    }


    @Override
    public BookViewHolder onCreateChildViewHolder(ViewGroup viewGroup) {
        View v = mInflater.inflate(R.layout.book_row_anim, viewGroup, false);
        return new BookViewHolder(v,this);

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

        } else {
            holder.bookTitle.setTypeface(Typeface.DEFAULT);
            holder.flipIcon.setImageDrawable(holder.closeBook);
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

    public void updateData(Book book, GroupBook group) {
        int parentListItemCount = getParentItemList().size();
        Log.i(DEBUG_TAG, "updateData: parent list size:  " + parentListItemCount);
        ParentListItem parentListItem;
        for (int i = 0; i < parentListItemCount; i++) {
            parentListItem = getParentItemList().get(i);
            GroupListItem gi = (GroupListItem) parentListItem;
            if (gi.getId() == group.getId()){
                gi.load(group);
                int idx = gi.getChildItemList().indexOf(book);
                if (idx != -1) {
                    gi.getChildItemList().set(idx, book);
                    notifyParentItemChanged(i);
                    Log.d(DEBUG_TAG, "updateData: update parent: " + i + "  update child: " + idx + " -- " + getParentWrapperIndex(i));
                    return;
                }
            }

        }
        Log.w(DEBUG_TAG, "updateData: No book found to update!");

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

    public void makeRead(int position) {
        Book book = getBook(position);
        if (book != null) {
            mCallBack.makeNewFlip(book.getId());
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
