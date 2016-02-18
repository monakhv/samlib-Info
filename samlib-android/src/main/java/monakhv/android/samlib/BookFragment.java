package monakhv.android.samlib;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;


import android.widget.Toast;
import monakhv.android.samlib.adapter.*;


import monakhv.android.samlib.data.DataExportImport;
import monakhv.android.samlib.dialogs.ContextMenuDialog;
import monakhv.android.samlib.dialogs.MyMenuData;
import monakhv.android.samlib.dialogs.SingleChoiceSelectDialog;
import monakhv.android.samlib.recyclerview.DividerItemDecoration;
import monakhv.samlib.service.AuthorGuiState;
import monakhv.samlib.service.BookGuiState;
import monakhv.samlib.service.GuiUpdateObject;
import monakhv.android.samlib.sortorder.BookSortOrder;

import monakhv.samlib.db.AuthorController;
import monakhv.samlib.db.entity.Book;
import monakhv.samlib.db.entity.GroupBook;
import monakhv.samlib.db.entity.SamLibConfig;
import monakhv.samlib.log.Log;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

import java.util.List;

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
 * 12/11/14.
 */
public class BookFragment extends MyBaseAbstractFragment implements
        ListSwipeListener.SwipeCallBack, LoaderManager.LoaderCallbacks<List<GroupListItem>>,
        BookExpandableAdapter.CallBack {
    public interface Callbacks {
        AuthorGuiState getAuthorGuiState();

        void showTags(long author_id);
    }

    @Override
    public void makeBookNewFlip(Book book) {
        getSamlibOperation().makeBookReadFlip(book, new BookGuiState((int) author_id, order.getOrder()), mCallbacks.getAuthorGuiState());
    }


    @Override
    public void makeGroupNewFlip(GroupBook groupBook) {
        getSamlibOperation().makeGroupReadFlip(groupBook, new BookGuiState((int) author_id, order.getOrder()), mCallbacks.getAuthorGuiState());
    }


    private static final String DEBUG_TAG = "BookFragment";
    public static final String AUTHOR_ID = "AUTHOR_ID";
    public static final String ADAPTER_STATE_EXTRA = "BookFragment.ADAPTER_STATE_EXTRA";
    private static final int BOOK_LOADER_ID = 190;
    private RecyclerView bookRV;
    private long author_id;
    private BookExpandableAdapter adapter;
    private Bundle adapterState;
    private Book book = null;//for context menu
    private BookSortOrder order;
    private GestureDetector detector;

    ProgressDialog progress;
    private ProgressBar mProgressBar;
    ContextMenuDialog contextMenuDialog;

    private TextView emptyText;
    private DataExportImport dataExportImport;
    private SingleChoiceSelectDialog dialog = null;
    private Callbacks mCallbacks;
    private AuthorController sql;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(DEBUG_TAG, "onCreate call");
        adapterState = null;

        if (getActivity().getIntent().getExtras() == null) {
            author_id = 0;
        } else {
            author_id = getActivity().getIntent().getExtras().getLong(AUTHOR_ID, 0);
        }
        Log.i(DEBUG_TAG, "onCreate: author_id = " + author_id);

        detector = new GestureDetector(getActivity(), new ListSwipeListener(this));


        order = BookSortOrder.valueOf(getSettingsHelper().getBookSortOrderString());
        dataExportImport = new DataExportImport(getSettingsHelper());
    }


    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        Log.i(DEBUG_TAG, "onAttach call");
        if (!(activity instanceof BookFragment.Callbacks)) {
            throw new IllegalStateException(
                    "Activity must implement fragment's callbacks.");
        }
        mCallbacks = (Callbacks) activity;
        sql = getAuthorController();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(DEBUG_TAG, "onCreateView call");

        View view = inflater.inflate(R.layout.book_fragment,
                container, false);
        Log.i(DEBUG_TAG, "Done making view");
        bookRV = (RecyclerView) view.findViewById(R.id.bookRV);
        emptyText = (TextView) view.findViewById(R.id.id_empty_book_text);
        mProgressBar = (ProgressBar) view.findViewById(R.id.bookProgress);


        adapter = new BookExpandableAdapter(GroupListItem.EMPTY, -1, getActivity(), this, getSettingsHelper());
        adapter.setAuthor_id(author_id);
        bookRV.setHasFixedSize(true);
        bookRV.setLayoutManager(new LinearLayoutManager(getActivity()));
        bookRV.setAdapter(adapter);


        bookRV.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

        bookRV.setOnTouchListener((v, event) -> {
            detector.onTouchEvent(event);
            return false;
        });

        emptyText.setVisibility(View.GONE);
        bookRV.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        getLoaderManager().initLoader(BOOK_LOADER_ID, null, this);
        bookRV.setItemAnimator(new BookAnimator());
        return view;
    }

    @Override
    public Loader<List<GroupListItem>> onCreateLoader(int id, Bundle args) {
        Log.d(DEBUG_TAG, "onCreateLoader call");
        return new BookLoader(getActivity(), getAuthorController(), author_id, order.getOrder());
    }

    @Override
    public void onLoadFinished(Loader<List<GroupListItem>> loader, List<GroupListItem> data) {

        //adapter.setData(data);

        BookLoader bookLoader = (BookLoader) loader;
        adapter = new BookExpandableAdapter(data, bookLoader.getMaxGroupId(), getActivity(), this, getSettingsHelper());
        adapter.setAuthor_id(author_id);
        bookRV.setAdapter(adapter);
        Log.d(DEBUG_TAG, "onLoadFinished: adapter size = " + adapter.getItemCount());
        mProgressBar.setVisibility(View.GONE);
        makeEmpty();
        if (adapterState != null && !adapterState.isEmpty()) {
            Log.d(DEBUG_TAG, "onLoadFinished: load adapter data");
            adapter.onRestoreInstanceState(adapterState);
            adapterState.clear();
        }
    }

    @Override
    public void onLoaderReset(Loader<List<GroupListItem>> loader) {

        //adapter.setData(null);
        adapter = new BookExpandableAdapter(GroupListItem.EMPTY, -1, getActivity(), this, getSettingsHelper());
        bookRV.setAdapter(adapter);

    }


    /**
     * Make empty text view
     */
    private void makeEmpty() {
        if (adapter.getItemCount() == 0) {
            bookRV.setVisibility(View.GONE);
            emptyText.setVisibility(View.VISIBLE);
            if (author_id == SamLibConfig.SELECTED_BOOK_ID) {
                emptyText.setText(R.string.no_selected_books);
            } else {
                emptyText.setText(R.string.no_new_books);
            }

        } else {
            bookRV.setVisibility(View.VISIBLE);
            emptyText.setVisibility(View.GONE);
        }
    }

    void updateAdapter() {
        Log.d(DEBUG_TAG, "updateAdapter call");
        //very ugly hack
        if (order == null) {
            Context ctx = getActivity().getApplicationContext();
            if (ctx == null) {
                Log.e(DEBUG_TAG, "Context is NULL");
            }

            order = BookSortOrder.valueOf(getSettingsHelper().getBookSortOrderString());
        }
        getLoaderManager().restartLoader(BOOK_LOADER_ID, null, this);
    }

    public void updateAdapter(Book book) {

        GroupBook groupBook = sql.getGroupBookController().getByBook(book);

        adapter.updateData(book, groupBook, -1);
    }


    Subscriber<GuiUpdateObject> mSubscriber = new Subscriber<GuiUpdateObject>() {
        @Override
        public void onCompleted() {
            Log.i(DEBUG_TAG, "onCompleted");
        }

        @Override
        public void onError(Throwable e) {
            Log.e(DEBUG_TAG, "onError", e);
        }

        @Override
        public void onNext(GuiUpdateObject guiUpdateObject) {
            //TODO: should move out of main thread
            if (guiUpdateObject.isBook()) {
                Book b = (Book) guiUpdateObject.getObject();
                GroupBook groupBook = sql.getGroupBookController().getByBook(b);

                adapter.updateData(b, groupBook, guiUpdateObject.getSortOrder());
            }
            if (guiUpdateObject.isGroup()) {
                Log.d(DEBUG_TAG,"onNext: get Group");
                GroupListItem groupListItem;
                if (guiUpdateObject.getObjectId() == -1) {
                    Log.d(DEBUG_TAG,"onNext: id = -1");
                    groupListItem = new GroupListItem();
                    List<Book> childList = sql.getBookController().getAll(sql.getById(author_id), order.getOrder());
                    Log.d(DEBUG_TAG,"onNext: childList: "+childList.size());
                    groupListItem.setChildItemList(childList);
                } else {
                    GroupBook groupBook = (GroupBook) guiUpdateObject.getObject();
                    List<Book> childList = sql.getBookController().getBookForGroup(groupBook, order.getOrder());
                    groupListItem = new GroupListItem(groupBook, childList);
                    Log.d(DEBUG_TAG,"onNext: get Group: "+groupBook.getName()+"  id: "+groupBook.getId());
                }


                adapter.updateData(groupListItem, guiUpdateObject.getSortOrder());
            }
        }
    };

    /**
     * Set new author_id and update selection,adapter and empty view
     *
     * @param id Author id or special parameters
     */
    public void setAuthorId(long id) {
        emptyText.setVisibility(View.GONE);
        bookRV.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        author_id = id;

        adapter.setAuthor_id(id);
        updateAdapter();


        adapter.cleanSelection();
    }

    @Override
    public boolean singleClick(MotionEvent e) {
        int position = bookRV.getChildAdapterPosition(bookRV.findChildViewUnder(e.getX(), e.getY()));

        if (position < 0) {
            return true;
        }
        try {
            if (adapter.getItemViewType(position) == BookExpandableAdapter.TYPE_PARENT) {
                adapter.flipCollapse(position);
                return true;
            }
        } catch (IllegalStateException ex) {
            Log.w(DEBUG_TAG, "singleClick: wrong state: " + position, ex);
        }

        book = adapter.getBook(position);


        if (book == null) {
            Log.e(DEBUG_TAG, "singleClick: null book error position = " + position);
            return false;
        }
        selected_position = position;
        loadBook(book);
        bookRV.playSoundEffect(SoundEffectConstants.CLICK);
        return true;
    }

    @Override
    public boolean swipeRight(MotionEvent e) {
        Log.v(DEBUG_TAG, "making swipeRight");
        int position = bookRV.getChildAdapterPosition(bookRV.findChildViewUnder(e.getX(), e.getY()));

        adapter.makeAllRead(position);
        return true;
    }

    @Override
    public boolean swipeLeft(MotionEvent e) {
        int position = bookRV.getChildAdapterPosition(bookRV.findChildViewUnder(e.getX(), e.getY()));
        adapter.toggleSelection(position);

        book = adapter.getSelected();

        if (book == null) {
            return false;
        }
        launchBrowser(book);
        return true;
    }

    private final int menu_mark_read = 1;
    private final int menu_browser = 2;
    private final int menu_selected = 3;
    private final int menu_deselected = 4;
    private final int menu_reload = 10;
    private final int menu_fixed = 6;
    private final int menu_choose_version = 7;

    private int selected_position;

    @Override
    public void longPress(MotionEvent e) {
        int position = bookRV.getChildAdapterPosition(bookRV.findChildViewUnder(e.getX(), e.getY()));
        adapter.toggleSelection(position);

        book = adapter.getSelected();

        if (book == null) {
            return;
        }
        selected_position = position;
        final MyMenuData menu = new MyMenuData();

        if (book.isIsNew()) {
            menu.add(menu_mark_read, getString(R.string.menu_read));
        }
        menu.add(menu_browser, getString(R.string.menu_open_web));
        if (book.isSelected()) {
            menu.add(menu_deselected, getString(R.string.menu_deselected));
        } else {
            menu.add(menu_selected, getString(R.string.menu_selected));
        }
        menu.add(menu_reload, getString(R.string.menu_reload));
        if (book.isPreserve()) {
            menu.add(menu_fixed, getString(R.string.menu_set_unfixed));
            menu.add(menu_choose_version, getString(R.string.menu_version_choose));
        } else {
            menu.add(menu_fixed, getString(R.string.menu_set_fixed));
        }


        contextMenuDialog = ContextMenuDialog.getInstance(menu, (parent, view, position1, id) -> {
            int item = menu.getIdByPosition(position1);
            contextSelector(item);
            contextMenuDialog.dismiss();
        }, null);

        contextMenuDialog.show(getActivity().getSupportFragmentManager(), "bookContext");


    }

    private void contextSelector(int item) {
        if (item == menu_browser) {
            launchBrowser(book);
        }
        if (item == menu_mark_read) {

            adapter.makeRead(selected_position);
        }
        if (item == menu_selected) {
            sql.getBookController().setSelected(book);
            updateAdapter(book);
        }
        if (item == menu_deselected) {
            sql.getBookController().setDeselected(book);
            updateAdapter(book);
        }
        if (item == menu_reload) {

            getSettingsHelper().cleanBookFile(book);


            loadBook(book);
        }
        if (item == menu_fixed) {

            if (book.isPreserve()) {
                Log.i(DEBUG_TAG, "remove preserved mark for book " + book.getUri());
                //TODO: alert Dialogs
                //TODO: clean all copies and reload
                book.setPreserve(false);
            } else {
                Log.i(DEBUG_TAG, "making book preserved " + book.getUri());
                getSettingsHelper().makePreserved(book);
                book.setPreserve(true);
            }
            sql.getBookController().update(book);
            updateAdapter(book);

        }
        if (item == menu_choose_version) {
            final String[] files = getSettingsHelper().getBookFileVersions(book);
            if (files.length == 0L) {
                Log.i(DEBUG_TAG, "file is NULL");
                //TODO: alarm no version is found
                return;
            }
            dialog = SingleChoiceSelectDialog.getInstance(files, (parent, view, position, id) -> {
                dialog.dismiss();
                String file = files[position];

                launchReader(book, file);
            }, getString(R.string.menu_version_choose));
            dialog.show(getActivity().getSupportFragmentManager(), "readVersion");

        }
    }

    /**
     * Launch Browser to load book from web server
     *
     * @param book book to read
     */
    private void launchBrowser(Book book) {

        String sUrl = book.getUrlForBrowser(getSettingsHelper());

        Log.d(DEBUG_TAG, "book url: " + sUrl);

        Uri uri = Uri.parse(sUrl);
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uri);

        if (getSettingsHelper().getAutoMarkFlag()) {

            adapter.makeRead(selected_position);
        }

        startActivity(launchBrowser);
    }


    private SingleChoiceSelectDialog sortDialog;


    /**
     * Show Dialog to select sort order for Book list
     */
    public void selectSortOrder() {
        AdapterView.OnItemClickListener listener = (parent, view, position, id) -> {
            BookSortOrder so = BookSortOrder.values()[position];
            setSortOrder(so);
            sortDialog.dismiss();

        };
        sortDialog = SingleChoiceSelectDialog.getInstance(BookSortOrder.getTitles(getActivity()), listener, this.getString(R.string.dialog_title_sort_book), getSortOrder().ordinal());
        sortDialog.show(getActivity().getSupportFragmentManager(), "DoBookSortDialog");
    }

    void setSortOrder(BookSortOrder so) {
        order = so;
        updateAdapter();
    }

    BookSortOrder getSortOrder() {
        return order;
    }


    private void loadBook(Book book) {
        book.setFileType(getSettingsHelper().getFileType());
        if (dataExportImport.needUpdateFile(book)) {
            progress = new ProgressDialog(getActivity());
            progress.setMessage(getActivity().getText(R.string.download_Loading));
            progress.setCancelable(true);
            //progress.setIndeterminate(true);
            progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progress.setMax(100);
            progress.show();
            //DownloadBookService.start(getActivity(), book.getId(), false);

            final Subscription subscription=getBookDownloadService().downloadBook(book)
                    .onBackpressureBuffer()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(getBookDownloadProgress());
            addSubscription(subscription);
        } else {

            launchReader(book);
        }

    }

    private Subscriber<Integer> getBookDownloadProgress(){
        return new Subscriber<Integer>() {
            @Override
            public void onCompleted() {
                progress.dismiss();
                launchReader(book);
            }

            @Override
            public void onError(Throwable e) {
                progress.dismiss();
                Log.e(DEBUG_TAG,"onError",e);
                Toast toast = Toast.makeText(getActivity(), getActivity().getText(R.string.download_book_error), Toast.LENGTH_SHORT);
                toast.show();
            }

            @Override
            public void onNext(Integer integer) {
                progress.setProgress(integer);
                //Log.d(DEBUG_TAG,"onNext: "+integer);
            }
        };

    }


    /**
     * Launch Reader to read the book considering book is downloaded
     *
     * @param book the book to read
     */
    void launchReader(Book book) {
        launchReader(book, null);
    }

    /**
     * Launch Reader to read the book considering book is downloaded
     * To read given version
     *
     * @param book the book to read
     * @param file version file name
     */
    void launchReader(Book book, String file) {
        String url;
        if (file == null) {
            url = getSettingsHelper().getBookFileURL(book);
        } else {
            url = getSettingsHelper().getBookFileURL(book, file);
        }


        Intent launchBrowser = new Intent();
        launchBrowser.setAction(android.content.Intent.ACTION_VIEW);
        launchBrowser.setDataAndType(Uri.parse(url), book.getFileMime());

        if (launchBrowser.resolveActivity(getActivity().getPackageManager())==null){
            Toast toast=Toast.makeText(getContext(),getString(R.string.no_such_application_to_view),Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        if (getSettingsHelper().getAutoMarkFlag()) {
            adapter.makeRead(selected_position);
        }
        startActivity(launchBrowser);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(DEBUG_TAG, "onDestroy call");
        getLoaderManager().destroyLoader(BOOK_LOADER_ID);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.books_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int sel = item.getItemId();
        if (sel == R.id.menu_books_tags && author_id > 0) {
            mCallbacks.showTags(author_id);
        }
        if (sel == R.id.menu_books_sort) {
            selectSortOrder();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(DEBUG_TAG, "onResume call");

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(DEBUG_TAG, "onPause call");
        if (adapterState == null) {
            adapterState = new Bundle();
        }

        adapter.onSaveInstanceState(adapterState);
    }

    public Bundle getAdapterState() {
        return adapterState;
    }

    public void setAdapterState(Bundle adapterState) {
        this.adapterState = adapterState;
    }
}
