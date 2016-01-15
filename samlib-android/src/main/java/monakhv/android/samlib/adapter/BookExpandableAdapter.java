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

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.bignerdranch.expandablerecyclerview.Adapter.ExpandableRecyclerAdapter;
import com.bignerdranch.expandablerecyclerview.Model.ParentListItem;
import monakhv.android.samlib.R;
import monakhv.android.samlib.animation.Flip3D;
import monakhv.android.samlib.animation.FlipIcon;
import monakhv.android.samlib.data.SettingsHelper;
import monakhv.samlib.db.entity.Book;
import monakhv.samlib.db.entity.SamLibConfig;

import java.util.HashMap;
import java.util.List;

/**
 * Base on this
 * https://www.bignerdranch.com/blog/expand-a-recyclerview-in-four-steps/
 * Created by monakhv on 28.12.15.
 */
public class BookExpandableAdapter extends ExpandableRecyclerAdapter<GroupViewHolder, BookViewHolder> {
    public interface CallBack {
        void makeNewFlip(int id);

        Book reloadBook(int id);
    }

    private static final String DEBUG_TAG = "BookExpandableAdapter";
    public static final int NOT_SELECTED = -1;
    private int selected = NOT_SELECTED;


    private final LayoutInflater mInflater;
    private final SettingsHelper mSettingsHelper;
    private long author_id;
    private final HashMap<Integer, FlipIcon> flips;
    private Context mContext;
    protected CallBack mCallBack;

    public BookExpandableAdapter(@NonNull List<? extends ParentListItem> parentItemList, Context context, CallBack callBack, SettingsHelper settingsHelper) {
        super(parentItemList);

        mInflater = LayoutInflater.from(context);
        flips = new HashMap<>();
        mCallBack = callBack;
        mContext=context;
        mSettingsHelper=settingsHelper;
    }

    public void setAuthor_id(long author_id) {
        this.author_id = author_id;
    }


    @Override
    public GroupViewHolder onCreateParentViewHolder(ViewGroup viewGroup) {
        View v = mInflater.inflate(R.layout.group_row, viewGroup, false);
        return new GroupViewHolder(v);
    }

    @Override
    public void onBindParentViewHolder(GroupViewHolder groupViewHolder, int i, ParentListItem parentListItem) {
        GroupListItem gi = (GroupListItem) parentListItem;
        groupViewHolder.groupTitle.setText(gi.getName());

        if ( gi.getName() == null || gi.getChildItemList().isEmpty()) {
            Log.i(DEBUG_TAG,"onBindParentViewHolder: empty for group "+gi.getName()+" size "+gi.getChildItemList().size());
            groupViewHolder.groupTitle.setVisibility(View.GONE);
            groupViewHolder.icon.setVisibility(View.GONE);
            groupViewHolder.bookNumber.setVisibility(View.GONE);
            groupViewHolder.rowLayout.setVisibility(View.GONE);
            groupViewHolder.rowLayout.setPadding(0,0,0,0);

        }
        else {
            groupViewHolder.groupTitle.setText(gi.getName());
            if (gi.newNumber == 0){
                groupViewHolder.bookNumber.setText(mContext.getString(R.string.group_book_number)+" "+gi.getChildItemList().size());
            }
            else {
                groupViewHolder.bookNumber.setText(mContext.getString(R.string.group_book_number)+" "+gi.getChildItemList().size()
                        +" "
                        +mContext.getString(R.string.group_book_number_new)
                        +" "
                        +gi.newNumber);
            }



            if ( gi.hidden){
                groupViewHolder.groupTitle.setAlpha(0.5f);
            }
            else {
                groupViewHolder.groupTitle.setAlpha(1.f);
            }
        }

    }


    @Override
    public BookViewHolder onCreateChildViewHolder(ViewGroup viewGroup) {
        View v = mInflater.inflate(R.layout.book_row_anim, viewGroup, false);
        return new BookViewHolder(v);

    }


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

        if (book.isIsNew() && book.getDelta() != 0){
            long delta = book.getDelta();
            String str;
            if (delta<0){
                str=""+book.getSize()+"K ("+delta+"K)";
            }
            else {
                str=""+book.getSize()+"K (+"+delta+"K)";
            }

            holder.bookSize.setText(str);
        }
        else {
            holder.bookSize.setText(book.getSize() + "K");
        }

        holder.bookForm.setText(book.getForm());

        final int openBook = (R.drawable.open);
        final int closeBook = (R.drawable.closed);
        Flip3D.animationFlip3DListener listener;


        if (book.isIsNew()) {
            listener = new Flip3D.animationFlip3DListener() {
                @Override
                public void onStart() {
                    Log.i(DEBUG_TAG, "Making book read!");
                    mCallBack.makeNewFlip(book.getId());
                }

                @Override
                public void onEnd() {
                    makeCleanNew(book);
                }
            };
            holder.flipIcon.setData(openBook, closeBook, listener, true);

        } else {
            listener = new Flip3D.animationFlip3DListener() {
                @Override
                public void onStart() {
                    Log.i(DEBUG_TAG, "Making book new!!");
                    mCallBack.makeNewFlip(book.getId());
                }

                @Override
                public void onEnd() {
                   makeSetNew(book);
                }
            };
            holder.flipIcon.setData(closeBook, openBook, listener, true);

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


        flips.put(position, holder.flipIcon);

    }

    private void updateBook(Book book,boolean isNew) {

        int parentListItemCount = getParentItemList().size();
        ParentListItem parentListItem;
        for (int i = 1; i < parentListItemCount; i++) {
            parentListItem = getParentItemList().get(i);
            GroupListItem gi = (GroupListItem) parentListItem;
            if (isNew){
                ++gi.newNumber;
            }
            else {
                if (gi.newNumber>0){
                    --gi.newNumber;
                }

            }

            int idx = gi.getChildItemList().indexOf(book);
            if (idx != -1){

                gi.getChildItemList().set(idx,book);
                notifyChildItemChanged(i,idx);
                notifyParentItemChanged(i);

            }

        }


    }
    private void makeCleanNew( Book b) {
        Book book = mCallBack.reloadBook(b.getId());


        int book_idx = getParentItemList().get(0).getChildItemList().indexOf(book);
        if (book_idx != -1){

            GroupListItem gi = (GroupListItem) getParentItemList().get(0);
            gi.getChildItemList().remove(book_idx);
            gi.newNumber=gi.getChildItemList().size();
            notifyChildItemRemoved(0,book_idx);
            notifyParentItemChanged(0);
        }
        updateBook(book,false);
    }

    private void makeSetNew( Book b) {
        Book book = mCallBack.reloadBook(b.getId());

        GroupListItem gi = (GroupListItem) getParentItemList().get(0);
        gi.getChildItemList().add(book);
        gi.newNumber=gi.getChildItemList().size();
        notifyChildItemInserted(0,gi.getChildItemList().size()-1);
        notifyParentItemChanged(0);


        updateBook(book,true);
        notifyParentItemChanged(0);


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
        if (mItemList == null){
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
        if (position <0){
            return null;
        }
        Object o = mItemList.get(position);

        if ( ! (o instanceof Book)){
            Log.e(DEBUG_TAG,"getBook wrong object type");
            return null;
        }
        return (Book) o;
    }
    public void makeRead(int position) {

        if (getBook(position) != null){
            flips.get(position).makeFlip();
        }
    }
//    /**
//     * Mark selected book as read
//     *
//     * @param animation if true make icon animation
//     */
//    public void makeSelectedRead(boolean animation) {
//        Book book = getSelected();
//        if (book == null) {
//            Log.e(DEBUG_TAG, "makeSelectedRead: Book is null");
//            return;
//        }
//        if (book.isIsNew()) {
//            Log.d(DEBUG_TAG,"makeSelectedRead: selected position = "+getSelectedPosition());
//
//            if (animation) {
//                flips.get(getSelectedPosition()).makeFlip();
//                Log.i(DEBUG_TAG, "makeSelectedRead: Making book flip animation at position: " + getSelectedPosition());
//
//            } else {
//                mCallBack.makeNewFlip(book.getId());
//                notifyItemChanged(getSelectedPosition());
//            }
//        }
//
//    }
}
