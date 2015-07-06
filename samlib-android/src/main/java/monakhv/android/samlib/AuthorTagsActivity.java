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
import android.support.v7.widget.Toolbar;
import android.view.*;
import monakhv.android.samlib.data.SettingsHelper;
import android.os.Bundle;



import monakhv.samlib.db.AuthorController;
import monakhv.samlib.db.entity.Author;

/**
 *
 * @author monakhv
 */
public class AuthorTagsActivity extends MyBaseAbstractActivity implements AuthorTagFragment.AuthorTagCallback {

    public static final String AUTHOR_ID = "TAGS_AUTHOR_ID";


    private SettingsHelper helper;
    private AuthorTagFragment authorTagFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        helper = new SettingsHelper(this);
        setTheme(helper.getTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.author_tags);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        authorTagFragment = (AuthorTagFragment) getSupportFragmentManager().findFragmentById(R.id.tagsFragment);

    }


    @Override
    protected void onResume() {
        super.onResume();
        AuthorController sql = new AuthorController(getDatabaseHelper());
        Author a = sql.getById(authorTagFragment.getAuthor_id());

        if (a.getTag2Authors().isEmpty()){
            getSupportActionBar().setTitle(a.getName() + ": NO TAGS" );
        }
        else {
            getSupportActionBar().setTitle(a.getName() + ": " +authorTagFragment. join(a.getTag2Authors(), ", "));
        }




    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.tags_menu, menu);

        return super.onCreateOptionsMenu(menu);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int sel = item.getItemId();
        if (sel == android.R.id.home ){
            onFinish(authorTagFragment.getAuthor_id());
            return true;
        }

        if (sel == R.id.add_option_item) {
            authorTagFragment.panelFlip();
            return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) { //Back key pressed
            onFinish(authorTagFragment.getAuthor_id());
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }



    @Override
    public void onFinish(long id) {
        Intent intent=new Intent(getApplicationContext(),BooksActivity.class);
        intent.putExtra(BookFragment.AUTHOR_ID, id);
        setResult(RESULT_OK, intent);
        finish();

    }
}
