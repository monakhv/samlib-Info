package monakhv.android.samlib.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;


import monakhv.android.samlib.R;
import monakhv.android.samlib.animation.Flip3D;
import monakhv.android.samlib.animation.FlipIcon;
import monakhv.android.samlib.service.AuthorEditorServiceIntent;
import monakhv.android.samlib.sql.AuthorController;

import monakhv.samlib.db.SQLController;
import monakhv.samlib.db.entity.Author;


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
public class AuthorCursorAdapter extends RecyclerCursorAdapter<AuthorCursorAdapter.ViewHolder>  {
    public static final String DATE_FORMAT = "dd.MM.yyyy HH:mm:ss";
    private static final String DEBUG_TAG = "AuthorCursorAdapter";
    private static long YEAR = 31556952000L;
    private AuthorController sql;
    private Context context;
    private HashMap<Integer, FlipIcon> flips;
    private SimpleDateFormat df;
    private Calendar now;




    public AuthorCursorAdapter(Context context,Cursor cursor) {
        super(context,cursor);
        this.context=context;
        sql= new AuthorController(context);
        flips = new HashMap<>();

        setName(DEBUG_TAG);
        df = new SimpleDateFormat(DATE_FORMAT);
         now = Calendar.getInstance();
    }


    @Override
    public void onBindViewHolderCursor(ViewHolder holder, Cursor cursor) {
        int idx_isNew = cursor.getColumnIndex(SQLController.COL_isnew);
        int idx_name = cursor.getColumnIndex(SQLController.COL_NAME);
        int idx_mtime = cursor.getColumnIndex(SQLController.COL_mtime);
        int idx_tags = cursor.getColumnIndex(SQLController.COL_TGNAMES);
        int idx_url = cursor.getColumnIndex(SQLController.COL_URL);
        final int id_author=cursor.getInt(cursor.getColumnIndex(SQLController.COL_ID));


        boolean isNew = cursor.getInt(idx_isNew) == 1;
        holder.authorName.setText(cursor.getString(idx_name));
        holder.authorURL.setText(cursor.getString(idx_url));

        long dd = cursor.getLong(idx_mtime);
        Date update = new Date(dd);
        holder.updatedData.setText(df.format(update));


        int oldBookResource;
        if (            (now.getTimeInMillis() -dd) < YEAR) {
            oldBookResource=(R.drawable.author_old);
        }
        else {
            oldBookResource=(R.drawable.author_very_old);
        }
        Flip3D.animationFlip3DListener listener;
        if (isNew) {
            holder.authorName.setTypeface(Typeface.DEFAULT_BOLD);
            listener = new Flip3D.animationFlip3DListener() {
                @Override
                public void onStart() {

                }

                @Override
                public void onEnd() {
                    Log.i(DEBUG_TAG, "Making Author read!");
                    AuthorEditorServiceIntent.markAuthorRead(context,id_author);
                    cleanSelection();
                }
            };
            holder.flipIcon.setData(R.drawable.author_new,oldBookResource,listener,false);
        } else {
            holder.authorName.setTypeface(Typeface.DEFAULT);
            listener = new  Flip3D.animationFlip3DListener() {
                @Override
                public void onStart() {

                }

                @Override
                public void onEnd() {
                    Log.i(DEBUG_TAG, "Making Author new!!");
//                    sql.getBookController().markUnRead(book);
//                    Author a = sql.getByBook(book);
//                    sql.testMarkRead(a);
                }
            };
            holder.flipIcon.setData(oldBookResource,R.drawable.author_new,listener,false);
        }
        flips.put(cursor.getPosition(), holder.flipIcon);
        String tags = cursor.getString(idx_tags);
        if (tags != null) {
            holder.tgnames.setText(tags.replaceAll(",", ", "));
        } else {
            holder.tgnames.setText("");
        }





        holder.itemView.setActivated(cursor.getPosition() == getSelectedPosition());

//        holder.itemView.setClickable(true);
//        holder.itemView.setFocusable(true);
//        holder.itemView.setFocusableInTouchMode(true);

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.author_row_anim, viewGroup, false);
        return new ViewHolder(v);
    }



    public static class ViewHolder extends RecyclerView.ViewHolder  {
        //{R.id.authorName, R.id.updated, R.id.icon, R.id.tgnames, R.id.authorURL};
        public TextView authorName, updatedData, tgnames, authorURL;
        public FlipIcon flipIcon;


        public ViewHolder(View itemView) {
            super(itemView);

            authorName = (TextView) itemView.findViewById(R.id.authorName);
            updatedData = (TextView) itemView.findViewById(R.id.updated);
            tgnames = (TextView) itemView.findViewById(R.id.tgnames);
            authorURL = (TextView) itemView.findViewById(R.id.authorURL);

            flipIcon= (FlipIcon) itemView.findViewById(R.id.FlipIcon);

        }

    }

    public Author getSelected(){
        int pos = getSelectedPosition();
        if (pos == NOT_SELECTED){
            return null;
        }
        return sql.getById(getItemId(pos));
    }

    public void update(Author author){
        sql.update(author);
    }
    public void makeSelectedRead() {
        Author author = getSelected();
        if (author == null) {
            return;
        }
        if (author.isIsNew()) {
            FlipIcon ff = flips.get(getSelectedPosition());
            if (ff != null) {
                ff.makeFlip();
            } else {
                AuthorEditorServiceIntent.markAuthorRead(context,author.getId());
                cleanSelection();
            }
        }


    }
}
