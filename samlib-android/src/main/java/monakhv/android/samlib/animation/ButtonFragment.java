package monakhv.android.samlib.animation;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import monakhv.android.samlib.R;


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
public class ButtonFragment extends Fragment {
    private ImageView closedBook, openBook;
    private boolean isClosed = true;
    private Flip3D flip;


    public boolean isClosed() {
        return isClosed;
    }

    public void setClosed(boolean isClosed) {
        this.isClosed = isClosed;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.buttons_flip, container, false);
        closedBook = (ImageView) view.findViewById(R.id.bookClosed);
        openBook = (ImageView) view.findViewById(R.id.bookOpen);

        if (isClosed) {
            flip = new Flip3D(closedBook, openBook) {
                @Override
                protected void afterAnimationEnd() {

                }
            };
        } else {
            flip = new Flip3D(openBook, closedBook) {
                @Override
                protected void afterAnimationEnd() {

                }
            };
        }


        return view;
    }


}
