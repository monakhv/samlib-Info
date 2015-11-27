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
import monakhv.android.samlib.data.SettingsHelper;
import monakhv.android.samlib.sortorder.AuthorSortOrder;
import monakhv.android.samlib.sql.DatabaseHelper;
import monakhv.samlib.db.AuthorController;
import monakhv.samlib.db.entity.Author;
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
    public void updateAuthor(int authoId) {
        makeUpdate(SamlibService.UpdateObjectSelector.Author, authoId);
    }

    /**
     * Chane for new updates of all authors with given tag
     *
     * @param tagId Tag id
     */
    public void updateTag(int tagId) {
        makeUpdate(SamlibService.UpdateObjectSelector.Tag, tagId);
    }

    private void makeUpdate(SamlibService.UpdateObjectSelector selector, int id) {

        if (isRun) {
            Log.i(DEBUG_TAG, "Update already running exiting");
            return;
        }
        Context context = this.getApplicationContext();
        updatedAuthors.clear();
        SettingsHelper settings = new SettingsHelper(context);
        Log.d(DEBUG_TAG, "makeUpdate");
        //dataExportImport = new DataExportImport(context);
        int currentCaller = AndroidGuiUpdater.CALLER_IS_ACTIVITY;

        mSharedPreferences = PrefsUtil.getSharedPreferences(context, UpdateServiceIntent.PREF_NAME);
        settings.requestFirstBackup();

        mSharedPreferences.edit().putInt(UpdateServiceIntent.PREF_KEY_CALLER, currentCaller).apply();
        AuthorController ctl = new AuthorController(getHelper());
        List<Author> authors;
        AndroidGuiUpdater guiUpdate = new AndroidGuiUpdater(context, currentCaller);

        if (!SettingsHelper.haveInternet(context)) {
            Log.e(DEBUG_TAG, "Ignore update - we have no internet connection");

            guiUpdate.finishUpdate(false, updatedAuthors);
            return;
        }

        if (selector == SamlibService.UpdateObjectSelector.Author) {//Check update for the only Author

            //int id = intent.getIntExtra(SELECT_ID, 0);//author_id
            Author author = ctl.getById(id);
            if (author != null) {
                authors = new ArrayList<>();
                authors.add(author);
                Log.i(DEBUG_TAG, "Check single Author: " + author.getName());
            } else {
                Log.e(DEBUG_TAG, "Can not fing Author: " + id);
                return;
            }
        } else {
            authors = ctl.getAll(id, AuthorSortOrder.DateUpdate.getOrder());

            Log.i(DEBUG_TAG, "selection index: " + id);
        }

        SamlibService service = new SamlibService(getHelper(), guiUpdate, settings);


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
        if (isRun) {

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
            mSharedPreferences.edit().putLong(UpdateServiceIntent.PREF_KEY_LAST_UPDATE, Calendar.getInstance().getTime().getTime()).apply();
            isRun = false;
            releaseLock();
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
}
