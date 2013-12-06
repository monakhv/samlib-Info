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

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import monakhv.android.samlib.MainActivity.DownloadReceiver;


import monakhv.android.samlib.R;
import monakhv.android.samlib.sql.AuthorController;
import monakhv.android.samlib.sql.entity.Book;
import monakhv.samlib.http.HttpClientController;

/**
 * Service to download book file
 *
 * @author monakhv
 */
public class DownloadBookServiceIntent extends IntentService {

    private static final String DEBUG_TAG = "DownloadBookServiceIntent";
    public static final String BOOK_ID = "BOOK_ID";
    private String fileName;
    private long book_id;

    public DownloadBookServiceIntent() {
        super("DownloadBookServiceIntent");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(DEBUG_TAG, "Got intent");
        book_id = intent.getLongExtra(BOOK_ID, 0);
        AuthorController ctl = new AuthorController(this.getApplicationContext());

        Book book = ctl.getBookController().getById(book_id);
        fileName = book.getFile().getAbsolutePath();


        HttpClientController http = HttpClientController.getInstance();
        try {
            http.downloadBook(book);
            finish(true);

        } catch (Exception ex) {
            book.cleanFile();//clean file on error
            finish(false);
            Log.e(DEBUG_TAG, "Download book error: " + book.getUri(), ex);

        }

    }

    private void finish(boolean b) {
        Log.d(DEBUG_TAG, "finish result: " + b);

        CharSequence msg;
        if (b) {
            msg = getApplicationContext().getText(R.string.download_book_success);
        } else {
            msg = getApplicationContext().getText(R.string.download_book_error);
        }
        Intent broadcastIntent = new Intent();
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.setAction(DownloadReceiver.ACTION_RESP);
        broadcastIntent.putExtra(DownloadReceiver.MESG, msg);
        broadcastIntent.putExtra(DownloadReceiver.RESULT, b);
        broadcastIntent.putExtra(DownloadReceiver.BOOK_ID, book_id);

        sendBroadcast(broadcastIntent);

    }

    /**
     * Helper method to start this service
     *
     * @param ctx
     * @param book
     */
    public static void start(Context ctx, Book book) {
        long book_id = book.getId();
        start(ctx, book_id);
    }

    /**
     * Helper method to start this method
     *
     * @param ctx
     * @param book_id
     */
    public static void start(Context ctx, long book_id) {
        Intent service = new Intent(ctx, DownloadBookServiceIntent.class);
        service.putExtra(DownloadBookServiceIntent.BOOK_ID, book_id);
        ctx.startService(service);
    }
}
