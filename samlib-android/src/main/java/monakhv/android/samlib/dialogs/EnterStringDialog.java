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
package monakhv.android.samlib.dialogs;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import monakhv.android.samlib.R;


/**
 *
 * @author monakhv
 */
public class EnterStringDialog {
    private static final String DEBUG_TAG = "EnterStringDialog";
    public interface ClickListener {
        public void okClick(String txt);        
    }
    
    private AlertDialog alertDialog;
    
    public EnterStringDialog(Context context, final ClickListener listener, String titleString, String defaultText) {
        

        LayoutInflater li = LayoutInflater.from(context);
        View dialogView = li.inflate(R.layout.enter_value, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        
        alertDialogBuilder.setView(dialogView);
        
        final TextView userInput = (TextView) dialogView.findViewById(R.id.enterValueText);
            userInput.setText(defaultText);
            alertDialogBuilder
                    .setTitle(titleString)
                    .setCancelable(false)
                    .setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    listener.okClick(userInput.getText().toString());
                    

                }
            })
                    .setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            // create alert dialog
            alertDialog = alertDialogBuilder.create();

    }
    
    public void show(){
        alertDialog.show();
    }
   
}
