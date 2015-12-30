package monakhv.samlib.db;


import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import monakhv.samlib.db.entity.Author;
import monakhv.samlib.db.entity.Book;
import monakhv.samlib.db.entity.GroupBook;
import monakhv.samlib.db.entity.SelectedBook;
import monakhv.samlib.log.Log;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/*
 * Copyright 2015  Dmitry Monakhov
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
 *
 *
 * 2/16/15.
 */

/**
 * COL_ID+"  integer primary key autoincrement, "+
 * COL_BOOK_LINK +" text,"+
 * COL_BOOK_AUTHOR                +" text,"+
 * COL_BOOK_TITLE                     +" text,"+
 * COL_BOOK_FORM                     +" text,"+
 * COL_BOOK_SIZE                        +" INTEGER,"+
 * COL_BOOK_GROUP_ID             +" INTEGER,"+
 * COL_BOOK_DATE                      +" timestamp,"+//from the samlib
 * COL_BOOK_DESCRIPTION        +" text,"+
 * COL_BOOK_AUTHOR_ID            +" INTEGER NOT NULL,"+
 * COL_BOOK_MTIME                    +" timestamp, "+//updated in the db
 * COL_BOOK_ISNEW                    +" BOOLEAN DEFAULT '0' NOT NULL"+
 */
public class BookController {
    private static final String DEBUG_TAG = "BookController";

    private final Dao<Book, Integer> dao;
    private final Dao<SelectedBook, Integer> selectedDao;
    private final GroupBookController grpCtl;

    public BookController(DaoBuilder sql) {

        dao = sql.getBookDao();
        selectedDao = sql.getSelectedBookDao();
        grpCtl = new GroupBookController(sql);

    }

    public void operate(Author author) {
        HashMap<String, GroupBook> groupBookHashMap = new HashMap<>();//cache for GroupBook lookup
        List<Book> books = new ArrayList<>();
        for (Book book : author.getBooks()) {
            Log.i(DEBUG_TAG, "Book: " + book.getUri() + " - " + book.isIsNew() + " Operation: " + book.getSqlOperation().name());
            switch (book.getSqlOperation()) {
                case DELETE:
                    if (! book.isPreserve()){
                        books.add(book);
                        delete(books);
                    }
                    break;
                case UPDATE:
                    restoreGroup(author, book, groupBookHashMap);
                    update(book);
                    break;
                case INSERT:
                    restoreGroup(author, book, groupBookHashMap);
                    insert(book);
                    break;
                case NONE:
                    break;
            }
        }
    }

    /**
     * Restore GroupBook for the Book according to the Author and Group Name
     *
     * @param author           Author
     * @param book             Book
     * @param groupBookHashMap Cache to store lookup value
     */
    private void restoreGroup(Author author, Book book, HashMap<String, GroupBook> groupBookHashMap) {
        String gName = book.getGroupBook().getName();

        if (groupBookHashMap.containsKey(gName)) {//Group found into cache
            book.setGroupBook(groupBookHashMap.get(gName));
        } else {
            GroupBook gb = grpCtl.getByAuthorAndName(author, gName);
            if (gb != null) {
                groupBookHashMap.put(gName, gb);
                book.setGroupBook(gb);
            }
        }

    }

    /**
     * Update book into database
     *
     * @param book The object to update
     * @return id
     */

    public int update(Book book) {

        int res;

        try {
            res = dao.update(book);
        } catch (SQLException e) {
            Log.e(DEBUG_TAG, "can not update: ", e);
            return -1;

        }
        return res;

    }

    /**
     * Insert new Book object into Database
     *
     * @param book object to insert
     * @return id
     */

    public long insert(Book book) {
        int res;
        try {
            res = dao.create(book);
        } catch (SQLException e) {
            Log.e(DEBUG_TAG, "can not insert: ", e);
            return -1;
        }
        return res;
    }

    /**
     * Delete Book object
     *
     * @param books objects to delete
     * @return id
     */
    public int delete(List<Book> books) {
        int res;
        try {
            res = dao.delete(books);
        } catch (SQLException e) {
            Log.e(DEBUG_TAG, "can not delete: ", e);
            return -1;
        }

        return res;
    }


    public List<Book> getAll() {
        return null;
    }

    /**
     * Get Books of given author
     *
     * @param author the Author
     * @param order  Sort order in row format
     * @return List of the books
     */
    public synchronized List<Book> getAll(Author author, String order) {
        List<Book> res;
        QueryBuilder<Book, Integer> qb = dao.queryBuilder();


        if (order != null) {
            qb.orderByRaw(order);
        }

        try {

            res = dao.query(getPrepared(qb, author));
        } catch (SQLException e) {
            Log.e(DEBUG_TAG, "getAll: Query error: ", e);
            return null;
        }

        return res;
    }

    /**
     * Return list of new book for given author
     * @param author Author
     * @param order sort order
     * @return List of author
     */
    public List<Book> getAllNew(Author author, String order) {
        List<Book> res;
        QueryBuilder<Book, Integer> qb = dao.queryBuilder();

        if (order != null) {
            qb.orderByRaw(order);
        }

        try {
            qb.where()
                    .eq(SQLController.COL_BOOK_AUTHOR_ID,author)
                    .and()
                    .eq(SQLController.COL_BOOK_ISNEW,true);
            res=dao.query(qb.prepare());
        }
        catch (SQLException e){
            Log.e(DEBUG_TAG, "getAllNew: Query error: ", e);
            return null;
        }
        return res;


    }

    private PreparedQuery<Book> getPrepared(QueryBuilder<Book, Integer> qb, Author author) throws SQLException {

        qb.where().eq(SQLController.COL_BOOK_AUTHOR_ID, author);

        return qb.prepare();

    }


    /**
     * Get Selected Book
     *
     * @param order Sort order if not null
     * @return List of selected books
     */
    public List<Book> getSelected(String order) {
        QueryBuilder<Book, Integer> qbBooks = dao.queryBuilder();
        QueryBuilder<SelectedBook, Integer> qbSelected = selectedDao.queryBuilder();

        try {
            qbBooks.join(qbSelected);
        } catch (SQLException e) {
            Log.e(DEBUG_TAG, "getSelected: join error", e);
            return null;
        }


        if (order != null) {
            qbBooks.orderByRaw(order);
        }
        try {
            //qbBooks.where().eq(SQLController.COL_BOOK_GROUP_ID, Book.SELECTED_GROUP_ID);
            return dao.query(qbBooks.prepare());

        } catch (SQLException e) {
            Log.e(DEBUG_TAG, "getSelected: query error", e);
            return null;
        }

    }

    public List<Book> getBookForGroup(GroupBook groupBook,String order) {
        QueryBuilder<Book, Integer> qbBooks = dao.queryBuilder();
        if (order != null) {
            qbBooks.orderByRaw(order);
        }
        try {
            qbBooks.where()

                    .eq(SQLController.COL_BOOK_GROUP_ID, groupBook);
            return dao.query(qbBooks.prepare());
        } catch (SQLException e) {
            Log.e(DEBUG_TAG,"getBookForGroup: query error",e);
            return null;
        }

    }


    public Book getById(long id) {
        Integer dd = (int) id;
        try {
            return dao.queryForId(dd);
        } catch (SQLException e) {
            return null;
        }

    }

    public List<Book> getBooksByAuthor(Author a) {
        return getAll(a, null);
    }

    public void markRead(Book book) {
        book.setIsNew(false);
        update(book);
    }

    public void markUnRead(Book book) {
        book.setIsNew(true);
        update(book);
    }

    /**
     * Set book selected
     *
     * @param book the book to make selected
     */
    public void setSelected(Book book) {
        if (book.isSelected()) {
            return;
        }

        book.setSelected(true);
        try {
            selectedDao.create(new SelectedBook(book));
            dao.update(book);
        } catch (SQLException e) {
            Log.e(DEBUG_TAG, "setSelected: sql error ", e);
        }
    }

    /**
     * Make book deselected - remove from selection
     *
     * @param book Book to deselect
     */
    public void setDeselected(Book book) {
        if (!book.isSelected()) {
            return;
        }
        book.setSelected(false);

        update(book);
        QueryBuilder<SelectedBook, Integer> qb = selectedDao.queryBuilder();
        List<SelectedBook> selected;
        try {
            selected = selectedDao.query(qb.where().eq(SQLController.COL_BOOK_ID, book).prepare());
        } catch (SQLException e) {
            Log.e(DEBUG_TAG, "setDeselected: sql query error ", e);
            return;
        }
        if (selected.size() != 1) {
            Log.e(DEBUG_TAG, "setDeselected: result size error " + selected.size());
        }
        try {
            selectedDao.delete(selected.get(0));
        } catch (SQLException e) {
            Log.e(DEBUG_TAG, "setDeselected: sql delete error ", e);
        }

    }
}
