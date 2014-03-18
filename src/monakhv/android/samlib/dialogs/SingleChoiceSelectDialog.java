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
 *General propose Class to select single item from the list of string variables
 * 
 * @author Dmitry Monakhov
 */
public class SingleChoiceSelectDialog extends DialogFragment {
    private static final String DEBUG_TAG = "SingleChoiceSelectDialog";
    private static final String EXTRA_DATA="EXTRA_DATA";
    private static final String EXTRA_TITLE="EXTRA_TITLE";
    private static final String EXTRA_SELECTED="EXTRA_SELECTED";
    private  String[] files;
    private  OnItemClickListener listener;
    private  String tite;
    private int selected = -1;

    public SingleChoiceSelectDialog(){
        super();

    }

//    public SingleChoiceSelectDialog(String[] data,OnItemClickListener listener,String title) {
//        this.files = data;
//        this.listener = listener;
//        this.tite=title;
//    }
//
//    public SingleChoiceSelectDialog(String[] data,OnItemClickListener listener,String title, int selected) {
//        this(data, listener, title);
//        this.selected = selected;
//
//    }

    public static SingleChoiceSelectDialog getInstance(String[] data,OnItemClickListener listener,String title, int selected) {
        SingleChoiceSelectDialog res = new SingleChoiceSelectDialog();
        Bundle args = new Bundle();
        args.putStringArray(EXTRA_DATA,data);
        args.putString(EXTRA_TITLE, title);
        args.putInt(EXTRA_SELECTED,selected);
        res.setArguments(args);
        res.setListener(listener);
        return res;
    }
    public static SingleChoiceSelectDialog getInstance(String[] data,OnItemClickListener listener,String title) {
        SingleChoiceSelectDialog res = new SingleChoiceSelectDialog();
        Bundle args = new Bundle();
        args.putStringArray(EXTRA_DATA,data);
        args.putString(EXTRA_TITLE, title);

        res.setArguments(args);
        res.setListener(listener);
        return res;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        files = args.getStringArray(EXTRA_DATA);
        tite = args.getString(EXTRA_TITLE);
        selected = args.getInt(EXTRA_SELECTED);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
        
        View v = inflater.inflate(R.layout.file_select, null);
       
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
        android.R.layout.simple_list_item_single_choice, files);


        ListView fileList = (ListView) v.findViewById(R.id.listFile);
        fileList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        fileList.setAdapter(adapter);
        fileList.setOnItemClickListener(listener);
        if (selected != -1){
            fileList.setItemChecked(selected, true);
        }
        
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

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
