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


import android.content.Intent;
import android.os.Bundle;


import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import monakhv.samlib.service.AuthorGuiState;
import monakhv.samlib.db.AuthorController;
import monakhv.samlib.db.entity.Author;
import monakhv.samlib.db.entity.SamLibConfig;
import monakhv.samlib.log.Log;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * @author monakhv
 */
public class BooksActivity extends MyAbstractAnimActivity implements BookFragment.Callbacks {
    private static final String DEBUG_TAG = "BooksActivity";
    private static final int TAGS_ACTIVITY = 21;
    private long author_id = 0;
    private BookFragment mBookFragment;
    private Subscription mBookSubscription;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(DEBUG_TAG, "onCreate");
        if (savedInstanceState == null) {
            Log.i(DEBUG_TAG, "onCreate: have NO save data");
        } else {
            Log.i(DEBUG_TAG, "onCreate: We have saved data!!!");
        }


        setContentView(R.layout.books);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        if(actionBar !=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extra = getIntent().getExtras();
        if (extra != null) {
            Log.i(DEBUG_TAG, "Load intent data!!!");
            author_id = extra.getLong(BookFragment.AUTHOR_ID);
        } else {
            Log.i(DEBUG_TAG, "Have NO intent data");
        }

        mBookFragment = (BookFragment) getSupportFragmentManager().findFragmentById(R.id.listBooksFragment);
        mBookFragment.setHasOptionsMenu(true);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(DEBUG_TAG, "onActivityResult");
        if (requestCode == TAGS_ACTIVITY) {
            Log.i(DEBUG_TAG, "onActivityResult - TagsActivity");
            author_id = data.getLongExtra(BookFragment.AUTHOR_ID, 0);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        Log.d(DEBUG_TAG, "onSaveInstanceState call");
        bundle.putLong(BookFragment.AUTHOR_ID, author_id);
        bundle.putBundle(BookFragment.ADAPTER_STATE_EXTRA, mBookFragment.getAdapterState());
        super.onSaveInstanceState(bundle);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(DEBUG_TAG, "onRestoreInstanceState call");
        Bundle state = savedInstanceState.getBundle(BookFragment.ADAPTER_STATE_EXTRA);
        if (state != null){
            mBookFragment.setAdapterState(state);
        }
    }

    @Override
    public void showTags(long author_id) {
        Intent intent = new Intent(this, AuthorTagsActivity.class);
        intent.putExtra(AuthorTagsActivity.AUTHOR_ID, author_id);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivityForResult(intent, TAGS_ACTIVITY);

    }


    @Override
    protected void onResume() {
        super.onResume();







        if (author_id != SamLibConfig.SELECTED_BOOK_ID) {
            AuthorController sql = getAuthorController();
            Author a = sql.getById(author_id);
            if (a != null) {
                setTitle(a.getName());
            }

        } else {
            setTitle(getText(R.string.menu_selected_go));
        }

        mBookSubscription = getBus().getObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(mBookFragment.mSubscriber);
        addSubscription(mBookSubscription);

    }

    @Override
    protected void onPause() {
        super.onPause();
        mBookSubscription.unsubscribe();
    }



    @Override
    public AuthorGuiState getAuthorGuiState() {
        return null;
    }
}
