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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;



import java.io.Serializable;
import java.util.List;


import monakhv.android.samlib.R;
import monakhv.android.samlib.data.SettingsHelper;
import monakhv.samlib.db.entity.AuthorCard;

/**
 *
 * @author Dmitry Monakhov
 */
public class SearchAuthorActivity extends ActionBarActivity {
    
    static private final String DEBUG_TAG = "SearchAuthorActivity";
    public static final String EXTRA_PATTERN = "EXTRA_PATTERN";
    private SearchReceiver receiver;
    private SearchAuthorsListFragment listFragment;
    private View searchPanel;
    private SettingsHelper settings;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        settings = new SettingsHelper(this);
        setTheme(settings.getTheme());
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.search_authors);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        EditText editText = (EditText) findViewById(R.id.searchAuthorText_sa);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH){
                    searchAuthor(v);
                    return true;
                }
                return false;
            }
        });

    }
    
    @Override
    protected void onResume() {
        super.onResume();
        receiver = new SearchReceiver();
        IntentFilter filter = new IntentFilter(SearchReceiver.ACTION_RESP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(receiver, filter);
        listFragment = (SearchAuthorsListFragment) getSupportFragmentManager().findFragmentById(R.id.listAuthorSearchFragment);
        searchPanel = findViewById(R.id.search_author_panel_sa);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
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
            flipPannel();
            
        }
        if (sel == android.R.id.home ){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void flipPannel() {
        if (searchPanel.getVisibility() == View.GONE) {
            searchPanel.setVisibility(View.VISIBLE);
        } else {
            searchPanel.setVisibility(View.GONE);
        }
    }
    
    public void searchAuthor(@SuppressWarnings("UnusedParameters") View view) {
        EditText editText = (EditText) findViewById(R.id.searchAuthorText_sa);
        if (editText == null){
            return;
        }
        if (editText.getText() == null){
            return;
        }
        String text = editText.getText().toString();
        
        editText.setText("");
        flipPannel();
        listFragment.search(text);
    }
    
    public class SearchReceiver extends BroadcastReceiver {
        
        public static final String ACTION_RESP = "monakhv.android.samlib.SearchReceiver";
        public static final String EXTRA_MESSAGE = "MESSAGE";
        public static final String EXTRA_RESULT = "EXTRA_RESULT";
        
        @Override
        public void onReceive(Context context, Intent intent) {
            String msg = intent.getStringExtra(EXTRA_MESSAGE);

            if (msg != null) {
                Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
                toast.show();
            }
            if (listFragment != null) {
                Serializable ss = intent.getSerializableExtra(EXTRA_RESULT);
                Log.d(DEBUG_TAG, "Send result to list");
                List<AuthorCard> rr = (List<AuthorCard>) ss;
                listFragment.setResult(rr);
                if ( rr.size()==0){
                    Log.d(DEBUG_TAG, "Try to close Activity");
                    SearchAuthorActivity.this.finish();
                }

            } else {
                Log.e(DEBUG_TAG, "ListView is NULL");
            }

            
        }
        
    }
    
}
