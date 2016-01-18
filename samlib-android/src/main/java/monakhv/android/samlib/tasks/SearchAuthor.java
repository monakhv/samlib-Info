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
import java.util.ArrayList;
import java.util.List;



import monakhv.android.samlib.R;
import monakhv.android.samlib.SamlibApplication;
import monakhv.android.samlib.search.SearchAuthorActivity.SearchReceiver;
import monakhv.android.samlib.service.UpdateObject;
import monakhv.android.samlib.sql.DatabaseHelper;
import monakhv.samlib.exception.SamlibInterruptException;
import monakhv.samlib.exception.SamlibParseException;
import monakhv.samlib.db.entity.AuthorCard;
import monakhv.samlib.db.entity.SamLibConfig;
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
        private final int iMessage;

        ResultStatus(int msg) {
            iMessage = msg;
        }

        public String getMessage(Context ctx) {
            return ctx.getString(iMessage);
        }
    }

    private ResultStatus status;
    private static final String DEBUG_TAG = "SearchAuthor";

    private  List<AuthorCard> result;
    private final SamlibApplication mSamlibApplication;
    private final SamlibService mSamlibService;

    public SearchAuthor(SamlibApplication samlibApplication, DatabaseHelper helper) {
        status = ResultStatus.Good;
        mSamlibApplication=samlibApplication;
        mSamlibService=mSamlibApplication.getServiceComponent(UpdateObject.UNDEF,helper).getSamlibService();

    }

    @Override
    protected Boolean doInBackground(String... params) {


        for (String pattern : params) {

            try {
                result = mSamlibService.makeSearch(pattern);
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
            } catch (SamlibInterruptException e) {
                Log.e(DEBUG_TAG, "Interrupted", e);
                status = ResultStatus.Error;
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean res) {

        mSamlibApplication.releaseServiceComponent();
        Intent broadcastIntent = new Intent();
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.setAction(SearchReceiver.ACTION_RESP);
        if (status != ResultStatus.Good) {
            broadcastIntent.putExtra(SearchReceiver.EXTRA_MESSAGE, status.getMessage(mSamlibApplication));
        }

        if (result == null){
            Log.i(DEBUG_TAG, "Results is NULL, nothing found");
            result=new ArrayList<>();
        }
        else {
            Log.i(DEBUG_TAG, "Results number is " + result.size());
        }
        broadcastIntent.putExtra(SearchReceiver.EXTRA_RESULT, (Serializable) result);


        mSamlibApplication.sendBroadcast(broadcastIntent);

    }



}
