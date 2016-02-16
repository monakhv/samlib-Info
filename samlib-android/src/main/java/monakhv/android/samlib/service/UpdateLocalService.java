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
import android.os.*;
import android.support.annotation.Nullable;
import android.util.Log;
import in.srain.cube.views.ptr.util.PrefsUtil;


import monakhv.android.samlib.data.SettingsHelper;
import monakhv.android.samlib.sortorder.AuthorSortOrder;
import monakhv.samlib.db.entity.Author;
import monakhv.samlib.service.*;


import java.util.Calendar;
import java.util.List;

/**
 * Bind Service to checkout Authors Updates
 * Created by monakhv on 23.11.15.
 */
public class UpdateLocalService extends MyService {
    private static final String DEBUG_TAG = "UpdateLocalService";

    public static final String PREF_NAME = "monakhv.android.samlib.service.UpdateLocalService";
    public static final String PREF_KEY_LAST_UPDATE = PREF_NAME + ".LAST_UPDATE";
    public static final String PREF_KEY_CALLER = PREF_NAME + ".CALLER";

    public static final String ACTION_STOP = "UpdateLocalService.ACTION_STOP";
    public static final String ACTION_UPDATE = "UpdateLocalService.ACTION_UPDATE";
    public static final String EXTRA_ARGUMENT = "UpdateLocalService.EXTRA_ARGUMENT";




    private final IBinder mBinder = new LocalBinder();

    //private DataExportImport dataExportImport;

    private SharedPreferences mSharedPreferences;
    private MessageConstructor mMessageConstructor;


    private static boolean isRun = false;
    private boolean isReceiver = false;
    private static PowerManager.WakeLock wl;
    private static Thread mThread;



    public UpdateLocalService() {
        super();

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

            ArgumentData arg = intent.getExtras().getParcelable(EXTRA_ARGUMENT);
            isReceiver = arg.isReceiver==1;
            runService(arg);

        }


        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    /**
     * Start service - use for receiver Calls
     *
     * @param ctx - Context
     */
    public static void makeUpdate(Context ctx) {
        Intent service = new Intent(ctx, UpdateLocalService.class);
        service.setAction(UpdateLocalService.ACTION_UPDATE);
        service.putExtra(UpdateLocalService.EXTRA_ARGUMENT, new ArgumentData());
        ctx.startService(service);
    }

    public static void makeUpdate(Context context,Author author, AuthorGuiState state){
        Intent service = new Intent(context, UpdateLocalService.class);
        service.setAction(UpdateLocalService.ACTION_UPDATE);
        ArgumentData argumentData;

        if (author==null){
            argumentData=new ArgumentData(state);
        }else {
            argumentData=new ArgumentData(author,state);
        }


        service.putExtra(UpdateLocalService.EXTRA_ARGUMENT, argumentData);
        context.startService(service);
    }


    private void runService(ArgumentData argDaya) {


        if (isRun && !isReceiver) {
            Log.i(DEBUG_TAG, "runService: Update already running exiting");
            return;
        }

        mSharedPreferences = PrefsUtil.getSharedPreferences(this, PREF_NAME);
        getSettingsHelper().requestFirstBackup();

        mSharedPreferences.edit().putBoolean(PREF_KEY_CALLER, isReceiver).apply();

        if (isReceiver && !getSettingsHelper().haveInternetWIFI()) {
            monakhv.samlib.log.Log.d(DEBUG_TAG, "runService: Ignore update task - we have no internet connection");
            return;
        }
        if (!SettingsHelper.haveInternet(this)) {
            Log.e(DEBUG_TAG, "runService: Ignore update - we have no internet connection");

            return;
        }

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, DEBUG_TAG);
        wl.acquire();

        SpecialSamlibService service=getSpecialSamlibService();
        service.setCallerIsReceiver(isReceiver);
        mThread = new SamlibUpdateTread(service, argDaya);

        getBus().getObservable()
                .subscribe(guiUpdateObject -> {
                    if (guiUpdateObject.isProgress()) {
                        if (mMessageConstructor == null) {
                            mMessageConstructor = new MessageConstructor(this, getSettingsHelper());
                        }
                        mMessageConstructor.updateNotification((SamlibUpdateProgress) guiUpdateObject.getObject());
                    }
                    if (guiUpdateObject.isResult()) {
                        mMessageConstructor.cancelProgress();
                        mMessageConstructor.showUpdateNotification((Result) guiUpdateObject.getObject());
                    }
                    if (guiUpdateObject.isAuthor()) {
                        mMessageConstructor.updateNotification((Author) guiUpdateObject.getObject());
                    }
                });
        mThread.start();


    }


    /**
     * Interrupt the thread if running
     */
    private void interrupt() {

        if (mMessageConstructor != null) {
            mMessageConstructor.cancelProgress();
        }

        if (isRun) {
            Log.d(DEBUG_TAG, "Making STOP");
            mThread.interrupt();
            getHttpClientController().cancelAll();

   //         UpdateLocalService.this.mSamlibApplication.releaseServiceComponent();
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

    private class SamlibUpdateTread extends Thread {
        final private SamlibUpdateService mSamlibUpdateService;
        final private ArgumentData mData;

        public SamlibUpdateTread(SamlibUpdateService samlibUpdateService, ArgumentData data) {
            mSamlibUpdateService = samlibUpdateService;
            mData=data;
        }

        @Override
        public void run() {
            super.run();
            isRun = true;
            boolean result;
            if (isReceiver) {
                String sTag = getSettingsHelper().getUpdateTag();
                int iSelected = Integer.parseInt(sTag);
                String order = AuthorSortOrder.valueOf(getSettingsHelper().getAuthorSortOrderString()).getOrder();
                List<Author> authors = getAuthorController().getAll(iSelected, order);
                result = mSamlibUpdateService.runUpdateService(authors, new AuthorGuiState(iSelected, order));

            } else {
                if (mData.author_id == -1) {
                    result = mSamlibUpdateService.runUpdateService(mData.getState());
                } else {
                    Author author=getAuthorController().getById(mData.author_id);
                    result = mSamlibUpdateService.runUpdateService(author, mData.getState());
                }
            }
            if (result) {
                if (getSettingsHelper().getLimitBookLifeTimeFlag() && isReceiver) {
                    CleanBookServiceIntent.start(UpdateLocalService.this);
                }

                mSharedPreferences.edit().putLong(PREF_KEY_LAST_UPDATE, Calendar.getInstance().getTime().getTime()).apply();
            }


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

    private static class ArgumentData implements Parcelable{
        int state_id;
        String order;
        int author_id=-1;
        int isReceiver;

        public ArgumentData(){
            isReceiver=1;
        }

        public ArgumentData(AuthorGuiState state){
            state_id=state.getSelectedTagId();
            order=state.getSorOrder();
            isReceiver=0;
        }
        public ArgumentData(Author author,AuthorGuiState state){
            this(state);
            author_id=author.getId();
        }

        public AuthorGuiState getState(){
            return new AuthorGuiState(state_id,order);
        }
        protected ArgumentData(Parcel in) {
            state_id = in.readInt();
            order = in.readString();
            author_id = in.readInt();
            isReceiver=in.readInt();
        }

        public static final Creator<ArgumentData> CREATOR = new Creator<ArgumentData>() {
            @Override
            public ArgumentData createFromParcel(Parcel in) {
                return new ArgumentData(in);
            }

            @Override
            public ArgumentData[] newArray(int size) {
                return new ArgumentData[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(state_id);
            dest.writeString(order);
            dest.writeInt(author_id);
            dest.writeInt(isReceiver);
        }
    }

}
