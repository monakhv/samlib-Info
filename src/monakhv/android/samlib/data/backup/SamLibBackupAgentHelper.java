package monakhv.android.samlib.data.backup;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;

import android.app.backup.SharedPreferencesBackupHelper;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.IOException;


import monakhv.android.samlib.data.SettingsHelper;


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
    // The name of the SharedPreferences file


    // A key to uniquely identify the set of backup data
    static final String PREFS_BACKUP_KEY = "prefs";
    static final String PREFS_AUTHORS = "AUTHOR_LIST";

    @Override
    public void onCreate() {
        Log.d(DEBUG_TAG,"onCreate");

        AuthorStatePrefs aa = new AuthorStatePrefs(getApplicationContext());
        aa.load();
        addHelper(PREFS_BACKUP_KEY, new SharedPreferencesBackupHelper(this, SettingsHelper.PREFS_NAME));
        addHelper(PREFS_AUTHORS, new SharedPreferencesBackupHelper(this, AuthorStatePrefs.PREF_NAME));

    }


    @Override
    public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState) throws IOException {
        super.onRestore(data, appVersionCode, newState);
        AuthorStatePrefs aa = new AuthorStatePrefs(getApplicationContext());
        aa.restore();

    }

}
