package monakhv.samlib.desk.sql;

import monakhv.samlib.db.AbstractController;
import monakhv.samlib.db.SQLController;
import monakhv.samlib.db.entity.Author;
import monakhv.samlib.db.entity.Book;
import monakhv.samlib.db.entity.BookCollection;
import monakhv.samlib.log.Log;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
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
 * 2/13/15.
 */


/**
 *COL_ID+"  integer primary key autoincrement, "+
 COL_NAME+" text, "+
 COL_URL   +" text UNIQUE NOT NULL, "+
 COL_isnew+" BOOLEAN DEFAULT '0' NOT NULL,"+
 COL_mtime+" timestamp "+
 */
public class AuthorController implements AbstractController<Author> {
    private static final String DEBUG_TAG = "AuthorController";
    private static final String SQL_UPDATE="UPDATE "+SQLController.TABLE_AUTHOR+" SET "+
            SQLController.COL_NAME+" =?, "+
            SQLController.COL_URL   +" =?, "+
            SQLController.COL_isnew+" =?, "+
            SQLController.COL_mtime+" =? "+
            " WHERE  "+SQLController.COL_ID+" =?";
    private static final String SQL_INSERT="INSERT INTO "+SQLController.TABLE_AUTHOR+" ( "+
            SQLController.COL_NAME+", "+
            SQLController.COL_URL   +" , "+
            SQLController.COL_isnew+" , "+
            SQLController.COL_mtime+" ) VALUES "+
            " (?,?,?,?)";
    private static final String SQL_DELETE="DELETE "+SQLController.TABLE_AUTHOR+
            " WHERE  "+SQLController.COL_ID+" =?";
    private final SQLController sql;
    private final BookController bookCtl;

    public AuthorController(SQLController sql) {
        this.sql = sql;
        this.bookCtl=new BookController(sql);
    }

    @Override
    public int update(Author author) {
        PreparedStatement ps;
        int res=0;
        try {
            ps = sql.getPrepare(SQL_UPDATE);
        } catch (SQLException e) {
            Log.e(DEBUG_TAG,"can not prepare statement for update: "+SQL_UPDATE,e);
            return -1;
        }
        try {
            ps.setString(1, author.getName());
            ps.setString(2,author.getUrl());
            if (author.isIsNew()){
                ps.setInt(3,1);
            }
            else {
                ps.setInt(3,0);
            }


            ps.setLong(4,author.getUpdateDate());

            ps.setInt(5, author.getId());

            res =ps.executeUpdate();
        } catch (SQLException e) {
            Log.e(DEBUG_TAG,"UPDATE Error: "+ps.toString(),e);

        }
        //Books of the author update
        BookCollection oldBooks = new BookCollection(bookCtl.getAll(author, null));//old books from BD
        for (Book book : author.getBooks()) {//Cycle on new Book list taken from Author object
            book.setAuthorId(author.getId());
            String url = book.getUri();
            Book oldb = oldBooks.take(url);

            if (oldb == null) {//insert
                bookCtl.insert(book);
            } else {//update
                if (book.isIsNew()) {//update
                    //TODO: we need save some parameters from the old book object
                    book.setId(oldb.getId());
                    book.setGroup_id(oldb.getGroup_id());
                    bookCtl.update(book);
                }
            }

        }
        for (Book bk : oldBooks.getLastBooks()) {
            bookCtl.delete(bk);
        }

        return res;
    }

    @Override
    public long insert(Author author) {
        PreparedStatement ps;
        int res=0;
        try {
            ps = sql.getPrepare(SQL_INSERT);
        } catch (SQLException e) {
            Log.e(DEBUG_TAG,"can not prepare statement for insert: "+SQL_INSERT,e);
            return -1;
        }
        try {
            ps.setString(1, author.getName());
            ps.setString(2,author.getUrl());
            if (author.isIsNew()){
                ps.setInt(3,1);
            }
            else {
                ps.setInt(3,0);
            }


            ps.setLong(4,author.getUpdateDate());
            res =ps.executeUpdate();
        } catch (SQLException e) {
            Log.e(DEBUG_TAG,"INSERT Error: "+ps.toString(),e);

        }
        //Insert book for the author
        Author a = getByUrl(author.getUrl());
        for (Book book : author.getBooks()) {
            book.setAuthorId(a.getId());
            bookCtl.insert(book);
        }
        return res;
    }

    @Override
    public int delete(Author author) {
        //Delete book of the author first
        List<Book> books = bookCtl.getAll(author,null);

        for (Book book : books) {
            bookCtl.delete(book);
        }
        //Delete Author
        PreparedStatement ps;
        int res=0;
        try {
            ps = sql.getPrepare(SQL_DELETE);
        } catch (SQLException e) {
            Log.e(DEBUG_TAG,"can not prepare statement for delete: "+SQL_DELETE,e);
            return -1;
        }
        try {

            ps.setInt(1, author.getId());
            res =ps.executeUpdate();
        } catch (SQLException e) {
            Log.e(DEBUG_TAG,"DELETE Error: "+ps.toString(),e);

        }
        return res;
    }


    @Override
    public List<Author> getAll() {
        return getAll(null, null);
    }

    public Author getByUrl(String url ){
        List<Author> rr = getAll(SQLController.COL_URL+"=\""+url+"\"",null);

        if (rr.size() != 1){
            Log.w(DEBUG_TAG,"Wrong result size: "+rr.size());

        }
        return rr.get(0);
    }
    public List<Author> getAll(String selection, String order) {

        String statement = SQLController.SELECT_AUTHOR_WITH_TAGS;

        if (order != null) {
            statement += " ORDER BY " + order;
        }

        if (selection == null) {
            statement = statement.replaceAll(SQLController.WHERE_PAT, "");

        } else {
            statement = statement.replaceAll(SQLController.WHERE_PAT, "where " + selection);
        }

        ResultSet rs;

        try {
            rs = sql.query(statement);
        } catch (SQLException e) {
            Log.e(DEBUG_TAG, "select error: " + statement, e);
            return null;
        }
        List<Author> res = new ArrayList<>();
        try {
            while (rs.next()) {
                res.add(resultSetToAuthor(rs));
            }
        } catch (SQLException e) {
            Log.e(DEBUG_TAG, "rs next error: " + statement, e);
            res = null;
        }


        try {
            rs.close();
        } catch (SQLException e) {
            Log.e(DEBUG_TAG, "rs close error: " + statement, e);
        }


        return res;
    }

    @Override
    public Author getById(long id) {
        return null;
    }

    private Author resultSetToAuthor(ResultSet rs) {
        Author a = new Author();

        try {
            a.setId(rs.getInt(SQLController.COL_ID));
            a.setName(rs.getString(SQLController.COL_NAME));
            a.setUrl(rs.getString(SQLController.COL_URL));
            a.setUpdateDate(rs.getLong(SQLController.COL_mtime));
            a.setIsNew(rs.getInt(SQLController.COL_isnew) == 1);

            //Populate List of Books


            List<Book> books = bookCtl.getAll(a, null);
            a.setBooks(books);


            String all_tag_names = rs.getString(SQLController.COL_TGNAMES);
            a.setAll_tags_name(all_tag_names);

            if (all_tag_names != null) {
                String[] names = all_tag_names.split(",");
                a.setTags_name(Arrays.asList(names));
            }

            String all_tag_ids = rs.getString(SQLController.COL_TGIDS);

            if (all_tag_ids != null) {
                String[] ids = all_tag_ids.split(",");

                List<Integer> res = new ArrayList<>();
                for (String s : ids) {
                    res.add(Integer.valueOf(s));
                }

                a.setTags_id(res);
            }


        } catch (SQLException e) {
            Log.e(DEBUG_TAG, "Author create error: ", e);
        }


        return a;
    }

}
