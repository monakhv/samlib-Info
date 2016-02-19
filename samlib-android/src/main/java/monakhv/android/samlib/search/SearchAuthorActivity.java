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
package monakhv.android.samlib.search;


import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;


import monakhv.android.samlib.MyBaseAbstractActivity;
import monakhv.android.samlib.R;
import monakhv.samlib.log.Log;


/**
 * @author Dmitry Monakhov
 */
public class SearchAuthorActivity extends MyBaseAbstractActivity {

    static private final String DEBUG_TAG = "SearchAuthorActivity";
    public static final String EXTRA_PATTERN = "EXTRA_PATTERN";
    private SearchAuthorsFragment mSearchAuthorsFragment;
    private View mSearchPanel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.search_authors);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        EditText editText = (EditText) findViewById(R.id.searchAuthorText_sa);
        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchAuthor(v);
                return true;
            }
            return false;
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        mSearchAuthorsFragment = (SearchAuthorsFragment) getSupportFragmentManager().findFragmentById(R.id.listAuthorSearchFragment);
        mSearchPanel = findViewById(R.id.search_author_panel_sa);
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    private final int id_menu_search = 21;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(1, id_menu_search, 1, getString(R.string.menu_search));
        menu.findItem(id_menu_search).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menu.findItem(id_menu_search).setIcon(R.drawable.action_search);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int sel = item.getItemId();
        if (sel == id_menu_search) {
            flipPanel();

        }
        if (sel == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void flipPanel() {
        if (mSearchPanel.getVisibility() == View.GONE) {
            mSearchPanel.setVisibility(View.VISIBLE);
        } else {
            mSearchPanel.setVisibility(View.GONE);
        }
    }

    public void searchAuthor(@SuppressWarnings("UnusedParameters") View view) {
        EditText editText = (EditText) findViewById(R.id.searchAuthorText_sa);
        if (editText == null) {
            return;
        }
        if (editText.getText() == null) {
            return;
        }
        String text = editText.getText().toString();

        editText.setText("");
        flipPanel();
        Log.d(DEBUG_TAG, "making search");
        mSearchAuthorsFragment.search(text);
    }


}
