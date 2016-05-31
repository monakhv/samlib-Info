/*
 *  Copyright 2016 Dmitry Monakhov.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  15.01.16 14:48
 *
 */

package monakhv.android.samlib.service;



import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.drive.DriveFile;
import monakhv.android.samlib.data.ApiClientAsyncTask;
import monakhv.android.samlib.data.DataExportImport;
import monakhv.android.samlib.data.GoogleDiskOperation;

import java.io.File;
import java.util.List;


/**
 * Service to make Silent db backup to Google Drive
 * <p/>
 * Started from Android GUI Adapter
 * Created by monakhv on 01.12.15.
 */
public class GoogleAutoService extends MyService {
    private static final String DEBUG_TAG = "GoogleAutoService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        GoogleCopy gc = new GoogleCopy(this, getSettingsHelper().getGoogleAccount());

        gc.execute();


        return START_NOT_STICKY;
    }

    private class GoogleCopy extends ApiClientAsyncTask<Void, Void, Boolean> {

        public GoogleCopy(Context context, String account) {
            super(context, account);
        }

        @Override
        protected Boolean doInBackgroundConnected(Void... params) {
            DataExportImport dei = new DataExportImport(getSettingsHelper());
            File dataBase = dei.getDataBase();
            reSync();
            List<DriveFile> files = getFile(GoogleDiskOperation.FileName);
            if (files == null) {
                return false;
            }
            Log.d(DEBUG_TAG, "write to existing file");
            return writeFile(dataBase, files.get(0));
        }

        @Override
        public void onConnectionFailedTask(ConnectionResult connectionResult) {

        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean == null) {
                Log.e(DEBUG_TAG, "result is NULL");
                return;
            }

            if (aBoolean) {
                Log.i(DEBUG_TAG, "Copy complete");
            } else {
                Log.e(DEBUG_TAG, getErrorMsg());
            }
            GoogleAutoService.this.stopSelf();

        }
    }

    /**
     * Start the service
     * Using in AndroidGUIAdapter class in
     *
     * @param context Context
     */
    public static void startService(Context context) {
        Intent intent = new Intent(context, GoogleAutoService.class);
        context.startService(intent);
    }
}
