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


import android.os.IBinder;
import android.support.annotation.Nullable;
import monakhv.samlib.db.entity.Book;
import monakhv.samlib.log.Log;
import rx.Subscriber;
import rx.Subscription;

/**
 * Service to download book file
 *
 * @author monakhv
 */
public class DownloadBookService extends MyService {

    private static final String DEBUG_TAG = "DownloadBookService";
    private static final  String EXTRA_BOOK_ID = "DownloadBookService.BOOK_ID";
    private static final  String ACTION_DOWNLOAD = "DownloadBookService.ACTION_DOWNLOAD ";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        int book_id=intent.getIntExtra(EXTRA_BOOK_ID,-1);
        Log.d(DEBUG_TAG,"onStartCommand: action - "+action+", id = "+book_id);

        if (book_id>0){
            Book book = getAuthorController().getBookController().getById(book_id);
            final Subscription subscription=getBookDownloadService().downloadBook(book)
                    .onBackpressureBuffer()
                    .subscribe(new Subscriber<Integer>() {
                        @Override
                        public void onCompleted() {
                            Log.d(DEBUG_TAG,"onStartCommand: onCompleted");
                            stopSelf();
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e(DEBUG_TAG,"onError:",e);
                            stopSelf();
                        }

                        @Override
                        public void onNext(Integer integer) {
                            //Do nothing, we do not need update GUI
                        }
                    });
            addSubscription(subscription);

        }
        return START_NOT_STICKY;

    }


    //    protected void onHandleIntent(Intent intent) {
//
//        Log.d(DEBUG_TAG, "Got intent");
//        long book_id = intent.getLongExtra(EXTRA_BOOK_ID, 0);
//        mSamlibApplication= (SamlibApplication) getApplication();
//        boolean isReceiver = intent.getBooleanExtra(EXTRA_IS_RECEIVER,false);
//        UpdateObject updateObject;
//        if (isReceiver){
//            updateObject=new UpdateObject();
//        }
//        else {
//            updateObject=UpdateObject.ACTIVITY_CALLER;
//        }
//
//
//
////        BookDownloadService service =mSamlibApplication.getServiceComponent(updateObject,getHelper()).getSamlibService();
////
////
////        Book book = getAuthorController().getBookController().getById(book_id);
////        service.downloadBook(book);
//        mSamlibApplication.releaseServiceComponent();
//
//    }
//


    /**
     * Helper method to start this method
     *
     * @param ctx context
     * @param book book

     */
    public static void start(Context ctx, Book book){
        Intent service = new Intent(ctx, DownloadBookService.class);
        service.setAction(ACTION_DOWNLOAD);
        service.putExtra(EXTRA_BOOK_ID, book.getId());
        ctx.startService(service);
    }


}
