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
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Toast;
import monakhv.android.samlib.actionbar.ActionBarActivity;
import monakhv.android.samlib.data.SettingsHelper;
import monakhv.android.samlib.service.CleanNotificationData;
import monakhv.android.samlib.service.UpdateServiceIntent;
import monakhv.android.samlib.sql.AuthorProvider;
import monakhv.android.samlib.sql.SQLController;
import monakhv.android.samlib.sql.entity.SamLibConfig;
import monakhv.android.samlib.tasks.AddAuthor;

public class MainActivity extends ActionBarActivity {

    private static String DEBUG_TAG = "MainActivity";
    public static String CLEAN_NOTIFICATION = "CLEAN_NOTIFICATION";
    final int ARCHIVE_ACTIVITY = 1;
    //AddAuthorDialog addAuthorDilog;
    private UpdateActivityReceiver receiver;
    private boolean refreshStatus = false;
    private FilterSelectDialog dialog;
    private String selection = null;

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
        //addAuthorDilog = new AddAuthorDialog();
        SettingsHelper.addAuthenticator(this.getApplicationContext());
        getActionBarHelper().setRefreshActionItemState(refreshStatus);

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
            if (selection != null) {
                refreshList(null);
            } else {
                finish();
            }

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void refreshList(String sel) {
        this.selection = sel;
        AuthorListFragment listFragment = (AuthorListFragment) getSupportFragmentManager().findFragmentById(R.id.listAuthirFragment);
        listFragment.refresh(sel);

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.options_menu, menu);

        return super.onCreateOptionsMenu(menu);


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
            refreshStatus = true;
            getActionBarHelper().setRefreshActionItemState(refreshStatus);
            Intent service = new Intent(this, UpdateServiceIntent.class);
            service.putExtra(UpdateServiceIntent.CALLER_TYPE, UpdateServiceIntent.CALLER_IS_ACTIVITY);
            service.putExtra(UpdateServiceIntent.SELECT_STRING, selection);
            startService(service);

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

                if (txt.startsWith(SamLibConfig.SAMLIB_URL)) {
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
                    NewBooksActivity.class);
            prefsIntent.putExtra(BookListFragment.AUTHOR_ID, -1);
            startActivity(prefsIntent);
        }
        if (sel == R.id.menu_filter) {
            Log.d(DEBUG_TAG, "go to Filter");
            Cursor tags = getContentResolver().query(AuthorProvider.TAG_URI, null, null, null, SQLController.COL_TAG_NAME);

            MatrixCursor extras = new MatrixCursor(new String[]{SQLController.COL_ID, SQLController.COL_TAG_NAME});

            extras.addRow(new String[]{"-1", getText(R.string.filter_all).toString()});
            extras.addRow(new String[]{"-2", getText(R.string.filter_new).toString()});
            Cursor[] cursors = {extras, tags};
            final Cursor extendedCursor = new MergeCursor(cursors);

            AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    extendedCursor.moveToPosition(position);

                    int tag_id = extendedCursor.getInt(extendedCursor.getColumnIndex(SQLController.COL_ID));
                    String tg_name = extendedCursor.getString(extendedCursor.getColumnIndex(SQLController.COL_TAG_NAME));
                    dialog.dismiss();


                    String select = SQLController.TABLE_TAGS + "." + SQLController.COL_ID + "=" + tag_id;

                    if (tag_id == -1) {
                        setTitle(R.string.app_name);
                        select = null;
                    } else {
                        String tt = tg_name;
                        setTitle(tt);
                    }

                    if (tag_id == -2) {
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

    /**
     * Receive updates from Update Service
     */
    public class UpdateActivityReceiver extends BroadcastReceiver {

        public static final String ACTION_RESP = "monakhv.android.samlib.action.UPDATED";
        public static final String TOAST_STRING = "TOAST_STRING";

        @Override
        public void onReceive(Context context, Intent intent) {
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, intent.getCharSequenceExtra(TOAST_STRING), duration);
            toast.show();
            refreshStatus = false;
            getActionBarHelper().setRefreshActionItemState(refreshStatus);
        }
    }
}
