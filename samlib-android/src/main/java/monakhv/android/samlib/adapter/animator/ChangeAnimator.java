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

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;

/**
 * Created by monakhv on 08.02.16.
 */
public abstract class ChangeAnimator {
    private AnimatorSet mAnimatorSet;
    protected ObjectAnimator mFirstAnimator, mSecondAnimator;


    public ChangeAnimator(){
        mAnimatorSet = new AnimatorSet();
    }
    public boolean isFirstHalf(){
        return mFirstAnimator != null && mFirstAnimator.isRunning();
    }

    public long getCurrentPlayTime(){
        return isFirstHalf() ?
                mFirstAnimator.getCurrentPlayTime()
                :
                mSecondAnimator.getCurrentPlayTime();
    }

    public void cancelAnimation(){
        long currentPlayTime = getCurrentPlayTime();
        if (isFirstHalf()){
            mFirstAnimator.setCurrentPlayTime(currentPlayTime);
        }
        else {
            mFirstAnimator.setCurrentPlayTime(mFirstAnimator.getDuration());
            mSecondAnimator.setCurrentPlayTime(currentPlayTime);
        }
    }

    public AnimatorSet getAnimatorSet(){
        mAnimatorSet.playSequentially(mFirstAnimator,mSecondAnimator);
        return mAnimatorSet;
    }

}
