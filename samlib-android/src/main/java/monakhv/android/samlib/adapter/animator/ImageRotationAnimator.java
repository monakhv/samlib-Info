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
 *  08.02.16 11:55
 *
 */

package monakhv.android.samlib.adapter.animator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;

/**
 * Image rotation animation
 * Created by monakhv on 08.02.16.
 */
public class ImageRotationAnimator extends ChangeAnimator {
    public ImageRotationAnimator(final ImageView imageToRotate, final Drawable oldImage, final Drawable newImage){

        if (android.os.Build.VERSION.SDK_INT>=14){
            initAnimatorICS(imageToRotate);
        }
        else {
            initAnimator(imageToRotate);
        }

        mFirstAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                imageToRotate.setImageDrawable(oldImage);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                imageToRotate.setImageDrawable(newImage);
            }
        });
    }


    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void initAnimatorICS(final ImageView imageToRotate){
        mFirstAnimator = ObjectAnimator.ofFloat(imageToRotate, View.ROTATION_Y, 0, 90);
        mSecondAnimator = ObjectAnimator.ofFloat(imageToRotate, View.ROTATION_Y, -90, 0);
    }
}
