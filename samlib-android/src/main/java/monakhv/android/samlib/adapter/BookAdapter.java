package monakhv.android.samlib.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import monakhv.android.samlib.R;
import monakhv.android.samlib.animation.Flip3D;
import monakhv.android.samlib.animation.FlipIcon;
import monakhv.android.samlib.data.SettingsHelper;
import monakhv.samlib.db.entity.Book;
import monakhv.samlib.db.entity.SamLibConfig;

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
public class BookAdapter   extends RecyclerAdapter<Book,BookAdapter.ViewHolder> {
    private static final String DEBUG_TAG="BookAdapter";
    private final SettingsHelper settingsHelper;

    private long author_id;
    private final HashMap<Integer,FlipIcon> flips;

    public BookAdapter(Context mContext,RecyclerAdapter.CallBack callBack ) {
        super(callBack);


        settingsHelper = new SettingsHelper( mContext);
        flips = new HashMap<>();

    }


    public void setAuthor_id(long author_id) {
        this.author_id = author_id;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.book_row_anim, viewGroup, false);
        return new ViewHolder(v);
    }



    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        final Book book = mData.get(position);
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

        holder.bookSize.setText(book.getSize() + "K");
        holder.bookForm.setText(book.getForm());

        final  int openBook = (R.drawable.open);
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
                    mCallBack.refresh();
                }
            };
            holder.flipIcon.setData(openBook,closeBook,listener,true);

        } else {
            listener = new Flip3D.animationFlip3DListener() {
                @Override
                public void onStart() {
                    Log.i(DEBUG_TAG, "Making book new!!");
                    mCallBack.makeNewFlip(book.getId());
                }

                @Override
                public void onEnd() {
                    mCallBack.refresh();
                }
            };
            holder.flipIcon.setData(closeBook,openBook,listener,true);

        }
        holder.itemView.setActivated(position == getSelectedPosition());


        if (book.isSelected()) {
            holder.starIcon.setImageResource(settingsHelper.getSelectedIcon());
            holder.starIcon.setVisibility(View.VISIBLE);
        } else {
            holder.starIcon.setImageResource(R.drawable.rating_not_important);
            holder.starIcon.setVisibility(View.GONE);
        }

        if (book.isPreserve()){
            holder.lockIcon.setImageResource(settingsHelper.getLockIcon());
            holder.lockIcon.setVisibility(View.VISIBLE);
        }
        else {
            holder.lockIcon.setImageResource(R.drawable.rating_not_important);
            holder.lockIcon.setVisibility(View.GONE);
        }


        flips.put(position, holder.flipIcon);
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
                mCallBack.makeNewFlip(book.getId());

            }
        }

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


}
