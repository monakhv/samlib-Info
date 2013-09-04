/*
 * Copyright 2013 Dmitry Monakhov.
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
 */
package monakhv.android.samlib;

import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 *
 * @author monakhv
 */
public class ListSwipeListener implements GestureDetector.OnGestureListener{
    public interface SwipeCallBack {
        public boolean singleClick(MotionEvent e);
        public boolean swipeRight(MotionEvent e);
        public boolean swipeLeft(MotionEvent e);
    }
    private SwipeCallBack calBack;
    public ListSwipeListener(SwipeCallBack calBack){
        this.calBack = calBack;
                
    }
    private float SWIPE_THRESHOLD = 150;
    private float SWIPE_VELOCITY_THRESHOLD = 200;

    public boolean onDown(MotionEvent e) {
        return false;
        
    }

    public void onShowPress(MotionEvent e) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean onSingleTapUp(MotionEvent e) {
        calBack.singleClick(e);
        return true;
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return true;
    }

    public void onLongPress(MotionEvent e) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float diffY = e2.getY() - e1.getY();
        float diffX = e2.getX() - e1.getX();
        if (Math.abs(diffX) > Math.abs(diffY)) {
            if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
               
                
                if (diffX > 0 ) {
                    //left to right
                    return calBack.swipeRight(e1);                    
                } else {
                    //right to left
                    return calBack.swipeLeft(e1);
                }
                                                                
            }

        }
        return false;



    }
    
}
