package monakhv.android.samlib;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import monakhv.android.samlib.adapter.AuthorCursorAdapter;
import monakhv.android.samlib.recyclerview.DividerItemDecoration;
import monakhv.android.samlib.recyclerview.RecyclerViewDelegate;
import monakhv.android.samlib.service.UpdateServiceIntent;
import monakhv.android.samlib.sql.AuthorController;
import monakhv.android.samlib.sql.AuthorProvider;
import monakhv.android.samlib.sql.SQLController;
import monakhv.android.samlib.sql.entity.Author;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.DefaultHeaderTransformer;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

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
 * 12/5/14.
 */
public class AuthorFragment extends Fragment implements OnRefreshListener, ListSwipeListener.SwipeCallBack {

    private RecyclerView authorRV;
    private AuthorCursorAdapter adapter;
    private AuthorController sql;
    private String selection = null;
    private SortOrder order;
    private PullToRefreshLayout mPullToRefreshLayout;
    private GestureDetector detector;
    private boolean updateAuthor=false;//true update the only selected author
    private Author author = null;//for context menu selection
    private TextView updateTextView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TODO: read from Settings
        order = SortOrder.AuthorName;
        detector = new GestureDetector(getActivity(), new ListSwipeListener(this));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.author_fragment,
                container, false);
        authorRV = (RecyclerView) view.findViewById(R.id.authorRV);

        sql = new AuthorController(getActivity());
        Cursor c = getActivity().getContentResolver().query(AuthorProvider.AUTHOR_URI, null, selection, null, order.getOrder());

        adapter = new AuthorCursorAdapter(c);
        authorRV.setHasFixedSize(true);
        authorRV.setLayoutManager(new LinearLayoutManager(getActivity()));
        authorRV.setAdapter(adapter);


        authorRV.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));


        mPullToRefreshLayout = (PullToRefreshLayout) view.findViewById(R.id.ptr_layout);

        ActionBarPullToRefresh.from(getActivity())
                .options(Options.create()
                        .refreshOnUp(true)
                        .headerLayout(R.layout.updateheader)
                        .noMinimize()
                        .build())
                        // We need to insert the PullToRefreshLayout into the Fragment's ViewGroup
                .allChildrenArePullable()
                .listener(this)
                .useViewDelegate(android.support.v7.widget.RecyclerView.class, new RecyclerViewDelegate())
                .setup(mPullToRefreshLayout);

        DefaultHeaderTransformer dht = (DefaultHeaderTransformer) mPullToRefreshLayout.getHeaderTransformer();
        updateTextView = (TextView) dht.getHeaderView().findViewById(R.id.ptr_text);



        authorRV.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                detector.onTouchEvent(event);
                return false;
            }
        });




//        authorRV.setClickable(true);
//        authorRV.setFocusable(true);
//        authorRV.setFocusableInTouchMode(true);

//        authorRV.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int position = authorRV.getChildPosition(v);
//                Author a = sql.getById(adapter.getItemId(position));
//                makeToast(a.getName());
//            }
//        });

        return view;

    }

    @Override
    public void onRefreshStarted(View view) {

        if (getActivity() == null){
            return;//try to prevent some ANR reports
        }
        Intent service = new Intent(getActivity(), UpdateServiceIntent.class);
        service.putExtra(UpdateServiceIntent.CALLER_TYPE, UpdateServiceIntent.CALLER_IS_ACTIVITY);
        if (updateAuthor){
            String str =   SQLController.TABLE_AUTHOR + "." +SQLController.COL_ID + "=" + Integer.toString(author.getId());
            service.putExtra(UpdateServiceIntent.SELECT_STRING, str);
        }
        else {
            service.putExtra(UpdateServiceIntent.SELECT_STRING, selection);
        }

        getActivity().startService(service);
    }

    void onRefreshComplete() {
        mPullToRefreshLayout.setRefreshComplete();
        updateTextView.setGravity(android.view.Gravity.CENTER);
        updateAuthor=false;

    }
    void updateProgress(String stringExtra) {
        updateTextView.setGravity(android.view.Gravity.CENTER_VERTICAL);
        updateTextView.setText(stringExtra);
    }

    @Override
    public boolean singleClick(MotionEvent e) {



        int position = authorRV.getChildPosition(authorRV.findChildViewUnder(e.getX(),e.getY()));
//        Author a = sql.getById(adapter.getItemId(position));
//        makeToast(a.getName());
        adapter.toggleSelection(position);
        authorRV.playSoundEffect(SoundEffectConstants.CLICK);
        return true;
        //return false;

    }

    @Override
    public boolean swipeRight(MotionEvent e) {
        return false;
    }

    @Override
    public boolean swipeLeft(MotionEvent e) {
        return false;
    }

    private void makeToast(String mesg){
        Toast toast = Toast.makeText(getActivity(), mesg, Toast.LENGTH_SHORT);

        toast.show();
    }

    public enum SortOrder {

        DateUpdate(R.string.sort_update_date, SQLController.COL_mtime + " DESC"),
        AuthorName(R.string.sort_author_name, SQLController.COL_isnew + " DESC, " + SQLController.COL_NAME);
        private final int name;
        private final String order;

        private SortOrder(int name, String order) {
            this.name = name;
            this.order = order;
        }

        public String getOrder() {
            return order;
        }

        public static String[] getTitles(Context ctx) {
            String[] res = new String[values().length];
            int i = 0;
            for (SortOrder so : values()) {
                res[i] = ctx.getString(so.name);
                ++i;
            }
            return res;
        }
    }


}
