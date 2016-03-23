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
 *  04.03.16 12:31
 *
 */

package monakhv.android.samlib;

import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.RecyclerView;
import android.test.suitebuilder.annotation.LargeTest;
import android.widget.TextView;
import monakhv.android.samlib.adapter.BookViewHolder;
import monakhv.android.samlib.sortorder.BookSortOrder;
import monakhv.samlib.db.AuthorController;
import monakhv.samlib.db.entity.Author;
import monakhv.samlib.db.entity.GroupBook;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * GUI test for Book fragment
 * Created by monakhv on 04.03.16.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class BookGuiTest {
    private static final String AUTHOR_URL="/a/abwow_a_s/";

    private static final long SLEEP_TIME=1000;


    @Rule
    public BookActivityTestRule mBookActivityTestRule=new BookActivityTestRule(BooksActivity.class);

    /**
     * Test set/clean mark read for Books and groups
     */
    @Test
    public void testMarkReadSetClean() {

        AuthorController sql=mBookActivityTestRule.getActivity().getAuthorController();
        Author author=sql.getByUrl(AUTHOR_URL);


        mBookActivityTestRule.getActivity().runOnUiThread(() -> mBookActivityTestRule.getActivity().mBookFragment.setAuthorId(author.getId()));

        sleep(SLEEP_TIME);
        int i =10;

        GroupBook groupBook=sql.getGroupBookController().getByAuthor(author).get(0);

        sql.getBookController().getBookForGroup(groupBook, BookSortOrder.valueOf(mBookActivityTestRule.getActivity().getSettingsHelper().getBookSortOrderString()).getOrder());

        int size =groupBook.getBooks().size();

        List<Integer> books = new ArrayList<>();
        books.add(groupBook.getBooks().get(size-1).getId());
        books.add(groupBook.getBooks().get(size-2).getId());
        books.add(groupBook.getBooks().get(size-3).getId());
        books.add(groupBook.getBooks().get(size-4).getId());

        //open first group of the books
        onView(withId(R.id.bookRV))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        //Scroll to book and make it unread
        while (i>0){
            for (int id : books){
                onView(withId(R.id.bookRV))
                        .perform(RecyclerViewActions.scrollToHolder(withHolderBookId(id)));

                onView(withId(R.id.bookRV))
                        .perform(RecyclerViewActions.actionOnHolderItem(withHolderBookId(id),MyViewAction.clickChildViewWithId(R.id.FlipContainer)));
                sleep(SLEEP_TIME);


            }

            onView(withId(R.id.bookRV))
                    .perform(RecyclerViewActions.scrollToPosition(0));//scroll to group again
            onView(withId(R.id.bookRV))
                    .perform(RecyclerViewActions.actionOnItemAtPosition(0, swipeRight()));//make group read by right swipe
            sleep(SLEEP_TIME);
            --i;

        }




    }


    private void sleep(long tt){
        try {
            Thread.sleep(tt);
        } catch (InterruptedException e) {

            e.printStackTrace();
        }
    }
    public static Matcher<RecyclerView.ViewHolder> withHolderBookId(final int id){
        return new BoundedMatcher<RecyclerView.ViewHolder, BookViewHolder>(BookViewHolder.class) {

            @Override
            public void describeTo(Description description) {
                description.appendText("No ViewHolder found for id " + id);
            }

            @Override
            protected boolean matchesSafely(BookViewHolder bookViewHolder) {
                TextView tv= (TextView) bookViewHolder.itemView.findViewById(R.id.bookTitle);
                if (tv == null){
                    return false;
                }

                int book_id = (int) tv.getTag();
                return book_id == id ;
            }
        };
    }
}
