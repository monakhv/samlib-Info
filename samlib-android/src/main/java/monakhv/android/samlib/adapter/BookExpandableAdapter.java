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
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.bignerdranch.expandablerecyclerview.Adapter.ExpandableRecyclerAdapter;
import com.bignerdranch.expandablerecyclerview.Model.ParentListItem;
import com.bignerdranch.expandablerecyclerview.Model.ParentWrapper;
import monakhv.android.samlib.R;
import monakhv.android.samlib.animation.Flip3D;
import monakhv.android.samlib.data.SettingsHelper;
import monakhv.samlib.db.entity.Book;
import monakhv.samlib.db.entity.SamLibConfig;
import monakhv.samlib.log.Log;

import java.util.ArrayList;
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

        Book reloadBook(int id);
    }

    private static final String DEBUG_TAG = "BookExpandableAdapter";
    public static final int NOT_SELECTED = -1;
    private int selected = NOT_SELECTED;


    private final LayoutInflater mInflater;
    private final SettingsHelper mSettingsHelper;
    private long author_id;
    private Context mContext;
    protected CallBack mCallBack;
    private boolean mAnimationRunning = false;
    private List<RecyclerView> mRecyclerViews;

    public BookExpandableAdapter(@NonNull List<? extends ParentListItem> parentItemList, Activity context, CallBack callBack, SettingsHelper settingsHelper) {
        super(parentItemList);

        mInflater = LayoutInflater.from(context);
        mCallBack = callBack;
        mContext = context;
        mSettingsHelper = settingsHelper;
        mRecyclerViews = new ArrayList<>();
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
    public void onBindParentViewHolder(GroupViewHolder groupViewHolder, final int position, ParentListItem parentListItem) {
        GroupListItem gi = (GroupListItem) parentListItem;
        groupViewHolder.groupTitle.setText(gi.getName());
        groupViewHolder.position = position;


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
                groupViewHolder.newIcon.setData(groupViewHolder.oldGroupImage, groupViewHolder.newGroupImage, groupViewHolder.listener, false);
            } else {
                groupViewHolder.groupTitle.setTypeface(Typeface.DEFAULT_BOLD);
                groupViewHolder.newIcon.setData(groupViewHolder.newGroupImage, groupViewHolder.oldGroupImage, groupViewHolder.listener, false);
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
        return new BookViewHolder(v);

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


        Flip3D.animationFlip3DListener listener;


        if (book.isIsNew()) {
            holder.bookTitle.setTypeface(Typeface.DEFAULT_BOLD);
            listener = new Flip3D.animationFlip3DListener() {
                @Override
                public void onStart() {
                    Log.i(DEBUG_TAG, "Making book read: " + book.getUri());
                    mAnimationRunning = true;
                    mCallBack.makeNewFlip(book.getId());
                }

                @Override
                public boolean canStart() {
                    return !mAnimationRunning;
                }

                @Override
                public void onEnd() {
                    mAnimationRunning = false;
                    makeCleanNew(book);
                }
            };
            holder.flipIcon.setData(holder.openBook, holder.closeBook, listener, true);

        } else {
            holder.bookTitle.setTypeface(Typeface.DEFAULT);
            listener = new Flip3D.animationFlip3DListener() {
                @Override
                public void onStart() {
                    Log.i(DEBUG_TAG, "Making book new: " + book.getUri());
                    mCallBack.makeNewFlip(book.getId());
                    mAnimationRunning = true;
                }

                @Override
                public boolean canStart() {
                    return !mAnimationRunning;
                }

                @Override
                public void onEnd() {
                    mAnimationRunning = false;
                    makeSetNew(book);

                }
            };

            holder.flipIcon.setData(holder.closeBook, holder.openBook, listener, true);

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

    /**
     * make update book List view
     *
     * @param book  book was changed
     * @param isNew true - set "new" mark or false to clean "new" mark
     */
    private void updateBook(Book book, boolean isNew) {

        int parentListItemCount = getParentItemList().size();
        Log.i(DEBUG_TAG, "updateBook: parent list size:  " + parentListItemCount);
        ParentListItem parentListItem;
        for (int i = 0; i < parentListItemCount; i++) {
            parentListItem = getParentItemList().get(i);
            GroupListItem gi = (GroupListItem) parentListItem;

            int idx = gi.getChildItemList().indexOf(book);
            if (idx != -1) {
                if (isNew) {
                    ++gi.newNumber;
                } else {
                    if (gi.newNumber > 0) {
                        --gi.newNumber;
                    }
                }


                Log.d(DEBUG_TAG, "updateBook: update parent: " + i + "  update child: " + idx + " -- " + getParentWrapperIndex(i));
                gi.getChildItemList().set(idx, book);
                //notifyChildItemChanged(i, idx);

                RecyclerView.ViewHolder viewHolder = mRecyclerViews.get(0).findViewHolderForAdapterPosition(getParentWrapperIndex(i));
                final GroupViewHolder groupViewHolder;
                if (viewHolder instanceof GroupViewHolder) {
                    groupViewHolder = (GroupViewHolder) viewHolder;
                    groupViewHolder.position = i;
                } else {
                    groupViewHolder = null;
                    Log.e(DEBUG_TAG, "updateBook: group holder is wrong");
                }

                if ((groupViewHolder != null) && (
                        (isNew && gi.newNumber == 1) || (!isNew && gi.newNumber == 0)
                )
                        ) {
                    groupViewHolder.newIcon.makeFlip();
                    Log.d(DEBUG_TAG, "updateBook: parent animation: " + isNew + "  " + i + "  " + gi.newNumber);
                    return;
                }

                Log.d(DEBUG_TAG, "updateBook: parent NO animation: " + isNew + "  " + i + "  " + gi.newNumber);

                notifyParentItemChanged(i);


                //
                return;

            }

        }//end parent item cycle

        Log.w(DEBUG_TAG, "updateBook: No book found to update!");
    }

    /**
     * Clean New mark for book and its group
     *
     * @param b Book to clean new mark
     */
    private void makeCleanNew(Book b) {
        Book book = mCallBack.reloadBook(b.getId());

        updateBook(book, false);
    }

    /**
     * Set new mark for book and its group
     *
     * @param b Book object to set new mark
     */
    private void makeSetNew(Book b) {
        Book book = mCallBack.reloadBook(b.getId());


        updateBook(book, true);

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

        if (mAnimationRunning) {
            return;//not start new animation when the previous not finished yet.
        }
        if (getBook(position) != null) {
            RecyclerView.ViewHolder viewHolder=mRecyclerViews.get(0).findViewHolderForAdapterPosition(position);
            if (viewHolder instanceof BookViewHolder){
                BookViewHolder bookViewHolder= (BookViewHolder) viewHolder;
                bookViewHolder.flipIcon.makeFlip();
            }
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerViews.add(recyclerView);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mRecyclerViews.remove(recyclerView);
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
