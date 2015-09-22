package monakhv.android.samlib.adapter;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;
import monakhv.android.samlib.sql.DatabaseHelper;
import monakhv.samlib.db.AuthorController;
import monakhv.samlib.db.entity.Author;
import monakhv.samlib.db.entity.Book;
import monakhv.samlib.db.entity.SamLibConfig;

import java.util.List;

/*
 * Copyright 2015  Dmitry Monakhov
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
 * 23.07.15.
 */
public class BookLoader extends AsyncTaskLoader<List<Book>> {
    private final String DEBUG_TAG="BookLoader";
    private List<Book> mBooks;
    private final AuthorController authorController;
    private long id;
    private String order;

    public BookLoader(final Context context,final DatabaseHelper databaseHelper,long id, String order) {
        super(context);
        this.id = id;
        this.order = order;
        authorController=new AuthorController(databaseHelper);
    }

    @Override
    public List<Book> loadInBackground() {
        if (id == SamLibConfig.SELECTED_BOOK_ID){
            return authorController.getBookController().getSelected(order);
        }
        else {
            Author a= authorController.getById(id);

            if (a == null){
                Log.e(DEBUG_TAG, "loadInBackground: author is not defined");
                return null;
            }
            return authorController.getBookController().getAll(a,order);
        }
    }

    @Override
    protected void onStartLoading() {
        if (mBooks != null){
            deliverResult(mBooks);
        }
        else {
            forceLoad();
        }
    }

    @Override
    protected void onReset() {
        if (mBooks != null){
            mBooks.clear();
            mBooks=null;
        }
    }
}
