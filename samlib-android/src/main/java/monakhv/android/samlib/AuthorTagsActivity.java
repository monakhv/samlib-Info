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
public class AuthorTagsActivity extends ActionBarActivity {

    public static final String AUTHOR_ID = "TAGS_AUTHOR_ID";
    private static final String DEBUG_TAG = "AuthorTagsActivity";
    private int author_id;
    private boolean addVisible = false;
    private SimpleCursorAdapter adapter;
    private SettingsHelper helper;

    /**
     * Find listView object using its id
     *
     * @return ListView with tags
     */
    private ListView getListView() {
        return (ListView) findViewById(R.id.listTags);
    }

    /**
     * Reread tags from the cursor
     */
    private void refreshList() {
        adapter.swapCursor(getCursor());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        helper = new SettingsHelper(this);
        setTheme(helper.getTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.author_tags);
        author_id = getIntent().getExtras().getInt(AuthorTagsActivity.AUTHOR_ID);
        ListView listView = getListView();

        String[] from = {SQLController.COL_TAG_NAME};
        int[] to = {android.R.id.text1};

        Cursor attributesCursor = getCursor();

        adapter = new SimpleCursorAdapter(
               this, android.R.layout.simple_list_item_multiple_choice,
                attributesCursor, from, to,
                0);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setItemsCanFocus(false);
        loadTagData();
        registerForContextMenu(listView);


    }
    private int delete_menu_id = 1;
    private int edit_menu_id = 2;
    private Cursor cursor = null;

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        cursor = null;
        if (v.getId() == R.id.listTags) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

            cursor = (Cursor) adapter.getItem(info.position);
            menu.add(1, delete_menu_id, 2, getText(R.string.menu_delete));
            menu.add(1, edit_menu_id, 1, getText(R.string.menu_edit));
        }
    }

    private Dialog tagRemoveAlert() {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle(R.string.Attention);

        String tagName = cursor.getString(cursor.getColumnIndex(SQLController.COL_TAG_NAME));
        String msg = getString(R.string.alert_tag_delete);
        msg = msg.replaceAll("__", tagName);

        adb.setMessage(msg);
        adb.setIcon(android.R.drawable.ic_dialog_alert);
        adb.setPositiveButton(R.string.Yes, deleteTagListener);
        adb.setNegativeButton(R.string.No, deleteTagListener);
        return adb.create();

    }
    private DialogInterface.OnClickListener deleteTagListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case Dialog.BUTTON_POSITIVE:
                    TagController sql = new TagController(getApplicationContext());
                    if (cursor != null) {
                        sql.delete(cursor.getInt(cursor.getColumnIndex(SQLController.COL_ID)));
                        cursor = null;
                        refreshList();
                    }

                    break;
                case Dialog.BUTTON_NEGATIVE:
                    break;

            }

        }
    };

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if (item.getItemId() == delete_menu_id) {
            Dialog alert = tagRemoveAlert();
            alert.show();
        }
        if (item.getItemId() == edit_menu_id && cursor != null) {
            final TagController sql = new TagController(this);
            final Tag tag = sql.getById(cursor.getInt(cursor.getColumnIndex(SQLController.COL_ID)));


            EnterStringDialog dialog = new EnterStringDialog(this, new EnterStringDialog.ClickListener() {
                public void okClick(String txt) {
                    tag.setName(txt);
                    sql.update(tag);
                    refreshList();
                }
            },getText(R.string.tag_edit_title).toString(),tag.getName());
            dialog.show();
        }
        return super.onContextItemSelected(item);
    }

    /**
     * Get selected TAGs to display in the ListView
     *
     * @return Cursor
     */
    private Cursor getCursor() {
        return getApplicationContext().getContentResolver().query(AuthorProvider.TAG_URI, null, null, null, SQLController.COL_TAG_NAME);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AuthorController sql = new AuthorController(this);
        Author a = sql.getById(author_id);
        TextView tv = (TextView) this.findViewById(R.id.tagAuthorTitle);
        tv.setText(a.getName());

        tv = (TextView) this.findViewById(R.id.tagTagNAme);
        tv.setText(join(a.getTags_name(), ", "));

    }

    public static String join(Collection<?> col, String deliminator) {
        StringBuilder sb = new StringBuilder();
        Iterator<?> iterator = col.iterator();
        if (iterator.hasNext()) {
            sb.append(iterator.next().toString());
        }
        while (iterator.hasNext()) {
            sb.append(deliminator);
            sb.append(iterator.next().toString());
        }
        return sb.toString();
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

        if (sel == R.id.add_option_item) {
            addVisible = !addVisible;
            View v = findViewById(R.id.add_tag_panel);

            if (addVisible) {
                v.setVisibility(View.VISIBLE);
            } else {
                v.setVisibility(View.GONE);
            }

        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Add new Tag into DB
     *
     * @param view view
     */
    public void addTag(View view) {
        EditText editText = (EditText) findViewById(R.id.addTagText);
        if (editText == null) {
            Log.e(DEBUG_TAG, "add tag textEdit not found!");
            return;
        }
        String text = editText.getText().toString();
        if (text == null) {
            Log.e(DEBUG_TAG, "add text is null");
            return;
        }
        if (text.equalsIgnoreCase("")) {
            Log.i(DEBUG_TAG, "can not add empty tag");
            return;
        }

        Log.i(DEBUG_TAG, "adding tag: " + text + " ...");

        TagController sql = new TagController(getApplicationContext());
        Tag tag = new Tag(text);
        sql.insert(tag);

        addVisible = !addVisible;

        editText.setText("");
        View v = findViewById(R.id.add_tag_panel);
        v.setVisibility(View.GONE);
        refreshList();

    }

    /**
     * User press cancel button
     *
     * @param view view
     */
    public void cancelClick(View view) {
        finish();

    }

    /**
     * User pre Ok button
     *
     * @param view View
     */
    public void okClick(View view) {

        SparseBooleanArray checked = getListView().getCheckedItemPositions();
        List<Integer> tags = new ArrayList<Integer>();
        for (int i = 0; i < checked.size(); i++) {
            if (checked.valueAt(i)) {
                Object o = getListView().getItemAtPosition(checked.keyAt(i));
                Cursor cur = (Cursor) o;//selected cursors
                Log.i(DEBUG_TAG, "selected: " + cur.getString(cur.getColumnIndex(SQLController.COL_TAG_NAME)));
                tags.add(cur.getInt(cur.getColumnIndex(SQLController.COL_ID)));
            }
        }
        AuthorController sql = new AuthorController(this);
        Author a = sql.getById(author_id);
        sql.syncTags(a, tags);
        helper.requestBackup();
        finish();
    }

    private void loadTagData() {
        int size = getListView().getAdapter().getCount();
        AuthorController sql = new AuthorController(this);
        Author a = sql.getById(author_id);
        for (int i = 0; i < size; i++) {
            Cursor cur = (Cursor) getListView().getAdapter().getItem(i);
            int tag_id = cur.getInt(cur.getColumnIndex(SQLController.COL_ID));

            if (a.getTags_id().contains(tag_id)) {
                getListView().setItemChecked(i, true);
            }
        }

    }
}
