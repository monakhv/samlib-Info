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
package monakhv.android.samlib.sql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import monakhv.android.samlib.sql.entity.Author;

/**
 *
 * @author monakhv
 * 
 * Version - 1: Initial release Author table only Book data into serialized BLOB
 * Version - 2: Separate table for Book
 * Version - 3: COL_BOOK_DATE into GMT
 * Version - 4: Tags for the authors
 * Version - 5: Remove samlib URL from the  Author url to use several mirrors
 * Version - 6:Search Author Result
 */
public class SQLController {
    public enum ORDER_BY {
        MODIFY_TIME("mtime DESC"),
        AUTHOR_NAME("name");
        private ORDER_BY(String sql_state){
            this.sql=sql_state;
        }
        private final String sql;
        public String getStatement(){
            return "ORDER BY "+sql;
        }
    }

    public static final String DB_NAME   = "AUTHOR_DATA";
    public static final int    DB_VERSION = 6;
    
    public static final String COL_ID = "_id";
    public static final String COL_NAME = "NAME";
    public static final String COL_URL    = "URL";
    public static final String COL_mtime = "MTIME";
    public static final String COL_books = "BOOKS";
    public static final String COL_isnew = "ISNEW";
    public static final String COL_TGNAMES ="tags_name";
    public static final String COL_TGIDS =     "tags_id";
    
    public static final String COL_BOOK_LINK                         ="LINK";
    public static final String COL_BOOK_AUTHOR                  ="AUTHOR";
    public static final String COL_BOOK_TITLE                       ="TITLE";
    public static final String COL_BOOK_FORM                       ="FORM";
    public static final String COL_BOOK_SIZE                         ="SIZE";
    public static final String COL_BOOK_DATE                        ="DATE";    
    public static final String COL_BOOK_DESCRIPTION          ="DESCRIPTION";
    public static final String COL_BOOK_AUTHOR_ID             ="AUTHOR_ID";
    public static final String COL_BOOK_MTIME                      ="MTIME";
    public static final String COL_BOOK_ISNEW                      ="ISNEW";
    public static final String COL_BOOK_GROUP_ID                ="GROUP_ID";
    
    public static final String COL_TAG_NAME= "NAME";
    public static final String COL_TAG_UCNAME= "UCNAME";
    
    public static final String COL_T2A_TAGID         = "TAG_ID";
    public static final String COL_T2A_AUTHORID  = "AUTHOR_ID";
    
    public static final String COL_STATE_VAR_NAME ="VAR_NAME";
    public static final String COL_STATE_VAR_VALUE="VAR_VALUE";
    
    public static final String COL_AC_URL       ="URL";
    public static final String COL_AC_NAME    ="NAME";
    public static final String COL_AC_TITLE    ="TITLE";
    public static final String COL_AC_DESC     ="DESC";
    public static final String COL_AC_SIZE      ="SIZE";
    public static final String COL_AC_COUNT ="COUNT";
    
    public static final String TABLE_AUTHOR = "Author";
    public static final String TABLE_BOOKS   = "Book";
    public static final String TABLE_TAGS      ="Tags";
    public static final String TABLE_T2A         ="Tag2Author" ;
    public static final String TABLE_STATE    ="StateData";
    public static final String TABLE_SEARCH_AUTHOR    ="AuthorCard";
    
    private static final String CLASS_NAME = "org.sqlite.JDBC";
    private static final String CONNECT_STRING = "jdbc:sqlite:"+DB_NAME+".db";
    
    public static final String DB_CREATE_AUTHOR ="create table if not exists "+TABLE_AUTHOR+"( "+
            COL_ID+"  integer primary key autoincrement, "+
            COL_NAME+" text, "+
            COL_URL   +" text UNIQUE NOT NULL, "+
            COL_isnew+" BOOLEAN DEFAULT '0' NOT NULL,"+
            COL_mtime+" timestamp "+
            //COL_books+" blob"+
            ");";
    public static final String WHERE_PAT="_WHERE_";
    public static final String SELECT_AUTHOR_WITH_TAGS ="SELECT "
            + "Author._id as _id, "
            + "Author.NAME as NAME, "
            + "Author.URL as URL, "
            + "Author.MTIME as MTIME, "
            + "Author.ISNEW as ISNEW, "
            + "GROUP_CONCAT(Tags._id) as tags_id, "
            + "GROUP_CONCAT(Tags.NAME) as tags_name "
            + "FROM Author "
            + "LEFT OUTER JOIN Tag2Author ON Tag2Author.AUTHOR_ID = Author._id "
            + "LEFT OUTER JOIN Tags ON Tags._id =Tag2Author.TAG_ID  "
            + "_WHERE_ GROUP BY Author._id ";
    public static final String DB_CREATE_BOOKS ="create table if not exists "+TABLE_BOOKS+"( "+
            COL_ID+"  integer primary key autoincrement, "+
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
            //"FOREIGN KEY ("+COL_BOOK_AUTHOR_ID+") REFERENCES "+TABLE_AUTHOR+"("+COL_ID+")"+
            ");";
    public static final String DB_ALTER_BOOK1="alter table "+TABLE_BOOKS+" add column  "+COL_BOOK_GROUP_ID +" INTEGER;";
    
    public static final String DB_CREATE_TAGS ="create table if not exists "+TABLE_TAGS+"( "+
            COL_ID+"  integer primary key autoincrement, "+
            COL_TAG_NAME                    +" text,"+
            COL_TAG_UCNAME               +" text"+           
            ");";
    
    public static final String DB_CREATE_AC ="create table if not exists "+TABLE_SEARCH_AUTHOR+"( "+
            COL_ID+"  integer primary key autoincrement, "+
            COL_AC_URL                   +" text,"+
            COL_AC_NAME                +" text,"+
            COL_AC_TITLE                +" text,"+
            COL_AC_DESC                 +" text,"+
            COL_AC_SIZE                   +" INTEGER,"+
            COL_AC_COUNT              +" INTEGER"+
            ");";
    
    public static final String DB_CREATE_TAG_TO_AUTHOR ="create table if not exists "+TABLE_T2A+"( "+
            COL_ID+"  integer primary key autoincrement, "+
            COL_T2A_TAGID               +" INTEGER NOT NULL,"+
            COL_T2A_AUTHORID        +" INTEGER NOT NULL"+
            ");";
    public static final String DB_CREATE_STATE ="create table if not exists "+TABLE_STATE+"( "+
            COL_ID+"  integer primary key autoincrement, "+
            COL_STATE_VAR_NAME                   +" text UNIQUE NOT NULL, "+
            COL_STATE_VAR_VALUE                  +" blob"+           
            ");";
    
    public static final String DB_IDX1 = "CREATE INDEX  author_url_idx ON Author(URL);";
    public static final String DB_IDX2 = "CREATE INDEX  book_author   ON Book(AUTHOR_ID);";
    public static final String DB_IDX3 = "CREATE INDEX  tagName  ON Tags(UCNAME);";
    public static final String DB_IDX4 = "CREATE INDEX  tag_author     ON Tag2Author(TAG_ID,AUTHOR_ID);";
    ///public static final String ALTER2_1 = "ALTER TABLE  "+TABLE_AUTHOR+" DROP COLUMN "+COL_books+" ;";//Not Supported by SQLight
    public static final String ALTER2_2 = "UPDATE   "+TABLE_AUTHOR+" SET  "+COL_isnew+" =0;";
    
    public static final String INSERT_AUTHOR ="INSERT INTO " + TABLE_AUTHOR +" ( "+
            COL_NAME +","+
            COL_URL+","+
            COL_mtime+"," +
            COL_isnew+","+
            COL_books+")";
            
    public static final String UPDATE_AUTHOR ="UPDATE "+ TABLE_AUTHOR +" SET "+
            COL_NAME + "=? ,"+
            COL_URL+"=?,"+
            COL_mtime+"=?,"+
            COL_isnew+"=?,"+
            COL_books+"=? "+
            " WHERE "+ COL_ID+"=?";
    private static SQLController instance = null;
    private final Connection bd;
    private Statement st;

    private SQLController() throws ClassNotFoundException, SQLException {
        Class.forName(CLASS_NAME);
        bd = DriverManager.getConnection(CONNECT_STRING);
        st = bd.createStatement();
        st.execute(DB_CREATE_AUTHOR);
        st.execute(DB_CREATE_BOOKS);
        st.execute(DB_IDX1);

    }

        
    /**
     * Delete author from SQL store
     * @param a author to delete
     * @throws SQLException
     * @throws IOException 
     */
    public void delete(Author a) throws SQLException, IOException {        
        PreparedStatement statement = bd.prepareStatement("DELETE from Author  WHERE ID=?");     
            statement.setInt(1, a.getId());
            statement.executeUpdate();
        
    }

    
  
    /**
     * Get the single instance of SQL controller
     *
     * @return
     */
    public static SQLController getInstance() throws ClassNotFoundException, SQLException {
        if (instance == null) {

            instance = new SQLController();

        }
        return instance;
    }

    

   
}
