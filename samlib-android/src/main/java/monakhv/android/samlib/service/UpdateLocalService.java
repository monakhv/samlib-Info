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
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;
import in.srain.cube.views.ptr.util.PrefsUtil;


import monakhv.android.samlib.SamlibApplication;
import monakhv.android.samlib.dagger.component.ServiceComponent;
import monakhv.android.samlib.data.SettingsHelper;
import monakhv.android.samlib.sortorder.AuthorSortOrder;
import monakhv.samlib.db.entity.Author;
import monakhv.samlib.db.entity.SamLibConfig;
import monakhv.samlib.service.SamlibService;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Bind Service to checkout Authors Updates
 * Created by monakhv on 23.11.15.
 */
public class UpdateLocalService extends MyService {
    private static final String DEBUG_TAG = "UpdateLocalService";

    public static final String PREF_NAME="monakhv.android.samlib.service.UpdateLocalService";
    public static final String PREF_KEY_LAST_UPDATE=PREF_NAME+".LAST_UPDATE";
    public static final String PREF_KEY_CALLER=PREF_NAME+".CALLER";

    public static final String ACTION_STOP = "UpdateLocalService.ACTION_STOP";
    public static final String ACTION_UPDATE = "UpdateLocalService.ACTION_UPDATE";
    public static final String UPDATE_OBJECT = "UpdateLocalService.UPDATE_OBJECT";

    private static final String EXTRA_SELECTED_TAG="UpdateLocalService.EXTRA_SELECTED_TAG";
    private static final String EXTRA_ORDER="UpdateLocalService.EXTRA_ORDER";


    private final IBinder mBinder = new LocalBinder();

    //private DataExportImport dataExportImport;
    private final List<Author> updatedAuthors;
    private SharedPreferences mSharedPreferences;


    private static boolean isRun = false;
    private static PowerManager.WakeLock wl;
    private static UpdateTread mThread;

    private AndroidGuiUpdater.CALLER_TYPE mCALLER_type;
    private Context context;
    SamlibApplication mSamlibApplication;


    public UpdateLocalService() {
        super();
        updatedAuthors = new ArrayList<>();
        // Log.d(DEBUG_TAG, "Constructor Call");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        Log.i(DEBUG_TAG, "OnStart");

        if (action.equalsIgnoreCase(ACTION_STOP)) {
            Log.i(DEBUG_TAG, "OnStart: making stop: is Run " + isRun);
            interrupt();
            stopSelf();
        }
        if (action.equalsIgnoreCase(ACTION_UPDATE)) {
            Log.i(DEBUG_TAG, "OnStart: making update");

            UpdateObject updateObject = intent.getParcelableExtra(UPDATE_OBJECT);

            int iSelected=intent.getIntExtra(EXTRA_SELECTED_TAG, SamLibConfig.TAG_AUTHOR_ALL);
            String order= intent.getStringExtra(EXTRA_ORDER);

            makeRealUpdate(updateObject,iSelected,order);
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
     * @param authorId -Author id
     */
    public static void updateAuthor(Context ctx, int authorId, int tagId,String order) {
        makeUpdate(ctx, SamlibService.UpdateObjectSelector.Author, authorId,tagId,order);
    }

    /**
     * Chane for new updates of all authors with given tag
     *
     * @param tagId Tag id
     */
    public static void updateTag(Context ctx, int tagId,String order) {
        makeUpdate(ctx, SamlibService.UpdateObjectSelector.Tag, tagId,tagId,order);
    }

    /**
     * Start service - use for receiver Calls
     *
     * @param ctx - Context
     */
    public static void makeUpdate(Context ctx) {
        Intent service = new Intent(ctx, UpdateLocalService.class);
        service.setAction(UpdateLocalService.ACTION_UPDATE);
        UpdateObject updateObject=new UpdateObject();
        service.putExtra(UpdateLocalService.UPDATE_OBJECT, updateObject);
        ctx.startService(service);
    }


    private static void makeUpdate(Context ctx, SamlibService.UpdateObjectSelector selector, int id,int selectedTag, String order) {
        Intent service = new Intent(ctx, UpdateLocalService.class);
        service.setAction(UpdateLocalService.ACTION_UPDATE);
        UpdateObject updateObject=new UpdateObject(selector,id);

        service.putExtra(UpdateLocalService.UPDATE_OBJECT, updateObject);
        service.putExtra(EXTRA_SELECTED_TAG,selectedTag);
        service.putExtra(EXTRA_ORDER,order);
        ctx.startService(service);
    }


    private void makeRealUpdate(UpdateObject updateObject, int tagId,String ord) {
        mCALLER_type=updateObject.getCALLER_type();

        if (isRun && updateObject.callerIsActivity()) {
            Log.i(DEBUG_TAG, "makeRealUpdate: Update already running exiting");
            return;
        }
        context = this.getApplicationContext();
        updatedAuthors.clear();

        Log.d(DEBUG_TAG, "makeRealUpdate: makeUpdate");


        mSharedPreferences = PrefsUtil.getSharedPreferences(context, PREF_NAME);
        getSettingsHelper().requestFirstBackup();

        mSharedPreferences.edit().putString(PREF_KEY_CALLER, updateObject.getCALLER_type().name()).apply();



        if ((updateObject.callerIsReceiver()) && !getSettingsHelper().haveInternetWIFI()) {
            monakhv.samlib.log.Log.d(DEBUG_TAG, "makeRealUpdate: Ignore update task - we have no internet connection");

            return;
        }
        mSamlibApplication= (SamlibApplication) getApplication();

        ServiceComponent serviceComponent=mSamlibApplication.getServiceComponent(updateObject,getHelper());

        AndroidGuiUpdater guiUpdate =serviceComponent.getAndroidGuiUpdater();
        if (!SettingsHelper.haveInternet(context)) {
            Log.e(DEBUG_TAG, "makeRealUpdate: Ignore update - we have no internet connection");

            guiUpdate.finishUpdate(false, updatedAuthors);
            return;
        }

        int iSelected;
        String order;
        if (updateObject.callerIsReceiver()){
            String sTag = getSettingsHelper().getUpdateTag();
            iSelected = Integer.parseInt(sTag);
            updateObject.setObjectId(iSelected);
            order = AuthorSortOrder.valueOf(getSettingsHelper().getAuthorSortOrderString()).getOrder();
            Log.i(DEBUG_TAG, "makeRealUpdate: Recurring  call select by tag: "+sTag);
        }
        else {
            iSelected=tagId;
            order=ord;
        }

        //http = HttpClientController.getInstance(mSettingsHelper);
        SpecialSamlibService service =serviceComponent.getSpecialSamlibService();
                //= new SpecialSamlibService(ctl, guiUpdate, mSettingsHelper, http,updateObject,dataExportImport);


        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, DEBUG_TAG);
        wl.acquire();


        mThread = new UpdateTread(service, updateObject,iSelected,order);
        mThread.start();

    }

    /**
     * Interrupt the thread if running
     */
    private void interrupt() {
        if (isRun && (mCALLER_type == AndroidGuiUpdater.CALLER_TYPE.CALLER_IS_ACTIVITY)) {
            Log.d(DEBUG_TAG, "Making STOP");
            mThread.interrupt();
            getHttpClientController().cancelAll();

            UpdateLocalService.this.mSamlibApplication.releaseServiceComponent();
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
        private SpecialSamlibService service;
        private UpdateObject mUpdateObject;
        private int mSelected ;
        private String mOrder;

        public UpdateTread(SpecialSamlibService service, UpdateObject updateObject, int iSelected, String order) {
            this.service = service;
            this.mUpdateObject = updateObject;
            this.mSelected=iSelected;
            this.mOrder=order;
        }

        @Override
        public void run() {
            super.run();
            isRun = true;
            boolean result = service.runUpdate(mUpdateObject.getObjectType(),mUpdateObject.getObjectId(),mSelected,mOrder);

            if (result) {
                if (getSettingsHelper().getLimitBookLifeTimeFlag() && (mUpdateObject.callerIsReceiver())) {
                    CleanBookServiceIntent.start(context);
                }

                mSharedPreferences.edit().putLong(PREF_KEY_LAST_UPDATE, Calendar.getInstance().getTime().getTime()).apply();
            }

            isRun = false;
            UpdateLocalService.this.mSamlibApplication.releaseServiceComponent();
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





}
