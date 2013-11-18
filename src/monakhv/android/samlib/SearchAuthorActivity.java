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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import monakhv.android.samlib.tasks.SearchAuthor;

/**
 *
 * @author Dmitry Monakhov
 */
public class SearchAuthorActivity extends FragmentActivity {

    public static final String EXTRA_PATTERN = "EXTRA_PATTERN";
    private SearchReceiver receiver;

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
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }
    
    private final int id_menu_search = 21;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(1, id_menu_search, 1, "Search");
        menu.findItem(id_menu_search).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menu.findItem(id_menu_search).setIcon(R.drawable.action_search);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int sel = item.getItemId();
        if (sel == id_menu_search){
             View v = findViewById(R.id.search_author_panel_sa);
             if (v.getVisibility() == View.GONE){
                 v.setVisibility(View.VISIBLE);
             }
             else {
                 v.setVisibility(View.GONE);
             }
        }
        return super.onOptionsItemSelected(item);
    }
    public void searchAuthor(View view) {
        EditText editText = (EditText) findViewById(R.id.searchAuthorText_sa);
        String text = editText.getText().toString();
        SearchAuthorsListFragment listFragment = (SearchAuthorsListFragment) getSupportFragmentManager().findFragmentById(R.id.listAuthorSearchFragment);
       
        listFragment.search(text);
    }
    
     public class SearchReceiver extends BroadcastReceiver {
        public static final String ACTION_RESP="monakhv.android.samlib.SearchReceiver";
        public static final String MESSAGE="MESSAGE";

        @Override
        public void onReceive(Context context, Intent intent) {
            SearchAuthorsListFragment listFragment = (SearchAuthorsListFragment) getSupportFragmentManager().findFragmentById(R.id.listAuthorSearchFragment);
            if (listFragment != null) {
                if (listFragment.progress != null) {
                    listFragment.progress.dismiss();
                }
            }
            String msg = intent.getStringExtra(MESSAGE);
            if (msg != null){
                Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
                toast.show();
            }
            
        }
         
     }

}
