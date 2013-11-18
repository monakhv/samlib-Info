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
import android.widget.Toast;
import java.io.IOException;
import java.text.Collator;
import java.text.RuleBasedCollator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import monakhv.android.samlib.R;
import monakhv.android.samlib.exception.SamlibParseException;
import monakhv.android.samlib.sql.SearchAuthorController;
import monakhv.android.samlib.sql.entity.AuthorCard;
import monakhv.android.samlib.sql.entity.SamLibConfig;
import monakhv.samlib.http.HttpClientController;

/**
 *
 * @author Dmitry Monakhov
 */
public class SearchAuthor extends AsyncTask<String, Void, Boolean>{
    public enum ResultStatus {
        Error(R.string.author_search_error),
        Empty(R.string.author_search_empty),
        Limit(R.string.author_search_limit),
        Good(0);
        private final int imessage;
        private ResultStatus(int imesg){
           imessage=imesg;
        }
        public String getMessage(Context ctx){
            return ctx.getString(imessage);
        }
    }   ;
    
    private ResultStatus status;
    private static final String DEBUG_TAG = "SearchAuthor";    
    private Context context = null;
    private final RuleBasedCollator russianCollator;
    private final HttpClientController http = HttpClientController.getInstance();
    private final SearchAuthorController sql;
    private int inum =0;//Result number
    
    public SearchAuthor(Context ctx){
        status = ResultStatus.Good;
        
        context = ctx;
        sql = new SearchAuthorController(context);
        russianCollator =  (RuleBasedCollator) Collator.getInstance(Locale.getDefault());
        //russianCollator =  (RuleBasedCollator) Collator.getInstance(new Locale("ru", "RU"));
//        RuleBasedCollator defaultCollator = (RuleBasedCollator) Collator.getInstance(new Locale("ru", "RU"));
//        final String rules = defaultCollator.getRules();
//        
//        if (rules == null){
//            Log.e(DEBUG_TAG,"NULL RULES!!");
//        }
//        
//        try {
//            russianCollator = new RuleBasedCollator( rules.
//                    replaceAll("<'\u005f'", "<' '<'\u005f'").
//                    replaceAll("<' '", "<' '<'-'")
//            );
//        } catch (ParseException ex) {
//            Log.e(DEBUG_TAG, "RuleBasedCollator Error", ex);
//            russianCollator = defaultCollator;
//        }

        russianCollator.setStrength(Collator.IDENTICAL);
    }

    @Override
    protected Boolean doInBackground(String... params) {
        for (String pattern: params){
            try {
                if (! makeSearch(pattern)){
                    if (inum == 0){
                        status =ResultStatus.Empty;
                    }
                    else {
                        status =ResultStatus.Limit;
                    }
                }
            } catch (IOException ex) {
                Log.e(DEBUG_TAG, null, ex);
                status = ResultStatus.Error;
                return false;
            } catch (SamlibParseException ex) {
                Log.e(DEBUG_TAG, null, ex);
                status = ResultStatus.Error;
                return false;
            }
        }
        return true;
    }
     @Override
    protected void onPostExecute(Boolean result) {
        int duration = Toast.LENGTH_SHORT;
       
        if (status != ResultStatus.Good){
            Toast toast = Toast.makeText(context, status.getMessage(context), duration);
            toast.show();
        }
        
    }

    private boolean makeSearch(String pattern) throws IOException,    SamlibParseException {
        
        int page = 1;
        HashMap<String, ArrayList<AuthorCard>> colAthors = http.searchAuhors(pattern, page);
        if (colAthors != null){
            Log.i(DEBUG_TAG, "Load "+colAthors.size()+ " items");
          
        }
        
        
        while (colAthors != null) {//page cycle while we find anything

            String[] keys = colAthors.keySet().toArray(new String[0]);
            Log.i(DEBUG_TAG, "The first value "+keys[0]+"  ->   "+colAthors.get(keys[0]).get(0).getName());
            Arrays.sort(keys, russianCollator);
            int ires = Arrays.binarySearch(keys, pattern, russianCollator);
            Log.d(DEBUG_TAG,"Search result " + ires + "   length is " + keys.length);
            int istart;
            if (ires < 0) {
                int ins = -ires - 1;
                istart = ins;
            } else {
                istart = ires;
            }
            for (int i = istart; i < keys.length; i++) {
                String skey = keys[i];
                if (skey.toLowerCase().startsWith(pattern.toLowerCase())) {
                    for(AuthorCard ac : colAthors.get(skey)){
                        sql.insert(ac);
                        ++inum;
                        if (inum > SamLibConfig.SEARCH_LIMIT){
                            return false;
                        }
                    }
                    
                } else {
                    Log.d(DEBUG_TAG,"Stop By Substring: " + pattern + "   -   " + skey + "   " + keys.length + "         " + istart + "  -  " + ires);
                    return true;
                }
            }

            ++page;
            colAthors = http.searchAuhors(pattern, page);
        }
        return inum != 0;
        
    }
    
}
