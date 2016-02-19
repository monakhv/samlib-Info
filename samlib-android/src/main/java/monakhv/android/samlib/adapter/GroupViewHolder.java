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
import android.os.Build;
import android.text.Layout;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bignerdranch.expandablerecyclerview.ViewHolder.ParentViewHolder;
import monakhv.android.samlib.R;
import monakhv.android.samlib.awesome.FontManager;
import monakhv.android.samlib.awesome.TextDrawable;
import monakhv.android.samlib.awesome.TextLabel;

/**
 * Base on this
 * https://www.bignerdranch.com/blog/expand-a-recyclerview-in-four-steps/
 * Created by monakhv on 28.12.15.
 */
public class GroupViewHolder extends ParentViewHolder {
    private final static String DEBUG_TAG = "GroupViewHolder";
    private static final float INITIAL_POSITION = 0.0f;
    private static final float ROTATED_POSITION = 180f;

    TextView groupTitle, bookNumber;
    ImageView icon;
    LinearLayout rowLayout;
    Drawable newGroupImage,oldGroupImage;
    ImageView newIcon;

    @SuppressWarnings("deprecation")
    public GroupViewHolder(View itemView,final BookExpandableAdapter adapter) {
        super(itemView);
        groupTitle = (TextView) itemView.findViewById(R.id.group_title);
        icon = (ImageView) itemView.findViewById(R.id.group_icon);
        bookNumber = (TextView) itemView.findViewById(R.id.group_number);
        rowLayout = (LinearLayout) itemView.findViewById(R.id.group_row);
        newIcon = (ImageView) itemView.findViewById(R.id.FlipIconGroup);

        final Context context=itemView.getContext();

        newGroupImage = TextLabel.builder()
                .beginConfig()
                .useFont(FontManager.getFontAwesome(itemView.getContext()))
                .textColor(Color.BLACK)
                .endConfig()
                .buildRound(context.getString(R.string.fa_pencil_square_o), Color.LTGRAY);

        oldGroupImage = TextLabel.builder()
                .beginConfig()
                .useFont(FontManager.getFontAwesome(itemView.getContext()))
                .textColor(context.getResources().getColor(R.color.green_dark))
                .endConfig()
                .buildRound(context.getString(R.string.fa_folder), Color.GRAY);

        final TextDrawable td = new TextDrawable(context);
        td.setTypeface(FontManager.getFontAwesome(context));
        td.setTextAlign(Layout.Alignment.ALIGN_CENTER);
        td.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
        td.setText(context.getString(R.string.fa_caret_down));
        icon.setImageDrawable(td);




    }

    @Override
    public boolean shouldItemViewClickToggleExpansion() {
        return false;
    }

    @Override
    public void setExpanded(boolean expanded) {
        super.setExpanded(expanded);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (expanded) {
                icon.setRotation(ROTATED_POSITION);
            } else {
                icon.setRotation(INITIAL_POSITION);
            }
        }
    }

    @Override
    public void onExpansionToggled(boolean expanded) {
        super.onExpansionToggled(expanded);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            RotateAnimation rotateAnimation;
            if (expanded) { // rotate clockwise
                rotateAnimation = new RotateAnimation(ROTATED_POSITION,
                        INITIAL_POSITION,
                        RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                        RotateAnimation.RELATIVE_TO_SELF, 0.5f);
            } else { // rotate counterclockwise
                rotateAnimation = new RotateAnimation(-1 * ROTATED_POSITION,
                        INITIAL_POSITION,
                        RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                        RotateAnimation.RELATIVE_TO_SELF, 0.5f);
            }

            rotateAnimation.setDuration(200);
            rotateAnimation.setFillAfter(true);
            icon.startAnimation(rotateAnimation);
        }
    }
}
