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

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import monakhv.android.samlib.R;

/**
 *
 * @author monakhv
 */
public class FileSelectDialog extends DialogFragment {
    private static final String DEBUG_TAG = "FileSelectDialog";
    private String[] files;
    private OnItemClickListener listener;
    private String tite;
    private ListView fileList = null;

    public FileSelectDialog(String[] files,OnItemClickListener listener,String title) {
        this.files = files;
        this.listener = listener;
        this.tite=title;
    }
    
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
        
        View v = inflater.inflate(R.layout.file_select, null);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
        android.R.layout.simple_list_item_single_choice, files);
        fileList = (ListView) v.findViewById(R.id.listFile);
        fileList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        fileList.setAdapter(adapter);
        fileList.setOnItemClickListener(listener);
       
        getDialog().setTitle(tite);
                
        Button close = (Button) v.findViewById(R.id.listFile_close);
        close.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                Log.d(DEBUG_TAG, "Close clicked");
                dismiss();
            }
            
        });
        return v;
        
    }

    

    
}
