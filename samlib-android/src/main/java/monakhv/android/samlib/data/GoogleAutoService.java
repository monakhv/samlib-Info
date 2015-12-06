/*
 *   Copyright 2015 Dmitry Monakhov.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *
 */

package monakhv.android.samlib.data;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.drive.DriveFile;

import java.io.File;
import java.util.List;


/**
 * Service to make Silent db backup to Google Drive
 * <p/>
 * Started from Android GUI Adapter
 * Created by monakhv on 01.12.15.
 */
public class GoogleAutoService extends Service {
    private static final String DEBUG_TAG = "GoogleAutoService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SettingsHelper settingsHelper = new SettingsHelper(this);
        GoogleCopy gc = new GoogleCopy(this, settingsHelper.getGoogleAccount());

        gc.execute();


        return START_NOT_STICKY;
    }

    private class GoogleCopy extends ApiClientAsyncTask<Void, Void, Boolean> {

        public GoogleCopy(Context context, String account) {
            super(context, account);
        }

        @Override
        protected Boolean doInBackgroundConnected(Void... params) {
            DataExportImport dei = new DataExportImport(GoogleAutoService.this);
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
                Log.i(DEBUG_TAG, "Copy compleate");
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
     * @param context
     */
    public static void startService(Context context) {
        Intent intent = new Intent(context, GoogleAutoService.class);
        context.startService(intent);
    }
}
