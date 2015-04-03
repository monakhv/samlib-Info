package monakhv.android.samlib.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.HashMap;


import monakhv.android.samlib.R;
import monakhv.android.samlib.animation.Flip3D;

import monakhv.android.samlib.animation.FlipIcon;
import monakhv.android.samlib.data.SettingsHelper;
import monakhv.android.samlib.service.AuthorEditorServiceIntent;
import monakhv.android.samlib.sql.AuthorController;

import monakhv.samlib.db.SQLController;
import monakhv.samlib.db.entity.Book;
import monakhv.samlib.db.entity.SamLibConfig;

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
 * 12/4/14.
 */
public class BookCursorAdapter extends RecyclerCursorAdapter<BookCursorAdapter.ViewHolder> {

    private static final String DEBUG_TAG = "BookCursorAdapter";
    private long author_id;
    private Context context;
    private SettingsHelper settingsHelper;
    private AuthorController sql;
    private HashMap<Integer,FlipIcon> flips;

    public BookCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        this.context = context;
        settingsHelper = new SettingsHelper(context);
        sql = new AuthorController(context);
        flips=new HashMap<>();

        setName(DEBUG_TAG);

    }

    public void setAuthor_id(long author_id) {
        this.author_id = author_id;
    }

    @Override
    public void onBindViewHolderCursor(ViewHolder holder, final Cursor cursor) {
        int idx_form = cursor.getColumnIndex(SQLController.COL_BOOK_FORM);
//        int idx_mtime = cursor.getColumnIndex(SQLController.COL_BOOK_MTIME);
//        int idx_date = cursor.getColumnIndex(SQLController.COL_BOOK_DATE);
        int idx_desc = cursor.getColumnIndex(SQLController.COL_BOOK_DESCRIPTION);
        int idx_size = cursor.getColumnIndex(SQLController.COL_BOOK_SIZE);
        int idx_title = cursor.getColumnIndex(SQLController.COL_BOOK_TITLE);
        int idx_isNew = cursor.getColumnIndex(SQLController.COL_BOOK_ISNEW);
        int idx_group_id = cursor.getColumnIndex(SQLController.COL_BOOK_GROUP_ID);
        int idx_author = cursor.getColumnIndex(SQLController.COL_BOOK_AUTHOR);
        final int book_id = cursor.getInt(cursor.getColumnIndex(SQLController.COL_ID));
        int book_options = cursor.getInt(cursor.getColumnIndex(SQLController.COL_BOOK_OPT));


        holder.bookTitle.setText(Html.fromHtml(cursor.getString(idx_title)));

        try {
            holder.bookDesc.setText(Html.fromHtml(cursor.getString(idx_desc)));
        } catch (Exception ex) {//This is because of old book scheme where Description could be null
            holder.bookDesc.setText("");
        }


        holder.bookAuthorName.setText(cursor.getString(idx_author));
        if (author_id != SamLibConfig.SELECTED_BOOK_ID) {
            holder.bookAuthorName.setVisibility(View.GONE);

        } else {
            holder.bookAuthorName.setVisibility(View.VISIBLE);
        }

        holder.bookSize.setText(cursor.getString(idx_size) + "K");
        holder.bookForm.setText(cursor.getString(idx_form));

        final  int openBook = (R.drawable.open);
        final int closeBook = (R.drawable.closed);
        Flip3D.animationEndListener listener;


        if (cursor.getInt(idx_isNew)==1) {
            listener = new Flip3D.animationEndListener() {
                @Override
                public void onEnd() {
                    Log.i(DEBUG_TAG, "Making book read!");
                    AuthorEditorServiceIntent.markBookReadFlip(context,book_id);
                }
            };
            holder.flipIcon.setData(openBook,closeBook,listener,true);

        } else {
            listener = new Flip3D.animationEndListener() {
                @Override
                public void onEnd() {
                    Log.i(DEBUG_TAG, "Making book new!!");
                    AuthorEditorServiceIntent.markBookReadFlip(context,book_id);
                }
            };
            holder.flipIcon.setData(closeBook,openBook,listener,true);

        }
        holder.itemView.setActivated(cursor.getPosition() == getSelectedPosition());


        if (cursor.getInt(idx_group_id) == 1) {
            holder.starIcon.setImageResource(settingsHelper.getSelectedIcon());
            holder.starIcon.setVisibility(View.VISIBLE);
        } else {
            holder.starIcon.setImageResource(R.drawable.rating_not_important);
            holder.starIcon.setVisibility(View.GONE);
        }

        if (Book.isPreserved(book_options)){
            holder.lockIcon.setImageResource(settingsHelper.getLockIcon());
            holder.lockIcon.setVisibility(View.VISIBLE);
        }
        else {
            holder.lockIcon.setImageResource(R.drawable.rating_not_important);
            holder.lockIcon.setVisibility(View.GONE);
        }


        flips.put(cursor.getPosition(), holder.flipIcon);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.book_row_anim, viewGroup, false);
        return new ViewHolder(v);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // {R.id.bookTitle, R.id.bookUpdate, R.id.bookDesc, R.id.Bookicon,R.id.Staricon,R.id.bookAuthorName,R.id.bookForm};
        public TextView bookTitle, bookSize, bookDesc, bookAuthorName, bookForm;
        public ImageView starIcon, lockIcon;
        public FlipIcon flipIcon;


        public ViewHolder(View itemView) {
            super(itemView);
            bookTitle = (TextView) itemView.findViewById(R.id.bookTitle);
            bookSize = (TextView) itemView.findViewById(R.id.bookUpdate);
            bookDesc = (TextView) itemView.findViewById(R.id.bookDesc);
            bookAuthorName = (TextView) itemView.findViewById(R.id.bookAuthorName);
            bookForm = (TextView) itemView.findViewById(R.id.bookForm);

            flipIcon= (FlipIcon) itemView.findViewById(R.id.FlipIcon);
            starIcon = (ImageView) itemView.findViewById(R.id.Staricon);
            lockIcon = (ImageView) itemView.findViewById(R.id.Lockicon);
        }
    }

    public Book getSelected() {
        int pos = getSelectedPosition();
        if (pos == NOT_SELECTED) {
            return null;
        }
        return sql.getBookController().getById(getItemId(pos));
    }

    /**
     * Mark selected book as read
     * @param animation if true make icon animation
     */
    public void makeSelectedRead(boolean animation) {
        Book book = getSelected();
        if (book == null) {
            Log.e(DEBUG_TAG,"Book is null");
            return;
        }
        if (book.isIsNew()) {

            if ( animation) {
                flips.get(getSelectedPosition()).makeFlip();
                Log.i(DEBUG_TAG,"Making book flip animation at position: "+getSelectedPosition());

            } else {
                sql.getBookController().markRead(book);
                sql.testMarkRead(sql.getByBook(book));
            }
        }
        cleanSelection();

    }

    public void update(Book book) {
        sql.getBookController().update(book);
    }

}
