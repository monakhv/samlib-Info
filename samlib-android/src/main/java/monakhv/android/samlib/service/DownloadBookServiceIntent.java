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
package monakhv.android.samlib.service;


import android.content.Context;
import android.content.Intent;


import monakhv.android.samlib.SamlibApplication;
import monakhv.samlib.log.Log;
import monakhv.samlib.service.SamlibService;

/**
 * Service to download book file
 *
 * @author monakhv
 */
public class DownloadBookServiceIntent extends MyServiceIntent {

    private static final String DEBUG_TAG = "DownloadBookServiceIntent";
    public static final  String BOOK_ID = "BOOK_ID";

    SamlibApplication mSamlibApplication;
    public DownloadBookServiceIntent() {
        super("DownloadBookServiceIntent");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d(DEBUG_TAG, "Got intent");
        long book_id = intent.getLongExtra(BOOK_ID, 0);
        mSamlibApplication= (SamlibApplication) getApplication();
        //currentCaller
        UpdateObject updateObject=      intent.getParcelableExtra(AndroidGuiUpdater.CALLER_TYPE_EXTRA);

        SamlibService service =mSamlibApplication.getServiceComponent(updateObject,getHelper()).getSamlibService();
                //new SamlibService(new AuthorController(getHelper()),guiUpdate,mSettingsHelper,new HttpClientController(mSettingsHelper));

        service.downloadBook(book_id);
        mSamlibApplication.releaseServiceComponent();

    }



    /**
     * Helper method to start this method
     *
     * @param ctx context
     * @param book_id book id
     * @param updateObject  Caller type Activity or Not
     */
    public static void start(Context ctx, long book_id,UpdateObject updateObject ){
        Intent service = new Intent(ctx, DownloadBookServiceIntent.class);
        service.putExtra(DownloadBookServiceIntent.BOOK_ID, book_id);
        service.putExtra(AndroidGuiUpdater.CALLER_TYPE_EXTRA, updateObject);
        ctx.startService(service);
    }
}
