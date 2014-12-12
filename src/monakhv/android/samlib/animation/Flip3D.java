package monakhv.android.samlib.animation;

import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

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
 public abstract class Flip3D {

    private ImageView image1;
    private ImageView image2;

    private boolean isFirstImage = true;
    protected abstract void afterAnimationEnd() ;



    public Flip3D(ImageView image1, ImageView image2) {
        this.image1 = image1;
        this.image2 = image2;
        image1.setVisibility(View.VISIBLE);
        image2.setVisibility(View.GONE);

        image1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (isFirstImage) {
                    applyRotation(0, 90);
                    isFirstImage = !isFirstImage;

                } else {
                    applyRotation(0, -90);
                    isFirstImage = !isFirstImage;
                }
            }
        });
    }

    private void applyRotation(float start, float end) {
// Find the center of image
        final float centerX = image1.getWidth() / 2.0f;
        final float centerY = image1.getHeight() / 2.0f;

// Create a new 3D rotation with the supplied parameter
// The animation listener is used to trigger the next animation
        final Flip3dAnimation rotation =
                new Flip3dAnimation(start, end, centerX, centerY);
        rotation.setDuration(500);
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
        }

        public void onAnimationEnd(Animation animation) {
            image1.post(new SwapViews(mCurrentView, image1, image2));
        }

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

                rotation.setDuration(500);
                rotation.setFillAfter(true);
                rotation.setInterpolator(new DecelerateInterpolator());
                rotation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
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
