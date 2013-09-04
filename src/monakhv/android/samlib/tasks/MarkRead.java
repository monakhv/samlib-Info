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
package monakhv.android.samlib.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import monakhv.android.samlib.sql.AuthorController;
import monakhv.android.samlib.sql.entity.Author;

/**
 *
 * @author monakhv
 */
public class MarkRead extends  AsyncTask<Integer, Void, Boolean>{
    private static final String DEBUG_TAG="MarkRead";
    private AuthorController ctl=null;
    
    public MarkRead(Context c){
        ctl = new AuthorController(c) ;
    }

    @Override
    protected Boolean doInBackground(Integer... params) {
        
        Author a = ctl.getById(params[0]);
        
        if (a == null){
            Log.e(DEBUG_TAG, "Author not found to update");
            return false;
        }
        
        if (! a.isIsNew()) {
            Log.d(DEBUG_TAG, "Author is read - no update need");
            return false;
        }
        
        
        int i = ctl.markRead(a);
        
        Log.d(DEBUG_TAG, "Update author status: "+i);
        
        return true;        
    }

}
