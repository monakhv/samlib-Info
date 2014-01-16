/*
 * Copyright 2013 Dmitry Monakhov.
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
 */
package monakhv.android.samlib;

import monakhv.android.samlib.dialogs.SingleChoiceSelectDialog;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import monakhv.android.samlib.data.DataExportImport;

/**
 *
 * @author monakhv
 */
public class ArchiveActivity extends SherlockFragmentActivity {

    public static final String UPDATE_KEY = "UPDATE_LIST_PARAM";
    public static final int UPDATE_LIST = 22;
    private static final String DEBUG_TAG = "ArchiveActivity";
    private SingleChoiceSelectDialog dialog = null;
    private String selectedFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.archive);

    }

    private Dialog createImportAlert(String filename) {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle(R.string.Attention);

        String msg = getString(R.string.alert_import);
        msg = msg.replaceAll("__", filename);

        adb.setMessage(msg);
        adb.setIcon(android.R.drawable.ic_dialog_alert);
        adb.setPositiveButton(R.string.Yes, importDBListener);
        adb.setNegativeButton(R.string.No, importDBListener);
        return adb.create();

    }
    private final DialogInterface.OnClickListener importDBListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case Dialog.BUTTON_POSITIVE:
                    _importDB(selectedFile);
                    break;
                case Dialog.BUTTON_NEGATIVE:
                    break;

            }

        }
    };

    @SuppressWarnings("UnusedParameters")
    public void exportDB(View v) {
        String file = DataExportImport.exportDB(this.getApplicationContext());

        String text;
        if (file != null) {
            text = getString(R.string.res_export_db_good) + " " + file;
        } else {
            text = getString(R.string.res_export_db_bad);
        }
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(this, text, duration);
        toast.show();
    }

    @SuppressWarnings("UnusedParameters")
    public void importDB(View v) {
        final String[] files = DataExportImport.getFilesToImportDB(getApplicationContext());
        OnItemClickListener listener = new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedFile = files[position];
                Log.d(DEBUG_TAG, selectedFile);
                dialog.dismiss();
                Dialog alert = createImportAlert(selectedFile);
                alert.show();
                //_importDB(files[position]);
            }
        };
        dialog = new SingleChoiceSelectDialog(files, listener,getText(R.string.dialog_title_file).toString());


        dialog.show(getSupportFragmentManager(), "importDBDlg");

    }

    private void _importDB(String fileName) {
        boolean res = DataExportImport.importDB(getApplicationContext(), fileName);

        String text;
        if (res) {
            text = getString(R.string.res_import_db_good);
        } else {
            text = getString(R.string.res_import_db_bad);

        }
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(this, text, duration);
        toast.show();

        if (!res) {
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(UPDATE_KEY, UPDATE_LIST);
        setResult(RESULT_OK, intent);
        finish();


    }

    @SuppressWarnings("UnusedParameters")
    public void exportTxt(View v) {
        String file = DataExportImport.exportAuthorList(this.getApplicationContext());
        String text;
        if (file != null) {
            text = getString(R.string.res_export_txt_good) + " " + file;
        } else {
            text = getString(R.string.res_export_txt_bad);
        }
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(this, text, duration);
        toast.show();
    }

    @SuppressWarnings("UnusedParameters")
    public void importTxt(View v) {
        final String[] files = DataExportImport.getFilesToImportTxt(getApplicationContext());
        OnItemClickListener listener = new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedFile = files[position];
                Log.d(DEBUG_TAG, selectedFile);
                dialog.dismiss();
//                Dialog alert= createImportAlert(selectedFile);
//                alert.show();
                _importTxt(files[position]);
            }
        };
        dialog = new SingleChoiceSelectDialog(files, listener,getText(R.string.dialog_title_file).toString());


        dialog.show(getSupportFragmentManager(), "importTxtDlg");
       
    }

    private void _importTxt(String file) {
        
         boolean res = DataExportImport.importAuthorList(this.getApplicationContext(),file);
         
         String text;
        if (res) {
            text = getString(R.string.res_import_txt_good);
        } else {
            text = getString(R.string.res_import_txt_bad);

        }
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(this, text, duration);
        toast.show();

        
    }
}
