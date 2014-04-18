package monakhv.android.samlib.data;


import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;

import android.util.Log;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import com.google.android.gms.drive.Contents;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;


import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import monakhv.android.samlib.ArchiveActivity;
import monakhv.android.samlib.R;

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
 * 4/1/14.
 */
public class GoogleDiskOperation extends ApiClientAsyncTask<Void, Void, Boolean> {
    public static final int RESOLVE_CONNECTION_REQUEST_CODE = 21;
    private static final long TIMEOUT_SEC = 20;
    private static final int BUF_SIZE = 4096;


    public enum OperationType {
        EXPORT(R.string.arc_msg_export),
        IMPORT(R.string.arc_msg_import);
        private int iMsg;

        private OperationType(int i) {
            iMsg = i;
        }

        public int getMessage() {
            return iMsg;
        }
    }

    private static final String DEBUG_TAG = "GoogleDiskOperation";
    private static final String FileName = "SamLib-Info.db";
    private static final String MimeType = "application/octet-stream";
    private Activity context;
    private String account;
    private OperationType operation;
    private File dataBase;

    public GoogleDiskOperation(Activity ctx, String account, OperationType operationType) {
        super(ctx, account);
        this.context = ctx;
        this.account = account;
        this.operation = operationType;
        dataBase = DataExportImport.getDataBase(ctx);
       // Log.d(DEBUG_TAG, "Original file: " + dataBase.getAbsolutePath());

    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        if (aBoolean == null) {
            sendResult(false);
            return;
        }
        if (aBoolean) {
            sendResult(true);
        } else {
            sendResult(false);
        }
    }

    @Override
    protected Boolean doInBackgroundConnected(Void... params) {
        DriveFolder folder = Drive.DriveApi.getAppFolder(getGoogleApiClient());
        Query query = new Query.Builder().addFilter(Filters.eq(SearchableField.TITLE, FileName)).build();
        DriveApi.MetadataBufferResult res = folder.queryChildren(getGoogleApiClient(), query).await(TIMEOUT_SEC, TimeUnit.SECONDS);

        if (!res.getStatus().isSuccess()) {
            showMessage("Error Search File");
            sendResult(false);
            return null;
        }
        MetadataBuffer mdSet = res.getMetadataBuffer();
        int count = mdSet.getCount();
        Log.d(DEBUG_TAG, "Found " + count + " files for " + account);
        switch (operation) {
            case EXPORT:
                if (count > 0) {
                    Log.d(DEBUG_TAG, "write to existing file");
                    return writeToFile(Drive.DriveApi.getFile(getGoogleApiClient(), mdSet.get(0).getDriveId()));
                } else {
                    Log.d(DEBUG_TAG, "add new  file");
                    return addFile();
                }

            case IMPORT:
                if (count > 0) {
                    Log.d(DEBUG_TAG, "start reading  file");
                    return readFile(Drive.DriveApi.getFile(getGoogleApiClient(), mdSet.get(0).getDriveId()));
                }
                break;

        }
        return true;
    }

    @Override
    public void onConnectionFailedTask(ConnectionResult connectionResult) {
        Log.d(DEBUG_TAG, "ConnectionFailed Start");
        if (connectionResult.hasResolution()) {
            Log.d(DEBUG_TAG, "ConnectionFailed - has resolution");
            try {
                connectionResult.startResolutionForResult(context, RESOLVE_CONNECTION_REQUEST_CODE);
            } catch (IntentSender.SendIntentException e) {
                sendResult(false);
                // Unable to resolve, message user appopriately
            }
        } else {
            Log.d(DEBUG_TAG, "ConnectionFailed - has NOT  resolution");
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), context, 0).show();
        }

    }


    /**
     * Add new file data to Google Drive
     */
    private boolean addFile() {
        DriveApi.ContentsResult contentsResult = Drive.DriveApi.newContents(getGoogleApiClient()).await(TIMEOUT_SEC, TimeUnit.SECONDS);
        if (!contentsResult.getStatus().isSuccess()) {
            showMessage(R.string.res_export_google_bad);

            return false;
        }
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(FileName)
                .setMimeType(MimeType)
                .setStarred(true).build();

        Contents originalContents = contentsResult.getContents();
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
            showMessage("Error Writing to file");

            return false;
        }
        // create a file
        DriveFolder.DriveFileResult fileResult =Drive.DriveApi.getAppFolder(getGoogleApiClient())
                .createFile(getGoogleApiClient(), changeSet, contentsResult.getContents())
                .await(TIMEOUT_SEC,TimeUnit.SECONDS);

        if (!fileResult.getStatus().isSuccess()){
            showMessage("Error saving to file");

            return false;
        }
        //reread file
        DriveResource.MetadataResult metadataResult = fileResult.getDriveFile()
                .getMetadata(getGoogleApiClient())
                .await(TIMEOUT_SEC,TimeUnit.SECONDS);

        if (!metadataResult.getStatus().isSuccess()) {
            showMessage("Error test writing");

            return false;
        }
        return true;

    }
    private boolean writeToFile(DriveFile file ){
        DriveApi.ContentsResult result = file.openContents(getGoogleApiClient(),DriveFile.MODE_WRITE_ONLY,null)
                .await(TIMEOUT_SEC, TimeUnit.SECONDS);
        if (!result.getStatus().isSuccess()){
            showMessage("Error Writing to file");

            return false;
        }
        Contents contents = result.getContents();
        OutputStream output = contents.getOutputStream();
        try {
            FileInputStream input = new FileInputStream(dataBase);
            byte buffer[] = new byte[BUF_SIZE];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            showMessage("Error Writing to file");

            return false;

        }
        com.google.android.gms.common.api.Status status = file.commitAndCloseContents(getGoogleApiClient(), contents)
                .await(TIMEOUT_SEC, TimeUnit.SECONDS);
        if (!status.isSuccess()){
            showMessage("Error Commit file");

            return false;
        }

        return true;
    }

    private boolean readFile(final DriveFile file) {

        DriveApi.ContentsResult result = file.openContents(getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null)
                .await(TIMEOUT_SEC, TimeUnit.SECONDS);
        if (   ! result.getStatus().isSuccess()){
            showMessage("Error Reading  file");
            sendResult(false);
            return false;
        }
        InputStream input = result.getContents().getInputStream();
        try {
            FileOutputStream output = new FileOutputStream(dataBase);
            byte buffer[] = new byte[BUF_SIZE];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
            sendResult(true);
            return true;
        }
        catch (Exception ex){
            showMessage("Error Reading to file");
            sendResult(false);
            return false;
        }

    }

    public void showMessage(String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public void showMessage(int res) {
        Toast.makeText(context, context.getString(res), Toast.LENGTH_LONG).show();
    }

    private void sendResult(boolean res) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.setAction(ArchiveActivity.GoogleReceiver.ACTION);
        broadcastIntent.putExtra(ArchiveActivity.GoogleReceiver.EXTRA_RESULT, res);
        broadcastIntent.putExtra(ArchiveActivity.GoogleReceiver.EXTRA_OPERATION, operation.toString());
        context.sendBroadcast(broadcastIntent);

    }

}
