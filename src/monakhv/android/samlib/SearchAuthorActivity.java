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

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import monakhv.android.samlib.tasks.SearchAuthor;

/**
 *
 * @author Dmitry Monakhov
 */
public class SearchAuthorActivity extends FragmentActivity {

    public static final String EXTRA_PATTERN = "EXTRA_PATTERN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.search_authors);
    }
    private final int id_menu_search = 21;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(1, id_menu_search, 1, "Search");
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
        SearchAuthor searcher = new SearchAuthor(this);
        searcher.execute(text);
        
    }

}
