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
import monakhv.samlib.db.AuthorController;

import monakhv.samlib.db.entity.SamLibConfig;
import monakhv.samlib.exception.SamlibParseException;


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
public class UpdateServiceIntent extends MyServiceIntent {
    public static final long SLEEP_INTERVAL_SECONDS=1;
    private static final String CALLER_TYPE = "CALLER_TYPE";
    private static final String SELECT_INDEX = "SELECT_INDEX";
    private static final String SELECT_ID="SELECT_ID";
    private static final int CALLER_IS_ACTIVITY = 1;
    private static final int CALLER_IS_RECEIVER = 2;
    private static final String DEBUG_TAG = "UpdateServiceIntent";
    private int currentCaller = 0;
    private Context context;
    private SettingsHelper settings;
    private DataExportImport dataExportImport;
    private final List<Author> updatedAuthors;

    public UpdateServiceIntent() {
        super("UpdateServiceIntent");
        updatedAuthors = new ArrayList<>();
       // Log.d(DEBUG_TAG, "Constructor Call");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int skippedAuthors = 0;
        context = this.getApplicationContext();
        updatedAuthors.clear();
        settings = new SettingsHelper(context);
        Log.d(DEBUG_TAG, "Got intent");
        dataExportImport = new DataExportImport(context);
        currentCaller = intent.getIntExtra(CALLER_TYPE, 0);
        int idx  = intent.getIntExtra(SELECT_INDEX, SamLibConfig.TAG_AUTHOR_ALL);

        settings.requestFirstBackup();
        if (currentCaller == 0) {
            Log.e(DEBUG_TAG, "Wrong Caller type");

            return;
        }
        AuthorController ctl = new AuthorController(getHelper());
        List<Author> authors;

        if (currentCaller == CALLER_IS_RECEIVER) {
            String stag = settings.getUpdateTag();
            int tag_id = Integer.parseInt(stag);
            idx = tag_id;
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
        if (idx == SamLibConfig.TAG_AUTHOR_ID){

            int id = intent.getIntExtra(SELECT_ID,0);
            Author author = ctl.getById(id);
            if (author != null){
                authors = new ArrayList<>();
                authors.add(author);
                Log.i(DEBUG_TAG,"Check single Author: "+author.getName());
            }
            else {
                Log.e(DEBUG_TAG,"Can not fing Author: "+id);
                return;
            }
        }
        else {
            authors=ctl.getAll(idx,AuthorSortOrder.DateUpdate.getOrder());

            Log.i(DEBUG_TAG, "selection index: " + idx);
        }



        HttpClientController http = HttpClientController.getInstance(settings);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, DEBUG_TAG);
        wl.acquire();

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
            Author newA=ctl.getEmptyObject();
            try {
                newA = http.getAuthorByURL(url,newA);
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
                sendRefresh(false);
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

    /**
     * Send notification that update is finished
     * and update status
     * @param result false if we have an error during update process
     */
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


    /**
     * Send update status
     * @param total Total number if Author we need checkout
     * @param iCurrent number of current author
     * @param name name of current Author
     */
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

    /**
     * Start service - use for receiver Calls
     * @param ctx - Context
     */
    public static void makeUpdate(Context ctx){
        Intent updater = new Intent(ctx, UpdateServiceIntent.class);
        updater.putExtra(UpdateServiceIntent.CALLER_TYPE, UpdateServiceIntent.CALLER_IS_RECEIVER);
        ctx.startService(updater);
    }
    public static void makeUpdateAuthor(Context ctx,int id){
        Intent service = new Intent(ctx, UpdateServiceIntent.class);
        service.putExtra(UpdateServiceIntent.CALLER_TYPE, UpdateServiceIntent.CALLER_IS_ACTIVITY);
        service.putExtra(UpdateServiceIntent.SELECT_INDEX, SamLibConfig.TAG_AUTHOR_ID);
        service.putExtra(UpdateServiceIntent.SELECT_ID, id);

        ctx.startService(service);
    }
    public static void makeUpdate(Context ctx,int tagId){
        Intent service = new Intent(ctx, UpdateServiceIntent.class);
        service.putExtra(UpdateServiceIntent.CALLER_TYPE, UpdateServiceIntent.CALLER_IS_ACTIVITY);
        service.putExtra(UpdateServiceIntent.SELECT_INDEX, tagId);

        ctx.startService(service);
    }
}
