package monakhv.android.samlib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;


import monakhv.android.samlib.data.SettingsHelper;
import monakhv.samlib.db.BookController;
import monakhv.samlib.db.DaoBuilder;
import monakhv.samlib.db.entity.Book;

/*
 * Copyright 2014  Dmitry Monakhov
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
 *
 * 12/15/14.
 */
public  class DownloadReceiver extends BroadcastReceiver {
    public static final String ACTION_RESP = "monakhv.android.samlib.action.BookDownload";
    public static final String MESG = "MESG";
    public static final String RESULT = "RESULT";
    public static final String BOOK_ID = "BOOK_ID";
    public static final String FILE_TYPE = "FILE_TYPE";
    private static final String DEBUG_TAG = "DownloadReceiver";

    private BookFragment books;
    private DaoBuilder sql;
            ;

    public DownloadReceiver(BookFragment books,DaoBuilder sql){
        this.books=books;
        this.sql = sql;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(DEBUG_TAG, "Starting onReceive");
        String mesg = intent.getStringExtra(MESG);
        long book_id = intent.getLongExtra(BOOK_ID, 0);

        boolean res = intent.getBooleanExtra(RESULT, false);


        BookController bCtl= new BookController(sql);
        Book book = bCtl.getById(book_id);
        String ft = intent.getStringExtra(FILE_TYPE);
        book.setFileType(SettingsHelper.FileType.valueOf(ft));

        if (books != null) {
            if (books.progress != null) {
                books.progress.dismiss();
            }
        }

        if (res) {
            //Log.d(DEBUG_TAG, "Starting web for url: " + book.getFileURL());
//
            if (books != null) {
                books.launchReader(book);
            }
        } else {
            Toast toast = Toast.makeText(context, mesg, Toast.LENGTH_SHORT);

            toast.show();
        }
    }


}
