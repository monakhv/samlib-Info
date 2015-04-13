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
import monakhv.android.samlib.data.SettingsHelper;
import monakhv.android.samlib.dialogs.EnterStringDialog;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import monakhv.android.samlib.sql.AuthorController;
import monakhv.android.samlib.sql.AuthorProvider;

import monakhv.android.samlib.sql.TagController;
import monakhv.samlib.db.SQLController;
import monakhv.samlib.db.entity.Author;
import monakhv.samlib.db.entity.Tag;

/**
 *
 * @author monakhv
 */
public class AuthorTagsActivity extends ActionBarActivity implements AuthorTagFragment.AuthorTagCallback {

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
        AuthorController sql = new AuthorController(this);
        Author a = sql.getById(authorTagFragment.getAuthor_id());

        if (a.getTags_name().isEmpty()){
            getSupportActionBar().setTitle(a.getName() + ": NO TAGS" );
        }
        else {
            getSupportActionBar().setTitle(a.getName() + ": " +authorTagFragment. join(a.getTags_name(), ", "));
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
    public void onFinish(long id) {
        Intent intent=new Intent(getApplicationContext(),BooksActivity.class);
        intent.putExtra(BookFragment.AUTHOR_ID, id);
        setResult(RESULT_OK, intent);
        finish();

    }
}
