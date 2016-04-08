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
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bignerdranch.expandablerecyclerview.ViewHolder.ChildViewHolder;
import monakhv.android.samlib.R;
import monakhv.android.samlib.awesome.FontManager;
import monakhv.android.samlib.awesome.TextLabel;


/**
 * Base on this
 * https://www.bignerdranch.com/blog/expand-a-recyclerview-in-four-steps/
 * Created by monakhv on 28.12.15.
 */
public class BookViewHolder extends ChildViewHolder {
    // {R.id.bookTitle, R.id.bookUpdate, R.id.bookDesc, R.id.Bookicon,R.id.Staricon,R.id.bookAuthorName,R.id.bookForm};

    TextView bookTitle, bookSize, bookDesc, bookAuthorName, bookForm,bookMTime;
    ImageView starIcon, lockIcon;
    ImageView flipIcon;
    Drawable openBook, closeBook;
    LinearLayout flipContainer;

    @SuppressWarnings("deprecation")
    public BookViewHolder(View itemView, final BookExpandableAdapter adapter) {
        super(itemView);
        bookTitle = (TextView) itemView.findViewById(R.id.bookTitle);
        bookSize = (TextView) itemView.findViewById(R.id.bookSize);
        bookDesc = (TextView) itemView.findViewById(R.id.bookDesc);
        bookAuthorName = (TextView) itemView.findViewById(R.id.bookAuthorName);
        bookForm = (TextView) itemView.findViewById(R.id.bookForm);
        flipContainer = (LinearLayout) itemView.findViewById(R.id.FlipContainer);
        bookMTime = (TextView) itemView.findViewById(R.id.bookMTime);

        flipIcon = (ImageView) itemView.findViewById(R.id.FlipIcon);
        starIcon = (ImageView) itemView.findViewById(R.id.Staricon);
        lockIcon = (ImageView) itemView.findViewById(R.id.Lockicon);

        final Context context = itemView.getContext();
        openBook = TextLabel.builder()
                .beginConfig()
                .useFont(FontManager.getFontAwesome(context))
                .endConfig()
                .buildRound(context.getString(R.string.fa_file_text), Color.GRAY);
        closeBook = TextLabel.builder()
                .beginConfig()
                .useFont(FontManager.getFontAwesome(context))
                .textColor(context.getResources().getColor(R.color.green_dark))
                .endConfig()
                .buildRound(context.getString(R.string.fa_book), Color.GRAY);

        flipContainer.setOnClickListener(v -> {
//                Log.d("BookViewHolder","Position: "+adapter.getBook(getAdapterPosition()).getTitle());
            adapter.makeRead(getAdapterPosition());
        });


    }
}
