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

import in.srain.cube.views.ptr.util.PrefsUtil;


import monakhv.android.samlib.R;
import monakhv.android.samlib.data.SettingsHelper;
import monakhv.samlib.db.entity.Author;
import monakhv.samlib.db.entity.SamLibConfig;
import monakhv.samlib.log.Log;
import monakhv.samlib.service.*;
import rx.Subscription;


import java.util.Calendar;


/**
 * Bind Service to checkout Authors Updates
 * Created by monakhv on 23.11.15.
 */
public class UpdateLocalService extends MyService {
    private static final String DEBUG_TAG = "UpdateLocalService";

    public static final String PREF_NAME = "monakhv.android.samlib.service.UpdateLocalService";
    public static final String PREF_KEY_LAST_UPDATE = PREF_NAME + ".LAST_UPDATE";
    private static final String PREF_KEY_CALLER = PREF_NAME + ".CALLER";

    static final String ACTION_STOP = "UpdateLocalService.ACTION_STOP";
    private static final String ACTION_UPDATE = "UpdateLocalService.ACTION_UPDATE";
    private static final String EXTRA_ARGUMENT = "UpdateLocalService.EXTRA_ARGUMENT";


    private final IBinder mBinder = new LocalBinder();

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
            if (arg != null) {
                isReceiver = arg.isReceiver == 1;
            }
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
     * Method to start update process
     *
     * @param context Context
     * @param author  Author can be null, if not null - check the author for updates
     * @param state   Author GUI state to make gui update, contains tagId to update authors by the tag
     */
    public static void makeUpdate(Context context, Author author, AuthorGuiState state) {
        Intent service = new Intent(context, UpdateLocalService.class);
        service.setAction(UpdateLocalService.ACTION_UPDATE);
        ArgumentData argumentData;

        if (author == null) {
            argumentData = new ArgumentData(state);
        } else {
            argumentData = new ArgumentData(author, state);
        }


        service.putExtra(UpdateLocalService.EXTRA_ARGUMENT, argumentData);
        context.startService(service);
    }


    private void runService(ArgumentData argData) {


        if (isRun) {
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
        String title;
        if (argData.author_id == -1) {
            switch (argData.state_id){
                case SamLibConfig.TAG_AUTHOR_ALL:
                    title = getString(R.string.notification_title_TAG_ALL) ;
                    break;
                case SamLibConfig.TAG_AUTHOR_NEW:
                    title = getString(R.string.notification_title_TAG_NEW);
                    break;
                default:
                    title = getString(R.string.notification_title_TAG) + " " + getAuthorController().getTagController().getById(argData.state_id).getName();
            }

        } else {
            title = getAuthorController().getById(argData.author_id).getName();
        }

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, DEBUG_TAG);
        wl.acquire();

        SpecialAuthorService service = getSpecialSamlibService();

        mThread = new SamlibUpdateTread(service, argData);

        final Subscription subscription = getBus().getObservable()
                .distinctUntilChanged()
                .subscribe(guiUpdateObject -> {
                    if (guiUpdateObject.isProgress()) {
                        if (mMessageConstructor == null) {
                            mMessageConstructor = new MessageConstructor(this, getSettingsHelper());
                        }
                        mMessageConstructor.updateNotification((AuthorUpdateProgress) guiUpdateObject.getObject(), title);
                        Log.d(DEBUG_TAG, "runService: progressUpdate");
                    }
                    if (guiUpdateObject.isResult()) {
                        mMessageConstructor.cancelProgress();
                        mMessageConstructor.showUpdateNotification((Result) guiUpdateObject.getObject());
                        Log.d(DEBUG_TAG, "runService: Result");
                    }
                    if (guiUpdateObject.isAuthor() && guiUpdateObject.getUpdateType() == GuiUpdateObject.UpdateType.UPDATE_UPDATE) {
                        Author author = (Author) guiUpdateObject.getObject();
                        mMessageConstructor.updateNotification(author);
                        Log.d(DEBUG_TAG, "runService: AuthorUpdate: " + author.getName());
                    }
                });
        addSubscription(subscription);
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
        final private AuthorUpdateService mAuthorUpdateService;
        final private ArgumentData mData;

        SamlibUpdateTread(AuthorUpdateService authorUpdateService, ArgumentData data) {
            mAuthorUpdateService = authorUpdateService;
            mData = data;
        }

        @Override
        public void run() {
            super.run();
            isRun = true;
            boolean result;

            if (mData.author_id == -1) {
                result = mAuthorUpdateService.runUpdateService(mData.getState());
            } else {
                Author author = getAuthorController().getById(mData.author_id);
                result = mAuthorUpdateService.runUpdateService(author, mData.getState());
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

    private static class ArgumentData implements Parcelable {
        int state_id;
        String order;
        int author_id = -1;
        int isReceiver;

        /**
         * For update all authors by the tag
         *
         * @param state Gui state contains tag id
         */
        ArgumentData(AuthorGuiState state) {
            state_id = state.getSelectedTagId();
            order = state.getSorOrder();
            if (order == null) {
                isReceiver = 1;
            } else {
                isReceiver = 0;
            }

        }

        /**
         * The constructor is used for check update for the single author
         * Call only from Activity
         *
         * @param author Author to update
         * @param state  GUI state
         */
        ArgumentData(Author author, AuthorGuiState state) {
            this(state);
            author_id = author.getId();
            isReceiver = 0;
        }

        AuthorGuiState getState() {
            return new AuthorGuiState(state_id, order);
        }

        ArgumentData(Parcel in) {
            state_id = in.readInt();
            order = in.readString();
            author_id = in.readInt();
            isReceiver = in.readInt();
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
