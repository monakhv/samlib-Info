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
import monakhv.samlib.db.AuthorController;
import monakhv.samlib.db.SQLController;
import monakhv.samlib.db.entity.*;
import monakhv.samlib.exception.SamlibInterruptException;
import monakhv.samlib.exception.SamlibParseException;
import monakhv.samlib.http.HttpClientController;
import monakhv.samlib.log.Log;

import javax.inject.Inject;
import java.io.IOException;
import java.text.Collator;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 * Service to making check for author updates Can be called from activity or
 * from alarm manager
 *
 * @author monakhv
 */
public class SamlibService {
    public enum UpdateObjectSelector {
        Tag,
        Author,
        Book,
        UNDEF
    }

    private static final String DEBUG_TAG = "SamlibService";
    public static final String ACTION_ADD = "SamlibService.AddAuthorServiceIntent_ACTION_ADD";
    public static final String ACTION_DELETE = "SamlibService.AddAuthorServiceIntent_ACTION_DELETE";

    public static final long SLEEP_INTERVAL_SECONDS = 1L;

    public static final int SLEEP_DELAY_MIN = 5;
    public static final int SLEEP_DELAY_MAX = 15;
    private int numberOfAdded = 0;
    private int numberOfDeleted = 0;
    private int doubleAdd = 0;
    private long author_id = 0;

    private final List<Author> updatedAuthors;
    protected final AuthorController authorController;
    private final GuiUpdate guiUpdate;
    private final AbstractSettings settingsHelper;
    private final HttpClientController http;


    @Inject
    public SamlibService(AuthorController sql, GuiUpdate guiUpdate, AbstractSettings settingsHelper, HttpClientController httpClientController) {
        this.guiUpdate = guiUpdate;
        this.settingsHelper = settingsHelper;
        authorController = sql;
        updatedAuthors = new ArrayList<>();
        http = httpClientController;

    }

    public boolean runUpdate(SamlibService.UpdateObjectSelector selector, int id, int selectionTag, String order) {
        List<Author> authors;
        if (selector == SamlibService.UpdateObjectSelector.Author) {//Check update for the only Author

            //int id = intent.getIntExtra(SELECT_ID, 0);//author_id
            Author author = authorController.getById(id);
            if (author != null) {
                authors = new ArrayList<>();
                authors.add(author);

                Log.i(DEBUG_TAG, "runUpdateAuthors: Check single Author: " + author.getName());
            } else {
                Log.e(DEBUG_TAG, "runUpdateAuthors: Can not find Author: " + id);
                return false;
            }
        } else {//Check update for authors by TAG
            authors = authorController.getAll(id, SQLController.COL_mtime + " DESC");


            Log.i(DEBUG_TAG, "runUpdateAuthors: selection index: " + id);
        }
        return runUpdateAuthors(authors, selectionTag, order);
    }

    /**
     * Check update information for the list of authors
     *
     * @param authors List of the Authors to check update
     * @return true if update successful false if error or interrupted
     */
    private boolean runUpdateAuthors(List<Author> authors, int selectionTag, String order) {
        updatedAuthors.clear();
        int skippedAuthors = 0;
        Random rnd = new Random(Calendar.getInstance().getTimeInMillis());

        int total = authors.size();
        int iCurrent = 0;//to send update information to pull-to-refresh
        for (Author a : authors) {//main author cycle
            guiUpdate.sendAuthorUpdateProgress(total, ++iCurrent, a.getName());
            authorController.loadBooks(a);
            authorController.loadGroupBooks(a);
            String url = a.getUrl();
            Author newA = new Author();
            try {
                newA = http.getAuthorByURL(url, newA);
            } catch (IOException ex) {//here we abort cycle author and total update
                Log.i(DEBUG_TAG, "runUpdateAuthors: Connection Error: " + url, ex);
                guiUpdate.finishUpdate(false, updatedAuthors);
                return false;

            } catch (SamlibParseException ex) {//skip update for given author
                Log.e(DEBUG_TAG, "runUpdateAuthors:Error parsing url: " + url + " skip update author ", ex);

                ++skippedAuthors;
                newA = a;
            } catch (SamlibInterruptException e) {
                Log.i(DEBUG_TAG, "runUpdateAuthors: catch Interrupted", e);

                guiUpdate.finishUpdate(false, updatedAuthors);
                return false;
            }
            if (a.update(newA)) {//we have update for the author
                Log.i(DEBUG_TAG, "runUpdateAuthors: We need update author: " + a.getName());
                authorController.update(a);

                if (a.isIsNew()) {
                    updatedAuthors.add(a);//sometimes we need update if the author has no new books
                    int idx = getIndex(a.getId(), selectionTag, order);
                    GuiUpdateObject guiUpdateObject=new GuiUpdateObject(a,idx, GuiUpdateObject.UpdateType.UPDATE_UPDATE);
                    guiUpdate.makeUpdateUpdate(a,guiUpdateObject);
                }
                if (settingsHelper.getAutoLoadFlag()) {
                    loadBook(a);
                }


            }
            long sleep;

            if (settingsHelper.isUpdateDelay()) {
                sleep = rnd.nextInt(SLEEP_DELAY_MAX - SLEEP_DELAY_MIN + 1) + SLEEP_DELAY_MIN;
            } else {
                sleep = SLEEP_INTERVAL_SECONDS;
            }

            try {
                Log.d(DEBUG_TAG, "runUpdateAuthors: sleep " + sleep + " seconds");

                TimeUnit.SECONDS.sleep(sleep);
            } catch (InterruptedException e) {
                Log.i(DEBUG_TAG, "runUpdateAuthors: Sleep interrupted exiting", e);

                guiUpdate.finishUpdate(false, updatedAuthors);
                return false;
            }
        }//main author cycle END

        authorController.cleanBooks();
        if (authors.size() == skippedAuthors) {
            //all authors skipped - this is the error
            guiUpdate.finishUpdate(false, updatedAuthors);
            return false;
        } else {
            guiUpdate.finishUpdate(true, updatedAuthors);
            return true;
        }


    }





    private int getIndex(int author_id, int selectedTag, String order) {
        final Author author = authorController.getById(author_id);
        final List<Author> authors = authorController.getAll(selectedTag, order);
        return authors.indexOf(author);
    }

    /**
     * Make author search according to the first part aof theAuthor name
     *
     * @param pattern part of the author name
     * @return List of found authors
     * @throws IOException
     * @throws SamlibParseException
     */
    public List<AuthorCard> makeSearch(String pattern) throws IOException, SamlibParseException, SamlibInterruptException {
        Log.i(DEBUG_TAG, "makeSearch: Search author with pattern: " + pattern);
        List<AuthorCard> result = new ArrayList<>();

        int page = 1;

        HashMap<String, ArrayList<AuthorCard>> colAuthors = http.searchAuthors(pattern, page);
        RuleBasedCollator russianCollator = (RuleBasedCollator) Collator.getInstance(new Locale("ru", "RU"));

        try {
            russianCollator = new RuleBasedCollator(settingsHelper.getCollationRule());
        } catch (ParseException ex) {
            Log.e(DEBUG_TAG, "makeSearch: Collator error", ex);

        }

        russianCollator.setStrength(Collator.IDENTICAL);
        russianCollator.setDecomposition(Collator.NO_DECOMPOSITION);


        while (colAuthors != null) {//page cycle while we find anything

            String[] keys = colAuthors.keySet().toArray(new String[1]);

            Arrays.sort(keys, russianCollator);
            int ires = Arrays.binarySearch(keys, pattern, russianCollator);
            Log.d(DEBUG_TAG, "makeSearch: Page number:" + page + "    search result " + ires + "   length is " + keys.length);

            int iStart;
            if (ires < 0) {
                iStart = -ires - 1;
            } else {
                iStart = ires;
            }
            for (int i = iStart; i < keys.length; i++) {
                String sKey = keys[i];
                if (sKey.toLowerCase().startsWith(pattern.toLowerCase())) {
                    for (AuthorCard ac : colAuthors.get(sKey)) {

                        result.add(ac);

                        if (result.size() >= SamLibConfig.SEARCH_LIMIT) {
                            return result;
                        }
                    }

                } else {
                    Log.d(DEBUG_TAG, "makeSearch: Search for " + pattern + " stop by substring  -   " + sKey + "   " + keys.length + "         " + iStart + "  -  " + ires);


                    return result;
                }
            }


            ++page;
            colAuthors = http.searchAuthors(pattern, page);
        }
        Log.d(DEBUG_TAG, "makeSearch: Results: " + result.size());

        return result;
    }




    /**
     * If need we can start download book service here
     *
     * @param a Author to load book for
     */
    public void loadBook(Author a) {

    }



    public void downloadBook(long book_id) {

        Book book = authorController.getBookController().getById(book_id);

        AbstractSettings.FileType ft = settingsHelper.getFileType();
        Log.d(DEBUG_TAG, "downloadBook: default type is  " + ft.toString());

        switch (ft) {
            case HTML:
                guiUpdate.finishBookLoad(getBook(book, AbstractSettings.FileType.HTML), AbstractSettings.FileType.HTML, book_id);
                break;
            case FB2:
                boolean rr = getBook(book, AbstractSettings.FileType.FB2);
                if (rr) {
                    guiUpdate.finishBookLoad(true, AbstractSettings.FileType.FB2, book_id);
                } else {
                    guiUpdate.finishBookLoad(getBook(book, AbstractSettings.FileType.HTML), AbstractSettings.FileType.HTML, book_id);
                }
                break;
        }
    }

    private boolean getBook(Book book, AbstractSettings.FileType ft) {
        book.setFileType(ft);

        try {
            http.downloadBook(book);
            return true;

        } catch (Exception ex) {

            settingsHelper.cleanBookFile(book);//clean file on error

            Log.e(DEBUG_TAG, "getBook: Download book error: " + book.getUri(), ex);

            return false;
        }
    }


}
