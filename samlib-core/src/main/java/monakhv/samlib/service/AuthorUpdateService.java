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
import monakhv.samlib.http.HttpClientController;
import monakhv.samlib.log.Log;



import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by monakhv on 12.02.16.
 */
public class AuthorUpdateService {
    private static final String DEBUG_TAG = "AuthorUpdateService";
    public static final long SLEEP_INTERVAL_SECONDS = 1L;
    public static final int SLEEP_DELAY_MIN = 5;
    public static final int SLEEP_DELAY_MAX = 15;


    private final AuthorController mAuthorController;
    private final HttpClientController mHttpClientController;
    private final AbstractSettings mAbstractSettings;
    private final List<Author> updatedAuthors;
    private final GuiEventBus mGuiEventBus;


    public AuthorUpdateService(AuthorController authorController, AbstractSettings settings, HttpClientController httpClientController, GuiEventBus guiEventBus) {
        mAuthorController = authorController;
        mHttpClientController = httpClientController;
        mAbstractSettings = settings;
        updatedAuthors = new ArrayList<>();
        mGuiEventBus = guiEventBus;
    }


    public boolean runUpdateService(Author author, AuthorGuiState authorGuiState) {

        List<Author> authors = new ArrayList<>();
        authors.add(author);
        return runUpdateService(authors,authorGuiState);
    }

    public boolean runUpdateService(AuthorGuiState authorGuiState) {

        List<Author> authors = mAuthorController.getAll(authorGuiState.mSelectedTagId, SQLController.COL_mtime + " DESC");
        return runUpdateService(authors,authorGuiState);
    }

    /**
     * Check update information for the list of authors
     *
     * @param authors List of the Authors to check update
     * @return true if update successful false if error or interrupted
     */
    private boolean runUpdateService(List<Author> authors, AuthorGuiState state) {
        updatedAuthors.clear();
        int skippedAuthors = 0;
        Random rnd = new Random(Calendar.getInstance().getTimeInMillis());

        int total = authors.size();
        int iCurrent = 0;//to send update information to pull-to-refresh
        for (Author a : authors) {//main author cycle
            mGuiEventBus.post(new GuiUpdateObject(new AuthorUpdateProgress(total, ++iCurrent, a.getName())));

            mAuthorController.loadBooks(a);
            mAuthorController.loadGroupBooks(a);
            String url = a.getUrl();
            Author newA = new Author();
            try {
                newA = mHttpClientController.getAuthorByURL(url, newA);
            } catch (IOException ex) {//here we abort cycle author and total update
                Log.i(DEBUG_TAG, "runUpdateAuthors: Connection Error: " + url, ex);
                finishUpdate(false, updatedAuthors);
                return false;

            } catch (SamlibParseException ex) {//skip update for given author
                Log.e(DEBUG_TAG, "runUpdateAuthors:Error parsing url: " + url + " skip update author ", ex);

                ++skippedAuthors;
                newA = a;
            } catch (SamlibInterruptException e) {
                Log.i(DEBUG_TAG, "runUpdateAuthors: catch Interrupted", e);

                finishUpdate(false, updatedAuthors);
                return false;
            }
            if (a.update(newA)) {//we have update for the author
                Log.i(DEBUG_TAG, "runUpdateAuthors: We need update author: " + a.getName());
                mAuthorController.update(a);

                if (a.isIsNew()) {
                    updatedAuthors.add(a);//sometimes we need update if the author has no new books
                    int idx = getAuthorIndex(a, state);
                    GuiUpdateObject guiUpdateObject = new GuiUpdateObject(a, idx, GuiUpdateObject.UpdateType.UPDATE_UPDATE);
                    mGuiEventBus.post(guiUpdateObject);

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

                finishUpdate(false, updatedAuthors);
                return false;
            }
        }//main author cycle END

        mAuthorController.cleanBooks();
        if (authors.size() == skippedAuthors) {
            //all authors skipped - this is the error
            finishUpdate(false, updatedAuthors);
            return false;
        } else {
            finishUpdate(true, updatedAuthors);
            return true;
        }


    }

    private void finishUpdate(boolean b, List<Author> updatedAuthors) {
        Result result = new Result(b,updatedAuthors);
        result.numberOfUpdated = updatedAuthors.size();
        mGuiEventBus.post(new GuiUpdateObject(result, GuiUpdateObject.UpdateType.UPDATE_UPDATE));
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
        if (state.mSorOrder== null){
            return -1;
        }
        final List<Author> authors = mAuthorController.getAll(state.mSelectedTagId, state.mSorOrder);
        return authors.indexOf(author);
    }


}
