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

/**
 *
 * @author monakhv
 */
public class DeleteAuthor extends  AsyncTask<Integer, Void, Boolean>{
    private AuthorController ctl =null;
    private static final String DEBUG_TAG="DeleteAuthor";
    
    public DeleteAuthor(Context c){
        ctl = new AuthorController(c);
    }

    @Override
    protected Boolean doInBackground(Integer... params) {
        
        int res = ctl.delete(ctl.getById(params[0]));
        Log.d(DEBUG_TAG, "Author id "+params[0]+" deleted, status "+res);
        return res == 1;
    }
    
}
