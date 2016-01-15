package monakhv.android.samlib.data.backup;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;

import android.app.backup.BackupDataOutput;
import android.app.backup.SharedPreferencesBackupHelper;
import android.content.Context;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import monakhv.android.samlib.dagger.ApplicationModule;
import monakhv.android.samlib.dagger.DaggerApplicationComponent;
import monakhv.android.samlib.data.SettingsHelper;
import monakhv.android.samlib.sql.DatabaseHelper;

import javax.inject.Inject;
import java.io.IOException;


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
 * 4/25/14.
 */
public class SamLibBackupAgentHelper extends BackupAgentHelper {
    private static final String DEBUG_TAG = "SamLibBackupAgentHelper";
    private volatile DatabaseHelper helper;
    private volatile boolean created = false;
    private volatile boolean destroyed = false;
    @Inject
    SettingsHelper mSettingsHelper;


    // A key to uniquely identify the set of backup data
    static final String PREFS_SETTINGS_KEY = "prefs";

    /**
     * Get a helper for this action.
     */
    public DatabaseHelper getDatabaseHelper() {
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

    @Override
    public void onCreate() {
        Log.d(DEBUG_TAG, "onCreate");
        addHelper(PREFS_SETTINGS_KEY, new SharedPreferencesBackupHelper(this, AuthorStatePrefs.PREF_NAME));
        if (helper == null) {
            helper = getHelperInternal(this);
            created = true;
        }
        DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(getApplicationContext()))
                .build().inject(this);
    }

    @Override
    public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) throws IOException {
        AuthorStatePrefs.load(mSettingsHelper, getDatabaseHelper());
        super.onBackup(oldState, data, newState);
    }

    @Override
    public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState) throws IOException {
        super.onRestore(data, appVersionCode, newState);
        AuthorStatePrefs.restore(mSettingsHelper, getDatabaseHelper());
    }

    @Override
    public void onDestroy() {
        releaseHelper(helper);
        destroyed = true;
        super.onDestroy();
    }
    protected DatabaseHelper getHelperInternal(Context context) {
        @SuppressWarnings({ "unchecked", "deprecation" })
        DatabaseHelper newHelper = (DatabaseHelper) OpenHelperManager.getHelper(context,DatabaseHelper.class);

        return newHelper;
    }
    protected void releaseHelper(DatabaseHelper helper) {
        OpenHelperManager.releaseHelper();

        this.helper = null;
    }
}
