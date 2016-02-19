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
 *  12.02.16 9:10
 *
 */

package monakhv.samlib.service;

import monakhv.samlib.data.AbstractSettings;
import monakhv.samlib.db.AuthorController;
import monakhv.samlib.db.entity.Author;
import monakhv.samlib.db.entity.Book;
import monakhv.samlib.db.entity.GroupBook;
import monakhv.samlib.db.entity.SamLibConfig;
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
 * Subject bus using java -rx
 * Created by monakhv on 12.02.16.
 */
@SuppressWarnings("Convert2streamapi")
public class SamlibOperation {
    private static final String DEBUG_TAG = "SamlibOperation";
    public static final long SLEEP_INTERVAL_SECONDS = 1L;
    public static final int SLEEP_DELAY_MIN = 5;
    public static final int SLEEP_DELAY_MAX = 15;


    protected final AuthorController mAuthorController;
    private final AbstractSettings mSettingsHelper;
    private final HttpClientController mHttpClientController;
    private final GuiEventBus mGuiEventBus;

    private  Thread mThread;

    public SamlibOperation(AuthorController sql, AbstractSettings settingsHelper, HttpClientController httpClientController,GuiEventBus guiEventBus) {
        mAuthorController = sql;
        mSettingsHelper = settingsHelper;
        mHttpClientController = httpClientController;
        mGuiEventBus=guiEventBus;

    }




    public void makeBookReadFlip(final Book book, final BookGuiState bState, final AuthorGuiState aState) {
         mThread=new Thread(){
            @Override
            public void run() {
                super.run();
                runBookReadFlip(book,bState,aState);
            }

        };
        mThread.start();
    }
    public void makeGroupReadFlip(final GroupBook groupBook, final BookGuiState bState, final AuthorGuiState aState) {
        mThread=new Thread(){
            @Override
            public void run() {
                super.run();
                runGroupReadFlip(groupBook,bState,aState);
            }
        };
        mThread.start();

    }

    public void makeAuthorRead(final Author author, final AuthorGuiState state) {
        mThread=new Thread(){
            @Override
            public void run() {
                super.run();
                runAuthorRead(author,state);
            }
        };
        mThread.start();
    }
    public void makeAuthorDel(final Author author, final AuthorGuiState state) {
        mThread =new Thread(){
            @Override
            public void run() {
                super.run();
                runAuthorDel(author,state);
            }
        };
        mThread.start();
    }
    public void makeAuthorAdd(final ArrayList<String> urls, final AuthorGuiState state) {
        mThread =new Thread(){
            @Override
            public void run() {
                super.run();
                runAuthorAdd(urls,state);
            }
        };
        mThread.start();
    }
    public void makeUpdateTags(final AuthorGuiState state) {
        mThread =new Thread(){
            @Override
            public void run() {
                super.run();
                runUpdateTags(state);
            }
        };
        mThread.start();
    }
    /**
     * Special method to make Author read, also make sure that all book re read either
     *
     * @param a Author
     * @return true if success
     */
    private boolean runAuthorRead(Author a, AuthorGuiState state) {

        if (a == null) {
            Log.e(DEBUG_TAG, "Author not found to update");
            return false;
        }

        if (!a.isIsNew()) {
            Log.d(DEBUG_TAG, "Author is read - no update need");
            return false;
        }


        int i = mAuthorController.markRead(a);


        List<Author> authors = mAuthorController.getAll(state.mSelectedTagId, state.mSorOrder);
        int sort = authors.indexOf(a);
        makeGuiUpdate(new GuiUpdateObject(a, sort));


        List<GroupBook> groupBooks = mAuthorController.getGroupBookController().getByAuthor(a);
        Log.d(DEBUG_TAG, "Update author status: " + i + "   sort " + sort+" groups: "+groupBooks.size());
        for (GroupBook groupBook : groupBooks) {
            makeGuiUpdate(new GuiUpdateObject(groupBook, groupBooks.indexOf(groupBook)));
        }
        if (groupBooks.size()==0){//special case when Author has no groups
            makeGuiUpdate(new GuiUpdateObject(GuiUpdateObject.ObjectType.GROUP));
        }
        return true;

    }

    @SuppressWarnings("ConstantConditions")
    private void runGroupReadFlip(GroupBook groupBook, BookGuiState bState, AuthorGuiState aState) {
        List<Book> books;

        if (groupBook == null) {//Author has no group
            Author author = mAuthorController.getById(bState.mAuthorId);
            books = mAuthorController.getBookController().getAll(author, bState.mSortOrder);
        } else {//Author has groups
            books = mAuthorController.getBookController().getBookForGroup(groupBook, bState.mSortOrder);
        }


        for (Book book : books) {
            if (book.isIsNew()) {
                mAuthorController.getBookController().markRead(book);
            }
        }


        Author author = mAuthorController.getById(bState.mAuthorId);
        if (mAuthorController.testMarkRead(author)) {
            if (aState != null){
                makeGuiUpdate(new GuiUpdateObject(author, getAuthorIndex(author, aState)));
            }
        }

        if (groupBook == null) {
            makeGuiUpdate(new GuiUpdateObject(GuiUpdateObject.ObjectType.GROUP));
        } else {
            List<GroupBook> rr = mAuthorController.getGroupBookController().getByAuthor(author);
            GroupBook g = mAuthorController.getGroupBookController().getById(groupBook.getId());
            makeGuiUpdate(new GuiUpdateObject(g, rr.indexOf(groupBook)));

        }

    }


    /**
     * Invert read book flag
     * Adjust author flag either
     *
     * @param  book Book
     */
    private void runBookReadFlip(Book book, BookGuiState bState, AuthorGuiState aState) {


        if (book == null) {
            Log.e(DEBUG_TAG, "makeBookReadFlip: book is null ");
            return;
        }
        Log.d(DEBUG_TAG, "makeBookReadFlip: book_id = " + book.getId() + " author_id = " + book.getAuthor().getId());

        Author a;
        if (book.isIsNew()) {
            mAuthorController.getBookController().markRead(book);
            a = mAuthorController.getById(book.getAuthor().getId());

            if (mAuthorController.testMarkRead(a) && aState != null) {
                makeGuiUpdate(new GuiUpdateObject(a, getAuthorIndex(a, aState)));
            }

        } else {
            mAuthorController.getBookController().markUnRead(book);
            a = mAuthorController.getById(book.getAuthor().getId());
            if (mAuthorController.testMarkRead(a) && aState != null) {
                makeGuiUpdate(new GuiUpdateObject(a, getAuthorIndex(a, aState)));
            }
        }
        GroupBook groupBook = mAuthorController.getGroupBookController().getByBook(book);
        List<Book> books;
        if (groupBook == null) {
            books = mAuthorController.getBookController().getAll(a, bState.mSortOrder);
        } else {
            books = mAuthorController.getBookController().getBookForGroup(groupBook, bState.mSortOrder);
        }

        makeGuiUpdate(new GuiUpdateObject(book, books.indexOf(book)));

    }

    /**
     * Delete author from DB
     *
     * @param author Author
     */
    private void runAuthorDel(Author author, AuthorGuiState state) {

        Result result = new Result(true);
        int id = author.getId();
        int idx = getAuthorIndex(author, state);
        int res = mAuthorController.delete(author);

        Log.d(DEBUG_TAG, "makeAuthorDel: Author id " + id + " deleted, status " + res);
        if (res == 1) {
            ++result.numberOfDeleted;
            makeGuiUpdate(new GuiUpdateObject(author, idx, GuiUpdateObject.UpdateType.DELETE));
            makeGuiUpdate(new GuiUpdateObject(result, GuiUpdateObject.UpdateType.DELETE));
            return;
        }

        result.mRes = false;
        makeGuiUpdate(new GuiUpdateObject(result, GuiUpdateObject.UpdateType.DELETE));
    }

    /**
     * Add authors
     *
     * @param urls list of author urls
     */
    private void runAuthorAdd(ArrayList<String> urls, AuthorGuiState state) {
        Result result = new Result(true);
        Random rnd = new Random(Calendar.getInstance().getTimeInMillis());

        result.totalToAdd=urls.size();
        for (String url : urls) {
            Author a = loadAuthor(result, url);
            if (a != null) {
                long author_id = mAuthorController.insert(a);
                ++result.numberOfAdded;
                int idx = getAuthorIndex((int) author_id, state);
                makeGuiUpdate(new GuiUpdateObject(mAuthorController.getById(author_id), idx, GuiUpdateObject.UpdateType.ADD));
            }
            long sleep;

            if (mSettingsHelper.isUpdateDelay()) {
                sleep = rnd.nextInt(SLEEP_DELAY_MAX - SLEEP_DELAY_MIN + 1) + SLEEP_DELAY_MIN;
            } else {
                sleep = SLEEP_INTERVAL_SECONDS;
            }

            try {
                Log.d(DEBUG_TAG, "makeAuthorAdd: sleep " + sleep + " seconds");

                TimeUnit.SECONDS.sleep(sleep);
            } catch (InterruptedException e) {
                Log.i(DEBUG_TAG, "makeAuthorAdd: Sleep interrupted exiting", e);
                result.mRes = false;
                makeGuiUpdate(new GuiUpdateObject(result, GuiUpdateObject.UpdateType.ADD));
                return;
            }
        }
        result.mRes = true;
        makeGuiUpdate(new GuiUpdateObject(result, GuiUpdateObject.UpdateType.ADD));
    }

    /**
     * Recalculate allTagsString for all Authors
     */
    private void runUpdateTags(AuthorGuiState state) {
        for (Author author : mAuthorController.getAll()) {
            String allTagString = mAuthorController.getAllTagString(author);

            if (!author.getAll_tags_name().equals(allTagString)) {
                author.setAll_tags_name(allTagString);
                if (state != null) {
                    makeGuiUpdate(new GuiUpdateObject(author, getAuthorIndex(author, state)));
                }

            }
        }
        makeGuiUpdate(new GuiUpdateObject(GuiUpdateObject.ObjectType.TAG));

    }


    private void makeGuiUpdate(GuiUpdateObject guiUpdateObject) {
        mGuiEventBus.post(guiUpdateObject);
    }

    private int getAuthorIndex(Author author, AuthorGuiState state) {
        final List<Author> authors = mAuthorController.getAll(state.mSelectedTagId, state.mSorOrder);
        return authors.indexOf(author);
    }

    private int getAuthorIndex(int id, AuthorGuiState state) {
        Author author = mAuthorController.getById(id);
        final List<Author> authors = mAuthorController.getAll(state.mSelectedTagId, state.mSorOrder);
        return authors.indexOf(author);
    }

    private Author loadAuthor(Result res, String url) {
        Author a;
        String text;


        text = testURL(url);
        if (text == null) {
            Log.e(DEBUG_TAG, "loadAuthor: URL syntax error: " + url);

            return null;
        }

        Author ta = mAuthorController.getByUrl(text);
        if (ta != null) {
            Log.i(DEBUG_TAG, "loadAuthor: Ignore Double entries: " + text);

            ++res.doubleAdd;
            return null;
        }
        try {
            a = mHttpClientController.addAuthor(text, new Author());
        } catch (IOException ex) {
            Log.e(DEBUG_TAG, "loadAuthor: DownLoad Error for URL: " + text, ex);

            return null;

        } catch (SamlibParseException ex) {
            Log.e(DEBUG_TAG, "loadAuthor: Author parsing Error: " + text, ex);

            return null;
        } catch (IllegalArgumentException ex) {
            Log.e(DEBUG_TAG, "loadAuthor: URL Parsing exception: " + text, ex);

            return null;
        } catch (SamlibInterruptException e) {
            Log.e(DEBUG_TAG, "loadAuthor: Interrupted catch: " + text, e);
            return null;
        }

        return a;
    }

    /**
     * URL syntax checkout
     *
     * @param url original URL
     * @return reduced URL without host prefix or NULL if the syntax is wrong
     */
    private String testURL(String url) {
        Log.d(DEBUG_TAG, "testURL: Got text: " + url);

        return SamLibConfig.reduceUrl(url);

    }

}
