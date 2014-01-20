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
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import java.io.Serializable;
import java.util.List;

import monakhv.android.samlib.R;
import monakhv.android.samlib.sql.entity.AuthorCard;

/**
 *
 * @author Dmitry Monakhov
 */
public class SearchAuthorActivity extends SherlockFragmentActivity {
    
    static private final String DEBUG_TAG = "SearchAuthorActivity";
    public static final String EXTRA_PATTERN = "EXTRA_PATTERN";
    private SearchReceiver receiver;
    private SearchAuthorsListFragment listFragment;
    private View searchPannel;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.search_authors);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        receiver = new SearchReceiver();
        IntentFilter filter = new IntentFilter(SearchReceiver.ACTION_RESP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(receiver, filter);
        listFragment = (SearchAuthorsListFragment) getSupportFragmentManager().findFragmentById(R.id.listAuthorSearchFragment);
        searchPannel = findViewById(R.id.search_author_panel_sa);
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
        if (searchPannel.getVisibility() == View.GONE) {
            searchPannel.setVisibility(View.VISIBLE);
        } else {
            searchPannel.setVisibility(View.GONE);
        }
    }
    
    public void searchAuthor(@SuppressWarnings("UnusedParameters") View view) {
        EditText editText = (EditText) findViewById(R.id.searchAuthorText_sa);
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
            
            if (listFragment != null) {
                Serializable ss = intent.getSerializableExtra(EXTRA_RESULT);
                Log.d(DEBUG_TAG, "Send result to list");
                listFragment.setResult((List<AuthorCard>) ss);
            } else {
                Log.e(DEBUG_TAG, "ListView is NULL");
            }
            String msg = intent.getStringExtra(EXTRA_MESSAGE);
            
            if (msg != null) {
                Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
                toast.show();
            }
            
        }
        
    }
    
}
