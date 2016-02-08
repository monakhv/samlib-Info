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
 *  08.02.16 15:38
 *
 */

package monakhv.android.samlib.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import monakhv.android.samlib.adapter.animator.AnimatorInfo;
import monakhv.android.samlib.adapter.animator.ChangeAnimator;
import monakhv.android.samlib.adapter.animator.ImageRotationAnimator;
import monakhv.android.samlib.adapter.animator.TextRotationAnimator;

import java.util.HashMap;

/**
 * Created by monakhv on 08.02.16.
 */
public class AuthorAnimator extends DefaultItemAnimator {
    private HashMap<RecyclerView.ViewHolder, AnimatorInfo> mAnimatorMap = new HashMap<>();
    @Override
    public boolean canReuseUpdatedViewHolder(RecyclerView.ViewHolder viewHolder) {
        return true;
    }

    @Override
    public ItemHolderInfo obtainHolderInfo() {
        return new AuthorHolderInfo();
    }


    @Override
    public boolean animateChange(@NonNull RecyclerView.ViewHolder oldHolder, @NonNull final RecyclerView.ViewHolder newHolder, @NonNull ItemHolderInfo preInfo, @NonNull ItemHolderInfo postInfo) {
        if (oldHolder instanceof AuthorAdapter.AuthorViewHolder && newHolder instanceof AuthorAdapter.AuthorViewHolder){
            final AuthorHolderInfo authorPreInfo= (AuthorHolderInfo) preInfo;
            final AuthorHolderInfo authorPostInfo= (AuthorHolderInfo) postInfo;
            final AuthorAdapter.AuthorViewHolder avh = (AuthorAdapter.AuthorViewHolder) newHolder;

            final String oldText=authorPreInfo.rotateText;
            final String newText = authorPostInfo.rotateText;
            ChangeAnimator imageRotation=null;
            ChangeAnimator textRotation=null;

            if (!oldText.equals(newText)) {
                textRotation=new TextRotationAnimator(avh.updatedData,oldText,newText);
            }
            if (!authorPreInfo.newTag.equals(authorPostInfo.newTag)){
                imageRotation = new ImageRotationAnimator(avh.flipIcon,authorPreInfo.rotateImage,authorPostInfo.rotateImage);
            }

            if (textRotation == null && imageRotation == null) {
                dispatchAnimationFinished(newHolder);
                return false;
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
        return false;
    }

    class AuthorHolderInfo extends ItemHolderInfo {
        Object newTag;
        String rotateText;
        Drawable rotateImage;
        @Override
        public ItemHolderInfo setFrom(RecyclerView.ViewHolder viewHolder, int flags) {
            super.setFrom(viewHolder, flags);
            if (viewHolder instanceof AuthorAdapter.AuthorViewHolder){
                AuthorAdapter.AuthorViewHolder avh = (AuthorAdapter.AuthorViewHolder) viewHolder;
                rotateImage=avh.flipIcon.getDrawable();
                newTag=avh.flipIcon.getTag();
                rotateText= (String) avh.updatedData.getText();
                return this;
            }
            return  super.setFrom(viewHolder, flags);
        }

    }
}
