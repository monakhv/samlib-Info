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

import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import monakhv.android.samlib.data.SettingsHelper;
import monakhv.android.samlib.sql.AuthorProvider;
import monakhv.android.samlib.sql.SQLController;

/**
 *
 * @author Dmitry Monakhov
 */
public class AuthorListFragment extends ListFragment implements  LoaderManager.LoaderCallbacks<Cursor>{
    public static final int AUTHOR_LIST_LOADER = 0x01;
    private static final String DEBUG_TAG = "AuthorListFragment";
    private static final String DATE_FORMAT = "dd.MM.yyyy HH:mm:ss";
    private SimpleCursorAdapter adapter;
    private MainActivity.SortOrder order;
    private String selection = null;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String[] from = {SQLController.COL_NAME, SQLController.COL_mtime, SQLController.COL_isnew, SQLController.COL_TGNAMES};
        int[] to = {R.id.authorName, R.id.updated, R.id.icon, R.id.tgnames};
        SettingsHelper settings = new SettingsHelper(getActivity());
        order = settings.getAuthorSortOrder();

        getLoaderManager().initLoader(AUTHOR_LIST_LOADER, null, this);
        adapter = new SimpleCursorAdapter(
                getActivity().getApplicationContext(), R.layout.rowlayout,
                null, from, to,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        
        adapter.setViewBinder(new AuthorViewBinder()); 
        setListAdapter(adapter);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListView().setEmptyView(getActivity().findViewById(R.id.id_empty_text));
       
        
    }
    public void setSortOrder(MainActivity.SortOrder so) {
        order = so;
        getLoaderManager().restartLoader(AUTHOR_LIST_LOADER, null, this);
    }

    public void refresh(String selection, MainActivity.SortOrder so) {
        Log.d(DEBUG_TAG, "set Selection: " + selection);
        this.selection = selection;
        if (so != null) {
            order = so;
        }
        getLoaderManager().restartLoader(AUTHOR_LIST_LOADER, null, this);

    }

    public MainActivity.SortOrder getSortOrder() {
        return order;
    }
    

    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.d(DEBUG_TAG,"order: "+order);
         CursorLoader cursorLoader = new CursorLoader(getActivity(),
                AuthorProvider.AUTHOR_URI, null, selection, null, order.getOrder());
        return cursorLoader;
        
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        adapter.swapCursor(cursor);
        
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
        
    }
    private static class AuthorViewBinder implements SimpleCursorAdapter.ViewBinder {

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
