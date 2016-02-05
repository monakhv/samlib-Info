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
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import monakhv.samlib.log.Log;

import java.util.HashMap;


/**
 * Based on https://github.com/kibao/recycler-view-animations-android-dev-summit-2015
 * Created by monakhv on 04.02.16.
 */
public class BookAnimator extends DefaultItemAnimator {
    private static final String DEBUG_TAG = "BookAnimator";
    private HashMap<RecyclerView.ViewHolder, AnimatorInfo> mAnimatorMap = new HashMap<>();


//    @Override
//    public boolean canReuseUpdatedViewHolder(RecyclerView.ViewHolder viewHolder) {
//        return false;
//    }

    @Override
    public ItemHolderInfo obtainHolderInfo() {
        return new BookItemHolderInfo();
    }

    @Override
    public boolean animateChange(@NonNull final RecyclerView.ViewHolder oldHolder, @NonNull final RecyclerView.ViewHolder newHolder, @NonNull final ItemHolderInfo preInfo, @NonNull final ItemHolderInfo postInfo) {
        if (oldHolder instanceof GroupViewHolder && newHolder instanceof GroupViewHolder) {
            final GroupViewHolder groupPreHolder = (GroupViewHolder) oldHolder;
            final GroupViewHolder groupPostHolder = (GroupViewHolder) newHolder;
            final BookItemHolderInfo preGroupInfo = (BookItemHolderInfo) preInfo;
            final BookItemHolderInfo postGroupInfo = (BookItemHolderInfo) postInfo;

            final String oldBookNumber = preGroupInfo.bookNumberString;
            final String newBookNumber = postGroupInfo.bookNumberString;


            Log.d(DEBUG_TAG, "animateChange: GroupChange from: " + preGroupInfo.bookNumberString);
            Log.d(DEBUG_TAG, "animateChange: GroupChange to: " + postGroupInfo.bookNumberString);


            ObjectAnimator oldTextRotate = ObjectAnimator.ofFloat(groupPostHolder.bookNumber, View.ROTATION_X, 0, 90);
            ObjectAnimator newTextRotate = ObjectAnimator.ofFloat(groupPostHolder.bookNumber, View.ROTATION_X, -90, 0);
            ObjectAnimator oldRotateImage=null;
            ObjectAnimator newRotateImage=null;
            AnimatorSet textAnim = new AnimatorSet();
            textAnim.playSequentially(oldTextRotate, newTextRotate);


            AnimatorSet changeAnim;
            if (preGroupInfo.newTag.equals(postGroupInfo.newTag)) {
                changeAnim = textAnim;
            } else {
                Log.d(DEBUG_TAG, "animateChange: GroupChange Make Flip icon ");
                changeAnim=new AnimatorSet();

                oldRotateImage = ObjectAnimator.ofFloat(groupPostHolder.newIcon, View.ROTATION_Y, 0, 90);
                newRotateImage = ObjectAnimator.ofFloat(groupPostHolder.newIcon, View.ROTATION_Y, -90, 0);
                AnimatorSet imageRotation = new AnimatorSet();
                imageRotation.playSequentially(oldRotateImage,newRotateImage);

                changeAnim.playTogether(textAnim,imageRotation);

                oldRotateImage.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        groupPostHolder.newIcon.setImageDrawable(preGroupInfo.rotateImage);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        groupPostHolder.newIcon.setImageDrawable(postGroupInfo.rotateImage);
                    }

                });


            }


            oldTextRotate.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    groupPostHolder.bookNumber.setText(oldBookNumber);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    groupPostHolder.bookNumber.setText(newBookNumber);
                }
            });


            changeAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    dispatchAnimationFinished(newHolder);
                    mAnimatorMap.remove(newHolder);
                }
            });

            AnimatorInfo runningInfo = mAnimatorMap.get(newHolder);
            if (runningInfo != null) {
                boolean firstHalf = runningInfo.oldTextRotate != null && runningInfo.oldTextRotate.isRunning();
                long currentPlayTime = firstHalf ? runningInfo.oldTextRotate.getCurrentPlayTime() : runningInfo.newTextRotate.getCurrentPlayTime();
                runningInfo.overallAnim.cancel();

                if (firstHalf){
                    oldTextRotate.setCurrentPlayTime(currentPlayTime);
                    if (oldRotateImage!= null){
                        oldRotateImage.setCurrentPlayTime(currentPlayTime);
                    }
                }
                else {
                    oldTextRotate.setCurrentPlayTime(oldTextRotate.getDuration());
                    newTextRotate.setCurrentPlayTime(currentPlayTime);
                    if (oldRotateImage != null){
                        oldRotateImage.setCurrentPlayTime(oldRotateImage.getDuration());
                        newRotateImage.setCurrentPlayTime(currentPlayTime);
                    }
                }


            }

            AnimatorInfo runningAnimInfo = new AnimatorInfo(changeAnim,oldTextRotate,newTextRotate,oldRotateImage,newRotateImage);
            mAnimatorMap.put(newHolder,runningAnimInfo);
            changeAnim.start();


        }

        return super.animateChange(oldHolder, newHolder, preInfo, postInfo);
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
                bookNumberString = (String) holder.bookNumber.getText();
                newTag = holder.newIcon.getTag();
                rotateImage=holder.newIcon.getDrawable();
                return this;
            } else {
                return super.setFrom(viewHolder, flags);
            }

        }
    }

    class AnimatorInfo {
        Animator overallAnim;
        ObjectAnimator oldTextRotate,newTextRotate;
        ObjectAnimator oldRotateImage,newRotateImage;

        public AnimatorInfo(Animator overallAnim, ObjectAnimator oldTextRotate, ObjectAnimator newTextRotate, ObjectAnimator oldRotateImage, ObjectAnimator newRotateImage) {
            this.overallAnim = overallAnim;
            this.oldTextRotate = oldTextRotate;
            this.newTextRotate = newTextRotate;
            this.oldRotateImage = oldRotateImage;
            this.newRotateImage = newRotateImage;
        }
    }


}
