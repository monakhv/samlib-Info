package monakhv.android.samlib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import monakhv.android.samlib.data.SettingsHelper;
import monakhv.android.samlib.search.SearchAuthorActivity;
import monakhv.android.samlib.search.SearchAuthorsListFragment;
import monakhv.android.samlib.service.AuthorEditorServiceIntent;
import monakhv.android.samlib.service.CleanNotificationData;
import monakhv.android.samlib.sql.entity.SamLibConfig;



/*
 * Copyright 2014  Dmitry Monakhov
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
 * 12/5/14.
 */
public class MainActivity extends ActionBarActivity implements AuthorFragment.Callbacks {

    private static final String DEBUG_TAG = "MainActivity";
//    private static final String STATE_SELECTION = "STATE_SELECTION";
//    private static final String STATE_AUTHOR_POS = "STATE_AUTHOR_ID";
    public static final int ARCHIVE_ACTIVITY = 1;
    public static final int SEARCH_ACTIVITY  = 2;
    public static final int PREFS_ACTIVITY  = 3;
    public static final  String CLEAN_NOTIFICATION = "CLEAN_NOTIFICATION";
    private UpdateActivityReceiver updateReceiver;
    private AuthorFragment authorFragment;
    private BookFragment bookFragment;
    private SettingsHelper settingsHelper;
    private DownloadReceiver downloadReceiver;
    private  AuthorEditReceiver authorReceiver;
    private boolean twoPain;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        settingsHelper = new SettingsHelper(this);
        setTheme(settingsHelper.getTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        Bundle bundle = getIntent().getExtras();

        String clean = null;
        if (bundle != null) {
            clean = bundle.getString(CLEAN_NOTIFICATION);
        }
        if (clean != null) {
            CleanNotificationData.start(this);
            bundle = null;
        }

        authorFragment = (AuthorFragment) getSupportFragmentManager().findFragmentById(R.id.authorFragment);
        authorFragment.setHasOptionsMenu(true);

//        if (bundle != null) {
//            Log.i(DEBUG_TAG, "Restore state");
//            onRestoreInstanceState(bundle);
//        }

        twoPain=findViewById(R.id.two_pain)!= null;
        if (twoPain){
            Log.i(DEBUG_TAG,"onCreate: two pane");
        }
        else {
            Log.i(DEBUG_TAG,"onCreate: one pane");
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent == null){
            return;
        }
        Bundle bundle= intent.getExtras();
        if (bundle != null){
            String clean = bundle.getString(CLEAN_NOTIFICATION);
            if (clean != null) {
                CleanNotificationData.start(this);
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter updateFilter = new IntentFilter(UpdateActivityReceiver.ACTION_RESP);
        IntentFilter authorFilter = new IntentFilter(AuthorEditorServiceIntent.RECEIVER_FILTER);


        updateFilter.addCategory(Intent.CATEGORY_DEFAULT);
        authorFilter.addCategory(Intent.CATEGORY_DEFAULT);


        updateReceiver = new UpdateActivityReceiver();
        authorReceiver = new AuthorEditReceiver();

        //getActionBarHelper().setRefreshActionItemState(refreshStatus);
        registerReceiver(updateReceiver, updateFilter);
        registerReceiver(authorReceiver, authorFilter);

        if (twoPain){
            bookFragment= (BookFragment) getSupportFragmentManager().findFragmentById(R.id.listBooksFragment);
            if (bookFragment == null){
                Log.e(DEBUG_TAG,"Fragment is NULL for two pane layout!!");
            }
            downloadReceiver = new DownloadReceiver(bookFragment);
            IntentFilter filter = new IntentFilter(DownloadReceiver.ACTION_RESP);
            filter.addCategory(Intent.CATEGORY_DEFAULT);
            registerReceiver(downloadReceiver, filter);
        }


    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(updateReceiver);
        unregisterReceiver(authorReceiver);
        if (twoPain){
            unregisterReceiver(downloadReceiver);
        }

        //Stop refresh status
        authorFragment.onRefreshComplete();
        //getActionBarHelper().setRefreshActionItemState(refreshStatus);
    }

//    @Override
//    public void onSaveInstanceState(Bundle bundle) {
//        super.onSaveInstanceState(bundle);
//        bundle.putString(STATE_SELECTION, authorFragment.getSelection());
//        bundle.putInt(STATE_AUTHOR_POS, authorFragment.getSelectedAuthorPosition());
//
//    }
//    @Override
//    public void onRestoreInstanceState(Bundle bundle) {
//        super.onRestoreInstanceState(bundle);
//        if (bundle == null) {
//            return;
//        }
//        Log.i(DEBUG_TAG,"onRestoreInstanceState");
//
//
//        authorFragment.refresh(bundle.getString(STATE_SELECTION), null);
//        authorFragment.restoreSelection(bundle.getInt(STATE_AUTHOR_POS));
//        if (bookFragment != null){
//            bookFragment.setAuthorId(authorFragment.getSelectedAuthorId());
//        }
//
//    }

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
            authorFragment.refresh(null, null);
            return;
        }
        if (requestCode == ARCHIVE_ACTIVITY) {

            int res = data.getIntExtra(ArchiveActivity.UPDATE_KEY, -1);
            if (res == ArchiveActivity.UPDATE_LIST) {
                Log.d(DEBUG_TAG, "Reconstruct List View");
                authorFragment.refresh(null, null);

            }
        }
        if (requestCode == SEARCH_ACTIVITY) {
            Log.v(DEBUG_TAG,"Start add Author");

            AuthorEditorServiceIntent.addAuthor(getApplicationContext(),data.getStringExtra(SearchAuthorsListFragment.AUTHOR_URL));
        }
        if (requestCode == PREFS_ACTIVITY){
            finish();
        }
    }

    @Override
    public void onAuthorSelected(long id) {
        Log.d(DEBUG_TAG, "onAuthorSelected: go to Books");
        if (twoPain){
            Log.i(DEBUG_TAG, "Two fragments Layout - set author_id: "+id);
            bookFragment.setAuthorId(id);
        }
        else {
            Log.i(DEBUG_TAG, "One fragment Layout - set author_id: "+id);
            Intent intent = new Intent(this,BooksActivity.class);
            intent.putExtra(BookFragment.AUTHOR_ID,id);

            startActivity(intent);
        }

        

    }

    @Override
    public void selectBookSortOrder() {
        bookFragment.selectSortOrder();
    }

    @Override
    public void onTitleChange(String lTitle) {
        Log.d(DEBUG_TAG, "set title: "+lTitle);

        getSupportActionBar().setTitle(lTitle);
        if (authorFragment.getSelection() == null){
            getSupportActionBar().setDisplayOptions(0, ActionBar.DISPLAY_HOME_AS_UP);
        }
        else {
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
        }

    }

    @Override
    public void cleanBookSelection() {
        if (twoPain){
            bookFragment.setAuthorId(0);//empty selection
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
        if (url != null){//add  Author by URL
            AuthorEditorServiceIntent.addAuthor(getApplicationContext(),url);

        }
        else {
            if (TextUtils.isEmpty(text)) {
                return;
            }
            //Start Search activity to make search and add selected Authors to Data Base
            Intent prefsIntent = new Intent(getApplicationContext(),
                    SearchAuthorActivity.class);
            prefsIntent.putExtra(SearchAuthorActivity.EXTRA_PATTERN, text);
            prefsIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

            startActivityForResult(prefsIntent, SEARCH_ACTIVITY);
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) { //Back key pressed

            if (authorFragment.getSelection() != null) {
                authorFragment.refresh(null, null);
                onTitleChange(getString(R.string.app_name));
            } else {
                finish();
            }

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    public class AuthorEditReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int duration = Toast.LENGTH_SHORT;
            CharSequence msg = intent.getCharSequenceExtra(AuthorEditorServiceIntent.RESULT_MESSAGE);
            Toast toast =Toast.makeText(context,msg,duration);

            if (intent.getStringExtra(AuthorEditorServiceIntent.EXTRA_ACTION_TYPE).equals(AuthorEditorServiceIntent.ACTION_ADD)){
                Log.d(DEBUG_TAG,"onReceive: author add");
                long id = intent.getLongExtra(AuthorEditorServiceIntent.RESULT_AUTHOR_ID,0);

                authorFragment.selectAuthor(id);
                toast.show();
                onAuthorSelected(id);

            }
            if (intent.getStringExtra(AuthorEditorServiceIntent.EXTRA_ACTION_TYPE).equals(AuthorEditorServiceIntent.ACTION_DELETE)){
                Log.d(DEBUG_TAG,"onReceive: author del");
                toast.show();
            }

        }
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
            if (action != null) {
                if (action.equalsIgnoreCase(ACTION_TOAST)) {
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, intent.getCharSequenceExtra(TOAST_STRING), duration);
                    toast.show();

                    authorFragment.onRefreshComplete();
                }//
                if (action.equalsIgnoreCase(ACTION_PROGRESS)) {
                    authorFragment.updateProgress(intent.getStringExtra(TOAST_STRING));
                }
            }


        }
    }
}
