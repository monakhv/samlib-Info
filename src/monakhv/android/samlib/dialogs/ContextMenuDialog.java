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
import android.view.Window;
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
public class ContextMenuDialog extends DialogFragment {
    private static final String DEBUG_TAG = "ContextMenuDialog";
    private static final String EXTRA_DATA="EXTRA_DATA";
    private static final String EXTRA_TITLE="EXTRA_TITLE";
    private static final String EXTRA_SELECTED="EXTRA_SELECTED";
    private  String[] data;
    private  OnItemClickListener listener;
    private  String title;


    public ContextMenuDialog(){
        super();

    }

    public static ContextMenuDialog getInstance(MyMenuData mdata,OnItemClickListener listener,String title) {
        ContextMenuDialog res = new ContextMenuDialog();
        Bundle args = new Bundle();
        args.putStringArray(EXTRA_DATA,mdata.getSData());
        args.putString(EXTRA_TITLE, title);

        res.setArguments(args);
        res.setListener(listener);
        return res;
    }


    public static ContextMenuDialog getInstance(String[] data,OnItemClickListener listener,String title) {
        ContextMenuDialog res = new ContextMenuDialog();
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
        data = args.getStringArray(EXTRA_DATA);
        title = args.getString(EXTRA_TITLE);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
        
        View v = inflater.inflate(R.layout.context_menu, null);
        if (v== null){
            Log.e(DEBUG_TAG,"Can not create View!!");
            return null;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
        android.R.layout.simple_list_item_1, data);


        ListView fileList = (ListView) v.findViewById(R.id.listMenu);
        fileList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        fileList.setAdapter(adapter);
        fileList.setOnItemClickListener(listener);

        if (title != null){
            getDialog().setTitle(title);
        }
        else {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

                

        return v;
        
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
