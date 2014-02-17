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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import java.text.SimpleDateFormat;
import java.util.Date;

import monakhv.android.samlib.data.SettingsHelper;
import monakhv.android.samlib.sql.AuthorController;
import monakhv.android.samlib.sql.AuthorProvider;
import monakhv.android.samlib.sql.SQLController;
import monakhv.android.samlib.sql.entity.Author;
import monakhv.android.samlib.tasks.MarkRead;

import static monakhv.android.samlib.ActivityUtils.cleanItemSelection;
import static monakhv.android.samlib.ActivityUtils.getClipboardText;
import static monakhv.android.samlib.ActivityUtils.setDivider;
import monakhv.android.samlib.dialogs.EnterStringDialog;
import monakhv.android.samlib.dialogs.FilterSelectDialog;
import monakhv.android.samlib.dialogs.SingleChoiceSelectDialog;
import monakhv.android.samlib.service.UpdateServiceIntent;
import monakhv.android.samlib.sql.entity.SamLibConfig;
import monakhv.android.samlib.tasks.DeleteAuthor;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.DefaultHeaderTransformer;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
/**
 *
 * @author Dmitry Monakhov
 * 
 * 
 */
public class AuthorListFragment  extends SherlockListFragment implements
        LoaderManager.LoaderCallbacks<Cursor>, ListSwipeListener.SwipeCallBack,OnRefreshListener {

    public static final int AUTHOR_LIST_LOADER = 0x01;
    private static final String DEBUG_TAG = "AuthorListHelper";
    private static final String DATE_FORMAT = "dd.MM.yyyy HH:mm:ss";
    
   
    private String selection = null;
    private SimpleCursorAdapter adapter;
    private TextView emptyText;
    private AuthorController sql;
    
    private View selected;
    private SortOrder order;
    private SingleChoiceSelectDialog sortDialog;
    private FilterSelectDialog filterDialog;
    private GestureDetector detector;

    void onRefreshComplete() {
        mPullToRefreshLayout.setRefreshComplete();
        updateTextView.setGravity(android.view.Gravity.CENTER);
        
    }

    void updateProgress(String stringExtra) {
        updateTextView.setGravity(android.view.Gravity.CENTER_VERTICAL);
        updateTextView.setText(stringExtra);
    }

    public void onRefreshStarted(View view) {
        
        Intent service = new Intent(getActivity(), UpdateServiceIntent.class);
        service.putExtra(UpdateServiceIntent.CALLER_TYPE, UpdateServiceIntent.CALLER_IS_ACTIVITY);
        service.putExtra(UpdateServiceIntent.SELECT_STRING, selection);
        getActivity().startService(service);
    }

    void startRefresh() {
        mPullToRefreshLayout.setRefreshing(true);
        onRefreshStarted(null);
    }
    public interface Callbacks {
        public void onAuthorSelected(int id);
        public void selectBookSortOrder();
        public void onTitleChange(String lTitle);
        public void addAuthorFromText();
    }
    private static Callbacks mCallbacks;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         String[] from = {SQLController.COL_NAME, SQLController.COL_mtime, SQLController.COL_isnew, SQLController.COL_TGNAMES};
        int[] to = {R.id.authorName, R.id.updated, R.id.icon, R.id.tgnames};
        
        adapter = new SimpleCursorAdapter(
                getActivity().getApplicationContext(), R.layout.rowlayout,
                null, from, to,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        
        SettingsHelper settings = new SettingsHelper(getActivity().getApplicationContext());
        
        adapter.setViewBinder(new AuthorViewBinder());
        order = settings.getAuthorSortOrder();
        setListAdapter(adapter);
        getLoaderManager().initLoader(AUTHOR_LIST_LOADER, null, this);
        detector = new GestureDetector(getActivity(), new ListSwipeListener(this));
    }
    
    private PullToRefreshLayout mPullToRefreshLayout;
    private TextView updateTextView;
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view,savedInstanceState);
        // This is the View which is created by ListFragment
        ViewGroup viewGroup = (ViewGroup) view;

        // We need to create a PullToRefreshLayout manually
        mPullToRefreshLayout = new PullToRefreshLayout(viewGroup.getContext());
        
        // We can now setup the PullToRefreshLayout
            ActionBarPullToRefresh.from(getActivity())
                    .options(Options.create()
                            .refreshOnUp(true)
                            .headerLayout(R.layout.updateheader)
                            .noMinimize()
                            .build())
                    // We need to insert the PullToRefreshLayout into the Fragment's ViewGroup
                    .insertLayoutInto(viewGroup)
                    // Here we mark just the ListView and it's Empty View as pullable
                    .theseChildrenArePullable(android.R.id.list, android.R.id.empty)
                    .listener(this)
                    .setup(mPullToRefreshLayout);
            
            DefaultHeaderTransformer dht = (DefaultHeaderTransformer) mPullToRefreshLayout.getHeaderTransformer();
            updateTextView = (TextView) dht.getHeaderView().findViewById(R.id.ptr_text);
            dht.setPullText(getActivity().getText(R.string.pull_to_refresh_pull_label));
            dht.setReleaseText(getActivity().getText(R.string.pull_to_refresh_release_label));
            dht.setRefreshingText(getActivity().getText(R.string.pull_to_refresh_refreshing_label));

    }
    private int selectedAuthorPosition =0;
    private void setEmptyText(int id){
        if (sql == null){
            return;
        }
        if (!sql.isEmpty(selection)){
            emptyText.setText(R.string.pull_to_refresh_refreshing_label);
        }
        else {
            emptyText.setText(id);
        }
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        emptyText = (TextView) getActivity().findViewById(R.id.id_empty_text);
        sql = new AuthorController(getActivity());
        setEmptyText(R.string.no_authors);
        
        getListView().setEmptyView(emptyText);
        registerForContextMenu(getListView());
        
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        //getListView().setSelector(R.drawable.author_item_bg);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedAuthorPosition = position;
                Cursor c = (Cursor) adapter.getItem(position);
                mCallbacks.onAuthorSelected(c.getInt(c.getColumnIndex(SQLController.COL_ID)));
                Log.i(DEBUG_TAG, "position: "+position+"  view: "+view.getId()+" --- "+View.NO_ID);                
                selectView(view);
            }
        });
        setDivider(getListView());
        EditText editText = (EditText) getActivity().findViewById(R.id.addUrlText);
        editText.setOnEditorActionListener( new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH){
                    mCallbacks.addAuthorFromText();
                    return true;
                }
                return false;
            }
        });

        getListView().setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                detector.onTouchEvent(event);
                return false;
            }
        });
    }
    public int getSelectedAuthorPosition(){
        return selectedAuthorPosition;
    }
    public int getSelectedAuthorId() {
        Cursor c = (Cursor) adapter.getItem(selectedAuthorPosition);
        if (c == null){
            return 0;
        }
        try {
            return c.getInt(c.getColumnIndex(SQLController.COL_ID));
        } catch (CursorIndexOutOfBoundsException ex) {
            Log.e(DEBUG_TAG, "Cursor is out of bounds");
            return 0;
        }
    }
    public void restoreSelection(int position) {
        selectedAuthorPosition = position;
        
       View v ;
       try {
           v = adapter.getView(position, null, getListView());
       }  
       catch(IllegalStateException ex){
           Log.e(DEBUG_TAG, "restoreSelection: Can not move cursor to restore selection",ex);
           return;
       }
        
        if (v == null){
            Log.e(DEBUG_TAG, "restoreSelection: View to select is null");
            return;
        }
        
        selectView(v);
        
        getListView().setItemChecked(position, true);
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
   
   

    @Override
    public void onCreateOptionsMenu(Menu menu,MenuInflater inflater) {
        
        inflater.inflate(R.menu.options_menu, menu);
        super.onCreateOptionsMenu(menu,inflater);
        
    }
    

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int sel = item.getItemId();
        if (sel == android.R.id.home ){
            //mCallbacks.onOpenPanel();
            if (getSelection() != null) {
                refresh(null, null);
                mCallbacks.onTitleChange(getString(R.string.app_name));
            }
        }

        if (sel == R.id.menu_refresh) {
            startRefresh();

        }

        if (sel == R.id.sort_option_item_books){
            mCallbacks.selectBookSortOrder();
        }
        if (sel == R.id.sort_option_item) {
           
            AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {

                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    SortOrder so = SortOrder.values()[position];
                    mCallbacks.onAuthorSelected(0);
                    setSortOrder(so);
                    sortDialog.dismiss();
                }

            };
            sortDialog = new SingleChoiceSelectDialog(SortOrder.getTitles(getActivity()), listener, this.getString(R.string.dialog_title_sort_author), getSortOrder().ordinal());

            sortDialog.show(getActivity().getSupportFragmentManager(), "DoSortDialog");
        }

        if (sel == R.id.add_option_item) {
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
        if (sel == R.id.settings_option_item) {
            Log.d(DEBUG_TAG, "go to Settings");
            Intent prefsIntent = new Intent(getActivity().getApplicationContext(),
                    SamlibPreferencesActivity.class);
            //prefsIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(prefsIntent);
        }
        if (sel == R.id.archive_option_item) {

            Log.d(DEBUG_TAG, "go to Archive");
            Intent prefsIntent = new Intent(getActivity().getApplicationContext(),
                    ArchiveActivity.class);
            prefsIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

            //startActivityForResult must be called via getActivity direct call produce wrong requestCode
            getActivity().startActivityForResult(prefsIntent, MainActivity.ARCHIVE_ACTIVITY);
        }
        if (sel == R.id.selected_option_item) {
            Log.d(DEBUG_TAG, "go to Selected");
            cleanSelection();
            mCallbacks.onAuthorSelected(SamLibConfig.SELECTED_BOOK_ID);
        }
        if (sel == R.id.menu_filter) {
            Log.d(DEBUG_TAG, "go to Filter");
            Cursor tags = getActivity().getContentResolver().query(AuthorProvider.TAG_URI, null, null, null, SQLController.COL_TAG_NAME);

            MatrixCursor extras = new MatrixCursor(new String[]{SQLController.COL_ID, SQLController.COL_TAG_NAME});

            extras.addRow(new String[]{Integer.toString(SamLibConfig.TAG_AUTHOR_ALL), getText(R.string.filter_all).toString()});
            extras.addRow(new String[]{Integer.toString(SamLibConfig.TAG_AUTHOR_NEW), getText(R.string.filter_new).toString()});
            Cursor[] cursors = {extras, tags};
            final Cursor extendedCursor = new MergeCursor(cursors);

            AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    extendedCursor.moveToPosition(position);

                    int tag_id = extendedCursor.getInt(extendedCursor.getColumnIndex(SQLController.COL_ID));
                    String tg_name = extendedCursor.getString(extendedCursor.getColumnIndex(SQLController.COL_TAG_NAME));
                    filterDialog.dismiss();

                    selection = SQLController.TABLE_TAGS + "." + SQLController.COL_ID + "=" + tag_id;

                    if (tag_id == SamLibConfig.TAG_AUTHOR_ALL) {
                        selection = null;
                        mCallbacks.onTitleChange(getActivity().getText(R.string.app_name).toString());
                    } else {
                        mCallbacks.onTitleChange(tg_name);
                    }

                    if (tag_id == SamLibConfig.TAG_AUTHOR_NEW) {
                        selection = SQLController.TABLE_AUTHOR + "." + SQLController.COL_isnew + "=1";
                    }
                    Log.i(DEBUG_TAG, "WHERE " + selection);
                    refresh(selection, null);
                    mCallbacks.onAuthorSelected(0);
                }
            };
            filterDialog = new FilterSelectDialog(extendedCursor, listener, getText(R.string.dialog_title_filtr).toString());
            filterDialog.show(getActivity().getSupportFragmentManager(), "FilterDialogShow");

        }
        return super.onOptionsItemSelected(item);

    }

    /**
     * Make view <b>selected<b/> and store it in the field
     * @param view the view to select
     */
    public void selectView(View view) {
        view.setSelected(true);
        selected = view;
    }

    /**
     * Clean selection for selected view
     */
    public void cleanSelection(){
        cleanItemSelection(selected);
        getListView().clearChoices();
        getListView().clearFocus();
    }

    /**
     * set sort order and restart loader to make is  actual
     * @param so new sort order
     */
    public void setSortOrder(SortOrder so){
        cleanSelection();
        order =so;
        getLoaderManager().restartLoader(AUTHOR_LIST_LOADER, null, this);
    }

    /**
     * update sort order and selection parameters and restart loader
     * @param selection selection string
     * @param so sort order string
     */
    public void refresh(String selection, SortOrder so) {
        Log.d(DEBUG_TAG, "set Selection: "+selection);
        cleanSelection();
        this.selection = selection;
        if (so != null){
            order =so;
        }
        if (selection == null){
            setEmptyText(R.string.no_authors);
        }
        else {
            setEmptyText(R.string.no_authors_tag);
        }
        
        getLoaderManager().restartLoader(AUTHOR_LIST_LOADER, null, this);
        
    }
    public SortOrder getSortOrder(){
        return order;
    }
    public String getSelection() {
        return selection;
    }
    
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.d(DEBUG_TAG,"order: "+order);
        return new CursorLoader(getActivity(),
                AuthorProvider.AUTHOR_URI, null, selection, null, order.getOrder());
    }
    
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        
        adapter.swapCursor(cursor);
    }
    
    public void onLoaderReset(Loader<Cursor> loader) {
        
        adapter.swapCursor(null);
        
    }
    
    public boolean singleClick(MotionEvent e) {
        return false;
    }
    
    public boolean swipeRight(MotionEvent e) {
        int position = getListView().pointToPosition((int) e.getX(), (int) e.getY());
        Cursor c = (Cursor) adapter.getItem(position);
        if (c.getPosition() != -1) {
            MarkRead marker = new MarkRead(getActivity().getApplicationContext());
            marker.execute(AuthorController.Cursor2Author(getActivity().getApplicationContext(), c).getId());
            return true;
        }
        return false;
        
    }
    
    public boolean swipeLeft(MotionEvent e) {
        int position = getListView().pointToPosition((int) e.getX(), (int) e.getY());
        Cursor c = (Cursor) adapter.getItem(position);
        if (c.getPosition() != -1) {
            
            launchBrowser(AuthorController.Cursor2Author(getActivity().getApplicationContext(), c));
            return true;
        }
        return false;
    }

    /**
     * Launch Browser to load Author home page
     *
     * @param a Author object
     */
    public void launchBrowser(Author a) {
        Uri uri = Uri.parse(a.getUrlForBrowser());
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uri);
        getActivity().startActivity(launchBrowser);
        
    }
    
    
    private static class AuthorViewBinder implements SimpleCursorAdapter.ViewBinder {
        
        public AuthorViewBinder() {
        }
        
        public boolean setViewValue(View view, Cursor cursor, int i) {
            
            int idx_isNew = cursor.getColumnIndex(SQLController.COL_isnew);
            int idx_name = cursor.getColumnIndex(SQLController.COL_NAME);
            int idx_mtime = cursor.getColumnIndex(SQLController.COL_mtime);
            int idx_tags = cursor.getColumnIndex(SQLController.COL_TGNAMES);
            
            if (i == idx_tags) {
                TextView tv = ((TextView) view);
                String str = cursor.getString(idx_tags);
                if (str != null) {
                    tv.setText(str.replaceAll(",", ", "));
                    return true;
                }
                return false;
            }
            if (i == idx_name) {
                TextView tv = ((TextView) view);
                tv.setText(cursor.getString(i));
                
                if (cursor.getInt(idx_isNew) == 1) {
                    
                    tv.setTypeface(Typeface.DEFAULT_BOLD);

                    //Log.d(DEBUG_TAG, "MAke Bold user: "+cursor.getString(idx_name));
                } else {
                    tv.setTypeface(Typeface.DEFAULT);
                }
                
                return true;
                
            }
            
            if (i == idx_mtime) {
                SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
                
                
                long dd = cursor.getLong(i);
                Date date = new Date(dd);
                ((TextView) view).setText(df.format(date));
                return true;
            }
            if (i == idx_isNew) {
                
                if (cursor.getInt(i) == 1) {
                    //((ImageView) view).setImageResource(R.drawable.bullet_green);
                    ((ImageView) view).setImageResource(R.drawable.open);
                } else {
                    //((ImageView) view).setImageResource(R.drawable.bullet_grey);
                    ((ImageView) view).setImageResource(R.drawable.closed);
                }
                return true;
            }
            return false;
        }
    }
    private Author author = null;
    private final int read_option_item = 21;
    private final int tags_option_item = 22;
    private final int browser_option_item = 23;
    private final int edit_author_option_item = 24;
    private final int delete_option_item = 25;

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == getListView().getId()) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

            Cursor cursor = (Cursor) adapter.getItem(info.position);
            author = AuthorController.Cursor2Author(getActivity().getApplicationContext(), cursor);

            if (author == null) {
                Log.d(DEBUG_TAG, "Context menu Created - author is NULL!!");
            } else {
                Log.d(DEBUG_TAG, "Context menu Created - author is " + author.getName());
            }
            if (author.isIsNew()) {
                menu.add(1, read_option_item, 10, getText(R.string.menu_read));
            }
            menu.add(1, tags_option_item, 20, getText(R.string.menu_tags));
            menu.add(1, browser_option_item, 30, getText(R.string.menu_open_web));
            menu.add(1, edit_author_option_item, 40, getText(R.string.menu_edit));
            menu.add(1, delete_option_item, 50, getText(R.string.menu_delete));
            menu.setHeaderTitle(author.getName());
        }

    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {

        boolean super_answer = super.onContextItemSelected(item);
        Log.d(DEBUG_TAG, "context menu item selected: " + item.getItemId() + "  super: " + super_answer);

        if (author != null) {
            if (item.getItemId() == delete_option_item) {
                Dialog alert = createDeleteAuthorAlert(author.getName());
                alert.show();
            }

            if (item.getItemId() == read_option_item) {
                MarkRead marker = new MarkRead(getActivity().getApplicationContext());
                marker.execute(author.getId());
            }
            if (item.getItemId() == tags_option_item) {
                Intent intent = new Intent(getActivity(), AuthorTagsActivity.class);
                intent.putExtra(AuthorTagsActivity.AUTHOR_ID, author.getId());
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);

            }
            if (item.getItemId() == browser_option_item) {
                launchBrowser(author);
            }
            if (item.getItemId() == edit_author_option_item) {
                EnterStringDialog ddialog = new EnterStringDialog(getActivity(), new EnterStringDialog.ClickListener() {
                    public void okClick(String txt) {
                        author.setName(txt);
                        sql.update(author);
                    }
                }, getText(R.string.dialog_title_edit_author).toString(), author.getName());

                ddialog.show();
            }

        } else {
            Log.e(DEBUG_TAG, "Author Object is NULL!!");
        }

        return super.onContextItemSelected(item);

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
                        DeleteAuthor deleter = new DeleteAuthor(getActivity().getApplicationContext());
                        deleter.execute(author.getId());
                    }
                    break;
                case Dialog.BUTTON_NEGATIVE:
                    break;
            }

        }
    };
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
