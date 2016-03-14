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
import monakhv.android.samlib.adapter.AuthorAdapter;
import monakhv.samlib.db.AuthorController;
import monakhv.samlib.db.SQLController;
import monakhv.samlib.db.entity.Author;
import monakhv.samlib.db.entity.Book;
import monakhv.samlib.db.entity.SamLibConfig;
import monakhv.samlib.service.AuthorGuiState;
import monakhv.samlib.service.BookGuiState;
import monakhv.samlib.service.SamlibOperation;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.matcher.ViewMatchers.withId;


/**
 * GUI tests for Author Fragment
 * Created by monakhv on 03.03.16.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AuthorGuiTest {
    public static final int [] AUTHOR_ID={13,141,36};
    public static final long SLEEP_TIME=3000;
    public static final AuthorGuiState authorGuiState = new AuthorGuiState(SamLibConfig.TAG_AUTHOR_ALL, SQLController.COL_isnew + " DESC, " + SQLController.COL_NAME);
    public static String bookOrder = SQLController.COL_BOOK_ISNEW + " DESC, " + SQLController.COL_BOOK_DATE + " DESC";

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);


    /**
     * Test clean mark read for the authors by right swipe
     */
    @Test
    public void testMarkRead() {

        final SamlibOperation samlibOperation=mActivityTestRule.getActivity().getSamlibOperation();
        final AuthorController sql = mActivityTestRule.getActivity().getAuthorController();

        /**
         * Set unread mark
         */
        for (int id: AUTHOR_ID){
            Author author =sql.getById(id);
            Book book=sql.getBookController().getAll(author,null).get(0);
            samlibOperation.makeBookReadFlip(book,new BookGuiState(id,bookOrder),authorGuiState);

        }

        sleep(SLEEP_TIME);

        /**
         * Clean mark unread by right swipe
         */
        for (int id: AUTHOR_ID){
            onView(withId(R.id.authorRV))
                    .perform(RecyclerViewActions.actionOnHolderItem(withHolderAuthorId(id),swipeRight()));
            sleep(SLEEP_TIME);
        }

    }



    public static Matcher<RecyclerView.ViewHolder> withHolderAuthorId(final int id){
        return new BoundedMatcher<RecyclerView.ViewHolder, AuthorAdapter.AuthorViewHolder>(AuthorAdapter.AuthorViewHolder.class) {

            @Override
            public void describeTo(Description description) {
                description.appendText("No ViewHolder found for id " + id);
            }

            @Override
            protected boolean matchesSafely(AuthorAdapter.AuthorViewHolder holder) {
                TextView tv= (TextView) holder.itemView.findViewById(R.id.authorName);
                if (tv == null){
                    return false;
                }

                int book_id = (int) tv.getTag();
                return book_id == id ;
            }
        };
    }

    private void sleep(long tt){
        try {
            Thread.sleep(tt);
        } catch (InterruptedException e) {

            e.printStackTrace();
        }
    }

}
