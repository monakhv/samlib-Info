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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import monakhv.android.samlib.sql.AuthorController;
import monakhv.android.samlib.sql.entity.Author;
import monakhv.android.samlib.sql.entity.Book;
import monakhv.android.samlib.sql.entity.SamLibConfig;

/**
 *
 * @author monakhv
 */
public class BooksActivity extends MyAbstractActivity {
    
    private long author_id;
    private DownloadReceiver receiver;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.books);
        
        author_id = getIntent().getExtras().getLong(BookListFragment.AUTHOR_ID);
        
        
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        
        if (author_id != SamLibConfig.SELECTED_BOOK_ID) {
            AuthorController sql = new AuthorController(this);
            Author a = sql.getById(author_id);
            setTitle(a.getName());
        } else {
            setTitle(getText(R.string.menu_selected_go));
        }
        
        
        
        receiver = new DownloadReceiver();
        IntentFilter filter = new IntentFilter(DownloadReceiver.ACTION_RESP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(receiver, filter);
        
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }
    
    public class DownloadReceiver extends BroadcastReceiver {
        
        public static final String ACTION_RESP = "monakhv.android.samlib.action.BookDownload";
        public static final String MESG = "MESG";
        public static final String RESULT = "RESULT";
        public static final String BOOK_ID = "BOOK_ID";
        private static final String DEBUG_TAG = "DownloadReceiver";
        
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(DEBUG_TAG, "Starting onReceive");
            String mesg = intent.getStringExtra(MESG);
            long book_id = intent.getLongExtra(BOOK_ID, 0);
            
            boolean res = intent.getBooleanExtra(RESULT, false);
            
            AuthorController sql = new AuthorController(context);
            Book book = sql.getBookController().getById(book_id);
            BookListFragment listFragment = (BookListFragment) getSupportFragmentManager().findFragmentById(R.id.listBooksFragment);
            if (listFragment != null) {
                if (listFragment.progress != null) {
                    listFragment.progress.dismiss();
                }
            }
            
            if (res) {
                Log.d(DEBUG_TAG, "Starting web for url: " + book.getFileURL());
//               
                if (listFragment != null) {
                    listFragment.launchReader(book);
                }
            } else {
                Toast toast = Toast.makeText(context, mesg, Toast.LENGTH_SHORT);
                
                toast.show();
            }
        }
    }
}
