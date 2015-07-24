package monakhv.android.samlib.adapter;

import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import monakhv.android.samlib.R;
import monakhv.android.samlib.animation.Flip3D;
import monakhv.android.samlib.animation.FlipIcon;
import monakhv.samlib.db.entity.Author;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

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
public class AuthorAdapter extends RecyclerAdapter<Author, AuthorAdapter.ViewHolder> {
    private static final String DEBUG_TAG = "AuthorAdapter";
    private static long YEAR = 31556952000L;
    public static final String DATE_FORMAT = "dd.MM.yyyy HH:mm:ss";
    private SimpleDateFormat df;
    private Calendar now;


    //private final AuthorController authorController;

    private final HashMap<Integer, FlipIcon> flips;

    public AuthorAdapter(RecyclerAdapter.CallBack callBack) {
        super(callBack);

        flips = new HashMap<>();
        df = new SimpleDateFormat(DATE_FORMAT);
        now = Calendar.getInstance();
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        final Author author = mData.get(position);

        boolean isNew = author.isIsNew();
        holder.authorName.setText(author.getName());
        holder.authorURL.setText(author.getUrl());

        long dd = author.getUpdateDate();
        Date update = new Date(dd);
        holder.updatedData.setText(df.format(update));


        int oldBookResource;
        if ((now.getTimeInMillis() - dd) < YEAR) {
            oldBookResource = (R.drawable.author_old);
        } else {
            oldBookResource = (R.drawable.author_very_old);
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
                    mCallBack.makeNewFlip(author.getId());
                }
            };
            holder.flipIcon.setData(R.drawable.author_new, oldBookResource, listener, false);
        } else {
            holder.authorName.setTypeface(Typeface.DEFAULT);
            listener = new Flip3D.animationFlip3DListener() {
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
            holder.flipIcon.setData(oldBookResource, R.drawable.author_new, listener, false);
        }
        flips.put(position, holder.flipIcon);
        holder.tgnames.setText(author.getAll_tags_name());


        holder.itemView.setActivated(position == getSelectedPosition());


    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.author_row_anim, viewGroup, false);
        return new ViewHolder(v);
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
                mCallBack.makeNewFlip(author.getId());
            }
        }


    }

    /**
     * Find item with given id and select it
     *
     * @param id Author id
     * @return true if found
     */
    public boolean findAndSelect(long id) {
        for (int i = 0; i < getItemCount(); i++) {
            if (mData.get(i).getId() == id) {
                toggleSelection(i);
                notifyItemChanged(i);
                return true;
            }
        }
        return false;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        //{R.id.authorName, R.id.updated, R.id.icon, R.id.tgnames, R.id.authorURL};
        public TextView authorName, updatedData, tgnames, authorURL;
        public FlipIcon flipIcon;


        public ViewHolder(View itemView) {
            super(itemView);

            authorName = (TextView) itemView.findViewById(R.id.authorName);
            updatedData = (TextView) itemView.findViewById(R.id.updated);
            tgnames = (TextView) itemView.findViewById(R.id.tgnames);
            authorURL = (TextView) itemView.findViewById(R.id.authorURL);

            flipIcon = (FlipIcon) itemView.findViewById(R.id.FlipIcon);

        }

    }
}
