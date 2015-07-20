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
import android.content.Intent;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.Serializable;
import java.text.Collator;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


import monakhv.android.samlib.R;
import monakhv.android.samlib.data.SettingsHelper;
import monakhv.android.samlib.search.SearchAuthorActivity.SearchReceiver;
import monakhv.samlib.exception.SamlibParseException;
import monakhv.samlib.db.entity.AuthorCard;
import monakhv.samlib.db.entity.SamLibConfig;
import monakhv.samlib.http.HttpClientController;
import monakhv.samlib.log.Log;
import monakhv.samlib.service.SamlibService;

/**
 *
 * @author Dmitry Monakhov
 */
public class SearchAuthor extends AsyncTask<String, Void, Boolean> {

    public enum ResultStatus {

        Error(R.string.author_search_error),
        Empty(R.string.author_search_empty),
        Limit(R.string.author_search_limit),
        Good(0);
        private final int imessage;

        private ResultStatus(int imesg) {
            imessage = imesg;
        }

        public String getMessage(Context ctx) {
            return ctx.getString(imessage);
        }
    }

    private ResultStatus status;
    private static final String DEBUG_TAG = "SearchAuthor";
    private Context context = null;
    private  final HttpClientController http ;


    private  List<AuthorCard> result;
    private final SettingsHelper settings;

    public SearchAuthor(Context ctx) {
        status = ResultStatus.Good;

        context = ctx;
        settings = new SettingsHelper(context);
        http = HttpClientController.getInstance(settings);

    }

    @Override
    protected Boolean doInBackground(String... params) {


        for (String pattern : params) {

            try {
                result = SamlibService.makeSearch(pattern,http);
                if (result.isEmpty()){
                    status = ResultStatus.Empty;
                }
                else if (result.size() >= SamLibConfig.SEARCH_LIMIT){
                    status = ResultStatus.Limit;
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
    protected void onPostExecute(Boolean res) {

        Intent broadcastIntent = new Intent();
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.setAction(SearchReceiver.ACTION_RESP);
        if (status != ResultStatus.Good) {
            broadcastIntent.putExtra(SearchReceiver.EXTRA_MESSAGE, status.getMessage(context));
        }
        broadcastIntent.putExtra(SearchReceiver.EXTRA_RESULT, (Serializable) result);
        Log.i(DEBUG_TAG, "Results number is " + result.size());

        context.sendBroadcast(broadcastIntent);

    }



}
