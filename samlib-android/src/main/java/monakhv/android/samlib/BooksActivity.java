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


import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;


import monakhv.android.samlib.sql.AuthorController;
import monakhv.samlib.db.entity.Author;
import monakhv.samlib.db.entity.SamLibConfig;

/**
 *
 * @author monakhv
 */
public class BooksActivity extends MyAbstractActivity {
    
    private long author_id;
    private DownloadReceiver receiver;
    private BookFragment listFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.books);
        
        author_id = getIntent().getExtras().getLong(BookFragment.AUTHOR_ID);
        listFragment = (BookFragment) getSupportFragmentManager().findFragmentById(R.id.listBooksFragment);
        listFragment.setHasOptionsMenu(true);
        
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        
        if (author_id != SamLibConfig.SELECTED_BOOK_ID) {
            AuthorController sql = new AuthorController(this);
            Author a = sql.getById(author_id);
            if (a != null){
                setTitle(a.getName());
            }

        } else {
            setTitle(getText(R.string.menu_selected_go));
        }
        
        
        
        receiver = new DownloadReceiver(listFragment);
        IntentFilter filter = new IntentFilter(DownloadReceiver.ACTION_RESP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(receiver, filter);
        
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }
    

}
