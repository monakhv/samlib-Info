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
import android.util.Log;



/**
 * Service To delete download book file at the end for life time
 * @author monakhv
 */
public class CleanBookServiceIntent extends MyServiceIntent {

    private static final String DEBUG_TAG = "CleanBookServiceIntent";

    public CleanBookServiceIntent() {
        super("CleanBookServiceIntent");

        Log.d(DEBUG_TAG, "Constructor Call");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(DEBUG_TAG, "Got intent");

        getDataExportImport().findDeleteBookFile();

    }

    public static void start(Context ctx) {
        Intent service = new Intent(ctx, CleanBookServiceIntent.class);
        //service.putExtra(DownloadBookServiceIntent.BOOK_ID, book_id);
        ctx.startService(service);
    }
}
