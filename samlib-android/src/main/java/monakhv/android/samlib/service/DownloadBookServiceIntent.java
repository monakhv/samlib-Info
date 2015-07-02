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




import monakhv.android.samlib.DownloadReceiver;

import monakhv.android.samlib.R;
import monakhv.android.samlib.data.SettingsHelper;
import monakhv.samlib.db.AuthorController;
import monakhv.samlib.db.entity.Book;
import monakhv.samlib.http.HttpClientController;
import monakhv.samlib.log.Log;

/**
 * Service to download book file
 *
 * @author monakhv
 */
public class DownloadBookServiceIntent extends MyServiceIntent {

    private static final String DEBUG_TAG = "DownloadBookServiceIntent";
    public static final  String BOOK_ID = "BOOK_ID";
    public static final  String SEND_UPDATE="SEND_UPDATE";
    private boolean sendResult;
    private long book_id;
    private SettingsHelper helper;

    public DownloadBookServiceIntent() {
        super("DownloadBookServiceIntent");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        helper = new SettingsHelper(this);
        Log.d(DEBUG_TAG, "Got intent");
        book_id = intent.getLongExtra(BOOK_ID, 0);
        sendResult = intent.getBooleanExtra(SEND_UPDATE, false);//do not send update by default
        AuthorController ctl = new AuthorController(getHelper());

        Book book = ctl.getBookController().getById(book_id);
       




        SettingsHelper.FileType ft = helper.getFileType();
        Log.d(DEBUG_TAG, "default type is  " + ft.toString());

        switch (ft){
            case HTML:
                finish(getBook(book, SettingsHelper.FileType.HTML),SettingsHelper.FileType.HTML);
                break;
            case FB2:
                boolean rr = getBook(book, SettingsHelper.FileType.FB2);
                if (rr){
                    finish(true,SettingsHelper.FileType.FB2);
                }
                else {
                    finish(getBook(book, SettingsHelper.FileType.HTML),SettingsHelper.FileType.HTML);
                }
                break;
        }

    }

    private boolean getBook(Book book, SettingsHelper.FileType ft) {
        book.setFileType(ft);
        HttpClientController http = HttpClientController.getInstance(helper);
        try {
            http.downloadBook(book);
           return true;

        } catch (Exception ex) {

            helper.cleanBookFile(book);//clean file on error

            Log.e(DEBUG_TAG, "Download book error: " + book.getUri(), ex);

            return false;
        }
    }

    private void finish(boolean b, SettingsHelper.FileType ft) {
        Log.d(DEBUG_TAG, "finish result: " + b);
        Log.d(DEBUG_TAG, "file type:  " + ft.toString());
        if (! sendResult){
            return;
        }
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
        broadcastIntent.putExtra(DownloadReceiver.FILE_TYPE, ft.toString());
        broadcastIntent.putExtra(DownloadReceiver.BOOK_ID, book_id);

        sendBroadcast(broadcastIntent);

    }

    /**
     * Helper method to start this service
     *
     * @param ctx Context
     * @param book Book to download
     * @param sendupdate  whether  send update information into activity or not
     */
    public static void start(Context ctx, Book book,boolean  sendupdate) {
        long book_id = book.getId();
        start(ctx, book_id,sendupdate);
    }

    /**
     * Helper method to start this method
     *
     * @param ctx context
     * @param book_id book id
     * @param sendupdate whether  send update information into activity or not
     */
    public static void start(Context ctx, long book_id,boolean  sendupdate) {
        Intent service = new Intent(ctx, DownloadBookServiceIntent.class);
        service.putExtra(DownloadBookServiceIntent.BOOK_ID, book_id);
        service.putExtra(DownloadBookServiceIntent.SEND_UPDATE, sendupdate);
        ctx.startService(service);
    }
}
