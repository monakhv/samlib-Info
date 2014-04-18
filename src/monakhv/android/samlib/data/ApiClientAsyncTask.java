package monakhv.android.samlib.data;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

import java.util.concurrent.CountDownLatch;

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
}
