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
import android.support.test.rule.ActivityTestRule;
import monakhv.samlib.db.entity.SamLibConfig;

/**
 * Test Rule for Book activity
 * Created by monakhv on 04.03.16.
 */
class BookActivityTestRule extends ActivityTestRule<BooksActivity> {

    BookActivityTestRule(Class<BooksActivity> activityClass) {
        super(activityClass);


    }

    @Override
    protected Intent getActivityIntent() {
        Intent intent= super.getActivityIntent();
        intent.putExtra(BookFragment.AUTHOR_ID, SamLibConfig.SELECTED_BOOK_ID);
        return intent;
    }

}
