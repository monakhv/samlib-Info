package monakhv.android.samlib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import monakhv.android.samlib.data.SettingsHelper;


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

    private static final String DEBUG_TAG = "TestActivity";
    public static final int ARCHIVE_ACTIVITY = 1;
    public static final int SEARCH_ACTIVITY  = 2;
    public static final int PREFS_ACTIVITY  = 3;
    public static final  String CLEAN_NOTIFICATION = "CLEAN_NOTIFICATION";
    private UpdateActivityReceiver updateReceiver;
    private AuthorFragment authorFragment;
    private SettingsHelper settingsHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        settingsHelper = new SettingsHelper(this);
        setTheme(settingsHelper.getTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_layout);
        authorFragment = (AuthorFragment) getSupportFragmentManager().findFragmentById(R.id.authorFragment);
        authorFragment.setHasOptionsMenu(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter updateFilter = new IntentFilter(UpdateActivityReceiver.ACTION_RESP);


        updateFilter.addCategory(Intent.CATEGORY_DEFAULT);


        updateReceiver = new UpdateActivityReceiver();

        //getActionBarHelper().setRefreshActionItemState(refreshStatus);
        registerReceiver(updateReceiver, updateFilter);

    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(updateReceiver);

        //Stop refresh status
        authorFragment.onRefreshComplete();
        //getActionBarHelper().setRefreshActionItemState(refreshStatus);
    }

    @Override
    public void onAuthorSelected(long id) {
        Log.d(DEBUG_TAG, "go to Books");
        Intent intent = new Intent(this,BooksActivity.class);
        intent.putExtra(BookFragment.AUTHOR_ID,id);

        startActivity(intent);
        

    }

    @Override
    public void selectBookSortOrder() {

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
    public void addAuthorFromText() {

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
