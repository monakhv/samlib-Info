package monakhv.android.samlib.animation;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import monakhv.android.samlib.R;

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
 * 4/3/15.
 */
public class FlipIcon extends FrameLayout implements View.OnClickListener {
    private ImageView imageFirst;
    private ImageView imageSecond;
    private Flip3D flip3D;

    public FlipIcon(Context context, AttributeSet attrs){
        super(context,attrs);
        initializeViews(context);
        handleAttributes(context,attrs);
    }

    public FlipIcon(Context context){
        super(context);
        initializeViews(context);
    }

    private void initializeViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.buttons_flip_merge,this);
        imageFirst= (ImageView) this.findViewById(R.id.bookClosed);
        imageFirst.setVisibility(VISIBLE);
        imageSecond= (ImageView) this.findViewById(R.id.bookOpen);
        imageSecond.setVisibility(GONE);

    }


    private void handleAttributes(Context context, AttributeSet attrs){
        TypedArray typedArray=context.obtainStyledAttributes(attrs, R.styleable.FlipIcon);
        final int count = typedArray.getIndexCount();

        for (int i = 0; i<count;i++){
            int attr = typedArray.getIndex(i);
            switch (attr){
                case R.styleable.FlipIcon_customImageFirst:
                    imageFirst.setImageDrawable(typedArray.getDrawable(attr));
                    break;
                case R.styleable.FlipIcon_customImageSecond:
                    imageSecond.setImageDrawable(typedArray.getDrawable(attr));
                    break;
            }//end switch
        }//end for
        typedArray.recycle();
    }

    public void setData(int imageFirstResource,int imageSecondResource , Flip3D.animationEndListener end,boolean clickable){
        imageFirst.setImageResource(imageFirstResource);
        imageSecond.setImageResource(imageSecondResource);
        flip3D = new Flip3D(imageFirst,imageSecond,end);
        setClickable(clickable);
        if (clickable){
            setOnClickListener(this);
        }

    }
    public void makeFlip(){
        if(flip3D != null){
            flip3D.makeFlip();
        }
    }

    @Override
    public void onClick(View v) {
        if (flip3D != null){
            flip3D.makeFlip();
        }
    }
}
