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
package monakhv.samlib.service;


import monakhv.samlib.data.AbstractSettings;
import monakhv.samlib.db.entity.*;
import monakhv.samlib.http.HttpClientController;
import monakhv.samlib.log.Log;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;


/**
 * Service to making check for author updates Can be called from activity or
 * from alarm manager
 *
 * @author monakhv
 */
public class BookDownloadService {

    private static final String DEBUG_TAG = "BookDownloadService";

    private final AbstractSettings mSettingsHelper;
    private final HttpClientController mHttpClientController;


    public BookDownloadService(AbstractSettings settingsHelper, HttpClientController httpClientController) {

        mSettingsHelper = settingsHelper;
        mHttpClientController = httpClientController;

    }


    public Observable<Integer> downloadBook(Book book) {

        Subject<Integer,Integer> subject= new SerializedSubject<>(PublishSubject.<Integer>create());

        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                AbstractSettings.FileType ft = mSettingsHelper.getFileType();
                Log.d(DEBUG_TAG, "downloadBook: default type is  " + ft.toString());

                switch (ft) {
                    case HTML:
                        getBook(book, AbstractSettings.FileType.HTML, subject);
                        break;
                    case FB2:
                        boolean rr = getBook(book, AbstractSettings.FileType.FB2, subject);
                        if (!rr) {
                            getBook(book, AbstractSettings.FileType.HTML, subject);
                        }
                        break;
                }
            }
        };
        thread.start();
        return subject;
    }

    private boolean getBook(Book book, AbstractSettings.FileType ft, Subject<Integer, Integer> subject) {
        book.setFileType(ft);

        try {
            mHttpClientController.downloadBook(book, subject);
            subject.onCompleted();
            return true;

        } catch (Exception ex) {

            mSettingsHelper.cleanBookFile(book);//clean file on error

            Log.e(DEBUG_TAG, "getBook: Download book error: " + book.getUri(), ex);
            if (ft == AbstractSettings.FileType.HTML) {//for FB2 we have the second chance
                subject.onError(ex);
            }

            return false;
        }
    }

}
