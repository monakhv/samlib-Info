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
 *  01.03.16 10:07
 *
 */

package monakhv.samlib.service;

import monakhv.samlib.db.AuthorController;
import monakhv.samlib.db.SQLController;
import monakhv.samlib.db.entity.Author;
import monakhv.samlib.db.entity.Book;
import monakhv.samlib.db.entity.GroupBook;
import monakhv.samlib.db.entity.SamLibConfig;
import monakhv.samlib.http.HttpClientController;
import monakhv.samlib.impl.DaoController;
import monakhv.samlib.impl.SettingsImpl;
import monakhv.samlib.log.Log;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


import static org.junit.Assert.*;

/**
 * Class for test main Samib Operations like Add delete Author
 * Created by monakhv on 01.03.16.
 */
@SuppressWarnings("Duplicates")
public class SamlibOperationTest {
    private static final String DEBUG_TAG = "SamlibOperationTest";
    private static final long TIMEOUT=300000;

    private static final String AUTHOR_URL = "http://samlib.ru/d/demchenko_aw/";
    private static final AuthorGuiState authorGuiState = new AuthorGuiState(SamLibConfig.TAG_AUTHOR_ALL, SQLController.COL_isnew + " DESC, " + SQLController.COL_NAME);
    private static String bookOrder = SQLController.COL_BOOK_ISNEW + " DESC, " + SQLController.COL_BOOK_DATE + " DESC";
    private static AuthorController authorController;
    private static SettingsImpl settings;
    private static GuiEventBus guiEventBus;
    private static HttpClientController httpClientController;

    private SamlibOperation mSamlibOperation;
    private final Object monitor = new Object();
    private Result mResult;
    private Book mBook;
    private GroupBook mGroupBook;
    private Author mAuthor;


    public SamlibOperationTest() {

        mSamlibOperation = new SamlibOperation(authorController, settings, httpClientController, guiEventBus);
    }

    @Before
    public void setUp()  {
        cleanDataBase();
    }

    @BeforeClass
    public static void globalSetUp() {
        settings = new SettingsImpl();
        guiEventBus = new GuiEventBus();
        httpClientController = new HttpClientController(new SettingsImpl());
        httpClientController.setProxyData(SettingsImpl.proxyData);
        System.out.println("setUp: database setup in " + settings.getDataDirectory());

        try {
            DaoController daoController = DaoController.getInstance(SQLController.getInstance(settings.getDataDirectoryPath()));
            authorController = new AuthorController(daoController);
        } catch (ClassNotFoundException e) {
            Log.e(DEBUG_TAG, "setUp: class not found", e);
        } catch (SQLException e) {
            Log.e(DEBUG_TAG, "setUp: SQL error", e);

        }
    }


    @Test(timeout = TIMEOUT)
    public void testMakeBookReadFlip()  {
        ArrayList<String> urls = new ArrayList<>();
        urls.add(AUTHOR_URL);
        mSamlibOperation.runAuthorAdd(urls, authorGuiState);
        List<Author> aa = authorController.getAll();
        assertEquals(1, aa.size());
        Author author = aa.get(0);

        Book book = authorController.getBookController().getAll(author, bookOrder).get(10);

        Log.i(DEBUG_TAG, "testMakeBookReadFlip: test gor book " + book.getTitle() + "   -    " + book.getUri());

        guiEventBus.getObservable().subscribe(guiUpdateObject -> {
            if (guiUpdateObject.isBook()) {
                mBook = (Book) guiUpdateObject.getObject();
                synchronized (monitor) {
                    monitor.notifyAll();
                }
            }
        });
        mSamlibOperation.makeBookReadFlip(book, new BookGuiState(author.getId(), bookOrder), authorGuiState);

        synchronized (monitor) {
            try {
                monitor.wait();
            } catch (InterruptedException e) {
                Log.e(DEBUG_TAG, "testMakeBookReadFlip: interrupted", e);
            }
        }

        author = authorController.getById(author.getId());
        assertTrue(mBook.isIsNew());
        assertTrue(author.isIsNew());
        assertEquals(1, mBook.getGroupBook().getNewNumber());

        guiEventBus.getObservable().subscribe(guiUpdateObject -> {
            if (guiUpdateObject.isBook()) {
                mBook = (Book) guiUpdateObject.getObject();
                synchronized (monitor) {
                    monitor.notifyAll();
                }
            }
        });
        mSamlibOperation.makeBookReadFlip(book, new BookGuiState(author.getId(), bookOrder), authorGuiState);

        synchronized (monitor) {
            try {
                monitor.wait();
            } catch (InterruptedException e) {
                Log.e(DEBUG_TAG, "testMakeBookReadFlip: interrupted", e);
            }
        }

        author = authorController.getById(author.getId());
        assertFalse(mBook.isIsNew());
        assertFalse(author.isIsNew());
        assertEquals(0, mBook.getGroupBook().getNewNumber());


    }

    @Test(timeout = TIMEOUT)
    public void testMakeGroupReadFlip()  {

        ArrayList<String> urls = new ArrayList<>();
        urls.add(AUTHOR_URL);
        mSamlibOperation.runAuthorAdd(urls, authorGuiState);
        List<Author> aa = authorController.getAll();
        assertEquals(1, aa.size());
        Author author = aa.get(0);

        Book book = authorController.getBookController().getAll(author, bookOrder).get(10);

        Log.i(DEBUG_TAG, "testMakeGroupReadFlip: test gor book " + book.getTitle() + "   -    " + book.getUri());

        authorController.getBookController().markUnRead(book);
        authorController.testMarkRead(author);
        book = authorController.getBookController().getById(book.getId());
        GroupBook groupBook = authorController.getGroupBookController().getByBook(book);

        assertTrue(book.isIsNew());
        assertEquals(1, groupBook.getNewNumber());

        guiEventBus.getObservable().subscribe(
                guiUpdateObject -> {
                    if (guiUpdateObject.isGroup()) {
                        mGroupBook = (GroupBook) guiUpdateObject.getObject();
                        myNotify();
                    }
                },
                throwable -> {
                    Log.e(DEBUG_TAG, "testMakeGroupReadFlip: error", throwable);
                    myNotify();
                }
        );

        mSamlibOperation.makeGroupReadFlip(groupBook, new BookGuiState(author.getId(), bookOrder), null);

        synchronized (monitor) {
            try {
                monitor.wait();
            } catch (InterruptedException e) {
                Log.e(DEBUG_TAG, "testMakeBookReadFlip: interrupted", e);
            }
        }

        assertEquals(0, mGroupBook.getNewNumber());

    }

    @Test(timeout = TIMEOUT)
    public void testMakeAuthorRead() {
        ArrayList<String> urls = new ArrayList<>();
        urls.add(AUTHOR_URL);
        mSamlibOperation.runAuthorAdd(urls, authorGuiState);
        List<Author> aa = authorController.getAll();
        assertEquals(1, aa.size());
        Author author = aa.get(0);

        Book book = authorController.getBookController().getAll(author, bookOrder).get(10);

        Log.i(DEBUG_TAG, "testMakeAuthorRead: test gor book " + book.getTitle() + "   -    " + book.getUri());

        authorController.getBookController().markUnRead(book);
        authorController.testMarkRead(author);
        book = authorController.getBookController().getById(book.getId());
        GroupBook groupBook = authorController.getGroupBookController().getByBook(book);

        assertTrue(book.isIsNew());
        assertEquals(1, groupBook.getNewNumber());
        guiEventBus.getObservable().subscribe(
                guiUpdateObject -> {
                    if (guiUpdateObject.isAuthor()) {
                        mAuthor = (Author) guiUpdateObject.getObject();
                        myNotify();
                    }
                },
                throwable -> {
                    Log.e(DEBUG_TAG, "testMakeAuthorRead: onError ", throwable);
                    myNotify();
                });
        mSamlibOperation.makeAuthorRead(author, authorGuiState);
        synchronized (monitor) {
            try {
                monitor.wait();
            } catch (InterruptedException e) {
                Log.e(DEBUG_TAG, "testMakeAuthorRead: interrupted", e);
            }
        }
        author = authorController.getById(author.getId());
        book = authorController.getBookController().getById(book.getId());
        groupBook = authorController.getGroupBookController().getByBook(book);

        assertFalse(author.isIsNew());
        assertFalse(book.isIsNew());
        assertEquals(author.getId(), mAuthor.getId());
        assertEquals(0, groupBook.getNewNumber());
    }

    @Test(timeout = TIMEOUT)
    public void testMakeAuthorDel()  {

        ArrayList<String> urls = new ArrayList<>();
        urls.add(AUTHOR_URL);
        mSamlibOperation.runAuthorAdd(urls, authorGuiState);
        List<Author> aa = authorController.getAll();
        assertEquals(1, aa.size());
        Author author = aa.get(0);

        guiEventBus.getObservable().subscribe(
                guiUpdateObject -> {
                    if (guiUpdateObject.isResult()) {
                        mResult = (Result) guiUpdateObject.getObject();
                        myNotify();
                    }
                },
                throwable -> {
                    Log.e(DEBUG_TAG, "testMakeAuthorDel: error", throwable);
                    myNotify();
                });
        mSamlibOperation.makeAuthorDel(author, authorGuiState);

        synchronized (monitor) {
            try {
                monitor.wait();
            } catch (InterruptedException e) {
                Log.e(DEBUG_TAG, "testMakeAuthorDel: interrupted", e);
            }
        }

        assertEquals(1, mResult.numberOfDeleted);
        assertEquals(0, authorController.getAll().size());
    }

    @Test(timeout = TIMEOUT)
    public void testMakeAuthorAdd()  {

        guiEventBus.getObservable().subscribe(
                guiUpdateObject -> {
                    if (guiUpdateObject.isResult()) {
                        mResult = (Result) guiUpdateObject.getObject();
                        myNotify();
                    }
                },
                throwable -> {
                    Log.e(DEBUG_TAG,"testMakeAuthorAdd: error",throwable);
                    myNotify();
                });


        ArrayList<String> urls = new ArrayList<>();
        urls.add(AUTHOR_URL);

        mSamlibOperation.makeAuthorAdd(urls, authorGuiState);
        synchronized (monitor) {
            try {
                monitor.wait();
            } catch (InterruptedException e) {
                Log.e(DEBUG_TAG, "testMakeAuthorAdd: interrupted", e);
            }
        }
        assertEquals(0, mResult.doubleAdd);
        assertEquals(1, mResult.numberOfAdded);
        assertEquals(1, authorController.getAll().size());

    }


    private void cleanDataBase() {
        for (Author author : authorController.getAll(authorGuiState.mSelectedTagId, authorGuiState.mSorOrder)) {
            authorController.delete(author);
        }
    }

    private void myNotify() {
        synchronized (monitor) {
            monitor.notifyAll();
        }
    }


}