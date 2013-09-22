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
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Html;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import monakhv.android.samlib.data.SettingsHelper;
import monakhv.android.samlib.service.DownloadBookServiceIntent;
import monakhv.android.samlib.sql.AuthorController;
import monakhv.android.samlib.sql.AuthorProvider;
import monakhv.android.samlib.sql.BookController;
import monakhv.android.samlib.sql.SQLController;
import monakhv.android.samlib.sql.entity.Author;
import monakhv.android.samlib.sql.entity.Book;

/**
 *
 * @author monakhv
 */
public class BookListFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<Cursor>, ListSwipeListener.SwipeCallBack {

    private static final String DATE_FORMAT = "dd.MM.yyyy";
    private static final String DEBUG_TAG = "BookListFragment";
    public static final String AUTHOR_ID = "AUTHOR_ID";
    public static final int BOOK_LIST_LOADER = 0x12;
    private SimpleCursorAdapter adapter;
    private GestureDetector detector;
    private long author_id;
    private AuthorController sql ;
    private SettingsHelper settings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        author_id = getActivity().getIntent().getExtras().getInt(AUTHOR_ID);
        getLoaderManager().initLoader(BOOK_LIST_LOADER, null, this);
        sql = new AuthorController(getActivity());

        String[] from = {SQLController.COL_BOOK_TITLE, SQLController.COL_BOOK_SIZE, 
            SQLController.COL_BOOK_DESCRIPTION, SQLController.COL_BOOK_ISNEW,
            SQLController.COL_BOOK_GROUP_ID,SQLController.COL_BOOK_AUTHOR};
        int[] to = {R.id.bookTitle, R.id.bookUpdate, R.id.bookDesc, R.id.Bookicon,R.id.Staricon,R.id.bookAuthorName};

        adapter = new SimpleCursorAdapter(
                getActivity().getApplicationContext(), R.layout.book_row,
                null, from, to,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);


        adapter.setViewBinder(new BookViewBinder());
        setListAdapter(adapter);
        detector = new GestureDetector(getActivity(), new ListSwipeListener(this));

        
        settings = new SettingsHelper(getActivity());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (author_id == -1){
            setEmptyText(getActivity().getText(R.string.no_selected_books));
        }
        else {
            setEmptyText(getActivity().getText(R.string.no_new_books));
        }
        
        registerForContextMenu(getListView());
        getListView().setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                detector.onTouchEvent(event);
                return false;
            }
        });
        getListView().setDivider(new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int [] { 0x3300FF00,0xFF00FF00,0xffffffff }));
        getListView().setDividerHeight(1);

    }
    private Book selected = null;
    private int menu_mark_read = 1;
    private int menu_browser = 2;
    private int menu_selected = 3;
    private int menu_deselected = 4;

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        if (v.getId() == getListView().getId()) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

            Cursor cursor = (Cursor) adapter.getItem(info.position);
            long book_id = cursor.getLong(cursor.getColumnIndex(SQLController.COL_ID));

            
            selected = sql.getBookController().getById(book_id);
            if (selected.isIsNew()){
                menu.add(1, menu_mark_read, 1, getText(R.string.menu_read));
            }            
            menu.add(1, menu_browser, 20, getText(R.string.menu_open_web));
            if (selected.getGroup_id() == Book.SELECTED_GROUP_ID) {
                menu.add(1, menu_deselected, 40, getText(R.string.menu_deselected));
            } else {
                menu.add(1, menu_selected, 30, getText(R.string.menu_selected));
            }


        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        settings.log("MARK_READ", "BookListFragment:   onContextItemSelected  item id: " +item.getItemId());
        BookController bookSQL = sql.getBookController();

        if (item.getItemId() == menu_browser) {
            launchBrowser(selected);
        }
        if (item.getItemId() == menu_mark_read) {
            settings.log("MARK_READ", "BookListFragment:  onContextItemSelected call markRead for book: "+selected.getId()+"  -  "+selected.getUri());
            bookSQL.markRead(selected);
            sql.testMarkRead(sql.getByBook(selected));
        }
        if (item.getItemId() == menu_selected) {
            selected.setGroup_id(Book.SELECTED_GROUP_ID);
            bookSQL.update(selected);
        }
        if (item.getItemId() == menu_deselected) {
            selected.setGroup_id(0);
            bookSQL.update(selected);
        }
        return super.onContextItemSelected(item);
    }

    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        author_id = getActivity().getIntent().getExtras().getInt(AUTHOR_ID);
        
        String selection;
        if (author_id == -1){
            selection = SQLController.COL_BOOK_GROUP_ID+"="+Book.SELECTED_GROUP_ID;
        }
        else {
            selection = SQLController.COL_BOOK_AUTHOR_ID + "=" + author_id;
        }
        



        CursorLoader cursorLoader = new CursorLoader(getActivity(),
                AuthorProvider.BOOKS_URI, null, selection, null, SQLController.COL_BOOK_MTIME + " DESC");



        return cursorLoader;

    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        adapter.swapCursor(cursor);

    }

    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);

    }

    /**
     * Launch Browser to load book from web server
     *
     * @param book book to read
     */
    private void launchBrowser(Book book) {
        
        String surl = book.getUrlForBrowser();

        Log.d(DEBUG_TAG, "book url: " + surl);

        Uri uri = Uri.parse(surl);
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uri);
        SettingsHelper setting = new SettingsHelper(getActivity());
        if (setting.getAutoMarkFlag()) {
            sql.getBookController().markRead(book);
            sql.testMarkRead(sql.getByBook(book));
        }

        startActivity(launchBrowser);
    }

    /**
     * Launch Reader to read the book considering book is downloaded
     *
     * @param book the book to read
     */
    void launchReader(Book book) {
        
        Intent launchBrowser = new Intent();
        launchBrowser.setAction(android.content.Intent.ACTION_VIEW);
        launchBrowser.setDataAndType(Uri.parse("file://" + book.getFile().getAbsolutePath()), "text/html");

        SettingsHelper setting = new SettingsHelper(getActivity());
        if (setting.getAutoMarkFlag()) {
            sql.getBookController().markRead(book);
            sql.testMarkRead(sql.getByBook(book));
        }
        startActivity(launchBrowser);
    }

    public boolean singleClick(MotionEvent e) {
        int position = getListView().pointToPosition((int) e.getX(), (int) e.getY());
        Cursor c = (Cursor) adapter.getItem(position);
        if (c.getPosition() < 0) {
            //Log.d(DEBUG_TAG, "get cursor at position: "+c.getPosition()+ " Exiting.");
            return false;
        }
        long book_id = c.getLong(c.getColumnIndex(SQLController.COL_ID));

        
        Book book = sql.getBookController().getById(book_id);
        //launchBrowser(book);

        if (book.needUpdateFile()) {
            progress = new ProgressDialog(getActivity());
            progress.setMessage(getActivity().getText(R.string.download_Loading));
            progress.setCancelable(true);
            progress.setIndeterminate(true);
            progress.show();
            DownloadBookServiceIntent.start(getActivity(), book_id);


        } else {

            launchReader(book);
        }
        return true;
    }

    public boolean swipeRight(MotionEvent e) {
        int position = getListView().pointToPosition((int) e.getX(), (int) e.getY());
        Cursor c = (Cursor) adapter.getItem(position);
       
        if (c.getPosition() < 0) {
            //Log.d(DEBUG_TAG, "get cursor at position: "+c.getPosition()+ " Exiting.");
            return false;
        }
        long book_id = c.getLong(c.getColumnIndex(SQLController.COL_ID));

        Book book = sql.getBookController().getById(book_id);
        settings.log("MARK_READ", "BookListFragment: call markRead for book: "+book_id+"  -  "+book.getUri());
        sql.getBookController().markRead(book);
        Author a = sql.getByBook(book);
        sql.testMarkRead(a);


        return true;

    }
    ProgressDialog progress;

    public boolean swipeLeft(MotionEvent e) {

        int position = getListView().pointToPosition((int) e.getX(), (int) e.getY());
        Cursor c = (Cursor) adapter.getItem(position);
        
        if (c.getPosition() < 0) {
            Log.d(DEBUG_TAG, "get cursor at position: " + c.getPosition() + " Exiting.");
            return false;
        }
        long book_id = c.getLong(c.getColumnIndex(SQLController.COL_ID));
        Book book = sql.getBookController().getById(book_id);
        launchBrowser(book);
        return true;
    }

    private class BookViewBinder implements SimpleCursorAdapter.ViewBinder {

        public boolean setViewValue(View view, Cursor cursor, int i) {
            int idx_mtime = cursor.getColumnIndex(SQLController.COL_BOOK_MTIME);
            int idx_date = cursor.getColumnIndex(SQLController.COL_BOOK_DATE);
            int idx_desc = cursor.getColumnIndex(SQLController.COL_BOOK_DESCRIPTION);
            int idx_size = cursor.getColumnIndex(SQLController.COL_BOOK_SIZE);
            int idx_title = cursor.getColumnIndex(SQLController.COL_BOOK_TITLE);
            int idx_isNew = cursor.getColumnIndex(SQLController.COL_BOOK_ISNEW);
            int idx_group_id = cursor.getColumnIndex(SQLController.COL_BOOK_GROUP_ID);
            int idx_author = cursor.getColumnIndex(SQLController.COL_BOOK_AUTHOR);

            if (i == idx_title || i == idx_desc) {
                TextView tv = ((TextView) view);

                //tv.setText(cursor.getString(i).replaceAll("&quot;", "\""));
                try {
                    tv.setText(Html.fromHtml(cursor.getString(i)));
                } catch (Exception ex) {//This is because of old book scheme where Description could be null
                    tv.setText("");
                }
                return true;
            }
            
            if (i == idx_author){
                TextView tv = ((TextView) view);
                if (author_id != -1){
                    tv.setVisibility(View.GONE);
                    return true;
                }
                
                tv.setText(cursor.getString(idx_author));
                return true;
            }

            if (i == idx_size) {
                TextView tv = ((TextView) view);
                tv.setText(cursor.getString(i) + "K");
                return true;
            }



            if (i == idx_mtime || i == idx_date) {
                SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);


                long dd = cursor.getLong(i);
                Date date = new Date(dd);
                ((TextView) view).setText(df.format(date));
                return true;
            }

            if (i == idx_isNew) {

                if (cursor.getInt(i) == 1) {
                    
                    ((ImageView) view).setImageResource(R.drawable.open);
                } else {
                    
                    ((ImageView) view).setImageResource(R.drawable.closed);
                }
                return true;
            }
            if (i == idx_group_id){
                if(cursor.getInt(i)==1){
                    ((ImageView) view).setImageResource(R.drawable.rating_important);
                    return true;
                }
                else {
                    ((ImageView) view).setImageResource(R.drawable.rating_not_important);
                    return false;
                }
                
            }
            return false;
        }
    }
}
