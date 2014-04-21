package monakhv.android.samlib.data;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFolder;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
 * 4/17/14.
 *
 *
 * Based on
 * https://github.com/googledrive/android-demos/blob/master/src/com/google/android/gms/drive/sample/demo/ApiClientAsyncTask.java
 *
 */
public abstract class ApiClientAsyncTask  <Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
    protected static final long TIMEOUT_SEC = 20;
    private GoogleApiClient mClient;

    public ApiClientAsyncTask(Context context,String account) {

        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(context)
                .addApi(Drive.API)
                .setAccountName(account)
                .addScope(Drive.SCOPE_FILE)
                .addScope(Drive.SCOPE_APPFOLDER);
        mClient = builder.build();
    }

    @Override
    protected Result doInBackground(Params... params) {
        final CountDownLatch latch = new CountDownLatch(1);
        mClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(Bundle bundle) {
                latch.countDown();
            }

            @Override
            public void onConnectionSuspended(int i) {

            }
        });
        mClient.registerConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(ConnectionResult connectionResult) {
                onConnectionFailedTask(connectionResult);
                latch.countDown();
            }
        });
        mClient.connect();
        try {
            latch.await();
        }catch (InterruptedException ex){
            return null;
        }
        if (!mClient.isConnected()){
            return null;
        }

        try {
            return doInBackgroundConnected(params);
        }
        finally {
            mClient.disconnect();
        }

    }

    protected GoogleApiClient getGoogleApiClient() {
        return mClient;
    }
    protected abstract Result doInBackgroundConnected(Params... params);
    public void onConnectionFailedTask(ConnectionResult connectionResult){

    }

    /**
     * Making request to make full re sync
      */
    protected void reSync(){
        Drive.DriveApi.requestSync(getGoogleApiClient())
                .await(TIMEOUT_SEC, TimeUnit.SECONDS);
    }

    /**
     * get default drive folder to store data
     * @return Folder
     */
    protected DriveFolder getFolder(){
        //return Drive.DriveApi.getAppFolder(getGoogleApiClient());
        return Drive.DriveApi.getRootFolder(getGoogleApiClient());
    }
}
