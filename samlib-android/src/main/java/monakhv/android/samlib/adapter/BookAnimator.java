/*
 *  Copyright 2016 Dmitry Monakhov.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  04.02.16 18:11
 *
 */

package monakhv.android.samlib.adapter;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.TextView;
import monakhv.android.samlib.adapter.animator.AnimatorInfo;
import monakhv.android.samlib.adapter.animator.ChangeAnimator;
import monakhv.android.samlib.adapter.animator.ImageRotationAnimator;
import monakhv.android.samlib.adapter.animator.TextRotationAnimator;

import java.util.HashMap;


/**
 * Based on https://github.com/kibao/recycler-view-animations-android-dev-summit-2015
 *
 * This is the special case where CAN NOT reuse holders
 * Created by monakhv on 04.02.16.
 */
public class BookAnimator extends DefaultItemAnimator {
    //private static final String DEBUG_TAG = "BookAnimator";
    private HashMap<RecyclerView.ViewHolder, AnimatorInfo> mAnimatorMap = new HashMap<>();


    @Override
    public boolean canReuseUpdatedViewHolder(RecyclerView.ViewHolder viewHolder) {
        return false;
    }

    @Override
    public ItemHolderInfo obtainHolderInfo() {
        return new BookItemHolderInfo();
    }

    @Override
    public boolean animateChange(@NonNull final RecyclerView.ViewHolder oldHolder, @NonNull final RecyclerView.ViewHolder newHolder, @NonNull final ItemHolderInfo preInfo, @NonNull final ItemHolderInfo postInfo) {
        if (oldHolder instanceof GroupViewHolder && newHolder instanceof GroupViewHolder) {
            final GroupViewHolder groupPostHolder = (GroupViewHolder) newHolder;
            groupAnimator(newHolder, groupPostHolder.bookNumber, groupPostHolder.newIcon, preInfo, postInfo);
            newHolder.setIsRecyclable(false);
            //return false;
        }

        if (oldHolder instanceof BookViewHolder && newHolder instanceof BookViewHolder) {
            final BookViewHolder bookPostHolder = (BookViewHolder) newHolder;
            groupAnimator(newHolder, bookPostHolder.bookSize, bookPostHolder.flipIcon, preInfo, postInfo);
            newHolder.setIsRecyclable(false);
           // return false;
        }
//        BookItemHolderInfo preBiInfo = (BookItemHolderInfo) preInfo;
//        BookItemHolderInfo postBiInfo = (BookItemHolderInfo) postInfo;
//        Log.w(DEBUG_TAG, "animateChange: Text change from: " +preBiInfo.bookNumberString);
//        Log.w(DEBUG_TAG, "animateChange: Text change from: " +postBiInfo.bookNumberString);
//        dispatchAnimationFinished(oldHolder);
//        dispatchAnimationFinished(newHolder);

        return super.animateChange(oldHolder, newHolder, preInfo, postInfo);
    }

    private void groupAnimator(@NonNull final RecyclerView.ViewHolder newHolder, final TextView textToRotate, final ImageView imageToRotate, @NonNull final ItemHolderInfo preInfo, @NonNull final ItemHolderInfo postInfo) {

        final BookItemHolderInfo preGroupInfo = (BookItemHolderInfo) preInfo;
        final BookItemHolderInfo postGroupInfo = (BookItemHolderInfo) postInfo;

        final String oldBookNumber = preGroupInfo.bookNumberString;
        final String newBookNumber = postGroupInfo.bookNumberString;


//        Log.d(DEBUG_TAG, "groupAnimator: Text change from: " + preGroupInfo.bookNumberString);
//        Log.d(DEBUG_TAG, "groupAnimator: Text change to: " + postGroupInfo.bookNumberString);

        ChangeAnimator imageRotation=null;
        ChangeAnimator textRotation=null;


        if (!oldBookNumber.equals(newBookNumber)) {
            textRotation=new TextRotationAnimator(textToRotate,oldBookNumber,newBookNumber);
        }


        if (!preGroupInfo.newTag.equals(postGroupInfo.newTag)) {
            imageRotation=new ImageRotationAnimator(imageToRotate,preGroupInfo.rotateImage,postGroupInfo.rotateImage);
        }

        if (textRotation == null && imageRotation == null) {
            dispatchAnimationFinished(newHolder);
            return;
        }

        AnimatorInfo animator=new AnimatorInfo();



        if (textRotation != null) {
            animator.add(textRotation);
        }
        if (imageRotation != null) {
            animator.add(imageRotation);
        }


        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                dispatchAnimationFinished(newHolder);
                mAnimatorMap.remove(newHolder);
  //              newHolder.setIsRecyclable(true);
            }
        });

        AnimatorInfo runningInfo = mAnimatorMap.get(newHolder);
        if (runningInfo != null) {
            runningInfo.cancelAll();
        }


        mAnimatorMap.put(newHolder, animator);
        animator.start();
    }

    class BookItemHolderInfo extends ItemHolderInfo {
        boolean group = false;
        Object newTag;
        String bookNumberString;
        Drawable rotateImage;

        @Override
        public ItemHolderInfo setFrom(RecyclerView.ViewHolder viewHolder, int flags) {
            if (viewHolder instanceof GroupViewHolder) {
                super.setFrom(viewHolder, flags);
                GroupViewHolder holder = (GroupViewHolder) viewHolder;
                group = true;
                bookNumberString = "" + holder.bookNumber.getText();
                newTag = holder.newIcon.getTag();
                rotateImage = holder.newIcon.getDrawable();
                return this;
            }

            if (viewHolder instanceof BookViewHolder) {
                super.setFrom(viewHolder, flags);
                BookViewHolder holder = (BookViewHolder) viewHolder;
                bookNumberString = "" + holder.bookSize.getText();
                newTag = holder.flipIcon.getTag();
                rotateImage = holder.flipIcon.getDrawable();
                return this;
            }
            return super.setFrom(viewHolder, flags);
        }
    }



}
