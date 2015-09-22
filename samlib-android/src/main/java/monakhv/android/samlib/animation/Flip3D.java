package monakhv.android.samlib.animation;

import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import monakhv.samlib.log.Log;

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
 * 12/11/14.
 */
public class Flip3D {
    public interface animationFlip3DListener {
        void onStart();

        void onEnd();
    }

    private static final long ANIMATION_DURATION = 500L;
    private static final String DEBUG_TAG = "Flip3D";
    private final ImageView image1;
    private final ImageView image2;
    private animationFlip3DListener end;

    private boolean isFirstImage = true;

    protected void afterAnimationEnd() {
        if (end != null) {
            end.onEnd();
        }

    }

    Flip3D(ImageView img1, ImageView img2, animationFlip3DListener e) {
        this.image1 = img1;
        this.image2 = img2;
        image1.setVisibility(View.VISIBLE);
        image2.setVisibility(View.GONE);

        end = e;

    }

    Flip3D(ImageView img1, ImageView img2) {
        this(img1, img2, null);
    }


//    public ImageView getFrontImage(){
//        return image1;
//    }


    public void makeFlip() {
        if (end != null) {
            end.onStart();
        }

        if (isFirstImage) {
            //Log.d(DEBUG_TAG,"making flip 0 -> 90");
            applyRotation(0, 90);
            isFirstImage = !isFirstImage;

        } else {
            //Log.d(DEBUG_TAG,"making flip 0 -> -90");
            applyRotation(0, -90);
            isFirstImage = !isFirstImage;
        }
    }

    private void applyRotation(float start, float end) {
// Find the center of image
        final float centerX = image1.getWidth() / 2.0f;
        final float centerY = image1.getHeight() / 2.0f;

// Create a new 3D rotation with the supplied parameter
// The animation listener is used to trigger the next animation
        final Flip3dAnimation rotation =
                new Flip3dAnimation(start, end, centerX, centerY);
        rotation.setDuration(ANIMATION_DURATION);
        rotation.setFillAfter(true);
        rotation.setInterpolator(new AccelerateInterpolator());
        rotation.setAnimationListener(new DisplayNextView(isFirstImage, image1, image2));

        if (isFirstImage) {
            image1.startAnimation(rotation);
        } else {
            image2.startAnimation(rotation);
        }

    }

    private class DisplayNextView implements Animation.AnimationListener {
        private boolean mCurrentView;
        ImageView image1;
        ImageView image2;

        public DisplayNextView(boolean currentView, ImageView image1, ImageView image2) {
            mCurrentView = currentView;
            this.image1 = image1;
            this.image2 = image2;
        }

        public void onAnimationStart(Animation animation) {
            Log.d(DEBUG_TAG, "Animation start");
        }

        public void onAnimationEnd(Animation animation) {
            Log.d(DEBUG_TAG, "Animation end - swap image");
            image1.post(new SwapViews(mCurrentView, image1, image2));
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }


        private class SwapViews implements Runnable {
            private boolean mIsFirstView;
            private ImageView image1;
            private ImageView image2;

            public SwapViews(boolean isFirstView, ImageView image1, ImageView image2) {
                mIsFirstView = isFirstView;
                this.image1 = image1;
                this.image2 = image2;
            }

            public void run() {
                final float centerX = image1.getWidth() / 2.0f;
                final float centerY = image1.getHeight() / 2.0f;
                Flip3dAnimation rotation;

                if (mIsFirstView) {
                    image1.setVisibility(View.GONE);
                    image2.setVisibility(View.VISIBLE);
                    image2.requestFocus();

                    rotation = new Flip3dAnimation(-90, 0, centerX, centerY);
                } else {
                    image2.setVisibility(View.GONE);
                    image1.setVisibility(View.VISIBLE);
                    image1.requestFocus();

                    rotation = new Flip3dAnimation(90, 0, centerX, centerY);
                }

                rotation.setDuration(ANIMATION_DURATION);
                rotation.setFillAfter(true);
                rotation.setInterpolator(new DecelerateInterpolator());
                rotation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        Log.d(DEBUG_TAG, "Second animation start");

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        Log.d(DEBUG_TAG, "Real animation end");
                        afterAnimationEnd();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                if (mIsFirstView) {
                    image2.startAnimation(rotation);
                } else {
                    image1.startAnimation(rotation);
                }
            }
        }
    }


}
