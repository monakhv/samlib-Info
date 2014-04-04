package monakhv.android.samlib.data;


import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Contents;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;


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
import java.util.ArrayList;
import java.util.List;

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
public class GoogleDiskOperation extends AsyncTask<Void, Void, Boolean> implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    public static final int RESOLVE_CONNECTION_REQUEST_CODE = 21;
    private static final int BUF_SIZE=4096;


    public enum OperationType {
        EXPORT(R.string.arc_msg_export),
        IMPORT(R.string.arc_msg_import);
        private int iMsg;
        private OperationType(int i){
            iMsg=i;
        }
        public int getMessage(){
            return iMsg;
        }
    }

    private static final String DEBUG_TAG = "GoogleDiskOperation";
    private static final String FileName = "SamLib-Info.db";
    private static final String MimeType = "application/octet-stream";
    private Activity context;
    private String account;
    private GoogleApiClient mGoogleApiClient;
    private OperationType operation;
    private File dataBase;

    public GoogleDiskOperation(Activity ctx, String account,OperationType operationType) {
        this.context = ctx;
        this.account = account;
        this.operation=operationType;
        dataBase = DataExportImport.getDataBase(ctx);

    }

    @Override
    protected Boolean doInBackground(Void... params) {


        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Drive.API)
                .setAccountName(account)
                .addScope(Drive.SCOPE_FILE)
                .addScope(Drive.SCOPE_APPFOLDER)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        try {
            if (mGoogleApiClient.isConnected()){
                Log.d(DEBUG_TAG, "Re-Connecting");
                mGoogleApiClient.reconnect();
            }
            else {
                Log.d(DEBUG_TAG, "Connecting");
                mGoogleApiClient.connect();
            }

            return true;
        } catch (Exception ex) {
            sendResult(false);
            Log.e(DEBUG_TAG, "Operation " + operation.toString() + " error!", ex);
        }

        return null;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(DEBUG_TAG, "Get Connected");

        getDriveFile();

    }

    private List<DriveFile> getDriveFile() {
        final List<DriveFile> res = new ArrayList<DriveFile>();
        DriveFolder folder = Drive.DriveApi.getAppFolder(mGoogleApiClient);
        Query query = new Query.Builder().addFilter(Filters.eq(SearchableField.TITLE, FileName)).build();
        folder.queryChildren(mGoogleApiClient, query).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
            @Override
            public void onResult(DriveApi.MetadataBufferResult metadataBufferResult) {
                if (!metadataBufferResult.getStatus().isSuccess()) {
                    showMessage("Error Search File");
                    sendResult(false);
                    return;
                }
                MetadataBuffer mdSet = metadataBufferResult.getMetadataBuffer();

                int count = mdSet.getCount();
                Log.d(DEBUG_TAG, "Found " + count + " files for "+account);
                switch (operation) {
                    case EXPORT:
                        if (count > 0) {
                            Log.d(DEBUG_TAG, "write to existing file");
                            writeToFile(Drive.DriveApi.getFile(mGoogleApiClient, mdSet.get(0).getDriveId()));
                        } else {
                            Log.d(DEBUG_TAG, "add new  file");
                            addFile();
                        }
                        break;
                    case IMPORT:
                        if (count > 0) {
                            Log.d(DEBUG_TAG, "start reading  file");
                            readFile(Drive.DriveApi.getFile(mGoogleApiClient, mdSet.get(0).getDriveId()));
                        }
                        break;

                }

//                int i = 0;
//                while (i < count) {
//                    Metadata md = mdSet.get(i);
//                    i++;
//                    res.add(Drive.DriveApi.getFile(mGoogleApiClient, md.getDriveId()));
//
//                    Log.d(DEBUG_TAG, md.getTitle()+" - "+md.getModifiedDate().toString()+" - "+md.getDriveId().toString());
//                }

            }
        });
        return res;


    }

    private void addFile() {
        Drive.DriveApi.newContents(mGoogleApiClient).setResultCallback(new ResultCallback<DriveApi.ContentsResult>() {
            @Override
            public void onResult(DriveApi.ContentsResult contentsResult) {
                if (!contentsResult.getStatus().isSuccess()) {
                    showMessage("Error while trying to create new file contents");
                    sendResult(false);
                    return;
                }
                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                        .setTitle(FileName)
                        .setMimeType(MimeType)
                        .setStarred(true).build();
                // create a file
                Drive.DriveApi.getAppFolder(mGoogleApiClient)
                        .createFile(mGoogleApiClient, changeSet, contentsResult.getContents())
                        .setResultCallback(fileCreateCallback);

            }
        });
    }


    public void showMessage(String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(DEBUG_TAG, "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
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

    private ResultCallback<DriveFolder.DriveFileResult> fileCreateCallback = new ResultCallback<DriveFolder.DriveFileResult>() {
        @Override
        public void onResult(DriveFolder.DriveFileResult driveFileResult) {
            if (!driveFileResult.getStatus().isSuccess()) {
                showMessage("Error while trying to create the file");
                sendResult(false);
                return;
            }

            writeToFile(driveFileResult.getDriveFile());

        }
    };

    private void readFile(final DriveFile file) {

        file.openContents(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null).
                setResultCallback(new ResultCallback<DriveApi.ContentsResult>() {
                    @Override
                    public void onResult(DriveApi.ContentsResult contentsResult) {
                        if (!contentsResult.getStatus().isSuccess()) {
                            showMessage("Error Reading  file");
                            return;
                        }

                        InputStream input =contentsResult.getContents().getInputStream();
                        try {
                            FileOutputStream output = new FileOutputStream(dataBase);
                            byte buffer[] = new byte[BUF_SIZE];
                            int bytesRead;
                            while ((bytesRead = input.read(buffer)) != -1) {
                                output.write(buffer, 0, bytesRead);
                            }
                            sendResult(true);
                        }
                        catch (Exception ex){
                            showMessage("Error Reading to file");
                            sendResult(false);
                        }

                    }
                });
    }

    private void writeToFile(final DriveFile file) {
        file.openContents(mGoogleApiClient, DriveFile.MODE_WRITE_ONLY, null).setResultCallback(new ResultCallback<DriveApi.ContentsResult>() {
            @Override
            public void onResult(DriveApi.ContentsResult contentsResult) {
                if (!contentsResult.getStatus().isSuccess()) {
                    showMessage("Error Writing to file");
                    return;
                }
                Contents contents = contentsResult.getContents();
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
                    sendResult(false);

                }

                file.commitAndCloseContents(mGoogleApiClient, contents).setResultCallback(new ResultCallback<com.google.android.gms.common.api.Status>() {
                    @Override
                    public void onResult(com.google.android.gms.common.api.Status status) {
                        if (!status.isSuccess()) {
                            showMessage("Error Commit file");
                            sendResult(false);
                        }
                        sendResult(true);
                    }
                });
            }
        });
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
