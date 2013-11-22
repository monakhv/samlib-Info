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

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import monakhv.android.samlib.sql.AuthorController;
import monakhv.android.samlib.sql.AuthorProvider;
import monakhv.android.samlib.sql.SQLController;
import monakhv.android.samlib.sql.entity.Author;
import monakhv.android.samlib.tasks.MarkRead;


/**
 *
 * @author Dmitry Monakhov
 * This is because of PullTorefresh class 
 */
public class AuthorListHelper implements
        LoaderManager.LoaderCallbacks<Cursor>, ListSwipeListener.SwipeCallBack {

    public static final int AUTHOR_LIST_LOADER = 0x01;
    private static final String DEBUG_TAG = "AuthorListHelper";
    private static final String DATE_FORMAT = "dd.MM.yyyy HH:mm:ss";
    private final Context context;
    private final LoaderManager loaderManager;
    private String selection = null;
    private final SimpleCursorAdapter adapter;
    private ListView listView;
    private final GestureDetector detector;
    private final FragmentActivity activity;
    
    private MainActivity.SortOrder order;

    public AuthorListHelper(FragmentActivity activity,PullToRefresh pull) {
        this.activity = activity;
        this.context = activity;
        this.loaderManager = activity.getSupportLoaderManager();
        String[] from = {SQLController.COL_NAME, SQLController.COL_mtime, SQLController.COL_isnew, SQLController.COL_TGNAMES};
        int[] to = {R.id.authorName, R.id.updated, R.id.icon, R.id.tgnames};
        
        adapter = new SimpleCursorAdapter(
                context.getApplicationContext(), R.layout.rowlayout,
                null, from, to,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        
        order = MainActivity.SortOrder.DateUpdate;
        adapter.setViewBinder(new AuthorViewBinder());
        detector = new GestureDetector(context, new ListSwipeListener(this));
        init(pull);
    }
    
    private void init(PullToRefresh pull) {
        loaderManager.initLoader(AUTHOR_LIST_LOADER, null, this);
        pull.setAdapter(adapter);
        listView = pull.getListView();
        pull.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                detector.onTouchEvent(event);
                return false;
            }
        });
        
        TextView tv = (TextView) activity.findViewById(R.id.id_empty_text);
        AuthorController sql = new AuthorController(context);
        if (!sql.isEmpty(selection)){
            tv.setText(R.string.pull_to_refresh_refreshing_label);
        }
        pull.getListView().setEmptyView(tv);
        
        listView.setDivider(new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{0x3300FF00, 0xFF00FF00, 0xffffffff}));
        listView.setDividerHeight(1);
        
    }
    
    public void refresh(String selection) {
        Log.d(DEBUG_TAG, "set Selection: "+selection);
        this.selection = selection;
        loaderManager.restartLoader(AUTHOR_LIST_LOADER, null, this);
        
    }

    public void setSortOrder(MainActivity.SortOrder so){
        order =so;
        loaderManager.restartLoader(AUTHOR_LIST_LOADER, null, this);
    }
    
    public MainActivity.SortOrder getSortOrder(){
        return order;
    }
    public String getSelection() {
        return selection;
    }
    
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        
        CursorLoader cursorLoader = new CursorLoader(context,
                AuthorProvider.AUTHOR_URI, null, selection, null, order.getOrder());
        return cursorLoader;
    }
    
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        
        adapter.swapCursor(cursor);
    }
    
    public void onLoaderReset(Loader<Cursor> loader) {
        
        adapter.swapCursor(null);
        
    }
    
    public boolean singleClick(MotionEvent e) {
        int position = listView.pointToPosition((int) e.getX(), (int) e.getY());
        Cursor c = (Cursor) adapter.getItem(position);
        if (c.getPosition() != -1) {
            Log.d(DEBUG_TAG, "get cursor at position: " + c.getPosition());
            authorClick(c);
            return true;
        }
        return false;
        
    }
    
    public boolean swipeRight(MotionEvent e) {
        int position = listView.pointToPosition((int) e.getX(), (int) e.getY());
        Cursor c = (Cursor) adapter.getItem(position);
        if (c.getPosition() != -1) {
            MarkRead marker = new MarkRead(context.getApplicationContext());
            marker.execute(AuthorController.Cursor2Author(context.getApplicationContext(), c).getId());
            return true;
        }
        return false;
        
    }
    
    public boolean swipeLeft(MotionEvent e) {
        int position = listView.pointToPosition((int) e.getX(), (int) e.getY());
        Cursor c = (Cursor) adapter.getItem(position);
        if (c.getPosition() != -1) {
            
            launchBrowser(AuthorController.Cursor2Author(context.getApplicationContext(), c));
            return true;
        }
        return false;
    }

    /**
     * Launch Browser to load Author home page
     *
     * @param a
     */
    public void launchBrowser(Author a) {
        Uri uri = Uri.parse(a.getUrlForBrowser());
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uri);
        context.startActivity(launchBrowser);
        
    }
    
    private void authorClick(Cursor c) {
        Author a = AuthorController.Cursor2Author(context.getApplicationContext(), c);
        Log.d(DEBUG_TAG, "Selected Author id: " + a.getId());
        showBooks(a);
    }
    
    private void showBooks(Author a) {
        Intent intent = new Intent(context, BooksActivity.class);
        intent.putExtra(BookListFragment.AUTHOR_ID, a.getId());
        //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        context.startActivity(intent);
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
}
