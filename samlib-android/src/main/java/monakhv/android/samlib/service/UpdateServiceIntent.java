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


import java.util.ArrayList;
import java.util.List;


import monakhv.android.samlib.data.DataExportImport;
import monakhv.android.samlib.data.SettingsHelper;
import monakhv.android.samlib.sortorder.AuthorSortOrder;
import monakhv.samlib.db.AuthorController;

import monakhv.samlib.db.DaoBuilder;
import monakhv.samlib.db.entity.SamLibConfig;


import monakhv.samlib.db.entity.Author;
import monakhv.samlib.db.entity.Book;
import monakhv.samlib.log.Log;
import monakhv.samlib.service.GuiUpdate;
import monakhv.samlib.service.AuthorService;

/**
 * Service to making check for author updates Can be called from activity or
 * from alarm manager
 *
 * @author monakhv
 */
public class UpdateServiceIntent extends MyServiceIntent {

    private static final String CALLER_TYPE = "CALLER_TYPE";
    private static final String SELECT_INDEX = "SELECT_INDEX";
    private static final String SELECT_ID="SELECT_ID";
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
        GuiUpdate guiUpdate = new AndroidGuiUpdater(context,currentCaller);

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

                guiUpdate.finishUpdate(false,updatedAuthors);
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



        AuthorService service = new SpecialAuthorService(getHelper(),guiUpdate,settings );


        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, DEBUG_TAG);
        wl.acquire();

        service.runUpdate(authors);

        wl.release();
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

    /**
     * Special Service with loadBook method
     */
    public class SpecialAuthorService extends AuthorService {

        public SpecialAuthorService(DaoBuilder sql, GuiUpdate guiUpdate, monakhv.samlib.data.SettingsHelper settingsHelper) {
            super(sql, guiUpdate, settingsHelper);
        }

        @Override
        public void loadBook(Author a) {
            if (settings.getAutoLoadFlag()) {//download the book

                for (Book book : authorController.getBookController().getBooksByAuthor(a)) {//book cycle for the author to update
                    if (book.isIsNew() && settings.testAutoLoadLimit(book) && dataExportImport.needUpdateFile(book)) {
                        Log.i(DEBUG_TAG, "Auto Load book: " + book.getId());
                        DownloadBookServiceIntent.start(context, book,false);
                    }
                }
            }

        }
    }
}
