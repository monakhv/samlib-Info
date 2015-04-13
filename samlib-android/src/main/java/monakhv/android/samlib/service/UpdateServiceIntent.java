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
package monakhv.android.samlib.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


import monakhv.android.samlib.MainActivity.UpdateActivityReceiver;

import monakhv.android.samlib.R;
import monakhv.android.samlib.data.DataExportImport;
import monakhv.android.samlib.data.SettingsHelper;
import monakhv.android.samlib.sortorder.AuthorSortOrder;
import monakhv.samlib.db.SQLController;
import monakhv.samlib.exception.SamlibParseException;
import monakhv.android.samlib.sql.AuthorController;

import monakhv.samlib.db.entity.SamLibConfig;
import monakhv.samlib.http.HttpClientController;
import monakhv.samlib.db.entity.Author;
import monakhv.samlib.db.entity.Book;
import monakhv.samlib.log.Log;

/**
 * Service to making check for author updates Can be called from activity or
 * from alarm manager
 *
 * @author monakhv
 */
public class UpdateServiceIntent extends IntentService {
    public static final long SLEEP_INTERVAL_SECONDS=1;
    public static final String CALLER_TYPE = "CALLER_TYPE";
    public static final String SELECT_STRING = "SELECT_STRING";
    public static final int CALLER_IS_ACTIVITY = 1;
    public static final int CALLER_IS_RECEIVER = 2;
    private static final String DEBUG_TAG = "UpdateServiceIntent";
    private int currentCaller = 0;
    private Context context;
    private SettingsHelper settings;
    private DataExportImport dataExportImport;
    private final List<Author> updatedAuthors;

    public UpdateServiceIntent() {
        super("UpdateServiceIntent");
        updatedAuthors = new ArrayList<Author>();
        Log.d(DEBUG_TAG, "Constructor Call");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int skippedAuthors = 0;
        Log.d(DEBUG_TAG, "Got intent");
        context = this.getApplicationContext();
        updatedAuthors.clear();
        settings = new SettingsHelper(context);
        dataExportImport = new DataExportImport(context);
        currentCaller = intent.getIntExtra(CALLER_TYPE, 0);
        String selection = intent.getStringExtra(SELECT_STRING);
        settings.requestFirstBackup();
        if (currentCaller == 0) {
            Log.e(DEBUG_TAG, "Wrong Caller type");

            return;
        }

        if (currentCaller == CALLER_IS_RECEIVER) {
            String stag = settings.getUpdateTag();
            int tag_id = Integer.parseInt(stag);
            if (tag_id == SamLibConfig.TAG_AUTHOR_ALL){
                selection = null;
            }
            else {
                selection = SQLController.TABLE_TAGS + "." + SQLController.COL_ID + "=" + tag_id;

            }
            if (!SettingsHelper.haveInternetWIFI(context)) {
                Log.d(DEBUG_TAG, "Ignore update task - we have no internet connection");

                return;
            }
        }
        if (currentCaller == CALLER_IS_ACTIVITY) {
            if (!SettingsHelper.haveInternet(context)) {
                Log.e(DEBUG_TAG, "Ignore update - we have no internet connection");

                finish(false);
                return;
            }

        }

        Log.i(DEBUG_TAG, "selection string: " + selection);


        HttpClientController http = HttpClientController.getInstance(settings);
        AuthorController ctl = new AuthorController(context);
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, DEBUG_TAG);
        wl.acquire();
        List<Author> authors = ctl.getAll(selection, AuthorSortOrder.DateUpdate.getOrder());
        int total = authors.size();
        int iCurrent = 0;//to send update information to pull-to-refresh
        for (Author a : authors) {//main author cycle
            if (currentCaller == CALLER_IS_ACTIVITY) {
                sendUpdate(total, ++iCurrent, a.getName());
            }
            else {
                Log.d(DEBUG_TAG,"update: "+a.getName());
            }
            String url = a.getUrl();
            Author newA;
            try {
                newA = http.getAuthorByURL(url);
            } catch (IOException ex) {//here we abort cycle author and total update
                Log.i(DEBUG_TAG, "Connection Error: "+url, ex);


                finish(false);
                wl.release();
                return;

            } catch (SamlibParseException ex) {//skip update for given author
                Log.e(DEBUG_TAG, "Error parsing url: " + url + " skip update author ", ex);

                ++skippedAuthors;
                newA = a;
            }
            if (a.update(newA)) {//we have update for the author
                updatedAuthors.add(a);
                Log.i(DEBUG_TAG, "We need update author: " + a.getName());
                ctl.update(a);

                if (settings.getAutoLoadFlag()) {//download the book

                    for (Book book : ctl.getBookController().getBooksByAuthor(a)) {//book cycle for the author to update
                        if (book.isIsNew() && settings.testAutoLoadLimit(book) && dataExportImport.needUpdateFile(book)) {
                            Log.i(DEBUG_TAG, "Auto Load book: " + book.getId());
                            DownloadBookServiceIntent.start(this, book,false);
                        }
                    }
                }
            }

            try {

                TimeUnit.SECONDS.sleep(SLEEP_INTERVAL_SECONDS);
            } catch (InterruptedException e) {
                Log.e(DEBUG_TAG,"Sleep interrupted",e);
            }
        }//main author cycle END
        if (authors.size() == skippedAuthors){
            finish(false);//all authors skipped - this is the error
        }
        else {
            finish(true);
        }
        
        wl.release();
    }

    private void finish(boolean result) {

        Log.d(DEBUG_TAG, "Finish intent.");
        settings = new SettingsHelper(context);
        if (settings.getLimitBookLifeTimeFlag()) {
            CleanBookServiceIntent.start(context);
        }
        if (currentCaller == CALLER_IS_ACTIVITY) {//Call from activity

            CharSequence text;

            if (result) {//Good Call
                if (updatedAuthors.isEmpty()) {
                    text = context.getText(R.string.toast_update_good_empty);
                } else {
                    text = context.getText(R.string.toast_update_good_good);
                }

            } else {//Error call
                text = context.getText(R.string.toast_update_error);
            }
            Intent broadcastIntent = new Intent();
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            broadcastIntent.setAction(UpdateActivityReceiver.ACTION_RESP);
            broadcastIntent.putExtra(UpdateActivityReceiver.ACTION, UpdateActivityReceiver.ACTION_TOAST);
            broadcastIntent.putExtra(UpdateActivityReceiver.TOAST_STRING, text);
            sendBroadcast(broadcastIntent);
        }

        if (currentCaller == CALLER_IS_RECEIVER) {//Call as a regular service


            if (result && updatedAuthors.isEmpty() && !settings.getDebugFlag()) {
                return;//no errors and no updates - no notification
            }

            if (!result && settings.getIgnoreErrorFlag()) {
                return;//error and we ignore them
            }

            NotificationData notifyData = NotificationData.getInstance(context);
            if (result) {//we have updates

                if (updatedAuthors.isEmpty()) {//DEBUG CASE
                    notifyData.notifyUpdateDebug(context);

                } else {

                    notifyData.notifyUpdate(context, updatedAuthors);
                }

            } else {//connection Error
                notifyData.notifyUpdateError(context);

            }
        }
    }

    private void sendUpdate(int total, int iCurrent, String name) {

        //String str = context.getText(R.string.update_update)+"  ["+iCurrent+"/"+total+"]:   "+name;
        String str = " ["+iCurrent+"/"+total+"]:   "+name;
        Intent broadcastIntent = new Intent();
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.setAction(UpdateActivityReceiver.ACTION_RESP);
        broadcastIntent.putExtra(UpdateActivityReceiver.ACTION, UpdateActivityReceiver.ACTION_PROGRESS);
        broadcastIntent.putExtra(UpdateActivityReceiver.TOAST_STRING, str);
        sendBroadcast(broadcastIntent);

    }
}
