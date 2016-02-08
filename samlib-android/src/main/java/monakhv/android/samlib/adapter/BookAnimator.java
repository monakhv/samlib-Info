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
import android.widget.ImageView;
import android.widget.TextView;
import monakhv.samlib.log.Log;

import java.util.HashMap;


/**
 * Based on https://github.com/kibao/recycler-view-animations-android-dev-summit-2015
 * Created by monakhv on 04.02.16.
 */
public class BookAnimator extends DefaultItemAnimator {
    private static final String DEBUG_TAG = "BookAnimator";
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
          //  newHolder.setIsRecyclable(false);
          //  return false;
        }

        if (oldHolder instanceof BookViewHolder && newHolder instanceof BookViewHolder) {
            final BookViewHolder bookPostHolder = (BookViewHolder) newHolder;
            groupAnimator(newHolder, bookPostHolder.bookSize, bookPostHolder.flipIcon, preInfo, postInfo);
            //newHolder.setIsRecyclable(false);
            //return false;
        }
        BookItemHolderInfo preBiInfo = (BookItemHolderInfo) preInfo;
        BookItemHolderInfo postBiInfo = (BookItemHolderInfo) postInfo;
        Log.w(DEBUG_TAG, "animateChange: Text change from: " +preBiInfo.bookNumberString);
        Log.w(DEBUG_TAG, "animateChange: Text change from: " +postBiInfo.bookNumberString);
//        dispatchAnimationFinished(oldHolder);
//        dispatchAnimationFinished(newHolder);

        return super.animateChange(oldHolder, newHolder, preInfo, postInfo);
    }

    private void groupAnimator(@NonNull final RecyclerView.ViewHolder newHolder, final TextView textToRotate, final ImageView imageToRotate, @NonNull final ItemHolderInfo preInfo, @NonNull final ItemHolderInfo postInfo) {

        final BookItemHolderInfo preGroupInfo = (BookItemHolderInfo) preInfo;
        final BookItemHolderInfo postGroupInfo = (BookItemHolderInfo) postInfo;

        final String oldBookNumber = preGroupInfo.bookNumberString;
        final String newBookNumber = postGroupInfo.bookNumberString;


        Log.d(DEBUG_TAG, "groupAnimator: Text change from: " + preGroupInfo.bookNumberString);
        Log.d(DEBUG_TAG, "groupAnimator: Text change to: " + postGroupInfo.bookNumberString);


        ObjectAnimator oldTextRotate = null;
        ObjectAnimator newTextRotate = null;
        ObjectAnimator oldRotateImage = null;
        ObjectAnimator newRotateImage = null;
        AnimatorSet textAnim = null;
        AnimatorSet imageRotation = null;

        if (!oldBookNumber.equals(newBookNumber)) {
            oldTextRotate = ObjectAnimator.ofFloat(textToRotate, View.ROTATION_X, 0, 90);

            newTextRotate = ObjectAnimator.ofFloat(textToRotate, View.ROTATION_X, -90, 0);
            oldTextRotate.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    textToRotate.setText(oldBookNumber);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    textToRotate.setText(newBookNumber);
                }
            });

            textAnim = new AnimatorSet();
            textAnim.playSequentially(oldTextRotate, newTextRotate);
        }


        if (!preGroupInfo.newTag.equals(postGroupInfo.newTag)) {
            Log.d(DEBUG_TAG, "groupAnimator: GroupChange Make Flip icon ");

            imageRotation = new AnimatorSet();
            oldRotateImage = ObjectAnimator.ofFloat(imageToRotate, View.ROTATION_Y, 0, 90);
            newRotateImage = ObjectAnimator.ofFloat(imageToRotate, View.ROTATION_Y, -90, 0);

            imageRotation.playSequentially(oldRotateImage, newRotateImage);


            oldRotateImage.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    imageToRotate.setImageDrawable(preGroupInfo.rotateImage);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    imageToRotate.setImageDrawable(postGroupInfo.rotateImage);
                }

            });


        }

        if (textAnim == null && imageRotation == null) {
            return;
        }


        AnimatorSet changeAnim = new AnimatorSet();

        if (textAnim != null) {
            changeAnim.playTogether(textAnim);
        }
        if (imageRotation != null) {
            changeAnim.playTogether(imageRotation);
        }


        changeAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                dispatchAnimationFinished(newHolder);
                mAnimatorMap.remove(newHolder);
  //              newHolder.setIsRecyclable(true);
            }
        });

        AnimatorInfo runningInfo = mAnimatorMap.get(newHolder);
        if (runningInfo != null) {
            boolean firstHalf;
            long currentPlayTime;
            if (runningInfo.oldTextRotate != null){
                firstHalf =  runningInfo.oldTextRotate.isRunning();
                currentPlayTime = firstHalf ? runningInfo.oldTextRotate.getCurrentPlayTime() : runningInfo.newTextRotate.getCurrentPlayTime();
            }
            else {
                firstHalf =  runningInfo.oldRotateImage.isRunning();
                currentPlayTime = firstHalf ? runningInfo.oldRotateImage.getCurrentPlayTime() : runningInfo.newRotateImage.getCurrentPlayTime();
            }


            runningInfo.overallAnim.cancel();

            if (firstHalf) {
                if (oldTextRotate != null) {
                    oldTextRotate.setCurrentPlayTime(currentPlayTime);
                }

                if (oldRotateImage != null) {
                    oldRotateImage.setCurrentPlayTime(currentPlayTime);
                }
            } else {
                if (oldTextRotate != null) {
                    oldTextRotate.setCurrentPlayTime(oldTextRotate.getDuration());
                    newTextRotate.setCurrentPlayTime(currentPlayTime);
                }

                if (oldRotateImage != null) {
                    oldRotateImage.setCurrentPlayTime(oldRotateImage.getDuration());
                    newRotateImage.setCurrentPlayTime(currentPlayTime);
                }
            }


        }

        AnimatorInfo runningAnimInfo = new AnimatorInfo(changeAnim, oldTextRotate, newTextRotate, oldRotateImage, newRotateImage);
        mAnimatorMap.put(newHolder, runningAnimInfo);
        changeAnim.start();
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
                rotateImage = holder.newIcon.getDrawable();
                return this;
            }

            if (viewHolder instanceof BookViewHolder) {
                super.setFrom(viewHolder, flags);
                BookViewHolder holder = (BookViewHolder) viewHolder;
                bookNumberString = (String) holder.bookSize.getText();
                newTag = holder.flipIcon.getTag();
                rotateImage = holder.flipIcon.getDrawable();
                return this;
            }
            return super.setFrom(viewHolder, flags);
        }
    }

    class AnimatorInfo {
        Animator overallAnim;
        ObjectAnimator oldTextRotate, newTextRotate;
        ObjectAnimator oldRotateImage, newRotateImage;

        public AnimatorInfo(Animator overallAnim, ObjectAnimator oldTextRotate, ObjectAnimator newTextRotate, ObjectAnimator oldRotateImage, ObjectAnimator newRotateImage) {
            this.overallAnim = overallAnim;
            this.oldTextRotate = oldTextRotate;
            this.newTextRotate = newTextRotate;
            this.oldRotateImage = oldRotateImage;
            this.newRotateImage = newRotateImage;
        }
    }


}
