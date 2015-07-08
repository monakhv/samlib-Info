package monakhv.android.samlib;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;


import com.j256.ormlite.android.AndroidDatabaseResults;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.PreparedQuery;
import monakhv.android.samlib.adapter.AuthorCursorAdapter;

import monakhv.android.samlib.data.SettingsHelper;
import monakhv.android.samlib.dialogs.ContextMenuDialog;
import monakhv.android.samlib.dialogs.EnterStringDialog;

import monakhv.android.samlib.dialogs.MyMenuData;

import monakhv.android.samlib.recyclerview.DividerItemDecoration;
import monakhv.android.samlib.recyclerview.RecyclerViewDelegate;
import monakhv.android.samlib.service.AuthorEditorServiceIntent;
import monakhv.android.samlib.service.UpdateServiceIntent;
import monakhv.android.samlib.sortorder.AuthorSortOrder;

import monakhv.android.samlib.sql.DatabaseHelper;
import monakhv.samlib.db.AuthorController;
import monakhv.samlib.db.entity.Author;
import monakhv.samlib.db.entity.SamLibConfig;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.DefaultHeaderTransformer;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

import java.sql.SQLException;

import static monakhv.android.samlib.ActivityUtils.getClipboardText;

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
    private static final String DEBUG_TAG = "AuthorFragment";

    private RecyclerView authorRV;
    private AuthorCursorAdapter adapter;


    private AuthorSortOrder order;
    private PullToRefreshLayout mPullToRefreshLayout;
    private GestureDetector detector;
    private boolean updateAuthor = false;//true update the only selected author
    private Author author = null;//for context menu selection
    private TextView updateTextView;
    private ContextMenuDialog contextMenu;

    private View empty;
    private boolean canUpdate;
    private SettingsHelper settingsHelper;
    private int selectedTag;

    public interface Callbacks {
        public DatabaseHelper getDatabaseHelper();
        public void onAuthorSelected(long id);

        public void selectBookSortOrder();

        public void onTitleChange(String lTitle);

        public void cleanBookSelection();
    }

    private  Callbacks mCallbacks;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingsHelper= new SettingsHelper(getActivity().getApplicationContext());
        order = AuthorSortOrder.valueOf(settingsHelper.getAuthorSortOrderString());
        detector = new GestureDetector(getActivity(), new ListSwipeListener(this));
        Log.d(DEBUG_TAG,"onCreate");
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException(
                    "Activity must implement fragment's callbacks.");
        }
        mCallbacks = (Callbacks) activity;
    }

    private View view;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(DEBUG_TAG,"onCreateView");
        canUpdate=true;
        view = inflater.inflate(R.layout.author_fragment,
                container, false);
        authorRV = (RecyclerView) view.findViewById(R.id.authorRV);
        empty = view.findViewById(R.id.add_author_panel);



        authorRV.setHasFixedSize(true);
        authorRV.setLayoutManager(new LinearLayoutManager(getActivity()));



        authorRV.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));


        makePulToRefresh();


        authorRV.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                detector.onTouchEvent(event);
                return false;
            }
        });
        adapter = new AuthorCursorAdapter((MyBaseAbstractActivity) getActivity(),getCursor());

        authorRV.setAdapter(adapter);
        adapter.registerAdapterDataObserver(observer);
        makeEmptyView();


        return view;

    }
    public void makePulToRefresh(){

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
    }

    private RecyclerView.AdapterDataObserver observer= new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            Log.d(DEBUG_TAG,"Observed: makeEmpty");
            makeEmptyView();
        }
    };


    private Cursor getCursor() {
        AuthorController sql = new AuthorController(mCallbacks.getDatabaseHelper());
        AndroidDatabaseResults res = (AndroidDatabaseResults) sql.getRowResults(selectedTag,order.getOrder());
        if (res == null){
            Log.e(DEBUG_TAG,"getCursor error");
            return null;
        }
        return res.getRawCursor();

    }

     private void updateAdapter() {
        adapter.changeCursor(getCursor());
        makeEmptyView();
    }
    private void makeEmptyView(){

        if (adapter.getItemCount()==0){
            authorRV.setVisibility(View.GONE);
            empty.setVisibility(View.VISIBLE);
        }
        else {
            authorRV.setVisibility(View.VISIBLE);
            empty.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRefreshStarted(View view) {
        Log.d(DEBUG_TAG, "Start update service");

        if (getActivity() == null) {
            return;//try to prevent some ANR reports
        }

        if (updateAuthor) {
            UpdateServiceIntent.makeUpdateAuthor(getActivity(),author.getId());

        } else {
            UpdateServiceIntent.makeUpdate(getActivity(),selectedTag);

        }
    }

    void onRefreshComplete() {
        Log.d(DEBUG_TAG,"Stop updating state");
        canUpdate=false;
        mPullToRefreshLayout.setRefreshing(false);
        mPullToRefreshLayout.setRefreshComplete();
        updateTextView.setGravity(android.view.Gravity.CENTER);
        updateAuthor = false;

    }

    void updateProgress(String stringExtra) {

        if ( !mPullToRefreshLayout.isRefreshing() && canUpdate){
            Log.d(DEBUG_TAG,"Restore refreshing state");
            mPullToRefreshLayout.setRefreshing(true);
        }
        updateTextView.setGravity(android.view.Gravity.CENTER_VERTICAL);
        updateTextView.setText(stringExtra);
    }

    @Override
    public boolean singleClick(MotionEvent e) {
        int position = authorRV.getChildAdapterPosition(authorRV.findChildViewUnder(e.getX(), e.getY()));
//        Author a = sql.getById(adapter.getItemId(position));
//        makeToast(a.getName());
        Log.d(DEBUG_TAG,"Selected position: "+position);
        if (position<0){
            Log.d(DEBUG_TAG,"Coordinates: "+e.getX()+" - "+e.getY());
            return false;
        }
        adapter.toggleSelection(position);
        authorRV.playSoundEffect(SoundEffectConstants.CLICK);
        mCallbacks.onAuthorSelected(adapter.getItemId(position));
        return true;
        //return false;

    }

    @Override
    public boolean swipeRight(MotionEvent e) {
        int position = authorRV.getChildPosition(authorRV.findChildViewUnder(e.getX(), e.getY()));
        adapter.toggleSelection(position,false);

        author = adapter.getSelected();

        if (author == null) {
            return false;
        }

       adapter.makeSelectedRead();
        return true;

    }

    @Override
    public boolean swipeLeft(MotionEvent e) {
        int position = authorRV.getChildPosition(authorRV.findChildViewUnder(e.getX(), e.getY()));
        adapter.toggleSelection(position);

        author = adapter.getSelected();

        if (author == null) {
            return false;
        }

        launchBrowser(author);

        return true;
    }

    private final int read_option_item = 21;

    private final int browser_option_item = 23;
    private final int edit_author_option_item = 24;
    private final int delete_option_item = 25;
    private final int update_option_item = 35;

    @Override
    public void longPress(MotionEvent e) {
        int position = authorRV.getChildPosition(authorRV.findChildViewUnder(e.getX(), e.getY()));
        adapter.toggleSelection(position);

        author = adapter.getSelected();

        if (author == null) {
            return;
        }
        final MyMenuData menu = new MyMenuData();

        if (author.isIsNew()) {
            menu.add(read_option_item, getString(R.string.menu_read));
        }

        menu.add(browser_option_item, getString(R.string.menu_open_web));
        menu.add(edit_author_option_item, getString(R.string.menu_edit));
        menu.add(delete_option_item, getString(R.string.menu_delete));
        menu.add(update_option_item, getString(R.string.menu_refresh));

        contextMenu = ContextMenuDialog.getInstance(menu, new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int item = menu.getIdByPosition(position);
                contextSelector(item);
                contextMenu.dismiss();
            }
        }, author.getName());

        contextMenu.show(getActivity().getSupportFragmentManager(), "authorContext");

    }

    private void contextSelector(int item) {


        if (item == delete_option_item) {
            Dialog alert = createDeleteAuthorAlert(author.getName());
            alert.show();
        }

        if (item == read_option_item) {
            adapter.makeSelectedRead();
        }

        if (item == browser_option_item) {
            launchBrowser(author);
        }
        if (item == update_option_item) {
            updateAuthor = true;
            startRefresh();
        }
        if (item == edit_author_option_item) {
            EnterStringDialog ddialog = new EnterStringDialog(getActivity(), new EnterStringDialog.ClickListener() {
                public void okClick(String txt) {
                    author.setName(txt);
                    adapter.update(author);
                }
            }, getText(R.string.dialog_title_edit_author).toString(), author.getName());

            ddialog.show();
        }

    }

    void startRefresh() {
        mPullToRefreshLayout.setRefreshing(true);
        onRefreshStarted(null);
    }

    /**
     * Launch Browser to load Author home page
     *
     * @param a Author object
     */
    public void launchBrowser(Author a) {
        Uri uri = Uri.parse(a.getUrlForBrowser(settingsHelper));
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uri);
        getActivity().startActivity(launchBrowser);

    }

    /**
     * Create Alert Dialog to wrn about Author delete
     *
     * @param authorName Name of the author
     * @return Warning Author delete dialog
     */
    private Dialog createDeleteAuthorAlert(String authorName) {
        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
        adb.setTitle(R.string.Attention);

        String msg = getString(R.string.alert_delete_author);
        msg = msg.replaceAll("__", authorName);

        adb.setMessage(msg);
        adb.setIcon(android.R.drawable.ic_dialog_alert);
        adb.setPositiveButton(R.string.Yes, deleteAuthorListener);
        adb.setNegativeButton(R.string.No, deleteAuthorListener);
        return adb.create();

    }

    private final DialogInterface.OnClickListener deleteAuthorListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case Dialog.BUTTON_POSITIVE:
                    if (author != null) {
                        AuthorEditorServiceIntent.delAuthor(getActivity().getApplicationContext(),author.getId());
                        mCallbacks.cleanBookSelection();
                    }
                    break;
                case Dialog.BUTTON_NEGATIVE:
                    break;
            }

        }
    };
    public void searchOrAdd(){
        View v = getActivity().findViewById(R.id.add_author_panel);

        v.setVisibility(View.VISIBLE);

        String txt = null;
        try {
            txt = getClipboardText(getActivity());
        } catch (Exception ex) {
            Log.e(DEBUG_TAG, "Clipboard Error!", ex);
        }

        if (txt != null) {

            if (SamLibConfig.getParsedUrl(txt) != null) {
                EditText editText = (EditText) getActivity().findViewById(R.id.addUrlText);
                editText.setText(txt);
            }
        }
    }

    /**
     * Show author list according selected tag
     * @param tag_id tag-id
     * @param tg_name tag name
     */
    public void selectTag(int tag_id, String tg_name){
        selectedTag=tag_id;


        if (tag_id == SamLibConfig.TAG_AUTHOR_ALL) {

            mCallbacks.onTitleChange(getActivity().getText(R.string.app_name).toString());
        } else {
            mCallbacks.onTitleChange(tg_name);
        }


        Log.i(DEBUG_TAG, "Selected tag " + selectedTag);
        refresh(selectedTag, null);
    }

    /**
     * Get selection string for author search
     * @return
     */
    public int getSelection() {
        return selectedTag;
    }

    /**
     * Get Selected Author list position
     * @return
     */
    public int getSelectedAuthorPosition() {
        return adapter.getSelectedPosition();
    }
    public long getSelectedAuthorId() {
        Author a  = adapter.getSelected();
        if (a == null){
            return 0;
        }
        else {
            return a.getId();
        }
    }
    public void restoreSelection(int position) {
        adapter.toggleSelection(position);

    }
    public void selectAuthor(long id){

        boolean res=adapter.findAndSelect(id);
        if (!res){
            Log.e(DEBUG_TAG,"selectAuthor: id not found - "+id);
        }
    }
    void cleanSelection() {
        adapter.cleanSelection();
    }

    /**
     * update sort order and selection parameters and restart loader
     *
     * @param selectedTag selection tag id
     * @param so        sort order string
     */
    public void refresh(int selectedTag, AuthorSortOrder so) {
        Log.d(DEBUG_TAG, "set Selection: " + selectedTag);
        cleanSelection();
        this.selectedTag = selectedTag;
        if (so != null) {
            order = so;
        }


        updateAdapter();
    }

    public void refresh(){
        adapter.refresh();
    }

    /**
     * set sort order and restart loader to make is  actual
     *
     * @param so new sort order
     */
    public void setSortOrder(AuthorSortOrder so) {
        cleanSelection();
        order = so;
        updateAdapter();
    }

    public AuthorSortOrder getSortOrder() {
        return order;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (adapter != null){
            adapter.clear();
            adapter.unregisterAdapterDataObserver( observer);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        canUpdate=true;
    }

    @Override
    public void onPause() {
        onRefreshComplete();
        super.onPause();

    }
}
