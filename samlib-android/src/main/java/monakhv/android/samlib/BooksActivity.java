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
import android.content.IntentFilter;
import android.os.Bundle;


import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import monakhv.android.samlib.sql.AuthorController;
import monakhv.samlib.db.entity.Author;
import monakhv.samlib.db.entity.SamLibConfig;
import monakhv.samlib.log.Log;

/**
 * @author monakhv
 */
public class BooksActivity extends MyAbstractActivity {
    private static final String DEBUG_TAG = "BooksActivity";
    private static final int TAGS_ACTIVITY = 21;
    private long author_id = 0;
    private DownloadReceiver receiver;
    private BookFragment listFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(DEBUG_TAG, "onCreate");
        if (savedInstanceState == null) {
            Log.i(DEBUG_TAG, "have NO save data");
        } else {
            Log.i(DEBUG_TAG, "We have saved data!!!");
        }


        setContentView(R.layout.books);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extra = getIntent().getExtras();
        if (extra != null) {
            Log.i(DEBUG_TAG, "Load intent data!!!");
            author_id = extra.getLong(BookFragment.AUTHOR_ID);
        } else {
            Log.i(DEBUG_TAG, "Have NO intent data");
        }

        listFragment = (BookFragment) getSupportFragmentManager().findFragmentById(R.id.listBooksFragment);

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
        super.onSaveInstanceState(bundle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.books_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int sel = item.getItemId();
        if (sel == R.id.menu_books_tags) {
            Intent intent = new Intent(this, AuthorTagsActivity.class);
            intent.putExtra(AuthorTagsActivity.AUTHOR_ID, (int) author_id);
            //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivityForResult(intent, TAGS_ACTIVITY);
        }
        if (sel == R.id.menu_books_sort) {
            listFragment.selectSortOrder();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {

        super.onOptionsMenuClosed(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();


        if (author_id != SamLibConfig.SELECTED_BOOK_ID) {
            AuthorController sql = new AuthorController(this);
            Author a = sql.getById(author_id);
            if (a != null) {
                setTitle(a.getName());
            }

        } else {
            setTitle(getText(R.string.menu_selected_go));
        }


        receiver = new DownloadReceiver(listFragment);
        IntentFilter filter = new IntentFilter(DownloadReceiver.ACTION_RESP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(receiver, filter);

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }


}
