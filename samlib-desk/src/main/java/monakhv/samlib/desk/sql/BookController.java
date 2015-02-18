package monakhv.samlib.desk.sql;

import monakhv.samlib.db.AbstractController;
import monakhv.samlib.db.SQLController;
import monakhv.samlib.db.entity.Author;
import monakhv.samlib.db.entity.Book;
import monakhv.samlib.log.Log;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
 COL_BOOK_LINK +" text,"+
 COL_BOOK_AUTHOR                +" text,"+
 COL_BOOK_TITLE                     +" text,"+
 COL_BOOK_FORM                     +" text,"+
 COL_BOOK_SIZE                        +" INTEGER,"+
 COL_BOOK_GROUP_ID             +" INTEGER,"+
 COL_BOOK_DATE                      +" timestamp,"+//from the samlib
 COL_BOOK_DESCRIPTION        +" text,"+
 COL_BOOK_AUTHOR_ID            +" INTEGER NOT NULL,"+
 COL_BOOK_MTIME                    +" timestamp, "+//updated in the db
 COL_BOOK_ISNEW                    +" BOOLEAN DEFAULT '0' NOT NULL"+
 */
public class BookController  implements AbstractController<Book> {
    private static final String DEBUG_TAG="BookController";
    private static final String SQL_UPDATE="UPDATE "+SQLController.TABLE_BOOKS+" SET "+
            SQLController.COL_BOOK_LINK +" =?, "+
            SQLController.COL_BOOK_AUTHOR                +" =?, "+
            SQLController.COL_BOOK_TITLE                     +" =?, "+
            SQLController.COL_BOOK_FORM                     +" =?, "+
            SQLController.COL_BOOK_SIZE                        +" =?, "+
            SQLController.COL_BOOK_GROUP_ID             +" =?, "+
            SQLController.COL_BOOK_DATE                      +" =?, "+//from the samlib
            SQLController.COL_BOOK_DESCRIPTION        +" =?, "+
            SQLController.COL_BOOK_AUTHOR_ID            +" =?, "+
            SQLController.COL_BOOK_MTIME                    +" =?,  "+//updated in the db
            SQLController.COL_BOOK_ISNEW                    +" =? "+
            " WHERE  "+SQLController.COL_ID+" =?";
    private static final String SQL_INSERT="INSERT INTO "+SQLController.TABLE_BOOKS+" ( "+
            SQLController.COL_BOOK_LINK +" , "+
            SQLController.COL_BOOK_AUTHOR                +", "+
            SQLController.COL_BOOK_TITLE                     +" , "+
            SQLController.COL_BOOK_FORM                     +" , "+
            SQLController.COL_BOOK_SIZE                        +" , "+
            SQLController.COL_BOOK_GROUP_ID             +", "+
            SQLController.COL_BOOK_DATE                      +" , "+//from the samlib
            SQLController.COL_BOOK_DESCRIPTION        +" , "+
            SQLController.COL_BOOK_AUTHOR_ID            +" , "+
            SQLController.COL_BOOK_MTIME                    +" ,  "+//updated in the db
            SQLController.COL_BOOK_ISNEW                    +"  ) VALUES "+
            "(?,?,?,?,?,?,?,?,?,?,?)";
    private static final String SQL_DELETE="DELETE "+SQLController.TABLE_BOOKS+
            " WHERE  "+SQLController.COL_ID+" =?";

    private final SQLController sql;

    public BookController(SQLController sql) {
        this.sql=sql;

    }
    @Override
    public int update(Book book) {
        PreparedStatement ps;
        int res=0;
        try {
            ps = sql.getPrepare(SQL_UPDATE);
        } catch (SQLException e) {
           Log.e(DEBUG_TAG,"can not prepare statement for update: "+SQL_UPDATE,e);
            return -1;
        }
        try {
            ps.setString(1,book.getUri());
            ps.setString(2,book.getAuthorName());
            ps.setString(3,book.getTitle());
            ps.setString(4,book.getForm());
            ps.setLong(5,book.getSize());
            ps.setInt(6,book.getGroup_id());
            ps.setLong(7,book.getUpdateDate());
            ps.setString(8,book.getDescription());
            ps.setLong(9,book.getAuthorId());
            ps.setLong(10,book.getModifyTime());
            if (book.isIsNew()){
                ps.setInt(11,1);
            }
            else {
                ps.setInt(11,0);
            }
            ps.setInt(12,book.getId());

            res =ps.executeUpdate();
        } catch (SQLException e) {
            Log.e(DEBUG_TAG,"UPDATE Error: "+ps.toString(),e);

        }
        return res;
    }

    @Override
    public long insert(Book book) {
        PreparedStatement ps;
        int res;
        try {
            ps = sql.getPrepare(SQL_INSERT);
        } catch (SQLException e) {
            Log.e(DEBUG_TAG,"can not prepare statement for insert: "+SQL_INSERT,e);
            return -1;
        }

        try {
            ps.setString(1,book.getUri());
            ps.setString(2,book.getAuthorName());
            ps.setString(3,book.getTitle());
            ps.setString(4,book.getForm());
            ps.setLong(5,book.getSize());
            ps.setInt(6,book.getGroup_id());
            ps.setLong(7,book.getUpdateDate());
            ps.setString(8,book.getDescription());
            ps.setLong(9,book.getAuthorId());
            ps.setLong(10,book.getModifyTime());
            if (book.isIsNew()){
                ps.setInt(11,1);
            }
            else {
                ps.setInt(11,0);
            }


            res =ps.executeUpdate();
        } catch (SQLException e) {
            Log.e(DEBUG_TAG,"INSERT Error: "+ps.toString(),e);
            return -1;

        }
        return res;

    }

    @Override
    public int delete(Book book) {
        PreparedStatement ps;
        int res;
        try {
            ps = sql.getPrepare(SQL_DELETE);
        } catch (SQLException e) {
            Log.e(DEBUG_TAG,"can not prepare statement for delete: "+SQL_DELETE,e);
            return -1;
        }
        try {
            ps.setInt(1,book.getId());
            res = ps.executeUpdate();
        } catch (SQLException e) {
            Log.e(DEBUG_TAG,"DELETE Error: "+ps.toString(),e);
            return -1;
        }
        return res;
    }

    @Override
    public List<Book> getAll() {
        return null;
    }
    public List<Book> getAll(Author author, String order) {
        List<Book> res = new ArrayList<>();


        String stat = "SELECT * FROM "+SQLController.TABLE_BOOKS+" WHERE "+
                SQLController.COL_BOOK_AUTHOR_ID+" = "+ author.getId();

        if (order != null){
            stat +=" ORDER by "+order;
        }
       ResultSet rs;

        try {
            rs= sql.query(stat);
        } catch (SQLException e) {
            Log.e(DEBUG_TAG, "Query error: " + stat, e);
            return null;
        }

        try {
            while(rs.next()){
                res.add(resultSetToBook(rs));
            }
        } catch (SQLException e) {
            Log.e(DEBUG_TAG, "Get result error: " + stat, e);
            res = null;
        }

        try {
            rs.close();
        } catch (SQLException e) {
            Log.e(DEBUG_TAG, "Close Result set error: " + stat, e);
        }

        return res;
    }

    @Override
    public Book getById(long id) {
        return null;
    }
    private static Book resultSetToBook(ResultSet rs){
        Book res = new Book();
        try {

            res.setAuthorName(rs.getString(SQLController.COL_BOOK_AUTHOR));
            res.setTitle(rs.getString(SQLController.COL_BOOK_TITLE));
            res.setUri(rs.getString(SQLController.COL_BOOK_LINK));
            res.setDescription(rs.getString(SQLController.COL_BOOK_DESCRIPTION));
            res.setUpdateDate(rs.getLong(SQLController.COL_BOOK_DATE));
            res.setModifyTime(rs.getLong(SQLController.COL_BOOK_MTIME));
            res.setSize(rs.getLong(SQLController.COL_BOOK_SIZE));
            res.setForm(rs.getString(SQLController.COL_BOOK_FORM));
            res.setIsNew(rs.getInt(SQLController.COL_isnew) == 1);
            res.setAuthorId(rs.getInt(SQLController.COL_BOOK_AUTHOR_ID));
            res.setGroup_id(rs.getInt(SQLController.COL_BOOK_GROUP_ID));
            res.setId(rs.getInt(SQLController.COL_ID));
        } catch (SQLException e) {
            Log.e(DEBUG_TAG,"Error create Book object:",e);
        }

        return res;
    }
}
