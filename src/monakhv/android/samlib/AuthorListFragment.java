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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ImageView;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;

import monakhv.android.samlib.sql.AuthorController;
import monakhv.android.samlib.sql.AuthorProvider;
import monakhv.android.samlib.tasks.DeleteAuthor;
import monakhv.android.samlib.tasks.MarkRead;
import monakhv.android.samlib.sql.SQLController;
import monakhv.android.samlib.sql.entity.Author;

/**
 *
 * @author monakhv
 */
public class AuthorListFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<Cursor>, ListSwipeListener.SwipeCallBack {

    public static final int AUTHOR_LIST_LOADER = 0x01;
    private static final String DEBUG_TAG = "AuthorListFragment";
    private static final String DATE_FORMAT = "dd.MM.yyyy HH:mm:ss";
    private SimpleCursorAdapter adapter;
    private GestureDetector detector;
    private String selection= null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String[] from = {SQLController.COL_NAME, SQLController.COL_mtime, SQLController.COL_isnew,SQLController.COL_TGNAMES};
        int[] to = {R.id.authorName, R.id.updated, R.id.icon,R.id.tgnames};

        getLoaderManager().initLoader(AUTHOR_LIST_LOADER, null, this);

        adapter = new SimpleCursorAdapter(
                getActivity().getApplicationContext(), R.layout.rowlayout,
                null, from, to,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);


        adapter.setViewBinder(new AuthorViewBinder());
        setListAdapter(adapter);

        detector = new GestureDetector(getActivity(), new ListSwipeListener(this));
        
    }

    public void refresh(String sel) {
        selection = sel;
        //getLoaderManager().getLoader(AUTHOR_LIST_LOADER).reset();
       
        getLoaderManager().restartLoader(AUTHOR_LIST_LOADER,null, this);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEmptyText(getActivity().getText(R.string.no_authors));
        registerForContextMenu(getListView());

        getListView().setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                detector.onTouchEvent(event);
                return false;
            }
        });
        //getListView().setDivider(new ColorDrawable(0x3300FF00));
        
        getListView().setDivider(new GradientDrawable(Orientation.LEFT_RIGHT, new int [] { 0x3300FF00,0xFF00FF00,0xffffffff }));
        getListView().setDividerHeight(1);
    }
    private Author author;//Selected by context menu

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);


        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;

        Cursor cursor = (Cursor) adapter.getItem(info.position);

        author = AuthorController.Cursor2Author(getActivity().getApplicationContext(), cursor);

        if (author == null) {
            Log.d(DEBUG_TAG, "Context menu Created - author is NULL!!");
        } else {
            Log.d(DEBUG_TAG, "Context menu Created - author is " + author.getName());
        }



    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        boolean super_answer = super.onContextItemSelected(item);
        Log.d(DEBUG_TAG, "context menu item selected: " + item.getItemId() + "  super: " + super_answer);

        if (author != null) {
            if (item.getItemId() == R.id.delete_option_item) {
                Dialog alert = createDeleteAuthorAlert(author.getName());
                alert.show();
            }

            if (item.getItemId() == R.id.read_option_item) {
                MarkRead marker = new MarkRead(getActivity().getApplicationContext());
                marker.execute(author.getId());
            }
            if (item.getItemId() == R.id.tags_option_item) {
                Intent intent = new Intent(getActivity(), AuthorTagsActivity.class);
                intent.putExtra(AuthorTagsActivity.AUTHOR_ID, author.getId());
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);

            }
            if (item.getItemId() == R.id.browser_option_item){
                launchBrowser(author);
            }
            if (item.getItemId() == R.id.edit_author_option_item){
                final AuthorController sql = new AuthorController(getActivity());
                EnterStringDialog dialog = new EnterStringDialog(getActivity(), new EnterStringDialog.ClickListener() {

                    public void okClick(String txt) {
                        author.setName(txt);
                        sql.update(author);
                    }
                }, getActivity().getText(R.string.dialog_title_edit_author).toString(), author.getName());
                
                dialog.show();
            }

        } else {
            Log.e(DEBUG_TAG, "Author Object is NULL!!");
        }

        return super.onContextItemSelected(item);

    }

    /**
     * Create Alert Dialog to wrn about Author delete
     *
     * @param filename
     * @return
     */
    private Dialog createDeleteAuthorAlert(String authorName) {
        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
        adb.setTitle(R.string.Attention);

        String msg = getString(R.string.alert_delete_author);
        msg = msg.replaceAll("__", authorName);

        adb.setMessage(msg);
        adb.setIcon(android.R.drawable.ic_dialog_alert);
        adb.setPositiveButton(R.string.Yes, deleteAuthoristener);
        adb.setNegativeButton(R.string.No, deleteAuthoristener);
        return adb.create();

    }
    private DialogInterface.OnClickListener deleteAuthoristener = new OnClickListener() {
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

    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        
        
        CursorLoader cursorLoader = new CursorLoader(getActivity(),
                AuthorProvider.AUTHOR_URI, null, selection, null, SQLController.COL_mtime + " DESC");
        return cursorLoader;
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        adapter.swapCursor(cursor);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    public boolean singleClick(MotionEvent e) {
        int position = getListView().pointToPosition((int) e.getX(), (int) e.getY());
        Cursor c = (Cursor) adapter.getItem(position);
        if (c.getPosition() != -1) {
            Log.d(DEBUG_TAG, "get cursor at position: " + c.getPosition());
            authorClick(c);
            return true;
        }
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
     * Set decoration for List elements
     */
    private class AuthorViewBinder implements SimpleCursorAdapter.ViewBinder {

        public boolean setViewValue(View view, Cursor cursor, int i) {
            int idx_isNew = cursor.getColumnIndex(SQLController.COL_isnew);
            int idx_name = cursor.getColumnIndex(SQLController.COL_NAME);
            int idx_mtime = cursor.getColumnIndex(SQLController.COL_mtime);
            int idx_tags = cursor.getColumnIndex(SQLController.COL_TGNAMES);

            if (i == idx_tags){
                TextView tv = ((TextView) view);
                String str = cursor.getString(idx_tags);
                if (str != null){
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

    private void authorClick(Cursor c) {
        Author a = AuthorController.Cursor2Author(getActivity().getApplicationContext(), c);
        Log.d(DEBUG_TAG, "Selected Author id: " + a.getId());
        showBooks(a);
    }

    /**
     * Launch Browser to load Author home page
     *
     * @param a
     */
    private void launchBrowser(Author a) {
        Uri uri = Uri.parse(a.getUrl().toString());
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(launchBrowser);

    }

    private void showBooks(Author a) {
        Intent intent = new Intent(getActivity(), NewBooksActivity.class);
        intent.putExtra(BookListFragment.AUTHOR_ID, a.getId());
        //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }
}
