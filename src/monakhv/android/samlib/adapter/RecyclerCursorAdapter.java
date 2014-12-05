package monakhv.android.samlib.adapter;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

/*
 * Copyright 2014  Dmitry Monakhov
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
 * 12/3/14.
 */
public abstract class RecyclerCursorAdapter <VH
        extends android.support.v7.widget.RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    private Cursor mCursor;
    private boolean mDataValid;
    private int mRowIDColumn;

    public RecyclerCursorAdapter(Cursor cursor){
        init(cursor);
    }

    public Cursor getCursor(){
        return mCursor;
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
     *   Swap in a new Cursor, returning the old Cursor.  Unlike
     * @param newCursor
     * @return
     */
    private Cursor swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
			return null;
		}
        Cursor oldCursor = mCursor;
        mCursor = newCursor;
        if (newCursor != null) {
            mRowIDColumn = newCursor.getColumnIndexOrThrow("_id");
			mDataValid = true;
            notifyDataSetChanged();
        }
        else {
            mRowIDColumn = -1;
			mDataValid = false;
            notifyItemRangeRemoved(0, getItemCount());

        }


        return oldCursor;
    }

    private void init(Cursor c) {
        boolean cursorPresent = c != null;
		mCursor = c;
		mDataValid = cursorPresent;
		mRowIDColumn = cursorPresent ? c.getColumnIndexOrThrow("_id") : -1;
    }


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
	 * See {@link android.widget.CursorAdapter#bindView(android.view.View, android.content.Context,
	 * android.database.Cursor)},
	 * {@link #onBindViewHolder(android.support.v7.widget.RecyclerView.ViewHolder, int)}
	 *
	 * @param holder View holder.
	 * @param cursor The cursor from which to get the data. The cursor is already
	 * moved to the correct position.
	 */
	public abstract void onBindViewHolderCursor(VH holder, Cursor cursor);

    @Override
    public int getItemCount() {
        if (mDataValid && mCursor != null) {
            return mCursor.getCount();
        } else {
            return 0;
        }
    }

    @Override
    public long getItemId(int position) {
       if (mDataValid && mCursor != null) {
			if (mCursor.moveToPosition(position)) {
				return mCursor.getLong(mRowIDColumn);
			} else {
				return 0;
			}
		} else {
			return 0;
		}
    }

}
