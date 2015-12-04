package monakhv.android.samlib.data;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.*;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import monakhv.android.samlib.R;

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
public abstract class ApiClientAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
    private static final String DEBUG_TAG = "ApiClientAsyncTask";
    protected static final long TIMEOUT_SEC = 20;
    private static final int BUF_SIZE = 4096;
    private static final int RETRY_SYNC = 10;
    private static final String MimeType = "application/octet-stream";

    private final GoogleApiClient mClient;
    private final Context context;
    private final String account;
    private String errorMsg;

    public ApiClientAsyncTask(Context context, String account) {
        this.context = context;
        this.account = account;

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
        } catch (InterruptedException ex) {
            return null;
        }
        if (!mClient.isConnected()) {
            return null;
        }

        try {
            return doInBackgroundConnected(params);
        } finally {
            mClient.disconnect();
        }

    }

    protected GoogleApiClient getGoogleApiClient() {
        return mClient;
    }

    protected abstract Result doInBackgroundConnected(Params... params);

    public abstract void onConnectionFailedTask(ConnectionResult connectionResult);

    /**
     * Making request to make full re sync
     */
    protected void reSync() {
        Drive.DriveApi.requestSync(getGoogleApiClient())
                .await(TIMEOUT_SEC, TimeUnit.SECONDS);
    }

    /**
     * get default drive folder to store data
     *
     * @return Folder
     */
    protected DriveFolder getFolder() {
        //return Drive.DriveApi.getAppFolder(getGoogleApiClient());
        return Drive.DriveApi.getRootFolder(getGoogleApiClient());
    }

    /**
     * Search file by name
     *
     * @return file or null if not found or Error
     */
    protected List<DriveFile> getFile(String fileName) {
        List<DriveFile> files = new ArrayList<>();
        DriveFolder folder = getFolder();
        Query query = new Query.Builder().addFilter(Filters.eq(SearchableField.TITLE, fileName)).build();
        DriveApi.MetadataBufferResult res = folder.queryChildren(getGoogleApiClient(), query).await(TIMEOUT_SEC, TimeUnit.SECONDS);

        if (!res.getStatus().isSuccess()) {
            setError("Error Search File");
            return null;
        }
        MetadataBuffer mdSet = res.getMetadataBuffer();
        int count = mdSet.getCount();
        Log.d(DEBUG_TAG, "Found " + count + " files for " + account);
        int i = 0;
        while (i < count) {
            Metadata data = mdSet.get(i);
            DriveFile file = Drive.DriveApi.getFile(getGoogleApiClient(), data.getDriveId());

            Log.d(DEBUG_TAG, i + " " + data.getModifiedDate());
            Log.d(DEBUG_TAG, i + " " + data.getModifiedByMeDate());
            Log.d(DEBUG_TAG, i + " " + data.getCreatedDate());

            if (data.getDriveId().getResourceId() != null && !data.isTrashed()) {
                Log.d(DEBUG_TAG, "add  file  " + i);
                files.add(file);
            }
            ++i;
        }
        return files;
    }

    /**
     * Waiting for full synchronization of the file when  ModifiedDate=ModifiedByMeDate
     *
     * @param file file is required to synchronize
     * @return true if the file is synchronized
     */
    protected boolean makeSync(DriveFile file) {
        DriveResource.MetadataResult metadataResult;
        int i = 0;
        while (i < RETRY_SYNC) {
            reSync();
            int is = i + 1;
            Log.d(DEBUG_TAG, "Retry number: " + i + "  sleep " + is + " second");
            try {
                TimeUnit.SECONDS.sleep(i + 1);
            } catch (InterruptedException ex1) {
                Log.e(DEBUG_TAG, "Sleep interrupted: ", ex1);
                setError(R.string.res_export_google_interrupt);
                return false;
            }
            metadataResult = file.getMetadata(getGoogleApiClient()).await(TIMEOUT_SEC, TimeUnit.SECONDS);

            if (!metadataResult.getStatus().isSuccess()) {
                setError("Error re-read file");
                return false;
            }
            Date mDate = metadataResult.getMetadata().getModifiedDate();
            Date mMeDate = metadataResult.getMetadata().getModifiedByMeDate();
            Date cDate = metadataResult.getMetadata().getCreatedDate();
            Log.d(DEBUG_TAG, i + " " + mDate);
            Log.d(DEBUG_TAG, i + " " + mMeDate);
            Log.d(DEBUG_TAG, i + " " + cDate);
            if (mDate.equals(mMeDate)) {
                return true;
            }
            ++i;
        }
        setError(R.string.res_export_google_delayed);
        return false;
    }

    /**
     * Blocking method to write data to existing file
     *
     * @param dataBase file to read from
     * @param file     file to write to
     * @return result status
     */
    protected boolean writeFile(final File dataBase, final DriveFile file) {
        Date date = Calendar.getInstance().getTime();
        Log.d(DEBUG_TAG, "Current: " + date);
        DriveApi.DriveContentsResult result = file.open(getGoogleApiClient(), DriveFile.MODE_WRITE_ONLY, null)
                .await(TIMEOUT_SEC, TimeUnit.SECONDS);
        if (!result.getStatus().isSuccess()) {
            setError("Error Writing to file");
            return false;
        }
        DriveContents contents = result.getDriveContents();
        OutputStream output = contents.getOutputStream();
        try {
            FileInputStream input = new FileInputStream(dataBase);
            byte buffer[] = new byte[BUF_SIZE];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            setError("Error Writing to file");
            return false;

        }
        com.google.android.gms.common.api.Status status = contents.commit(getGoogleApiClient(), null).await(TIMEOUT_SEC, TimeUnit.SECONDS);
        if (!status.isSuccess()) {
            setError("Error Commit file");
            return false;
        }

        return makeSync(file);
    }


    /**
     * Read data base from the file
     *
     * @param file     the to read from
     * @param dataBase data base to restore
     * @return result status
     */
    protected boolean readFile(final DriveFile file, File dataBase) {

        DriveApi.DriveContentsResult result = file.open(getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null)
                .await(TIMEOUT_SEC, TimeUnit.SECONDS);
        if (!result.getStatus().isSuccess()) {
            setError("Error Reading  file");

            return false;
        }
        InputStream input = result.getDriveContents().getInputStream();
        try {
            FileOutputStream output = new FileOutputStream(dataBase);
            byte buffer[] = new byte[BUF_SIZE];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }

            return true;
        } catch (Exception ex) {
            setError("Error Reading to file");

            return false;
        }

    }

    /**
     * Blocking method to Add new file data to Google Drive
     */
    protected boolean createFile(File dataBase, String fileName) {
        DriveApi.DriveContentsResult contentsResult = Drive.DriveApi.newDriveContents(getGoogleApiClient()).await(TIMEOUT_SEC, TimeUnit.SECONDS);
        if (!contentsResult.getStatus().isSuccess()) {
            setError(R.string.res_export_google_bad);
            return false;
        }
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(fileName)
                .setMimeType(MimeType)
                .setStarred(true).build();

        DriveContents originalContents = contentsResult.getDriveContents();
        OutputStream os = originalContents.getOutputStream();
        //write data to file
        try {
            FileInputStream input = new FileInputStream(dataBase);
            byte buffer[] = new byte[BUF_SIZE];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            setError("Error Writing to file");
            return false;
        }
        // create a file
        DriveFolder.DriveFileResult fileResult = getFolder()
                .createFile(getGoogleApiClient(), changeSet, originalContents)
                .await(TIMEOUT_SEC, TimeUnit.SECONDS);

        if (!fileResult.getStatus().isSuccess()) {
            setError("Error saving to file");
            return false;
        }
        //reread file
        DriveResource.MetadataResult metadataResult = fileResult.getDriveFile()
                .getMetadata(getGoogleApiClient())
                .await(TIMEOUT_SEC, TimeUnit.SECONDS);

        if (!metadataResult.getStatus().isSuccess()) {
            setError("Error test writing");
            return false;
        }
        DriveFile file = Drive.DriveApi.getFile(getGoogleApiClient(), metadataResult.getMetadata().getDriveId());

        return makeSync(file);
    }


    public void setError(String message) {
        errorMsg = message;

    }

    public void setError(int res) {
        setError(context.getString(res));
    }

    public String getErrorMsg() {
        return errorMsg;
    }
}
