package monakhv.android.samlib.animation;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/*
 * Copyright 2014  Dmitry Monakhov
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
 * 12/10/14.
 */

/**
 * Class is base on http://www.inter-fuser.com/2009/08/android-animations-3d-flip.html
 */
public class Flip3dAnimation extends Animation {
    private static final String DEBUG_TAG ="Flip3dAnimation" ;
    private final float mFromDegrees;
    private final float mToDegrees;
    private final float mCenterX;
    private final float mCenterY;
    private Camera mCamera;

    public Flip3dAnimation(float fromDegrees, float toDegrees,   float centerX, float centerY) {
        //Log.i(DEBUG_TAG,"Begin rotate from "+fromDegrees+" to "+toDegrees);
        mFromDegrees = fromDegrees;
        mToDegrees = toDegrees;
        mCenterX = centerX;
        mCenterY = centerY;
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
        mCamera = new Camera();
    }


//    The main steps that happen in the applyTransformation are:
//
//Calculate the degrees rotation for the current transformation
//Get the tranformation matrix for the Animation
//Generate a rotation matrix using camera.rotate(degrees)
//Apply that matrix to the Animation transform
//Set a pre translate so that the view is moved to the edge of the screen and rotates around it centre and not it's edge
//Set a post translate so that the animated view is placed back in the centre of the screen.


    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        final float fromDegrees = mFromDegrees;
        float degrees = fromDegrees + ((mToDegrees - fromDegrees) * interpolatedTime);
        //Log.d(DEBUG_TAG, "time: "+interpolatedTime+"  rotate: " + degrees);

        final float centerX = mCenterX;
        final float centerY = mCenterY;
        final Camera camera = mCamera;

        final Matrix matrix = t.getMatrix();

        camera.save();

        camera.rotateY(degrees);

        camera.getMatrix(matrix);
        camera.restore();

        matrix.preTranslate(-centerX, -centerY);
        matrix.postTranslate(centerX, centerY);

    }

}
