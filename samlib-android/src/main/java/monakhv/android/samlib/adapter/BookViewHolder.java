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

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bignerdranch.expandablerecyclerview.ViewHolder.ChildViewHolder;
import monakhv.android.samlib.R;
import monakhv.android.samlib.animation.FlipIcon;

/**
 * Created by monakhv on 28.12.15.
 */
public class BookViewHolder extends ChildViewHolder {
    // {R.id.bookTitle, R.id.bookUpdate, R.id.bookDesc, R.id.Bookicon,R.id.Staricon,R.id.bookAuthorName,R.id.bookForm};
    public TextView bookTitle, bookSize, bookDesc, bookAuthorName, bookForm;
    public ImageView starIcon, lockIcon;
    public FlipIcon flipIcon;
    public BookViewHolder(View itemView) {
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
