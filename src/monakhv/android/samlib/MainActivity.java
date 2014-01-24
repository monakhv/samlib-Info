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
import android.os.Handler;
import android.support.v4.widget.SlidingPaneLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;

import android.view.View;

import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;

import monakhv.android.samlib.search.SearchAuthorActivity;
import monakhv.android.samlib.data.SettingsHelper;
import monakhv.android.samlib.search.SearchAuthorsListFragment;
import monakhv.android.samlib.service.CleanNotificationData;
import monakhv.android.samlib.sql.AuthorController;
import monakhv.android.samlib.sql.entity.Author;
import monakhv.android.samlib.sql.entity.Book;
import monakhv.android.samlib.sql.entity.SamLibConfig;
import monakhv.android.samlib.tasks.AddAuthor;

public class MainActivity extends SherlockFragmentActivity implements AuthorListFragment.Callbacks, BookListFragment.Callbacks,
        SlidingPaneLayout.PanelSlideListener {

    private SlidingPaneLayout pane;
    private Handler handler;
    private static final int TIME_BEFORE_CLOSE_MILLI=100;
    private static final int TIME_BEFORE_OPEN_MILLI=100;
    private static final String DEBUG_TAG = "MainActivity";
    private static final String STATE_SELECTION = "STATE_SELECTION";
    private static final String STATE_AUTHOR_POS = "STATE_AUTHOR_ID";
    private static final String STATE_TITLE = "STATE_TITLE";
    public static final  String CLEAN_NOTIFICATION = "CLEAN_NOTIFICATION";
    public static final int ARCHIVE_ACTIVITY = 1;
    public static final int SEARCH_ACTIVITY  = 2;
    //AddAuthorDialog addAuthorDialog;
    private UpdateActivityReceiver updateReceiver;
    private DownloadReceiver downloadReceiver;
    private AuthorListFragment listHelper;

    private BookListFragment books = null;
    private boolean isOpen = true;

    private String title;

    /**
     * Callback When select author in AuthorListFragment
     * @param id  author-id
     */
    public void onAuthorSelected(int id) {
            books.setAuthorId(id);
            if (pane.isSlideable() && id != 0){
                handler.postDelayed(new Runnable() {
                    public void run() {
                        Log.d(DEBUG_TAG, "delay close");
                        pane.closePane();
                    }
                },TIME_BEFORE_CLOSE_MILLI);
            }            
        //pane.closePane();
        
    }

    @Override
    public void selectBookSortOrder() {
        books.selectSortOrder();
    }

    public void onTitleChange(String lTitle){
        Log.d(DEBUG_TAG, "set title: "+lTitle);
        title = lTitle;
        getSupportActionBar().setTitle(title);
        if (listHelper.getSelection() == null){
            getSupportActionBar().setDisplayOptions(0, ActionBar.DISPLAY_HOME_AS_UP);
        }
        else {
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
        }
    }

    public void onPanelSlide(View view, float f) {
        //Log.d(DEBUG_TAG, "panel is sliding");
    }

    public void onPanelOpened(View view) {
        if (view.getId() != R.id.pane2){
            return;
        }
        Log.d(DEBUG_TAG, "panel is opened");
        onTitleChange(title);
        isOpen = true;
        invalidateOptionsMenu();
        listHelper.setHasOptionsMenu(true);
        books.setHasOptionsMenu(false);


    }

    public void onPanelClosed(View view) {
        if (view.getId() != R.id.pane2){
            return;
        }
        isOpen = false;
        invalidateOptionsMenu();
        listHelper.setHasOptionsMenu(false);
        books.setHasOptionsMenu(true);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
        int author_id = books.getAuthorId();
        Log.d(DEBUG_TAG, "panel is closed, author_id = " + author_id);

        if (author_id == 0) {
            return;
        }
        if (author_id != SamLibConfig.SELECTED_BOOK_ID) {
            AuthorController sql = new AuthorController(this);
            Author a = sql.getById(author_id);
            if (a == null) {
                Log.e(DEBUG_TAG, "Can not find author for id: " + author_id);
                return;
            }
            
            getSupportActionBar().setTitle(a.getName());
        } else {
            
            getSupportActionBar().setTitle(getText(R.string.menu_selected_go));
        }
        
    }

    public void cleanAuthorSelection() {
        listHelper.cleanSelection();

    }



    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.main_twopane);

        Bundle bundle = getIntent().getExtras();
        
        String clean = null;
        if (bundle != null) {
            clean = bundle.getString(CLEAN_NOTIFICATION);
        }
        if (clean != null) {
            CleanNotificationData.start(this);
            bundle = null;
        }
        handler = new Handler();
        
        SettingsHelper.addAuthenticator(this.getApplicationContext());

        books = (BookListFragment) getSupportFragmentManager().findFragmentById(R.id.listBooksFragment);
        listHelper = (AuthorListFragment) getSupportFragmentManager().findFragmentById(R.id.listAuthirFragment);

        pane = (SlidingPaneLayout) findViewById(R.id.pane);
        pane.setPanelSlideListener(this);
        pane.setParallaxDistance(10);

        listHelper.setHasOptionsMenu(true);
        ActivityUtils.setShadow(pane);


        isOpen = true;
        //use here bundle but not icicle !!
        if (bundle != null) {
            Log.i(DEBUG_TAG, "Restore state");
            onRestoreInstanceState(bundle);
        }
        else {
            Log.i(DEBUG_TAG, "Make initial state");
            onTitleChange(getString(R.string.app_name));
        }
        //Ugly hack to make sure open pan on application startup
        handler.postDelayed(new Runnable() {
            public void run() {
                Log.d(DEBUG_TAG, "delay open");
                logPaneDetails();
                pane.openPane();
            }
        },TIME_BEFORE_OPEN_MILLI);

    }

    /**
     * Log some information about pane properties
     */
    private void  logPaneDetails(){
        if (pane == null){
            Log.e(DEBUG_TAG,"pane is NULL");
            return;
        }
        if (pane.isOpen()){
            Log.i(DEBUG_TAG,"pane is open");
        }
        else{
            Log.i(DEBUG_TAG,"pane is closed");
        }

        if (pane.isSlideable()){
            Log.i(DEBUG_TAG,"pane can slide");
        }
        else {
            Log.i(DEBUG_TAG,"pane can not slide");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putString(STATE_SELECTION, listHelper.getSelection());
        bundle.putString(STATE_TITLE, title);
        bundle.putInt(STATE_AUTHOR_POS, listHelper.getSelectedAuthorPosition());

    }

    @Override
    public void onRestoreInstanceState(Bundle bundle) {
        super.onRestoreInstanceState(bundle);
        if (bundle == null) {
            return;
        }
        Log.i(DEBUG_TAG,"onRestoreInstanceState");

        onTitleChange(bundle.getString(STATE_TITLE));
        listHelper.refresh(bundle.getString(STATE_SELECTION), null);
        listHelper.restoreSelection(bundle.getInt(STATE_AUTHOR_POS));
        books.setAuthorId(listHelper.getSelectedAuthorId());
    }

    @Override
    protected void onResume() {
        super.onResume();
        pane.openPane();
        IntentFilter updateFilter = new IntentFilter(UpdateActivityReceiver.ACTION_RESP);
        IntentFilter downloadFilter = new IntentFilter(DownloadReceiver.ACTION_RESP);

        updateFilter.addCategory(Intent.CATEGORY_DEFAULT);
        downloadFilter.addCategory(Intent.CATEGORY_DEFAULT);

        updateReceiver = new UpdateActivityReceiver();
        downloadReceiver = new DownloadReceiver();
        //getActionBarHelper().setRefreshActionItemState(refreshStatus);
        registerReceiver(updateReceiver, updateFilter);
        registerReceiver(downloadReceiver, downloadFilter);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) { //Back key pressed
            if (!isOpen) {
                pane.openPane();
                return true;
            }
            if (listHelper.getSelection() != null) {
                listHelper.refresh(null, null);
                onTitleChange(getString(R.string.app_name));
            } else {
                finish();
            }

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(updateReceiver);
        unregisterReceiver(downloadReceiver);
        //Stop refresh status
        listHelper.onRefreshComplete();
        //getActionBarHelper().setRefreshActionItemState(refreshStatus);
    }

    /**
     * Return from ArchiveActivity or SearchActivity
     *
     * @param requestCode request code
     * @param resultCode result code
     * @param data Intent data
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
                listHelper.refresh(null, null);

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
     * @param view View
     */
    @SuppressWarnings("UnusedParameters")
    public void addAuthor(View view) {

        addAuthorFromText();

    }

    @Override
    public void addAuthorFromText(){
        EditText editText = (EditText) findViewById(R.id.addUrlText);

        if (editText == null){
            return;
        }
        if (editText.getText() == null){
            return;
        }
        String text = editText.getText().toString();
        editText.setText("");


        View v = findViewById(R.id.add_author_panel);
        v.setVisibility(View.GONE);

        String url = SamLibConfig.getParsedUrl(text);
        if (url != null){
            AddAuthor aa = new AddAuthor(this.getApplicationContext());
            aa.execute(url);
        }
       else {
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

    public void onOpenPanel() {
        if (!isOpen) {
            pane.openPane();
        }
    }

    /*public void onClosePanel() {
        if (isOpen) {
            pane.closePane();
        }
    }*/

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
            if (action != null) {
                if (action.equalsIgnoreCase(ACTION_TOAST)) {
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, intent.getCharSequenceExtra(TOAST_STRING), duration);
                    toast.show();

                    listHelper.onRefreshComplete();
                }//
                if (action.equalsIgnoreCase(ACTION_PROGRESS)) {
                    listHelper.updateProgress(intent.getStringExtra(TOAST_STRING));
                }
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
