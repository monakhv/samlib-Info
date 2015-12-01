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

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import in.srain.cube.views.ptr.util.PrefsUtil;
import monakhv.android.samlib.R;
import monakhv.android.samlib.data.DataExportImport;
import monakhv.android.samlib.data.SettingsHelper;
import monakhv.android.samlib.sortorder.AuthorSortOrder;
import monakhv.android.samlib.sql.DatabaseHelper;
import monakhv.samlib.data.AbstractSettings;
import monakhv.samlib.db.AuthorController;
import monakhv.samlib.db.DaoBuilder;
import monakhv.samlib.db.TagController;
import monakhv.samlib.db.entity.Author;
import monakhv.samlib.db.entity.Book;
import monakhv.samlib.db.entity.SamLibConfig;
import monakhv.samlib.db.entity.Tag;
import monakhv.samlib.service.GuiUpdate;
import monakhv.samlib.service.SamlibService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Bind Service
 * Created by monakhv on 23.11.15.
 */
public class UpdateLocalService extends Service {
    private static final String DEBUG_TAG = "UpdateLocalService";
    public static final String ACTION_TYPE = "UpdateLocalService.ACTION_TYPE";
    public static final int ACTION_STOP = 101;
    public static final int ACTION_UPDATE = 103;
    public static final String SELECTOR_TYPE = "UpdateLocalService.SELECTOR_TYPE";
    public static final String SELECTOR_ID = "UpdateLocalService.SELECTOR_ID";

    private final IBinder mBinder = new LocalBinder();

    //private DataExportImport dataExportImport;
    private final List<Author> updatedAuthors;
    private SharedPreferences mSharedPreferences;
    private volatile DatabaseHelper helper;
    private volatile boolean created = false;
    private volatile boolean destroyed = false;


    private static boolean isRun = false;
    private static PowerManager.WakeLock wl;
    private static UpdateTread mThread;
    private int currentCaller = 0;
    private SettingsHelper settings;
    private DataExportImport dataExportImport;
    private Context context;

    public UpdateLocalService() {
        super();
        updatedAuthors = new ArrayList<>();
        // Log.d(DEBUG_TAG, "Constructor Call");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int action = intent.getExtras().getInt(ACTION_TYPE);
        Log.i(DEBUG_TAG, "OnStart");

        if (action == ACTION_STOP) {
            Log.i(DEBUG_TAG, "OnStart: making stop: is Run " + isRun);
            interrupt();
            stopSelf();
        }
        if (action == ACTION_UPDATE) {
            Log.i(DEBUG_TAG, "OnStart: making update");
            int id = intent.getExtras().getInt(SELECTOR_ID);
            String nn = intent.getExtras().getString(SELECTOR_TYPE);
            currentCaller = intent.getExtras().getInt(AndroidGuiUpdater.CALLER_TYPE);

            makeUpdate(SamlibService.UpdateObjectSelector.valueOf(nn), id);
        }


        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    /**
     * Check for new update of givenAuthor
     *
     * @param authoId -Author id
     */
    public static void updateAuthor(Context ctx, int authoId) {
        makeUpdate(ctx, SamlibService.UpdateObjectSelector.Author, authoId);
    }

    /**
     * Chane for new updates of all authors with given tag
     *
     * @param tagId Tag id
     */
    public static void updateTag(Context ctx, int tagId) {
        makeUpdate(ctx, SamlibService.UpdateObjectSelector.Tag, tagId);
    }

    /**
     * Start service - use for receiver Calls
     *
     * @param ctx - Context
     */
    public static Intent getUpdateIntent(Context ctx) {
        Intent service = new Intent(ctx, UpdateLocalService.class);
        SettingsHelper settings = new SettingsHelper(ctx);
        String stag = settings.getUpdateTag();
        int idx = Integer.parseInt(stag);
        service.putExtra(UpdateLocalService.ACTION_TYPE, UpdateLocalService.ACTION_UPDATE);
        service.putExtra(UpdateLocalService.SELECTOR_ID, idx);
        service.putExtra(UpdateLocalService.SELECTOR_TYPE, SamlibService.UpdateObjectSelector.Tag.name());
        service.putExtra(AndroidGuiUpdater.CALLER_TYPE, AndroidGuiUpdater.CALLER_IS_RECEIVER);
        return service;
    }


    private static void makeUpdate(Context ctx, SamlibService.UpdateObjectSelector selector, int id) {
        Intent service = new Intent(ctx, UpdateLocalService.class);
        service.putExtra(UpdateLocalService.ACTION_TYPE, UpdateLocalService.ACTION_UPDATE);
        service.putExtra(UpdateLocalService.SELECTOR_ID, id);
        service.putExtra(UpdateLocalService.SELECTOR_TYPE, selector.name());
        service.putExtra(AndroidGuiUpdater.CALLER_TYPE, AndroidGuiUpdater.CALLER_IS_ACTIVITY);

        ctx.startService(service);
    }


    private void makeUpdate(SamlibService.UpdateObjectSelector selector, int id) {

        if (isRun && (currentCaller == AndroidGuiUpdater.CALLER_IS_ACTIVITY)) {
            Log.i(DEBUG_TAG, "Update already running exiting");
            return;
        }
        context = this.getApplicationContext();
        updatedAuthors.clear();
        settings = new SettingsHelper(context);
        Log.d(DEBUG_TAG, "makeUpdate");
        dataExportImport = new DataExportImport(context);


        mSharedPreferences = PrefsUtil.getSharedPreferences(context, UpdateServiceIntent.PREF_NAME);
        settings.requestFirstBackup();

        mSharedPreferences.edit().putInt(UpdateServiceIntent.PREF_KEY_CALLER, currentCaller).apply();
        AuthorController ctl = new AuthorController(getHelper());
        List<Author> authors;


        String notificationTitle;

        if ((currentCaller == AndroidGuiUpdater.CALLER_IS_RECEIVER) && !SettingsHelper.haveInternetWIFI(context)) {
            monakhv.samlib.log.Log.d(DEBUG_TAG, "Ignore update task - we have no internet connection");

            return;
        }

        if (selector == SamlibService.UpdateObjectSelector.Author) {//Check update for the only Author

            //int id = intent.getIntExtra(SELECT_ID, 0);//author_id
            Author author = ctl.getById(id);
            if (author != null) {
                authors = new ArrayList<>();
                authors.add(author);
                notificationTitle = context.getString(R.string.notification_title_author) + " " + author.getName();
                Log.i(DEBUG_TAG, "Check single Author: " + author.getName());
            } else {
                Log.e(DEBUG_TAG, "Can not fing Author: " + id);
                return;
            }
        } else {//Check update for authors by TAG
            authors = ctl.getAll(id, AuthorSortOrder.DateUpdate.getOrder());
            notificationTitle = context.getString(R.string.notification_title_TAG);
            if (id == SamLibConfig.TAG_AUTHOR_ALL) {
                notificationTitle += " " + context.getString(R.string.filter_all);
            } else if (id == SamLibConfig.TAG_AUTHOR_NEW) {
                notificationTitle += " " + context.getString(R.string.filter_new);
            } else {
                TagController tagCtl = new TagController(getHelper());
                Tag tag = tagCtl.getById(id);
                if (tag != null) {
                    notificationTitle += " " + tag.getName();
                }

            }
            Log.i(DEBUG_TAG, "selection index: " + id);
        }
        AndroidGuiUpdater guiUpdate = new AndroidGuiUpdater(context, currentCaller, notificationTitle);
        if (!SettingsHelper.haveInternet(context)) {
            Log.e(DEBUG_TAG, "Ignore update - we have no internet connection");

            guiUpdate.finishUpdate(false, updatedAuthors);
            return;
        }

        SpecialSamlibService service = new SpecialSamlibService(getHelper(), guiUpdate, settings);


        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, DEBUG_TAG);
        wl.acquire();


        mThread = new UpdateTread(service, authors);
        mThread.start();

    }

    /**
     * Interrupt the thread if running
     */
    public void interrupt() {
        if (isRun && (currentCaller == AndroidGuiUpdater.CALLER_IS_ACTIVITY)) {

            mThread.interrupt();

            releaseLock();
        }
    }

    public boolean isRunning() {
        return isRun;
    }


    public class LocalBinder extends Binder {
        public UpdateLocalService getService() {
            return UpdateLocalService.this;
        }
    }

    private class UpdateTread extends Thread {
        private SamlibService service;
        private List<Author> authors;

        public UpdateTread(SamlibService service, List<Author> authors) {
            this.service = service;
            this.authors = authors;
        }

        @Override
        public void run() {
            super.run();
            isRun = true;
            service.runUpdate(authors);
            if (settings.getLimitBookLifeTimeFlag() && (currentCaller == AndroidGuiUpdater.CALLER_IS_RECEIVER)) {
                CleanBookServiceIntent.start(context);
            }

            mSharedPreferences.edit().putLong(UpdateServiceIntent.PREF_KEY_LAST_UPDATE, Calendar.getInstance().getTime().getTime()).apply();
            isRun = false;
            releaseLock();
            UpdateLocalService.this.stopSelf();
        }
    }

    /**
     * Release power lock
     */
    private void releaseLock() {
        if (wl.isHeld()) {
            wl.release();
        }
    }

    @Override
    public void onCreate() {
        if (helper == null) {
            helper = getHelperInternal(this);
            created = true;
        }
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseHelper();
        destroyed = true;
    }

    public DatabaseHelper getHelper() {
        if (helper == null) {
            if (!created) {
                throw new IllegalStateException("A call has not been made to onCreate() yet so the helper is null");
            } else if (destroyed) {
                throw new IllegalStateException(
                        "A call to onDestroy has already been made and the helper cannot be used after that point");
            } else {
                throw new IllegalStateException("Helper is null for some unknown reason");
            }
        } else {
            return helper;
        }
    }

    /**
     * This is called internally by the class to populate the helper object instance. This should not be called directly
     * by client code unless you know what you are doing. Use {@link #getHelper()} to get a helper instance. If you are
     * managing your own helper creation, override this method to supply this activity with a helper instance.
     * <p/>
     * <p/>
     * <b> NOTE: </b> I
     */
    protected DatabaseHelper getHelperInternal(Context context) {
        @SuppressWarnings({"unchecked", "deprecation"})
        DatabaseHelper newHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        return newHelper;
    }

    /**
     * Release the helper instance created in {@link #getHelperInternal(Context)}. You most likely will not need to call
     * this directly since {@link #onDestroy()} does it for you.
     * <p/>
     * <p>
     * <b> NOTE: </b> If you override this method, you most likely will need to override the
     * {@link #getHelperInternal(Context)} method as well.
     * </p>
     */
    protected void releaseHelper() {
        OpenHelperManager.releaseHelper();
        this.helper = null;
    }

    /**
     * Special Service with loadBook method
     */
    public class SpecialSamlibService extends SamlibService {

        public SpecialSamlibService(DaoBuilder sql, GuiUpdate guiUpdate, AbstractSettings settingsHelper) {
            super(sql, guiUpdate, settingsHelper);
        }

        @Override
        public void loadBook(Author a) {

            if (currentCaller == AndroidGuiUpdater.CALLER_IS_RECEIVER) {
                for (Book book : authorController.getBookController().getBooksByAuthor(a)) {//book cycle for the author to update
                    if (book.isIsNew() && settings.testAutoLoadLimit(book) && dataExportImport.needUpdateFile(book)) {
                        monakhv.samlib.log.Log.i(DEBUG_TAG, "Auto Load book: " + book.getId());
                        DownloadBookServiceIntent.start(context, book.getId(), AndroidGuiUpdater.CALLER_IS_RECEIVER);//we do not need GUI update
                    }
                }

            }


        }
    }
}
