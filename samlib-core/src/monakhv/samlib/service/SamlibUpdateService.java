/*
 *  Copyright 2016 Dmitry Monakhov.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  12.02.16 17:42
 *
 */

package monakhv.samlib.service;

import monakhv.samlib.data.AbstractSettings;
import monakhv.samlib.db.AuthorController;
import monakhv.samlib.db.SQLController;
import monakhv.samlib.db.entity.Author;
import monakhv.samlib.exception.SamlibInterruptException;
import monakhv.samlib.exception.SamlibParseException;
import monakhv.samlib.exception.SamlibUpdateErrorException;
import monakhv.samlib.http.HttpClientController;
import monakhv.samlib.log.Log;
import rx.Observable;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by monakhv on 12.02.16.
 */
public class SamlibUpdateService {
    private static final String DEBUG_TAG="SamlibUpdateService";
    public static final long  SLEEP_INTERVAL_SECONDS = 1L;
    public static final int     SLEEP_DELAY_MIN = 5;
    public static final int     SLEEP_DELAY_MAX = 15;



    private final AuthorController mAuthorController;
    private final HttpClientController mHttpClientController;
    private final AbstractSettings mAbstractSettings;
    private final List<Author> updatedAuthors;



    public SamlibUpdateService(AuthorController authorController,AbstractSettings settings,HttpClientController httpClientController){
        mAuthorController=authorController;
        mHttpClientController=httpClientController;
        mAbstractSettings=settings;
        updatedAuthors= new ArrayList<>();
    }


    public Observable<GuiUpdateObject> getUpdateService(Author author,AuthorGuiState authorGuiState){

        List<Author> authors = new ArrayList<>();
        authors.add(author);
        return getUpdateService(authors,authorGuiState);
    }

    public Observable<GuiUpdateObject> getUpdateService(AuthorGuiState authorGuiState){

        List<Author> authors  = mAuthorController.getAll(authorGuiState.mSelectedTagId, SQLController.COL_mtime + " DESC");
        return getUpdateService(authors,authorGuiState);
    }

    public Observable<GuiUpdateObject> getUpdateService(List<Author> authors,AuthorGuiState authorGuiState) {

        return Observable.create(subscriber ->{
            updatedAuthors.clear();
            int skippedAuthors = 0;
            Random rnd = new Random(Calendar.getInstance().getTimeInMillis());

            int total = authors.size();
            int iCurrent = 0;//to send update information to pull-to-refresh

            for (Author a : authors) {//main author cycle
                subscriber.onNext(new GuiUpdateObject(new SamlibUpdateProgress(total,++iCurrent,a.getName())));

                mAuthorController.loadBooks(a);
                mAuthorController.loadGroupBooks(a);
                String url = a.getUrl();
                Author newA = new Author();
                try {
                    newA = mHttpClientController.getAuthorByURL(url, newA);
                } catch (IOException ex) {//here we abort cycle author and total update
                    Log.i(DEBUG_TAG, "runUpdateAuthors: Connection Error: " + url, ex);
                    subscriber.onError(ex);
                    return ;

                } catch (SamlibParseException ex) {//skip update for given author
                    Log.e(DEBUG_TAG, "runUpdateAuthors:Error parsing url: " + url + " skip update author ", ex);

                    ++skippedAuthors;
                    newA = a;
                } catch (SamlibInterruptException ex) {
                    Log.i(DEBUG_TAG, "runUpdateAuthors: catch Interrupted", ex);
                    subscriber.onError(ex);
                    return ;
                }
                if (a.update(newA)) {//we have update for the author
                    Log.i(DEBUG_TAG, "runUpdateAuthors: We need update author: " + a.getName());
                    mAuthorController.update(a);

                    if (a.isIsNew()) {
                        updatedAuthors.add(a);//sometimes we need update if the author has no new books
                        int idx = getAuthorIndex(a, authorGuiState);
                        GuiUpdateObject guiUpdateObject=new GuiUpdateObject(a,idx, GuiUpdateObject.UpdateType.UPDATE_UPDATE);
                        subscriber.onNext(guiUpdateObject);
                    }
                    if (mAbstractSettings.getAutoLoadFlag()) {
                        loadBook(a);
                    }


                }
                long sleep;

                if (mAbstractSettings.isUpdateDelay()) {
                    sleep = rnd.nextInt(SLEEP_DELAY_MAX - SLEEP_DELAY_MIN + 1) + SLEEP_DELAY_MIN;
                } else {
                    sleep = SLEEP_INTERVAL_SECONDS;
                }

                try {
                    Log.d(DEBUG_TAG, "runUpdateAuthors: sleep " + sleep + " seconds");

                    TimeUnit.SECONDS.sleep(sleep);
                } catch (InterruptedException e) {
                    Log.i(DEBUG_TAG, "runUpdateAuthors: Sleep interrupted exiting", e);
                    subscriber.onError(e);
                    return ;
                }
            }//main author cycle END

            mAuthorController.cleanBooks();
            if (authors.size() == skippedAuthors) {
                //all authors skipped - this is the error
                subscriber.onError(new SamlibUpdateErrorException());
                //guiUpdate.finishUpdate(false, updatedAuthors);
                //return ;
            } else {
                Result result = new Result(true);
                result.numberOfUpdated=updatedAuthors.size();
                subscriber.onNext(new GuiUpdateObject(result, GuiUpdateObject.UpdateType.UPDATE_UPDATE));
                subscriber.onCompleted();
                //return ;
            }



        });



    }
    /**
     * If need we can start download book service here
     *
     * @param a Author to load book for
     */
    @SuppressWarnings("UnusedParameters")
    public void loadBook(Author a) {

    }


    private int getAuthorIndex(Author author, AuthorGuiState state) {
        final List<Author> authors = mAuthorController.getAll(state.mSelectedTagId, state.mSorOrder);
        return authors.indexOf(author);
    }



}
