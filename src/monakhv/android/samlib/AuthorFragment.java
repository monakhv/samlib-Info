package monakhv.android.samlib;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;

import monakhv.android.samlib.adapter.AuthorCursorAdapter;
import monakhv.android.samlib.data.SettingsHelper;
import monakhv.android.samlib.dialogs.ContextMenuDialog;
import monakhv.android.samlib.dialogs.EnterStringDialog;
import monakhv.android.samlib.dialogs.FilterSelectDialog;
import monakhv.android.samlib.dialogs.MyMenuData;
import monakhv.android.samlib.dialogs.SingleChoiceSelectDialog;
import monakhv.android.samlib.recyclerview.DividerItemDecoration;
import monakhv.android.samlib.recyclerview.RecyclerViewDelegate;
import monakhv.android.samlib.service.UpdateServiceIntent;
import monakhv.android.samlib.sql.AuthorProvider;
import monakhv.android.samlib.sql.SQLController;
import monakhv.android.samlib.sql.entity.Author;
import monakhv.android.samlib.sql.entity.SamLibConfig;
import monakhv.android.samlib.tasks.DeleteAuthor;
import monakhv.android.samlib.tasks.MarkRead;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.DefaultHeaderTransformer;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

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
    private static final String DEBUG_TAG="AuthorFragment";

    private RecyclerView authorRV;
    private AuthorCursorAdapter adapter;

    private String selection = null;
    private SortOrder order;
    private PullToRefreshLayout mPullToRefreshLayout;
    private GestureDetector detector;
    private boolean updateAuthor=false;//true update the only selected author
    private Author author = null;//for context menu selection
    private TextView updateTextView;
    private ContextMenuDialog contextMenu;
    private SingleChoiceSelectDialog sortDialog;
    private FilterSelectDialog filterDialog;

    public interface Callbacks {
        public void onAuthorSelected(long id);

        public void selectBookSortOrder();

        public void onTitleChange(String lTitle);

        public void addAuthorFromText();
    }

    private static Callbacks mCallbacks;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SettingsHelper settingsHelper= new SettingsHelper(getActivity().getApplicationContext());
        order = SortOrder.valueOf(settingsHelper.getAuthorSortOrderString());
        detector = new GestureDetector(getActivity(), new ListSwipeListener(this));
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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.author_fragment,
                container, false);
        authorRV = (RecyclerView) view.findViewById(R.id.authorRV);




        adapter = getAdapter();
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



        return view;

    }
    private AuthorCursorAdapter  getAdapter(){
        Cursor c = getActivity().getContentResolver().query(AuthorProvider.AUTHOR_URI, null, selection, null, order.getOrder());

        return  new AuthorCursorAdapter(getActivity(),c);
    }
    private void updateAdapter(){
        adapter.changeCursor(getActivity().getContentResolver().query(AuthorProvider.AUTHOR_URI, null, selection, null, order.getOrder()));
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
        mCallbacks.onAuthorSelected(adapter.getItemId(position));
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

    private final int read_option_item = 21;
    private final int tags_option_item = 22;
    private final int browser_option_item = 23;
    private final int edit_author_option_item = 24;
    private final int delete_option_item = 25;
    private final int update_option_item = 35;
    @Override
    public void longPress(MotionEvent e) {
        int position = authorRV.getChildPosition(authorRV.findChildViewUnder(e.getX(),e.getY()));
        adapter.toggleSelection(position);

        author= adapter.getSelected();

        if (author == null){
            return;
        }
        final MyMenuData menu = new MyMenuData();

        if (author.isIsNew()) {
            menu.add(read_option_item,getString(R.string.menu_read));
        }
        menu.add(tags_option_item, getString(R.string.menu_tags));
        menu.add(browser_option_item, getString(R.string.menu_open_web));
        menu.add(edit_author_option_item, getString(R.string.menu_edit));
        menu.add(delete_option_item, getString(R.string.menu_delete));
        menu.add( update_option_item,getString(R.string.menu_refresh));

        contextMenu = ContextMenuDialog.getInstance(menu, new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int item = menu.getIdByPosition(position);
                contextSelector(item);
                contextMenu.dismiss();
            }
        },author.getName());

        contextMenu.show(getActivity().getSupportFragmentManager(), "authorContext");

    }

    private void contextSelector(int item){


        if (item== delete_option_item) {
            Dialog alert = createDeleteAuthorAlert(author.getName());
            alert.show();
        }

        if (item == read_option_item) {
            MarkRead marker = new MarkRead(getActivity().getApplicationContext());
            marker.execute(author.getId());
        }
        if (item == tags_option_item) {
            Intent intent = new Intent(getActivity(), AuthorTagsActivity.class);
            intent.putExtra(AuthorTagsActivity.AUTHOR_ID, author.getId());
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);

        }
        if (item == browser_option_item) {
            launchBrowser(author);
        }
        if (item==update_option_item){
            updateAuthor=true;
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
        Uri uri = Uri.parse(a.getUrlForBrowser(getActivity()));
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
                        DeleteAuthor deleter = new DeleteAuthor(getActivity().getApplicationContext());
                        deleter.execute(author.getId());
                    }
                    break;
                case Dialog.BUTTON_NEGATIVE:
                    break;
            }

        }
    };

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.options_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int sel = item.getItemId();
        if (sel == android.R.id.home) {
            if (getSelection() != null) {
                refresh(null, null);
                mCallbacks.onTitleChange(getString(R.string.app_name));
            } else {
                getActivity().finish();
            }
        }
        if (sel == R.id.menu_refresh) {
            startRefresh();

        }

        if (sel == R.id.sort_option_item_books) {
            mCallbacks.selectBookSortOrder();
        }
        if (sel == R.id.sort_option_item) {

            AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {

                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    SortOrder so = SortOrder.values()[position];
                    //mCallbacks.onAuthorSelected(0);
                    setSortOrder(so);
                    sortDialog.dismiss();
                }

            };
            sortDialog = SingleChoiceSelectDialog.getInstance(SortOrder.getTitles(getActivity()), listener, this.getString(R.string.dialog_title_sort_author), getSortOrder().ordinal());

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
            getActivity(). startActivityForResult(prefsIntent,MainActivity.PREFS_ACTIVITY);
        }
        if (sel == R.id.archive_option_item) {

            Log.d(DEBUG_TAG, "go to Archive");
            Intent prefsIntent = new Intent(getActivity().getApplicationContext(),
                    ArchiveActivity.class);
            //prefsIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

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
                    //mCallbacks.onAuthorSelected(0);
                }
            };
            filterDialog = FilterSelectDialog.getInstance(extendedCursor, listener, getText(R.string.dialog_title_filtr).toString());
            filterDialog.show(getActivity().getSupportFragmentManager(), "FilterDialogShow");

        }
        return super.onOptionsItemSelected(item);

    }

    public String getSelection() {
        return selection;
    }

    private void cleanSelection() {
        adapter.cleanSelection();
    }
    /**
     * update sort order and selection parameters and restart loader
     *
     * @param selection selection string
     * @param so        sort order string
     */
    public void refresh(String selection, SortOrder so) {
        Log.d(DEBUG_TAG, "set Selection: " + selection);
        cleanSelection();
        this.selection = selection;
        if (so != null) {
            order = so;
        }
        //TODO: make empty text VIEW
//        if (selection == null) {
//            setEmptyText(R.string.no_authors);
//        } else {
//            setEmptyText(R.string.no_authors_tag);
//        }

        updateAdapter();
    }
    /**
     * set sort order and restart loader to make is  actual
     *
     * @param so new sort order
     */
    public void setSortOrder(SortOrder so) {
        cleanSelection();
        order = so;
        updateAdapter();
    }
    public SortOrder getSortOrder(){
        return order;
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
