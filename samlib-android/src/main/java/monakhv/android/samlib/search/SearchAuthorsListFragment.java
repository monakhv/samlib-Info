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
package monakhv.android.samlib.search;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static monakhv.android.samlib.ActivityUtils.setDivider;



import monakhv.android.samlib.ListSwipeListener;
import monakhv.android.samlib.MyBaseAbstractFragment;
import monakhv.android.samlib.R;
import monakhv.android.samlib.SamlibApplication;
import monakhv.samlib.db.entity.AuthorCard;
import monakhv.android.samlib.tasks.SearchAuthor;


/**
 * @author Dmitry Monakhov
 */
public class SearchAuthorsListFragment extends ListFragment implements ListSwipeListener.SwipeCallBack {

    static public final String AUTHOR_URL = "AUTHOR_URL";
    static private final String KEY_RESULT_DATA = "RESULT_DATA";
    static private final String DEBUG_TAG = "SearchAuthorsFragment";
    private SearchAuthorAdapter adapter;
    private String pattern;
    ProgressDialog progress;
    private List<AuthorCard> result;
    private GestureDetector detector;
    private MyBaseAbstractFragment.DaggerCaller mDaggerCaller;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            result = (List<AuthorCard>) savedInstanceState.getSerializable(KEY_RESULT_DATA);
        }
        pattern = getActivity().getIntent().getExtras().getString(SearchAuthorActivity.EXTRA_PATTERN);

        ((SamlibApplication)getActivity().getApplication()).getApplicationComponent().inject(this);

        if (result == null) {
            result = new ArrayList<>();
            search(pattern);
        }
        adapter = new SearchAuthorAdapter(getActivity());

        setListAdapter(adapter);
        detector = new GestureDetector(getActivity(), new ListSwipeListener(this));

    }

    @Override
    public void onAttach(Context context) {

        super.onAttach(context);
        if (!(context instanceof MyBaseAbstractFragment.DaggerCaller)) {
            throw new IllegalStateException(
                    "MyBaseAbstractFragment: Activity must implement fragment's callbacks.");
        }
        mDaggerCaller = (MyBaseAbstractFragment.DaggerCaller) context;

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
        setDivider(getListView());
    }



    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(KEY_RESULT_DATA, (Serializable) result);
        super.onSaveInstanceState(outState);
    }

    /**
     * start task to search author on samLib site
     * @param ptr string pattern using for search
     */
    public void search(String ptr) {
        if (adapter != null) {
            result.clear();
            adapter.load();
        }

        pattern = ptr;
        SearchAuthor task = new SearchAuthor((SamlibApplication) getActivity().getApplication(),mDaggerCaller.getDatabaseHelper());
        progress = new ProgressDialog(getActivity());
        progress.setMessage(getActivity().getText(R.string.search_Loading));
        progress.setCancelable(true);
        progress.setIndeterminate(true);
        progress.show();
        task.execute(pattern);
    }

    public void setResult(List<AuthorCard> res) {
        if (progress != null) {
            progress.dismiss();
            Log.d(DEBUG_TAG, "Stop Progress Dialog");
        } else {
            Log.e(DEBUG_TAG, "Progress dialog is NULL");
        }

        result.clear();
        result.addAll(res);
        adapter.load();

        Log.d(DEBUG_TAG, "Got new result: " + res.size());
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    private AuthorCard selectedAuthor;

    public boolean singleClick(MotionEvent e) {
        int position = getListView().pointToPosition((int) e.getX(), (int) e.getY());
        if (position < 0) {
            Log.w(DEBUG_TAG, "Wrong List selection");
            return false;
        }
        selectedAuthor = adapter.getItem(position);
        Dialog alert = createAddAuthorAlert(selectedAuthor.getName());
        alert.show();

        return true;
    }

    public boolean swipeRight(MotionEvent e) {
        return true;

    }

    public boolean swipeLeft(MotionEvent e) {
        return true;
    }

    @Override
    public void longPress(MotionEvent e) {

    }

    private Dialog createAddAuthorAlert(String authorname) {
        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
        adb.setTitle(R.string.Attention);

        String msg = getString(R.string.alert_add_author);
        msg = msg.replaceAll("__", authorname);

        adb.setMessage(msg);
        adb.setIcon(android.R.drawable.ic_dialog_alert);
        adb.setPositiveButton(R.string.Yes, importDBListener);
        adb.setNegativeButton(R.string.No, importDBListener);
        return adb.create();

    }


    private final DialogInterface.OnClickListener importDBListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case Dialog.BUTTON_POSITIVE:
                    Intent intent = new Intent();

                    intent.putExtra(AUTHOR_URL, selectedAuthor.getUrl());

                    getActivity().setResult(RESULT_OK, intent);
                    getActivity().finish();
                    //AddAuthor aa = new AddAuthor(getActivity().getApplicationContext());
                    //aa.execute(selectedAuthor.getUrl());
                    //
                    break;
                case Dialog.BUTTON_NEGATIVE:
                    break;

            }

        }
    };


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
            public TextView url;

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
                holder.url = (TextView) rowView.findViewById(R.id.acURL);
                rowView.setTag(holder);//store holder into rowView tag
            } else {
                holder = (ViewHolder) rowView.getTag();//existing View can find holder in Tag
            }
            holder.name.setText(data[position].getName());
            holder.title.setText(data[position].getTitle());
            holder.desc.setText(data[position].getDescription());
            String ss = Integer.toString(data[position].getSize()) + "K/" + Integer.toString(data[position].getCount());
            holder.size.setText(ss);
            holder.url.setText(data[position].getUrl());

            return rowView;

        }

    }
}
