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
 * Data base Operations like Add or remove Authors
 * Make book read or unread
 * <p>
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

    private Thread mThread;

    public SamlibOperation(AuthorController sql, AbstractSettings settingsHelper, HttpClientController httpClientController, GuiEventBus guiEventBus) {
        mAuthorController = sql;
        mSettingsHelper = settingsHelper;
        mHttpClientController = httpClientController;
        mGuiEventBus = guiEventBus;

    }


    public void makeBookReadFlip(final Book book, final BookGuiState bState, final AuthorGuiState aState) {
        mThread = new Thread() {
            @Override
            public void run() {
                super.run();
                runBookReadFlip(book, bState, aState);
            }

        };
        mThread.start();
    }

    public void makeGroupReadFlip(final GroupBook groupBook, final BookGuiState bState, final AuthorGuiState aState) {
        mThread = new Thread() {
            @Override
            public void run() {
                super.run();
                runGroupReadFlip(groupBook, bState, aState);
            }
        };
        mThread.start();

    }

    public void makeAuthorRead(final Author author, final AuthorGuiState state) {
        mThread = new Thread() {
            @Override
            public void run() {
                super.run();
                runAuthorRead(author, state);
            }
        };
        mThread.start();
    }

    public void makeAuthorDel(final Author author, final AuthorGuiState state) {
        mThread = new Thread() {
            @Override
            public void run() {
                super.run();
                runAuthorDel(author, state);
            }
        };
        mThread.start();
    }

    public void makeAuthorAdd(final ArrayList<String> urls, final AuthorGuiState state) {
        mThread = new Thread() {
            @Override
            public void run() {
                super.run();
                runAuthorAdd(urls, state);
            }
        };
        mThread.start();
    }

    public void makeUpdateTags(final AuthorGuiState state) {
        mThread = new Thread() {
            @Override
            public void run() {
                super.run();
                runUpdateTags(state);
            }
        };
        mThread.start();
    }

    public void makeGroupReload(final GroupBook groupBook, final BookGuiState state) {

        mThread = new Thread() {
            @Override
            public void run() {
                super.run();
                runGroupReload(groupBook, state);
            }
        };
        mThread.start();
    }

    public void makeBookReload(final Book book, final BookGuiState state) {

        mThread = new Thread() {
            @Override
            public void run() {
                super.run();
                runBookReload(book, state);
            }
        };
        mThread.start();
    }

    public void makeAuthorReload(final Author author, final AuthorGuiState state) {
        mThread = new Thread() {
            @Override
            public void run() {
                super.run();
                runAuthorReload(author, state);
            }
        };
        mThread.start();
    }

    private void runAuthorReload(Author author, AuthorGuiState state) {
        Author a = mAuthorController.getById(author.getId());
        int idx = getAuthorIndex(a, state);
        makeGuiUpdate(new GuiUpdateObject(a, idx));
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

        List<GroupBook> newGroups = mAuthorController.getGroupBookController().getByAuthorNew(a);

        int i = mAuthorController.markRead(a);


        int idx = getAuthorIndex(a, state);
        makeGuiUpdate(new GuiUpdateObject(a, idx));


        List<GroupBook> groupBooks = mAuthorController.getGroupBookController().getByAuthor(a);
        Log.d(DEBUG_TAG, "Update author status: " + i + "   sort " + idx + " groups: " + groupBooks.size());
        for (GroupBook groupBook : newGroups) {
            //groupBook.setBooks(mAuthorController.getBookController().getBookForGroup(groupBook,b));
            makeGuiUpdate(new GuiUpdateObject(groupBook, groupBooks.indexOf(groupBook)));
        }
        if (groupBooks.size() == 0) {//special case when Author has no groups
            makeGuiUpdate(new GuiUpdateObject(GuiUpdateObject.ObjectType.GROUP, null));
        }
        return true;

    }

    @SuppressWarnings("ConstantConditions")
    private void runGroupReadFlip(GroupBook groupBook, BookGuiState bState, AuthorGuiState aState) {
        Log.d(DEBUG_TAG,"runGroupReadFlip: id "+groupBook.getId());

        List<Book> books=groupBook.getBooks();


        for (Book book : books) {
            if (book.isIsNew()) {
                mAuthorController.getBookController().markRead(book);
            }
            Author author = book.getAuthor();
            if (mAuthorController.testMarkRead(author)) {
                if (aState != null) {
                    makeGuiUpdate(new GuiUpdateObject(author, getAuthorIndex(author, aState)));
                }
            }
        }

        if (groupBook.getId() <0) {
            if (groupBook.getId() ==-2){//selected books
                GroupBook g = mAuthorController.getBookController().getSelectedGroup(bState.mSortOrder);
                makeGuiUpdate(new GuiUpdateObject(g, 0));
                return;
            }else if (groupBook.getId() ==-1){
                mAuthorController.getBookController().getBookForGroup(groupBook, bState.mSortOrder);
                makeGuiUpdate(new GuiUpdateObject(groupBook, 0));
                return;
            }


        } else {
            List<GroupBook> rr = mAuthorController.getGroupBookController().getByAuthor(groupBook.getAuthor());
            GroupBook g = mAuthorController.getGroupBookController().getById(groupBook.getId());
            mAuthorController.getBookController().getBookForGroup(g, bState.mSortOrder);

            makeGuiUpdate(new GuiUpdateObject(g, rr.indexOf(groupBook)));

        }

    }


    /**
     * Invert read book flag
     * Adjust author flag either
     *
     * @param book Book
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

        makeGuiUpdate(new GuiUpdateObject(book, loadBooks(book, bState).indexOf(book)));

    }

    private void runGroupReload(GroupBook groupBook, BookGuiState bState) {
        Author author = mAuthorController.getById(bState.mAuthorId);
        if (groupBook == null) {//Author has no group that means all book are in the single default group

            List<Book> books = mAuthorController.getBookController().getAll(author, bState.mSortOrder);
            makeGuiUpdate(new GuiUpdateObject(GuiUpdateObject.ObjectType.GROUP, books));
        } else {
            GroupBook g = mAuthorController.getGroupBookController().getById(groupBook.getId());
            mAuthorController.getBookController().getBookForGroup(g, bState.mSortOrder);
            List<GroupBook> gg = mAuthorController.getGroupBookController().getByAuthor(author);
            makeGuiUpdate(new GuiUpdateObject(g, gg.indexOf(g)));
        }
    }

    private void runBookReload(Book book, BookGuiState state) {
        List<Book> books = loadBooks(book, state);
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

        result.totalToAdd = urls.size();
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
        makeGuiUpdate(new GuiUpdateObject(GuiUpdateObject.ObjectType.TAG, null));

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

    /**
     * Return list of books of group for given book
     *
     * @param book   the Book to determine the Group
     * @param bState Book Gui state
     * @return List of book
     */
    private List<Book> loadBooks(Book book, BookGuiState bState) {
        GroupBook groupBook;
        List<Book> books;
        if (bState.mAuthorId == SamLibConfig.SELECTED_BOOK_ID) {//Selected book search
            groupBook = mAuthorController.getBookController().getSelectedGroup(bState.mSortOrder);

        } else {
            groupBook = mAuthorController.getGroupBookController().getByBook(book);
            Log.d(DEBUG_TAG,"groupBook id: "+groupBook.getId());
            mAuthorController.getBookController().getBookForGroup(groupBook, bState.mSortOrder);
        }

        books = groupBook.getBooks();


        book.setGroupBook(groupBook);
        return books;

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
