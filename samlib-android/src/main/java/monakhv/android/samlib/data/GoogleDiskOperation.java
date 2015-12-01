package monakhv.android.samlib.data;


import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.util.Log;



import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.drive.DriveFile;


import java.io.File;
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
public class GoogleDiskOperation extends ApiClientAsyncTask<Void, Void, Boolean> {
    private static final String DEBUG_TAG = "GoogleDiskOperation";
    public static final String FileName = "SamLib-Info.db";
    public static final int RESOLVE_CONNECTION_REQUEST_CODE = 21;



    public enum OperationType {
        EXPORT(R.string.arc_msg_export),
        IMPORT(R.string.arc_msg_import);
        private int iMsg;

        OperationType(int i) {
            iMsg = i;
        }

        public int getMessage() {
            return iMsg;
        }
    }


    private final Activity context;
    private final OperationType operation;
    private final File dataBase;



    public GoogleDiskOperation(Activity ctx, String account, OperationType operationType) {
        super(ctx, account);
        this.context = ctx;

        this.operation = operationType;
        DataExportImport dei = new DataExportImport(ctx);
        dataBase = dei.getDataBase();

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

    /**
     * Blocking algorithm to query for data base file in Google drive application folder
     *
     * @param params Parameters
     * @return result status
     */
    @Override
    protected Boolean doInBackgroundConnected(Void... params) {
        reSync();
        List<DriveFile> files = getFile(FileName);
        if (files == null) {
            return false;
        }
        switch (operation) {
            case EXPORT:
                if (files.size() > 0) {
                    Log.d(DEBUG_TAG, "write to existing file");
                    return writeFile(dataBase, files.get(0));
                } else {
                    Log.d(DEBUG_TAG, "add new  file");
                    return createFile(dataBase, FileName);
                }

            case IMPORT:
                if (files.size() > 0) {
                    Log.d(DEBUG_TAG, "start reading  file");
                    return readFile(files.get(0), dataBase);
                } else {
                    Log.d(DEBUG_TAG, "there is no file to read");
                    setError(R.string.res_import_google_bad);
                    return false;
                }
            default:
                setError("Unknown operation");
                return false;
        }

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
                // Unable to resolve, message user appropriately
            }
        } else {
            Log.d(DEBUG_TAG, "ConnectionFailed - has NOT  resolution");
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), context, 0).show();
        }

    }


    private void sendResult(boolean res) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.setAction(ArchiveActivity.GoogleReceiver.ACTION);
        broadcastIntent.putExtra(ArchiveActivity.GoogleReceiver.EXTRA_RESULT, res);
        broadcastIntent.putExtra(ArchiveActivity.GoogleReceiver.EXTRA_OPERATION, operation.toString());
        broadcastIntent.putExtra(ArchiveActivity.GoogleReceiver.EXTRA_ERROR, getErrorMsg());
        context.sendBroadcast(broadcastIntent);

    }

}
