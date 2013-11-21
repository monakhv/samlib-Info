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

import monakhv.android.samlib.search.SearchAuthorActivity;
import monakhv.android.samlib.dialogs.FilterSelectDialog;
import monakhv.android.samlib.dialogs.EnterStringDialog;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Toast;
import monakhv.android.samlib.PullToRefresh.OnRefreshListener;
import monakhv.android.samlib.actionbar.ActionBarActivity;
import monakhv.android.samlib.data.SettingsHelper;
import monakhv.android.samlib.service.CleanNotificationData;
import monakhv.android.samlib.service.UpdateServiceIntent;
import monakhv.android.samlib.sql.AuthorController;
import monakhv.android.samlib.sql.AuthorProvider;
import monakhv.android.samlib.sql.SQLController;
import monakhv.android.samlib.sql.entity.Author;
import monakhv.android.samlib.sql.entity.SamLibConfig;
import monakhv.android.samlib.tasks.AddAuthor;
import monakhv.android.samlib.tasks.DeleteAuthor;
import monakhv.android.samlib.tasks.MarkRead;

public class MainActivity extends ActionBarActivity {

    private static final String DEBUG_TAG = "MainActivity";
    public static String CLEAN_NOTIFICATION = "CLEAN_NOTIFICATION";
    final int ARCHIVE_ACTIVITY = 1;
    //AddAuthorDialog addAuthorDilog;
    private UpdateActivityReceiver receiver;
    private boolean refreshStatus = false;
    private FilterSelectDialog dialog;
    private PullToRefresh listView;
    private AuthorListHelper listHelper;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);
        setTitle(R.string.app_name);
        Bundle bundle = getIntent().getExtras();
        //CleanNotificationData.start(this);
        String clean = null;
        if (bundle != null) {
            clean = bundle.getString(CLEAN_NOTIFICATION);
        }
        if (clean != null) {
            CleanNotificationData.start(this);

        }
        
        SettingsHelper.addAuthenticator(this.getApplicationContext());
        getActionBarHelper().setRefreshActionItemState(refreshStatus);

        listView = (PullToRefresh) findViewById(R.id.listAuthirFragment);
        listHelper = new AuthorListHelper(this, listView);


        
        listView.setOnRefreshListener(new OnRefreshListener() {
            public void onRefresh() {
                makeUpdate();
            }
        });
        registerForContextMenu(listView.getListView());
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(UpdateActivityReceiver.ACTION_RESP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new UpdateActivityReceiver();
        getActionBarHelper().setRefreshActionItemState(refreshStatus);
        registerReceiver(receiver, filter);
        

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) { //Back key pressed
            if (listHelper.getSelection() != null) {
                refreshList(null);
            } else {
                finish();
            }

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void refreshList(String sel) {

        listHelper.refresh(sel);


    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
        //Stop refresh status
        listView.onRefreshComplete();
        refreshStatus = false;
        getActionBarHelper().setRefreshActionItemState(refreshStatus);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.options_menu, menu);

        return super.onCreateOptionsMenu(menu);


    }

    /**
     * Start service to check out update
     */
    private void makeUpdate() {
        refreshStatus = true;
        getActionBarHelper().setRefreshActionItemState(refreshStatus);
        Intent service = new Intent(this, UpdateServiceIntent.class);
        service.putExtra(UpdateServiceIntent.CALLER_TYPE, UpdateServiceIntent.CALLER_IS_ACTIVITY);
        service.putExtra(UpdateServiceIntent.SELECT_STRING, listHelper.getSelection());
        startService(service);
    }

    /**
     * Option menu select items
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int sel = item.getItemId();

        if (sel == R.id.menu_refresh) {
            makeUpdate();

        }
        if (sel == R.id.search_option_item){
            View v = findViewById(R.id.search_author_panel);
            if (v.getVisibility() == View.GONE){
               
                v.setVisibility(View.VISIBLE);
            }
            else {
                v.setVisibility(View.GONE);
            }
        }
        if (sel == R.id.add_option_item) {
            View v = findViewById(R.id.add_author_panel);
            v.setVisibility(View.VISIBLE);
            int sdk = android.os.Build.VERSION.SDK_INT;
            String txt = null;
            try {

                if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
                    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    if (clipboard != null) {
                        txt = clipboard.getText().toString();
                    }
                } else {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    if (clipboard != null) {
                        if (clipboard.hasPrimaryClip()) {
                            txt = clipboard.getPrimaryClip().getItemAt(0).getText().toString();
                        }
                    }
                }

            } catch (Exception ex) {
                Log.e(DEBUG_TAG, "Clipboard Error!", ex);
            }

            if (txt != null) {

                if (SamLibConfig.testFullUrl(txt)) {
                    EditText editText = (EditText) findViewById(R.id.addUrlText);
                    editText.setText(txt);
                }
            }



        }
        if (sel == R.id.settings_option_item) {
            Log.d(DEBUG_TAG, "go to Settings");
            Intent prefsIntent = new Intent(getApplicationContext(),
                    SamlibPreferencesActivity.class);
            //prefsIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(prefsIntent);
        }
        if (sel == R.id.archive_option_item) {

            Log.d(DEBUG_TAG, "go to Archive");
            Intent prefsIntent = new Intent(getApplicationContext(),
                    ArchiveActivity.class);
            prefsIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

            startActivityForResult(prefsIntent, ARCHIVE_ACTIVITY);
        }
        if (sel == R.id.selected_option_item) {
            Log.d(DEBUG_TAG, "go to Selected");
            Intent prefsIntent = new Intent(getApplicationContext(),
                    BooksActivity.class);
            prefsIntent.putExtra(BookListFragment.AUTHOR_ID, SamLibConfig.SELECTED_ID);
            startActivity(prefsIntent);
        }
        if (sel == R.id.menu_filter) {
            Log.d(DEBUG_TAG, "go to Filter");
            Cursor tags = getContentResolver().query(AuthorProvider.TAG_URI, null, null, null, SQLController.COL_TAG_NAME);

            MatrixCursor extras = new MatrixCursor(new String[]{SQLController.COL_ID, SQLController.COL_TAG_NAME});

            extras.addRow(new String[]{Integer.toString(SamLibConfig.TAG_AUTHOR_ALL), getText(R.string.filter_all).toString()});
            extras.addRow(new String[]{Integer.toString(SamLibConfig.TAG_AUTHOR_NEW), getText(R.string.filter_new).toString()});
            Cursor[] cursors = {extras, tags};
            final Cursor extendedCursor = new MergeCursor(cursors);

            AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    extendedCursor.moveToPosition(position);

                    int tag_id = extendedCursor.getInt(extendedCursor.getColumnIndex(SQLController.COL_ID));
                    String tg_name = extendedCursor.getString(extendedCursor.getColumnIndex(SQLController.COL_TAG_NAME));
                    dialog.dismiss();


                    String select = SQLController.TABLE_TAGS + "." + SQLController.COL_ID + "=" + tag_id;

                    if (tag_id == SamLibConfig.TAG_AUTHOR_ALL) {
                        setTitle(R.string.app_name);
                        select = null;
                    } else {
                        String tt = tg_name;
                        setTitle(tt);
                    }

                    if (tag_id == SamLibConfig.TAG_AUTHOR_NEW) {
                        select = SQLController.TABLE_AUTHOR + "." + SQLController.COL_isnew + "=1";
                    }
                    Log.i(DEBUG_TAG, "WHERE " + select);
                    refreshList(select);
                }
            };
            dialog = new FilterSelectDialog(extendedCursor, listener, getText(R.string.dialog_title_filtr).toString());
            dialog.show(getSupportFragmentManager(), "FilterDialogShow");


        }
        return super.onOptionsItemSelected(item);

    }

    /**
     * Return from ArchiveActivity
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            Log.d(DEBUG_TAG, "Wrong result code from onActivityResult");
            return;
        }
        if (requestCode == ARCHIVE_ACTIVITY) {

            int res = data.getIntExtra(ArchiveActivity.UPDATE_KEY, -1);
            if (res == ArchiveActivity.UPDATE_LIST) {
                Log.d(DEBUG_TAG, "Reconstruct List View");
                refreshList(null);


            }
        }
    }

    /**
     * Add new Author to SQL Store
     *
     * @param view
     */
    public void addAuthor(View view) {
        EditText editText = (EditText) findViewById(R.id.addUrlText);
        String text = editText.getText().toString();
        AddAuthor aa = new AddAuthor(this.getApplicationContext());
        aa.execute(text);
        editText.setText("");
        View v = findViewById(R.id.add_author_panel);
        v.setVisibility(View.GONE);

    }
    public void searchAuthor(View view) {
        EditText editText = (EditText) findViewById(R.id.searchAuthorText);
        String text = editText.getText().toString();

        Intent prefsIntent = new Intent(getApplicationContext(),
                SearchAuthorActivity.class);
        prefsIntent.putExtra(SearchAuthorActivity.EXTRA_PATTERN, text);
        prefsIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        
        startActivity(prefsIntent);
    }
    private Author author=null;
    private final int read_option_item           = 21;
    private final int tags_option_item           = 22;
    private final int browser_option_item     = 23;
    private final int edit_author_option_item = 24;
    private final int delete_option_item = 25;

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == listView.getListView().getId()) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

            Cursor cursor = (Cursor) listView.getAdapter().getItem(info.position);

            author = AuthorController.Cursor2Author(getApplicationContext(), cursor);

            if (author == null) {
                Log.d(DEBUG_TAG, "Context menu Created - author is NULL!!");
            } else {
                Log.d(DEBUG_TAG, "Context menu Created - author is " + author.getName());
            }
            if (author.isIsNew()){
                menu.add(1, read_option_item, 10, getText(R.string.menu_read));
            }
            menu.add(1, tags_option_item, 20, getText(R.string.menu_tags));
            menu.add(1, browser_option_item, 30, getText(R.string.menu_open_web));
            menu.add(1, edit_author_option_item, 40, getText(R.string.menu_edit));
            menu.add(1, delete_option_item, 50, getText(R.string.menu_delete));
        }


    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        boolean super_answer = super.onContextItemSelected(item);
        Log.d(DEBUG_TAG, "context menu item selected: " + item.getItemId() + "  super: " + super_answer);

        if (author != null) {
            if (item.getItemId() == delete_option_item) {
                Dialog alert = createDeleteAuthorAlert(author.getName());
                alert.show();
            }

            if (item.getItemId() == read_option_item) {
                MarkRead marker = new MarkRead(getApplicationContext());
                marker.execute(author.getId());
            }
            if (item.getItemId() == tags_option_item) {
                Intent intent = new Intent(this, AuthorTagsActivity.class);
                intent.putExtra(AuthorTagsActivity.AUTHOR_ID, author.getId());
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);

            }
            if (item.getItemId() == browser_option_item) {
                listHelper.launchBrowser(author);
            }
            if (item.getItemId() == edit_author_option_item) {
                final AuthorController sql = new AuthorController(this);
                EnterStringDialog ddialog = new EnterStringDialog(this, new EnterStringDialog.ClickListener() {
                    public void okClick(String txt) {
                        author.setName(txt);
                        sql.update(author);
                    }
                }, getText(R.string.dialog_title_edit_author).toString(), author.getName());

                ddialog.show();
            }

        } else {
            Log.e(DEBUG_TAG, "Author Object is NULL!!");
        }

        return super.onContextItemSelected(item);

    }
    private final DialogInterface.OnClickListener deleteAuthoristener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case Dialog.BUTTON_POSITIVE:
                    if (author != null) {
                        DeleteAuthor deleter = new DeleteAuthor(getApplicationContext());
                        deleter.execute(author.getId());
                    }
                    break;
                case Dialog.BUTTON_NEGATIVE:
                    break;
            }

        }
    };

    /**
     * Create Alert Dialog to wrn about Author delete
     *
     * @param filename
     * @return
     */
    private Dialog createDeleteAuthorAlert(String authorName) {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle(R.string.Attention);

        String msg = getString(R.string.alert_delete_author);
        msg = msg.replaceAll("__", authorName);

        adb.setMessage(msg);
        adb.setIcon(android.R.drawable.ic_dialog_alert);
        adb.setPositiveButton(R.string.Yes, deleteAuthoristener);
        adb.setNegativeButton(R.string.No, deleteAuthoristener);
        return adb.create();

    }

    /**
     * Receive updates from Update Service
     */
    public class UpdateActivityReceiver extends BroadcastReceiver {

        public static final String ACTION_RESP = "monakhv.android.samlib.action.UPDATED";
        public static final String TOAST_STRING = "TOAST_STRING";
        public static final String ACTION = "ACTION";
        public static final String ACTION_TOAST = "TOAST";
        public static final String ACTION_PROGRESS = "PROGRESS";

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getStringExtra(ACTION);
            if (action.equalsIgnoreCase(ACTION_TOAST)) {
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, intent.getCharSequenceExtra(TOAST_STRING), duration);
                toast.show();
                refreshStatus = false;
                getActionBarHelper().setRefreshActionItemState(refreshStatus);
                listView.onRefreshComplete();
            }//
            if (action.equalsIgnoreCase(ACTION_PROGRESS)) {
                listView.updateProgress(intent.getStringExtra(TOAST_STRING));
            }


        }
    }
}
