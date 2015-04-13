package monakhv.android.samlib.recyclerview;

import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import uk.co.senab.actionbarpulltorefresh.library.viewdelegates.ViewDelegate;

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
 * 12/5/14.
 */
public class RecyclerViewDelegate   implements ViewDelegate {
    @Override
    public boolean isReadyForPull(View view, float v, float v2) {

        //direction	Negative to check scrolling up, positive to check scrolling down.

        int direction = - 1;
        RecyclerView  rv=  (RecyclerView) view;

        boolean original = ViewCompat.canScrollHorizontally(rv,direction);
        boolean res = !original && rv.getChildAt(0) != null && rv.getChildAt(0).getTop() < 0 || original;

        return !res;
    }
}
