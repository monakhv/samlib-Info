package monakhv.android.samlib.adapter;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;



/*
 * Copyright (C) 2014 skyfish.jy@gmail.com
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
 *
 * https://gist.github.com/skyfishjy/443b7448f59be978bc59
 *
 */

public abstract class RecyclerCursorAdapter<VH
        extends android.support.v7.widget.RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    private static final String DEBUG_TAG="RecyclerCursorAdapter";

    private Context mContext;
    private Cursor mCursor;
    private boolean mDataValid;
    private int mRowIdColumn;
    private DataSetObserver mDataSetObserver;
    private ContentObserver mChangeObserver;

    private String name;//for testing

    public RecyclerCursorAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
        mDataValid = cursor != null;
        mRowIdColumn = mDataValid ? mCursor.getColumnIndex("_id") : -1;
        mDataSetObserver = new NotifyingDataSetObserver();
        mChangeObserver = new ChangeObserver();

        if (mCursor != null) {
            mCursor.registerDataSetObserver(mDataSetObserver);
            mCursor.registerContentObserver(mChangeObserver);
        }
    }

    public Cursor getCursor() {
        return mCursor;
    }

    @Override
    public int getItemCount() {
        if (mDataValid && mCursor != null) {
            return mCursor.getCount();
        }
        return 0;
    }

    @Override
    public long getItemId(int position) {
        if (mDataValid && mCursor != null && mCursor.moveToPosition(position)) {
            return mCursor.getLong(mRowIdColumn);
        }
        return 0;
    }

    /**
     * Find item with given id and select it
     *
     * @param id
     * @return true if found
     */
    public boolean findAndSelect(long id){
        for(int i=0; i<getItemCount(); i++){
              if (getItemId(i) == id){
                  toggleSelection(i);
                  return true;
              }
         }
        return false;
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(true);
    }

    public abstract void onBindViewHolderCursor(VH holder, Cursor cursor);

    @Override
    public void onBindViewHolder(VH holder, int i) {
        if (!mDataValid) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }
        if (!mCursor.moveToPosition(i)) {
            throw new IllegalStateException("couldn't move cursor to position " + i);
        }
        onBindViewHolderCursor(holder, mCursor);

    }

    /**
     * Change the underlying cursor to a new cursor. If there is an existing cursor it will be
     * closed.
     *
     * @param cursor The new cursor to be used
     */
    public void changeCursor(Cursor cursor) {
        Cursor old = swapCursor(cursor);
        if (old != null) {
            old.close();
        }
    }

    /**
     * Swap in a new Cursor, returning the old Cursor.  Unlike
     *
     * @param newCursor
     * @return
     */
    private Cursor swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return null;
        }
        Cursor oldCursor = mCursor;
        if (oldCursor != null && mDataSetObserver != null) {
            oldCursor.unregisterDataSetObserver(mDataSetObserver);
            oldCursor.unregisterContentObserver(mChangeObserver);
        }
        mCursor = newCursor;
        if (mCursor != null) {
            if (mDataSetObserver != null){
                mCursor.registerDataSetObserver(mDataSetObserver);
            }
            if (mChangeObserver!= null){
                mCursor.registerContentObserver(mChangeObserver);
            }
            mRowIdColumn = newCursor.getColumnIndexOrThrow("_id");
            mDataValid = true;
            notifyDataSetChanged();
        } else {
            mRowIdColumn = -1;
            mDataValid = false;
            notifyDataSetChanged();
            //There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
        }


        return oldCursor;
    }


    protected void onContentChanged() {
        if ( mCursor != null && !mCursor.isClosed()) {
            Log.d(DEBUG_TAG,"onContentChanged: reQuery");
            mDataValid = mCursor.requery();
        }
    }

    private class NotifyingDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            super.onChanged();
            mDataValid = true;
            notifyDataSetChanged();
            Log.d(DEBUG_TAG, "NotifyingDataSetObserver: onChanged "+name);
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            mDataValid = false;
            notifyDataSetChanged();
            //There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
            Log.d(DEBUG_TAG, "NotifyingDataSetObserver: onInvalidated  "+name);
        }
    }


    private class ChangeObserver extends ContentObserver {

        public ChangeObserver() {
            super(new Handler());
        }

        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            Log.d(DEBUG_TAG, "ChangeObserver: onChange - " + name + " -- " + selfChange);
            onContentChanged();
        }

    }
    public static final int NOT_SELECTED=-1;
    private int selected = NOT_SELECTED;

    /**
     * Change selection position
     * make notification by default
     * @param position new selected item position
     */
    public void toggleSelection(int position){
        toggleSelection(position,true);
    }

    /**
     * Change selected element position
     *
     * @param position new selection position
     * @param notified whether make change item notification or not
     */
    public void toggleSelection(int position,boolean notified){
        if (position == selected){
            return;//selection is not changed - ignore it
        }

        int old_selection = selected;//preserve old selection position
        selected = position;//new position

        if (old_selection!= NOT_SELECTED&&notified){
            notifyItemChanged(old_selection);//clean up old selection
        }
        if (selected != NOT_SELECTED&&notified){
            notifyItemChanged(selected);//make new selection
        }
    }

    public void cleanSelection(){
        toggleSelection(NOT_SELECTED);
    }


    public int getSelectedPosition() {
        return selected;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * This method must be call onDestroy handler
     * To unregister Observers and close cursor
     */
    public void clear(){
        changeCursor(null);
    }
}
