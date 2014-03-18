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

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import monakhv.android.samlib.R;
import monakhv.android.samlib.sql.SQLController;

/**
 * @author monakhv
 */
public class FilterSelectDialog extends DialogFragment {
    private static final String DEBUG_TAG = "FilterSelectDialog";
    private static final String EXTRA_TITLE = "EXTRA_TITLE";
    private Cursor cursor;
    private AdapterView.OnItemClickListener listener;
    private String title;
    private ListView fileList = null;

    //    public FilterSelectDialog(Cursor cursor,AdapterView.OnItemClickListener listener,String title){
//        this.cursor   = cursor;
//        this.listener = listener;
//        this.title=title;
//    }
    public FilterSelectDialog() {
        super();
    }

    public static FilterSelectDialog getInstance(Cursor cursor, AdapterView.OnItemClickListener listener, String title) {
        FilterSelectDialog res = new FilterSelectDialog();
        Bundle args = new Bundle();
        args.putString(EXTRA_TITLE,title);
        res.setArguments(args);
        res.setListener(listener);
        res.setCursor(cursor);
        return res;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        title=getArguments().getString(EXTRA_TITLE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.file_select, null);
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
//        android.R.layout.simple_list_item_single_choice, files);

        getDialog().setTitle(title);
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                getActivity(), android.R.layout.simple_list_item_single_choice,
                cursor, new String[]{SQLController.COL_TAG_NAME}, new int[]{android.R.id.text1},
                0);

        fileList = (ListView) v.findViewById(R.id.listFile);
        fileList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        fileList.setAdapter(adapter);
        fileList.setOnItemClickListener(listener);

        Button close = (Button) v.findViewById(R.id.listFile_close);
        close.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Log.d(DEBUG_TAG, "Close clicked");
                dismiss();
            }

        });
        return v;

    }

    public void setCursor(Cursor cursor) {
        this.cursor = cursor;
    }

    public void setListener(AdapterView.OnItemClickListener listener) {
        this.listener = listener;
    }
}
