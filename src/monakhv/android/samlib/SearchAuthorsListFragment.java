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

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import monakhv.android.samlib.sql.entity.AuthorCard;
import monakhv.android.samlib.tasks.SearchAuthor;

/**
 *
 * @author Dmitry Monakhov
 */
public class SearchAuthorsListFragment extends ListFragment implements ListSwipeListener.SwipeCallBack{

    private SearchAuthorAdapter adapter;
    static private final String DEBUG_TAG = "SearchAuthorsListFragment";
    private String pattern;
    ProgressDialog progress;
    private List<AuthorCard> result;
    private GestureDetector detector;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pattern = getActivity().getIntent().getExtras().getString(SearchAuthorActivity.EXTRA_PATTERN);

        if (result == null) {
            result = new ArrayList<AuthorCard>();
            search(pattern);
        }
        adapter = new SearchAuthorAdapter(getActivity());

        setListAdapter(adapter);
        detector = new GestureDetector(getActivity(), new ListSwipeListener(this));
    }
     @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getListView().setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                detector.onTouchEvent(event);
                return false;
            }
        });
    }

    public void search(String ptr) {
        if (adapter != null){
            result.clear();
            adapter.load();
        }
        
        pattern = ptr;
        SearchAuthor task = new SearchAuthor(getActivity());
        progress = new ProgressDialog(getActivity());
        progress.setMessage(getActivity().getText(R.string.search_Loading));
        progress.setCancelable(true);
        progress.setIndeterminate(true);
        progress.show();
        task.execute(pattern);
    }

    public void setResult(List<AuthorCard> res) {
        if (res == null) {
            Log.e(DEBUG_TAG, "Result is NULL");
            return;
        }
        result.clear();
        result.addAll(res);
        adapter.load();
        
        Log.d(DEBUG_TAG, "Got new result: " + res.size());
        if (progress != null) {
            progress.dismiss();
            Log.d(DEBUG_TAG, "Stop Progress Dialog");
        } else {
            Log.e(DEBUG_TAG, "Progress dialog is NULL");
        }
    }
    
    @Override
    public void onResume(){
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    public boolean singleClick(MotionEvent e) {
        int position = getListView().pointToPosition((int) e.getX(), (int) e.getY());
        if (position < 0){
            Log.w(DEBUG_TAG, "Wrong List selection");
            return false;
        }
        AuthorCard ac = adapter.getItem(position);
        Toast toast = Toast.makeText(getActivity(), ac.getName(), Toast.LENGTH_SHORT);
         toast.show();
        
        
        return true;
    }

    public boolean swipeRight(MotionEvent e) {
        return true;
        
    }

    public boolean swipeLeft(MotionEvent e) {
        return true;
    }

    public class SearchAuthorAdapter extends ArrayAdapter<AuthorCard> {

        private final Context context;
        private AuthorCard[] data;

       

        public SearchAuthorAdapter(Context context) {
            super(context, R.layout.author_search_row, result);
            this.context = context;
            data = result.toArray(new AuthorCard[1]);
        }

        public class ViewHolder {

            public TextView name;
            public TextView title;
            public TextView desc;
            public TextView size;

        }

        public void load() {
            data = result.toArray(new AuthorCard[1]);
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = convertView;
            ViewHolder holder;
            if (rowView == null) {//there is no reusable view construct new one
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                
                rowView = inflater.inflate(R.layout.author_search_row, parent, false);
                holder = new ViewHolder();
                holder.name = (TextView) rowView.findViewById(R.id.acName);
                holder.title = (TextView) rowView.findViewById(R.id.acTitle);
                holder.desc = (TextView) rowView.findViewById(R.id.acDesc);
                holder.size = (TextView) rowView.findViewById(R.id.acSize);
                rowView.setTag(holder);//store holder into rowView tag
            } else {
                holder = (ViewHolder) rowView.getTag();//existing View can find holder in Tag
            }
            holder.name.setText(data[position].getName());
            holder.title.setText(data[position].getTitle());
            holder.desc.setText(data[position].getDescription());
            String ss = Integer.toString(data[position].getSize()) + "K/" + Integer.toString(data[position].getCount());
            holder.size.setText(ss);

            return rowView;

        }
        
    }
}
