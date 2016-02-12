package monakhv.android.samlib.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import monakhv.android.samlib.R;
import monakhv.android.samlib.awesome.FontManager;
import monakhv.android.samlib.awesome.TextLabel;
import monakhv.samlib.db.entity.Author;
import monakhv.samlib.log.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
public class AuthorAdapter extends RecyclerAdapter<Author, AuthorAdapter.AuthorViewHolder> {
    private static final String DEBUG_TAG = "AuthorAdapter";
    //private static long YEAR = 31556952000L;
    public static final String DATE_FORMAT = "dd.MM.yyyy HH:mm:ss";
    private SimpleDateFormat df;
   // private Calendar now;



    public AuthorAdapter(RecyclerAdapter.CallBack callBack) {
        super(callBack);

        setHasStableIds(true);
        df = new SimpleDateFormat(DATE_FORMAT, Locale.FRANCE);
     //   now = Calendar.getInstance();
    }


    @Override
    public void onBindViewHolder(AuthorViewHolder holder, int position) {

        final Author author = mData.get(position);

        boolean isNew = author.isIsNew();
        holder.authorName.setText(author.getName());
        holder.authorURL.setText(author.getUrl());

        long dd = author.getUpdateDate();
        Date update = new Date(dd);
        holder.updatedData.setText(df.format(update));



        if (isNew) {
            holder.authorName.setTypeface(Typeface.DEFAULT_BOLD);
            holder.flipIcon.setImageDrawable(holder.newAuthorImage);
            holder.flipIcon.setTag(1);

        } else {
            holder.authorName.setTypeface(Typeface.DEFAULT);
            holder.flipIcon.setImageDrawable(holder.oldAuthorImage);
            holder.flipIcon.setTag(0);
        }

        holder.tgnames.setText(author.getAll_tags_name());


        holder.itemView.setActivated(position == getSelectedPosition());


    }

    @Override
    public AuthorViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.author_row_anim, viewGroup, false);
        return new AuthorViewHolder(v);
    }


    public void makeSelectedRead() {
        Author author = getSelected();
        if (author == null) {
            return;
        }
        if (author.isIsNew()) {
            mCallBack.makeNewFlip(author);
            toggleSelection(NOT_SELECTED,false);//clean selection
        }


    }

    @Override
    public long getItemId(int position) {
        return mData.get(position).getId();
    }


    public void add(Author author,int sort){
        mData.add(sort,author);
        notifyItemInserted(sort);
    }
    public void remove(int idx){
        mData.remove(idx);
        notifyItemRemoved(idx);
    }

    /**
     *  Change reflection of Item by the data of the Author
     * @param author  Author
     * @param sort position to change
     */
    public void notifyChange(Author author, int sort){

        int idx = mData.indexOf(author);

        if (idx != -1 && sort != -1 && idx !=sort){
            mData.remove(idx);
            mData.add(sort,author);
//            notifyItemRemoved(idx);
//            notifyItemInserted(sort);
            notifyItemMoved(idx,sort);
            Log.d(DEBUG_TAG,"notifyChange: make move: "+idx+" to: "+sort );
            return;
        }

        if (idx != -1){
            mData.set(idx,author);
            notifyItemChanged(idx);
            Log.d(DEBUG_TAG,"notifyChange: in-place update" );
            return;
        }

        Log.e(DEBUG_TAG,"notifyChange: wrong index: "+idx );

    }


    /**
     * Find item with given id and select it
     *
     * @param id Author id
     * @return true if found
     */
    public int findAndSelect(long id) {
        for (int i = 0; i < getItemCount(); i++) {
            if (mData.get(i).getId() == id) {
                toggleSelection(i);
                notifyItemChanged(i);
                return i;
            }
        }
        return NOT_SELECTED;
    }



    public static class AuthorViewHolder extends RecyclerView.ViewHolder {
        //{R.id.authorName, R.id.updated, R.id.icon, R.id.tgnames, R.id.authorURL};
        public TextView authorName, updatedData, tgnames, authorURL;
        public ImageView flipIcon;
        public Drawable oldAuthorImage, newAuthorImage;


        @SuppressWarnings("deprecation")
        public AuthorViewHolder(View itemView) {
            super(itemView);

            authorName = (TextView) itemView.findViewById(R.id.authorName);
            updatedData = (TextView) itemView.findViewById(R.id.updated);
            tgnames = (TextView) itemView.findViewById(R.id.tgnames);
            authorURL = (TextView) itemView.findViewById(R.id.authorURL);

            flipIcon = (ImageView) itemView.findViewById(R.id.FlipIcon);
            final Context context=itemView.getContext();
            newAuthorImage = TextLabel.builder()
                    .beginConfig()
                    .useFont(FontManager.getFontAwesome(itemView.getContext()))
                    .textColor(Color.BLACK)
                    .endConfig()
                    .buildRound(context.getString(R.string.fa_pencil), Color.LTGRAY);

            oldAuthorImage = TextLabel.builder()
                    .beginConfig()
                    .useFont(FontManager.getFontAwesome(itemView.getContext()))
                    .textColor(context.getResources().getColor(R.color.green_dark))
                    .endConfig()
                    .buildRound(context.getString(R.string.fa_user), Color.GRAY);


        }

    }
}
