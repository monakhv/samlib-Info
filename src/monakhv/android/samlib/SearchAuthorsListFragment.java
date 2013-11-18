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
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import monakhv.android.samlib.sql.AuthorProvider;
import monakhv.android.samlib.sql.SQLController;
import monakhv.android.samlib.tasks.SearchAuthor;

/**
 *
 * @author Dmitry Monakhov
 */
public class SearchAuthorsListFragment extends ListFragment  implements
        LoaderManager.LoaderCallbacks<Cursor>{
   
    public static final int AC_LIST_LOADER = 0x13;
    private SimpleCursorAdapter adapter;
    
    
    private String pattern;
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pattern = getActivity().getIntent().getExtras().getString(SearchAuthorActivity.EXTRA_PATTERN);
        
        SearchAuthor task = new SearchAuthor(getActivity());
        task.execute(pattern);
        
        
        getLoaderManager().initLoader(AC_LIST_LOADER, null, this);
        
        String[] from = {SQLController.COL_AC_NAME};
        int[] to = {R.id.acName};
        adapter = new SimpleCursorAdapter(
                getActivity().getApplicationContext(), R.layout.author_search_row,
                null, from, to,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        setListAdapter(adapter);
        
        
    }

    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
         CursorLoader cursorLoader = new CursorLoader(getActivity(), AuthorProvider.SEARCH_AUTHOR_URI, null, null, null, null);
         
         return cursorLoader;
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        adapter.swapCursor(cursor);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
    
}
