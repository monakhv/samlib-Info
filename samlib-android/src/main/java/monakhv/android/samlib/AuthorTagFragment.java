package monakhv.android.samlib;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.*;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import monakhv.android.samlib.data.SettingsHelper;
import monakhv.android.samlib.dialogs.EnterStringDialog;
import monakhv.android.samlib.sql.AuthorController;
import monakhv.android.samlib.sql.AuthorProvider;
import monakhv.android.samlib.sql.TagController;
import monakhv.samlib.db.SQLController;
import monakhv.samlib.db.entity.Author;
import monakhv.samlib.db.entity.Tag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/*
 * Copyright 2015  Dmitry Monakhov
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
 *
 * 4/9/15.
 */
public class AuthorTagFragment extends Fragment {
    public interface AuthorTagCallback{
        public void onFinish(long id);
    }
    private static final String DEBUG_TAG = "AuthorTagFragment";
    private long author_id=0;
    private SimpleCursorAdapter adapter;
    private SettingsHelper helper;
    private AuthorTagCallback callBack;

    private boolean addVisible = false;
    private ListView listView;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        helper = new SettingsHelper(getActivity());
        Bundle extra =getActivity(). getIntent().getExtras();
        if (extra != null){
            author_id =extra.getLong(AuthorTagsActivity.AUTHOR_ID);
        }



        String[] from = {SQLController.COL_TAG_NAME};
        int[] to = {android.R.id.text1};

        Cursor attributesCursor = getCursor();

        adapter = new SimpleCursorAdapter(
                getActivity(), android.R.layout.simple_list_item_multiple_choice,
                attributesCursor, from, to,
                0);


    }
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tags_fargment, container, false);
        Log.i(DEBUG_TAG, "Done making view");

        listView = (ListView) view.findViewById(R.id.listTags);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setItemsCanFocus(false);
        loadTagData();
        registerForContextMenu(listView);

        Button bt;
        bt = (Button)view.findViewById(R.id.addTagBt);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTag();
            }
        });
        bt = (Button) view.findViewById(R.id.cancelTags);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelClick();
            }
        });
        bt = (Button) view.findViewById(R.id.okTags);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                okClick();
            }
        });



        return view;
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof AuthorTagCallback)) {
            throw new IllegalStateException(
                    "Activity must implement fragment's callbacks.");
        }
        callBack = (AuthorTagCallback) activity;
    }


    public long getAuthor_id() {
        return author_id;
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
        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
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
                    TagController sql = new TagController(getActivity().getApplicationContext());
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
            final TagController sql = new TagController(getActivity());
            final Tag tag = sql.getById(cursor.getInt(cursor.getColumnIndex(SQLController.COL_ID)));


            EnterStringDialog dialog = new EnterStringDialog(getActivity(), new EnterStringDialog.ClickListener() {
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
     * User pre Ok button
     *
     */
    public void okClick() {

        SparseBooleanArray checked = listView.getCheckedItemPositions();
        List<Integer> tags = new ArrayList<Integer>();
        for (int i = 0; i < checked.size(); i++) {
            if (checked.valueAt(i)) {
                Object o = listView.getItemAtPosition(checked.keyAt(i));
                Cursor cur = (Cursor) o;//selected cursors
                Log.i(DEBUG_TAG, "selected: " + cur.getString(cur.getColumnIndex(SQLController.COL_TAG_NAME)));
                tags.add(cur.getInt(cur.getColumnIndex(SQLController.COL_ID)));
            }
        }
        AuthorController sql = new AuthorController(getActivity());
        Author a = sql.getById(author_id);
        sql.syncTags(a, tags);
        helper.requestBackup();
        cancelClick();
    }

    public void setAuthor_id(long author_id) {
        this.author_id = author_id;
    }

    /**
     * Get selected TAGs to display in the ListView
     *
     * @return Cursor
     */
    private Cursor getCursor() {
        return getActivity().getApplicationContext().getContentResolver().query(AuthorProvider.TAG_URI, null, null, null, SQLController.COL_TAG_NAME);
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
    /**
     * Add new Tag into DB
     *
     *
     */
    public void addTag() {
        EditText editText = (EditText) getActivity().findViewById(R.id.addTagText);
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

        TagController sql = new TagController(getActivity().getApplicationContext());
        Tag tag = new Tag(text);
        sql.insert(tag);

        addVisible = !addVisible;

        editText.setText("");
        View v =getActivity(). findViewById(R.id.add_tag_panel);
        v.setVisibility(View.GONE);
        refreshList();

    }
    /**
     * Reread tags from the cursor
     */
    private void refreshList() {
        adapter.swapCursor(getCursor());
    }

    /**
     * Mark selected tags for the author
     */
    void loadTagData() {
        int size =listView.getAdapter().getCount();
        AuthorController sql = new AuthorController(getActivity());
        Author a = sql.getById(author_id);
        if (a==null){
            return;
        }
        for (int i = 0; i < size; i++) {
            Cursor cur = (Cursor) listView.getAdapter().getItem(i);
            int tag_id = cur.getInt(cur.getColumnIndex(SQLController.COL_ID));

            listView.setItemChecked(i, a.getTags_id().contains(tag_id));

        }

    }


    /**
     * User press cancel button
     *
     */
    public void cancelClick() {
        callBack.onFinish(author_id);

    }

    public void panelFlip(){
        addVisible = !addVisible;
        View v = getActivity().findViewById(R.id.add_tag_panel);

        if (addVisible) {
            v.setVisibility(View.VISIBLE);
        } else {
            v.setVisibility(View.GONE);
        }
    }


}
