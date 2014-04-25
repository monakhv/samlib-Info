package monakhv.android.samlib.data;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.SharedPreferencesBackupHelper;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import monakhv.android.samlib.tasks.AddAuthor;

import static monakhv.android.samlib.data.DataExportImport.getAuthorUrls;

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
    static final String PREFS = SettingsHelper.PREFS_NAME;

    // A key to uniquely identify the set of backup data
    static final String PREFS_BACKUP_KEY = "prefs";
    static final String APP_DATA_KEY = "AUTHOR_LIST";

    @Override
    public void onCreate() {
        SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(this, PREFS);
        addHelper(PREFS_BACKUP_KEY, helper);

    }

    @Override
    public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) throws IOException {
        super.onBackup(oldState, data, newState);
        boolean doBackup = (oldState == null);
        if (!doBackup) {
            //doBackup = compareStateFile(oldState);
            doBackup =true;
        }
        if (doBackup) {
            ByteArrayOutputStream bufStream = new ByteArrayOutputStream();
            DataOutputStream outWriter = new DataOutputStream(bufStream);
            List<String> aa = getAuthorUrls(getApplicationContext());
            outWriter.writeInt(aa.size());
            for (String u : aa) {
                outWriter.writeUTF(u);
                Log.d(DEBUG_TAG,"backup url: "+u);
            }
            byte[] buffer = bufStream.toByteArray();
            int len = buffer.length;


            data.writeEntityHeader(APP_DATA_KEY, len);
            data.writeEntityData(buffer, len);

            FileOutputStream outstream = new FileOutputStream(newState.getFileDescriptor());
            DataOutputStream out = new DataOutputStream(outstream);
            out.writeInt(aa.size());

        }

    }

//    private boolean compareStateFile(ParcelFileDescriptor oldState) {
//        FileInputStream instream = new FileInputStream(oldState.getFileDescriptor());
//        DataInputStream in = new DataInputStream(instream);
//        try {
//            int aNum = in.readInt();
//            List<String> aa = getAuthorUrls(getApplicationContext());
//            return aNum == aa.size();
//        } catch (IOException e) {
//            Log.e(DEBUG_TAG, "Read state Error", e);
//            return true;
//        }
//
//
//    }

    @Override
    public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState) throws IOException {
        super.onRestore(data, appVersionCode, newState);
        while (data.readNextHeader()) {
            String key = data.getKey();
            int dataSize = data.getDataSize();
            if (APP_DATA_KEY.equals(key)) {
                byte[] dataBuf = new byte[dataSize];
                data.readEntityData(dataBuf, 0, dataSize);
                ByteArrayInputStream baStream = new ByteArrayInputStream(dataBuf);
                DataInputStream in = new DataInputStream(baStream);
                int aNum = in.readInt();
                int i = 0;
                String[] aa = new String[aNum];
                while (i < aNum) {
                    aa[i] = in.readUTF();
                    Log.d(DEBUG_TAG,"read url: "+aa[i]);
                    ++i;
                }
                AddAuthor adder = new AddAuthor(getApplicationContext());
                adder.execute(aa);

            }


        }


    }
}
