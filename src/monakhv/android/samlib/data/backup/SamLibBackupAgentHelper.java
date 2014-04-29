package monakhv.android.samlib.data.backup;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;

import android.app.backup.SharedPreferencesBackupHelper;
import android.os.ParcelFileDescriptor;
import android.util.Log;

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


    // A key to uniquely identify the set of backup data
    static final String PREFS_SETTINGS_KEY = "prefs";

    @Override
    public void onCreate() {
        Log.d(DEBUG_TAG, "onCreate");

        AuthorStatePrefs.load(this);
        addHelper(PREFS_SETTINGS_KEY, new SharedPreferencesBackupHelper(this,  AuthorStatePrefs.PREF_NAME));

    }

    @Override
    public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState) throws IOException {
        super.onRestore(data, appVersionCode, newState);
        AuthorStatePrefs.restore(this);

    }

}
