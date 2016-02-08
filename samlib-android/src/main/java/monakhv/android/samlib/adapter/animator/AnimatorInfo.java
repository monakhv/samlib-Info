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
import android.animation.AnimatorSet;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by monakhv on 08.02.16.
 */
public class AnimatorInfo {
    private AnimatorSet overallAnim;
    private List<ChangeAnimator> mChangeAnimators;

    public AnimatorInfo(){
        mChangeAnimators=new ArrayList<>();
        overallAnim=new AnimatorSet();
    }

    public void add(ChangeAnimator changeAnimator){
        if (changeAnimator != null){
            mChangeAnimators.add(changeAnimator);
            overallAnim.playTogether( changeAnimator.getAnimatorSet());
        }
    }

    public void addListener(Animator.AnimatorListener animatorListener){
        overallAnim.addListener(animatorListener);
    }

    public void cancelAll(){
       overallAnim.cancel();
        for (ChangeAnimator changeAnimator:mChangeAnimators){
            changeAnimator.cancelAnimation();
        }
    }

    public void start(){
        overallAnim.start();
    }

}
