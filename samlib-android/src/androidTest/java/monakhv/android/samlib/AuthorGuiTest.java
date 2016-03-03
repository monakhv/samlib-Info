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
 *  03.03.16 12:24
 *
 */

package monakhv.android.samlib;

import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.RecyclerView;
import android.test.suitebuilder.annotation.LargeTest;
import android.widget.TextView;
import monakhv.android.samlib.adapter.BookViewHolder;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by monakhv on 03.03.16.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AuthorGuiTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);


    @Test
    public void testRecycleClick() {

        //got to the books of the first author
        onView(withId(R.id.authorRV))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        //open first group of the books
        onView(withId(R.id.bookRV))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));


        onView(withId(R.id.bookRV))
                .perform(RecyclerViewActions.actionOnHolderItem(withHolderBookTitle(""),click()));



    }

    public static Matcher<RecyclerView.ViewHolder> withHolderBookTitle(final String title){
        return new BoundedMatcher<RecyclerView.ViewHolder, BookViewHolder>(BookViewHolder.class) {

            @Override
            public void describeTo(Description description) {
                description.appendText("No ViewHolder found with text: " + title);
            }

            @Override
            protected boolean matchesSafely(BookViewHolder bookViewHolder) {
                TextView tv= (TextView) bookViewHolder.itemView.findViewById(R.id.bookTitle);
                if (tv == null){
                    return false;
                }

                return tv.getText().toString().contains(title);
            }
        };
    }

}
