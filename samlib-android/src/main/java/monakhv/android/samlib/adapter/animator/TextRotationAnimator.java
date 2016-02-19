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
 *  08.02.16 11:50
 *
 */

package monakhv.android.samlib.adapter.animator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;
import android.widget.TextView;

/**
 * Text view Rotation animation or cross fading for API <14
 * Created by monakhv on 08.02.16.
 */
public class TextRotationAnimator extends ChangeAnimator {

    public TextRotationAnimator(final TextView textView, final String oldText, final String newText) {

        if (android.os.Build.VERSION.SDK_INT>=14){
            initAnimatorICS(textView);
        }
        else {
            initAnimator(textView);
        }

        mFirstAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                textView.setText(oldText);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                textView.setText(newText);
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void initAnimatorICS(final  TextView textView){
        mFirstAnimator = ObjectAnimator.ofFloat(textView, View.ROTATION_X, 0, 90);
        mSecondAnimator = ObjectAnimator.ofFloat(textView, View.ROTATION_X, -90, 0);
    }



}
