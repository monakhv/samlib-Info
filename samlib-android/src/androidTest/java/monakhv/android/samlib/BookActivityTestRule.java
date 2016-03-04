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
 *  04.03.16 12:25
 *
 */

package monakhv.android.samlib;

import android.content.Intent;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.rule.ActivityTestRule;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;
import monakhv.android.samlib.adapter.BookViewHolder;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

/**
 * Created by monakhv on 04.03.16.
 */
public class BookActivityTestRule extends ActivityTestRule<BooksActivity> {
    private long author_id;
    public BookActivityTestRule(Class<BooksActivity> activityClass,int id) {
        super(activityClass);
        author_id=id;
    }

    @Override
    protected Intent getActivityIntent() {

        Intent intent= super.getActivityIntent();
        intent.putExtra(BookFragment.AUTHOR_ID, author_id);
        return intent;
    }

}
