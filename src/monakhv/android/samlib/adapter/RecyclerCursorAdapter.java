package monakhv.android.samlib.adapter;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.ContentObserver;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


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
        }
        mCursor = newCursor;
        if (newCursor != null) {
            registerDataSetObserver(newCursor);
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


    private void registerDataSetObserver(Cursor c) {
        if (mDataSetObserver != null) {
            c.registerDataSetObserver(mDataSetObserver);
        }
    }
    protected void onContentChanged() {
        if ( mCursor != null && !mCursor.isClosed()) {
            mDataValid = mCursor.requery();
        }
    }

    private class NotifyingDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            super.onChanged();
            mDataValid = true;
            notifyDataSetChanged();
            Log.d(DEBUG_TAG, "NotifyingDataSetObserver: onChanged");
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            mDataValid = false;
            notifyDataSetChanged();
            //There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
            Log.d(DEBUG_TAG, "NotifyingDataSetObserver: onInvalidated");
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
            Log.d(DEBUG_TAG, "ChangeObserver: onChange");
            onContentChanged();
        }
    }
    public static final int NOT_SELECTED=-1;
    private int selected = NOT_SELECTED;
    public void toggleSelection(int position){
        List<Integer> poss = new ArrayList<>();
        if (selected != NOT_SELECTED ){
            poss.add(selected);
        }
        selected = position;
        poss.add(selected);
        for(Integer ii : poss){
            notifyItemChanged(ii);
        }
    }

    public int getSelectedPosition() {
        return selected;
    }
}
