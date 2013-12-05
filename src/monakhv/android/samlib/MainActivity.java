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

import android.app.ActionBar;
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
import android.support.v4.widget.SlidingPaneLayout;
import android.text.TextUtils;
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
import monakhv.android.samlib.dialogs.SingleChoiceSelectDialog;
import monakhv.android.samlib.search.SearchAuthorsListFragment;
import monakhv.android.samlib.service.CleanNotificationData;
import monakhv.android.samlib.service.UpdateServiceIntent;
import monakhv.android.samlib.sql.AuthorController;
import monakhv.android.samlib.sql.AuthorProvider;
import monakhv.android.samlib.sql.SQLController;
import monakhv.android.samlib.sql.entity.Author;
import monakhv.android.samlib.sql.entity.Book;
import monakhv.android.samlib.sql.entity.SamLibConfig;
import monakhv.android.samlib.tasks.AddAuthor;
import monakhv.android.samlib.tasks.DeleteAuthor;
import monakhv.android.samlib.tasks.MarkRead;

public class MainActivity extends ActionBarActivity implements AuthorListHelper.Callbacks,SlidingPaneLayout.PanelSlideListener {
    private SlidingPaneLayout pane;

    public void onAuthorSelected(int id) {
        books.setAuthorId(id);
       
    }

    public void onPanelSlide(View view, float f) {
       
    }

    public void onPanelOpened(View view) {
        Log.d(DEBUG_TAG, "panel is opened");
        setTitle(R.string.app_name);
        isOpen = true;
        invalidateOptionsMenu();
        books.setColor(ActivityUtils.FAIDING_COLOR);
        getActionBar().setDisplayOptions(0,ActionBar.DISPLAY_HOME_AS_UP);
    }

    public void onPanelClosed(View view) {
        isOpen = false;
        invalidateOptionsMenu();
       
        getActionBar().setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP,ActionBar.DISPLAY_HOME_AS_UP);
        int author_id = books.getAuthorId();
        Log.d(DEBUG_TAG, "panel is closed, author_id = "+author_id);
        books.setColor(ActivityUtils.ACTIVE_COLOR);
        if (author_id == 0){
            return;
        }
        if (author_id != -1) {
            AuthorController sql = new AuthorController(this);
            Author a = sql.getById(author_id);
            setTitle(a.getName());
        } else {
            setTitle(getText(R.string.menu_selected_go));
        }
        
    }

    public enum SortOrder {

        DateUpdate(R.string.sort_update_date, SQLController.COL_mtime + " DESC"),
        AuthorName(R.string.sort_author_name, SQLController.COL_isnew + " DESC, " + SQLController.COL_NAME);
        private final int iname;
        private final String order;

        private SortOrder(int iname, String order) {
            this.iname = iname;
            this.order = order;
        }

        public String getOrder() {
            return order;
        }

        public static String[] getTites(Context ctx) {
            String[] res = new String[values().length];
            int i = 0;
            for (SortOrder so : values()) {
                res[i] = ctx.getString(so.iname);
                ++i;
            }
            return res;
        }
    }

    private static final String DEBUG_TAG = "MainActivity";
    public static String CLEAN_NOTIFICATION = "CLEAN_NOTIFICATION";
    private final int ARCHIVE_ACTIVITY = 1;
    private final int SEARCH_ACTIVITY = 2;
    //AddAuthorDialog addAuthorDilog;
    private UpdateActivityReceiver updateReceiver;
    private DownloadReceiver        downloadReceiver;
    private boolean refreshStatus = false;
    private FilterSelectDialog filterDialog;
    private PullToRefresh listView;
    private AuthorListHelper listHelper;
    private SingleChoiceSelectDialog sortDialog;
    private String select;
    private SortOrder so;
    private static final String KEY_DATA_SELECTION = "selection";
    private static final String KEY_DATA_ORDER = "order";

    private BookListFragment books = null;
    private boolean isOpen = true;
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.main_twopane);
       
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
        books = (BookListFragment) getSupportFragmentManager().findFragmentById(R.id.listBooksFragment);
        pane = (SlidingPaneLayout) findViewById(R.id.pane);
        pane.setPanelSlideListener(this);
        pane.openPane();
        
        ActivityUtils.setShadow(pane);
        
        Log.d(DEBUG_TAG, "Faiding color: "+pane.getSliderFadeColor());
        isOpen = true;
      
        listView = (PullToRefresh) findViewById(R.id.listAuthirFragment);
        listHelper = new AuthorListHelper(this, listView);

        listView.setOnRefreshListener(new OnRefreshListener() {
            public void onRefresh() {
                makeUpdate();
            }
        });
        registerForContextMenu(listView.getListView());
        if (icicle != null) {
            select = icicle.getString(KEY_DATA_SELECTION);
            so = SortOrder.valueOf(icicle.getString(KEY_DATA_ORDER));
            refreshList(select, so);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(KEY_DATA_SELECTION, select);
        so = listHelper.getSortOrder();
        outState.putString(KEY_DATA_ORDER, so.name());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter updateFilter = new IntentFilter(UpdateActivityReceiver.ACTION_RESP);
        IntentFilter downloadFilter = new IntentFilter(DownloadReceiver.ACTION_RESP);
        
        updateFilter.addCategory(Intent.CATEGORY_DEFAULT);
        downloadFilter.addCategory(Intent.CATEGORY_DEFAULT);
        
        updateReceiver = new UpdateActivityReceiver();
        downloadReceiver = new DownloadReceiver();
        getActionBarHelper().setRefreshActionItemState(refreshStatus);
        registerReceiver(updateReceiver, updateFilter);
        registerReceiver(downloadReceiver, downloadFilter);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) { //Back key pressed
            if (! isOpen) {
                pane.openPane();
                return true;
            }
            if (listHelper.getSelection() != null) {
                refreshList(null, null);
            } else {
                finish();
            }

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void refreshList(String sel, SortOrder order) {

        listHelper.refresh(sel, order);

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(updateReceiver);
        unregisterReceiver(downloadReceiver);
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

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if (isOpen){
            
            MenuInflater menuInflater = getMenuInflater();
            menuInflater.inflate(R.menu.options_menu, menu);
        }
       
        return super.onPrepareOptionsMenu(menu); //To change body of generated methods, choose Tools | Templates.
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
        if (sel == android.R.id.home && ! isOpen){
            pane.openPane();
        }

        if (sel == R.id.menu_refresh) {
            makeUpdate();

        }

        if (sel == R.id.sort_option_item) {

            AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {

                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    so = SortOrder.values()[position];
                    listHelper.setSortOrder(so);
                    sortDialog.dismiss();
                }

            };
            sortDialog = new SingleChoiceSelectDialog(SortOrder.getTites(this), listener, this.getString(R.string.dialog_title_sort), listHelper.getSortOrder().ordinal());

            sortDialog.show(getSupportFragmentManager(), "Dosrtdlg");
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
                    filterDialog.dismiss();

                    select = SQLController.TABLE_TAGS + "." + SQLController.COL_ID + "=" + tag_id;

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
                    refreshList(select, null);
                }
            };
            filterDialog = new FilterSelectDialog(extendedCursor, listener, getText(R.string.dialog_title_filtr).toString());
            filterDialog.show(getSupportFragmentManager(), "FilterDialogShow");

        }
        return super.onOptionsItemSelected(item);

    }

    /**
     * Return from ArchiveActivity or SearchActivity
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
                refreshList(null, null);

            }
        }
        if (requestCode == SEARCH_ACTIVITY) {
            AddAuthor aa = new AddAuthor(getApplicationContext());
            aa.execute(data.getStringExtra(SearchAuthorsListFragment.AUTHOR_URL));
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
        View v = findViewById(R.id.add_author_panel);
        editText.setText("");
        v.setVisibility(View.GONE);
        if (SamLibConfig.reduceUrl(text) != null) {
            AddAuthor aa = new AddAuthor(this.getApplicationContext());
            aa.execute(text);
        } else {
            if (TextUtils.isEmpty(text)) {
                return;
            }
            Intent prefsIntent = new Intent(getApplicationContext(),
                    SearchAuthorActivity.class);
            prefsIntent.putExtra(SearchAuthorActivity.EXTRA_PATTERN, text);
            prefsIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

            startActivityForResult(prefsIntent, SEARCH_ACTIVITY);
        }

    }

    private Author author = null;
    private final int read_option_item = 21;
    private final int tags_option_item = 22;
    private final int browser_option_item = 23;
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
            if (author.isIsNew()) {
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
    public class DownloadReceiver extends BroadcastReceiver {
        
        public static final String ACTION_RESP = "monakhv.android.samlib.action.BookDownload";
        public static final String MESG = "MESG";
        public static final String RESULT = "RESULT";
        public static final String BOOK_ID = "BOOK_ID";
        private static final String DEBUG_TAG = "DownloadReceiver";
        
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(DEBUG_TAG, "Starting onReceive");
            String mesg = intent.getStringExtra(MESG);
            long book_id = intent.getLongExtra(BOOK_ID, 0);
            
            boolean res = intent.getBooleanExtra(RESULT, false);
            
            AuthorController sql = new AuthorController(context);
            Book book = sql.getBookController().getById(book_id);
            
            if (books != null) {
                if (books.progress != null) {
                    books.progress.dismiss();
                }
            }
            
            if (res) {
                Log.d(DEBUG_TAG, "Starting web for url: " + book.getFileURL());
//               
                if (books != null) {
                    books.launchReader(book);
                }
            } else {
                Toast toast = Toast.makeText(context, mesg, Toast.LENGTH_SHORT);
                
                toast.show();
            }
        }
    }
}
