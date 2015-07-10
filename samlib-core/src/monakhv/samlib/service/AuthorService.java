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


import monakhv.samlib.data.SettingsHelper;
import monakhv.samlib.db.AuthorController;
import monakhv.samlib.db.DaoBuilder;
import monakhv.samlib.db.entity.Author;
import monakhv.samlib.db.entity.Book;
import monakhv.samlib.db.entity.SamLibConfig;
import monakhv.samlib.exception.SamlibParseException;
import monakhv.samlib.http.HttpClientController;
import monakhv.samlib.log.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Service to making check for author updates Can be called from activity or
 * from alarm manager
 *
 * @author monakhv
 */
public class AuthorService {
    private static final String DEBUG_TAG         = "AuthorService";
    public static final String ACTION_ADD          = "AuthorService.AddAuthorServiceIntent_ACTION_ADD";
    public static final String ACTION_DELETE    = "AuthorService.AddAuthorServiceIntent_ACTION_DELETE";

    public static final long SLEEP_INTERVAL_SECONDS=1;
    private  int numberOfAdded=0;
    private  int numberOfDeleted=0;
    private  int doubleAdd = 0;
    private long author_id=0;

    private final List<Author> updatedAuthors;
    protected final AuthorController authorController;
    private final GuiUpdate guiUpdate;
    private final SettingsHelper settingsHelper;

    public AuthorService(DaoBuilder sql, GuiUpdate guiUpdate, SettingsHelper settingsHelper) {
        this.guiUpdate = guiUpdate;
        this.settingsHelper = settingsHelper;
        authorController = new AuthorController(sql);
        updatedAuthors = new ArrayList<>();

    }

    /**
     * Check update information for the list of authors
     * @param authors List of the Authors to check update
     */
    public void runUpdate(List<Author> authors) {
        updatedAuthors.clear();
        int skippedAuthors = 0;
        HttpClientController http = HttpClientController.getInstance(settingsHelper);
        int total = authors.size();
        int iCurrent = 0;//to send update information to pull-to-refresh
        for (Author a : authors) {//main author cycle
            guiUpdate.sendAuthorUpdateProgress(total,++iCurrent,a.getName());

            String url = a.getUrl();
            Author newA=authorController.getEmptyObject();
            try {
                newA = http.getAuthorByURL(url,newA);
            } catch (IOException ex) {//here we abort cycle author and total update
                Log.i(DEBUG_TAG, "Connection Error: "+url, ex);
                guiUpdate.finishUpdate(false,updatedAuthors);

                return;

            } catch (SamlibParseException ex) {//skip update for given author
                Log.e(DEBUG_TAG, "Error parsing url: " + url + " skip update author ", ex);

                ++skippedAuthors;
                newA = a;
            }
            if (a.update(newA)) {//we have update for the author
                updatedAuthors.add(a);
                Log.i(DEBUG_TAG, "We need update author: " + a.getName());
                authorController.update(a);

                loadBook(a);

                guiUpdate.makeUpdateAuthors();
            }

            try {

                TimeUnit.SECONDS.sleep(SLEEP_INTERVAL_SECONDS);
            } catch (InterruptedException e) {
                Log.e(DEBUG_TAG,"Sleep interrupted",e);
            }
        }//main author cycle END
        if (authors.size() == skippedAuthors){
            //all authors skipped - this is the error
            guiUpdate.finishUpdate(false,updatedAuthors);
        }
        else {
            guiUpdate.finishUpdate(true,updatedAuthors);

        }

    }
    /**
     * Special method to make Author read, also make sure that all book re read either
     * @param id author id
     * @return true if success
     */
    public   boolean makeAuthorRead(int id) {

        Author a = authorController.getById(id);

        if (a == null){
            Log.e(DEBUG_TAG, "Author not found to update");
            return false;
        }

        if (! a.isIsNew()) {
            Log.d(DEBUG_TAG, "Author is read - no update need");
            return false;
        }


        int i = authorController.markRead(a);

        Log.d(DEBUG_TAG, "Update author status: " + i);
        guiUpdate.makeUpdateAuthors();
        guiUpdate.makeUpdateBooks();
        return true;

    }
    /**
     * Invert read book flag
     * Adjust author flag either
     * @param id  book id
     */
    public   void makeBookReadFlip(int id) {

        Book book=authorController.getBookController().getById(id);
        if (book == null){
            Log.e(DEBUG_TAG,"makeBookReadFlip: book not found id = "+id);
            return;
        }

        if (book.isIsNew()){
            authorController.getBookController().markRead(book);
            Author a = authorController.getById(book.getAuthor().getId());
            authorController.testMarkRead(a);
        }
        else {
            authorController.getBookController().markUnRead(book);
            Author a = authorController.getById(book.getAuthor().getId());
            authorController.testMarkRead(a);
        }
        guiUpdate.makeUpdateAuthors();

    }
    /**
     * Delete author from DB
     * @param id author id
     */
    public void makeAuthorDel(int id){

        int res = authorController.delete(authorController.getById(id));
        Log.d(DEBUG_TAG, "Author id " + id + " deleted, status " + res);
        if (res == 1){
            ++numberOfDeleted;
            guiUpdate.makeUpdateAuthors();
            guiUpdate.makeUpdateBooks();
        }


        guiUpdate.sendResult(ACTION_DELETE, numberOfAdded, numberOfDeleted, doubleAdd, 0, author_id);

    }
    /**
     * Add authors
     * @param urls list of author urls
     */
    public void makeAuthorAdd(ArrayList<String> urls){
        HttpClientController http = HttpClientController.getInstance(settingsHelper);

        for (String url : urls) {
            Author a = loadAuthor(http, authorController, url);
            if (a != null) {
                author_id=authorController.insert(a);
                ++numberOfAdded;
            }
        }
        guiUpdate.makeUpdateAuthors();
        guiUpdate.sendResult(ACTION_ADD, numberOfAdded, numberOfDeleted, doubleAdd, urls.size(), author_id);
    }
    private Author loadAuthor(HttpClientController http, AuthorController sql, String url) {
        Author a;
        String text;


        text = testURL(url);
        if (text == null){
            Log.e(DEBUG_TAG, "URL syntax error: " + url);

            return null;
        }

        Author ta = sql.getByUrl(text);
        if (ta != null) {
            Log.i(DEBUG_TAG, "Ignore Double entries: "+text);

            ++doubleAdd;
            return null;
        }
        try {
            a = http.addAuthor(text,sql.getEmptyObject());
        } catch (IOException ex) {
            Log.e(DEBUG_TAG, "DownLoad Error for URL: " + text, ex);

            return null;

        } catch (SamlibParseException ex) {
            Log.e(DEBUG_TAG, "Author parsing Error: " + text, ex);

            return null;
        } catch (IllegalArgumentException ex) {
            Log.e(DEBUG_TAG, "URL Parsing exception: " + text, ex);

            return null;
        }

        return a;
    }
    /**
     * URL syntax checkout
     *
     * @param url original URL
     * @return reduced URL without host prefix or NULL if the syntax is wrong
     *
     */
    private String testURL(String url)   {
        Log.d(DEBUG_TAG, "Got text: " + url);

        return SamLibConfig.reduceUrl(url);

    }


    /**
     * If need we can start download book service here
     * @param a Author to load book for
     */
    public void loadBook(Author a) {

    }

    /**
     * Recalculate allTagsString for all Authors
     */
    public void makeUpdateTags() {

        authorController.updateAuthorTags();
        guiUpdate.makeUpdateAuthors();
        guiUpdate.makeUpdateTagList();
    }
}
