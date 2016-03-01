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
package monakhv.samlib.db;


import java.sql.*;



/**
 *
 * @author monakhv
 * 
 * Version - 1: Initial release Author table only Book data into serialized BLOB
 * Version - 2: Separate table for Book
 * Version - 3: COL_BOOK_DATE into GMT
 * Version - 4: Tags for the authors
 * Version - 5: Remove samlib URL from the  Author url to use several mirrors
 * Version - 6:add option column for Book table
 * Version - 7:add ALL_TAGS_STRING column for Author Table
 * Version - 8 GroupBook & Selected Book tables, selected book Flag option
 * 
 */
@SuppressWarnings("SqlNoDataSourceInspection")
public class SQLController {


    public static final String DB_NAME   = "AUTHOR_DATA";
    public static final String DB_EXT = ".db";
    public static final int    DB_VERSION = 8;
    
    public static final String COL_ID = "_id";
    public static final String COL_NAME = "NAME";
    public static final String COL_URL    = "URL";
    public static final String COL_mtime = "MTIME";
    public static final String COL_books = "BOOKS";
    public static final String COL_isnew = "ISNEW";
    public static final String COL_ALL_TAGS_NAME ="ALL_TAGS_NAME";

    public static final String COL_BOOK_ID                              ="BOOK_ID";
    public static final String COL_BOOK_LINK                         ="LINK";
    public static final String COL_BOOK_AUTHOR                  ="AUTHOR";
    public static final String COL_BOOK_TITLE                       ="TITLE";
    public static final String COL_BOOK_FORM                       ="FORM";
    public static final String COL_BOOK_SIZE                         ="SIZE";
    public static final String COL_BOOK_DELTA                         ="DELTA_SIZE";//newSize-oldSize
    public static final String COL_BOOK_DATE                        ="DATE";    
    public static final String COL_BOOK_DESCRIPTION          ="DESCRIPTION";
    public static final String COL_BOOK_AUTHOR_ID             ="AUTHOR_ID";
    public static final String COL_BOOK_MTIME                      ="MTIME";
    public static final String COL_BOOK_ISNEW                      ="ISNEW";
    public static final String COL_BOOK_GROUP_ID                ="GROUP_ID";
    public static final String COL_BOOK_OPT                             ="OPTS";

    public static final String COL_TAG_NAME= "NAME";
    public static final String COL_TAG_UCNAME= "UCNAME";

    public static final String COL_GROUP_IS_HIDDEN                ="IS_HIDDEN";
    public static final String COL_GROUP_AUTHOR_ID              ="AUTHOR_ID";
    public static final String COL_GROUP_NEW_NUMBER                        ="NEW_NUMBER";
    public static final String COL_GROUP_NAME                        = "NAME";
    public static final String COL_GROUP_DISPLAY_NAME        = "DISPLAY_NAME";//to



    
    public static final String COL_T2A_TAGID         = "TAG_ID";
    public static final String COL_T2A_AUTHORID  = "AUTHOR_ID";
    
    public static final String COL_STATE_VAR_NAME ="VAR_NAME";
    public static final String COL_STATE_VAR_VALUE="VAR_VALUE";
    
    public static final String TABLE_AUTHOR = "Author";
    public static final String TABLE_BOOKS   = "Book";
    public static final String TABLE_TAGS      ="Tags";
    public static final String TABLE_T2A         ="Tag2Author" ;
    public static final String TABLE_SELECTED_BOOK         ="SelectedBook" ;
    public static final String TABLE_GROUP_BOOK         ="GroupBook" ;
    public static final String TABLE_STATE    ="StateData";
    
    private static final String CLASS_NAME = "org.sqlite.JDBC";
    private static final String CONNECT_STRING_PREFIX = "jdbc:sqlite:";
    
    public static final String DB_CREATE_AUTHOR ="create table if not exists "+TABLE_AUTHOR+"( "+
            COL_ID+"  integer primary key autoincrement, "+
            COL_NAME+" text, "+
            COL_URL   +" text UNIQUE NOT NULL, "+
            COL_isnew+" BOOLEAN DEFAULT '0' NOT NULL,"+
            COL_mtime+" timestamp, "+
            COL_ALL_TAGS_NAME+" text"+
            //COL_books+" blob"+
            ");";

    public static final String DB_CREATE_SELECTED ="create table if not exists "+TABLE_SELECTED_BOOK+"( "+
            COL_ID+"  integer primary key autoincrement, "+
            COL_BOOK_ID   +" INTEGER NOT NULL "+
            ");";

    public static final String DB_CREATE_GROUP_BOOK ="create table if not exists "+TABLE_GROUP_BOOK+"( "+
            COL_ID+"  integer primary key autoincrement, "+
            COL_GROUP_AUTHOR_ID+" INTEGER NOT NULL, "+
            COL_GROUP_NEW_NUMBER+" INTEGER  NOT NULL,"+
            COL_GROUP_IS_HIDDEN+" BOOLEAN DEFAULT '0' NOT NULL,"+
            COL_GROUP_NAME   +" text,  "+
            COL_GROUP_DISPLAY_NAME   +" text "+
            ");";
    public static final String DB_CREATE_BOOKS ="create table if not exists "+TABLE_BOOKS+"( "+
            COL_ID+"  integer primary key autoincrement, "+
            COL_BOOK_LINK +" text,"+
            COL_BOOK_AUTHOR                +" text,"+
            COL_BOOK_TITLE                     +" text,"+
            COL_BOOK_FORM                     +" text,"+
            COL_BOOK_SIZE                        +" INTEGER,"+
            COL_BOOK_DELTA                    +" INTEGER,"+
            COL_BOOK_OPT                        +" INTEGER,"+
            COL_BOOK_GROUP_ID             +" INTEGER,"+
            COL_BOOK_DATE                      +" timestamp,"+//from the samlib we do not use it anymore
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
    
    public static final String DB_IDX1 = "CREATE INDEX if not exists  author_url_idx ON Author(URL);";
    public static final String DB_IDX2 = "CREATE INDEX if not exists book_author   ON Book(AUTHOR_ID);";
    public static final String DB_IDX3 = "CREATE INDEX if not exists tagName  ON Tags(UCNAME);";
    public static final String DB_IDX4 = "CREATE INDEX  if not exists tag_author     ON Tag2Author(TAG_ID,AUTHOR_ID);";
    public static final String DB_IDX51 = "CREATE INDEX  if not exists group_author     ON GroupBook(NAME,AUTHOR_ID);";
    public static final String DB_IDX52 = "CREATE INDEX  if not exists book_group       ON Book(GROUP_ID);";
    ///public static final String ALTER2_1 = "ALTER TABLE  "+TABLE_AUTHOR+" DROP COLUMN "+COL_books+" ;";//Not Supported by SQLight
    public static final String ALTER2_2 = "UPDATE   "+TABLE_AUTHOR+" SET  "+COL_isnew+" =0;";
    public static final String ALTER6_1=  "ALTER TABLE "+TABLE_BOOKS+" ADD COLUMN "+COL_BOOK_OPT+" INTEGER;";
    public static final String ALTER7_1 = "ALTER TABLE   "+TABLE_AUTHOR+" ADD COLUMN  "+COL_ALL_TAGS_NAME+" text;";
    public static final String ALTER8_1=  "ALTER TABLE "+TABLE_BOOKS+" ADD COLUMN "+COL_BOOK_DELTA+" INTEGER;";
    public static final String UPDATE8_2 = "UPDATE   "+TABLE_BOOKS+" SET  "+COL_BOOK_DELTA+" =0;";
    public static final String UPDATE8_3 = "UPDATE   "+TABLE_BOOKS+" SET  "+COL_BOOK_GROUP_ID+" =0;";


            

    private static SQLController instance = null;
    private final Connection bd;
    private final String dbUrl;


    private SQLController(String data_path ) throws ClassNotFoundException, SQLException {
        Class.forName(CLASS_NAME);
        dbUrl=CONNECT_STRING_PREFIX+data_path+"/"+DB_NAME+DB_EXT;
        bd = DriverManager.getConnection(dbUrl);
        Statement st = bd.createStatement();
        st.execute(DB_CREATE_AUTHOR);
        st.execute(DB_CREATE_BOOKS);
        st.execute(DB_IDX1);

        st.execute(DB_IDX2);

        st.execute(DB_CREATE_TAGS);
        st.execute(DB_CREATE_TAG_TO_AUTHOR);
        st.execute(DB_CREATE_STATE);
        st.execute(DB_CREATE_GROUP_BOOK);
        st.execute(DB_IDX3);
        st.execute(DB_IDX4);
        st.execute(DB_IDX51);
        st.execute(DB_IDX52);

        st.close();

    }

    public String getDbUrl() {
        return dbUrl;
    }

    /**
     * Make low level SQL query
     * @param sql SQL Language query string
     * @return ResultSet Object
     * @throws SQLException
     */
    public ResultSet query(String sql) throws SQLException {
        Statement st = bd.createStatement();
        return st.executeQuery(sql);
    }

    public PreparedStatement getPrepare(String sql) throws SQLException {
        return  bd.prepareStatement(sql);
    }
    
  
    /**
     * Get the single instance of SQL controller
     *
     * @return
     */
    public static SQLController getInstance(String path) throws ClassNotFoundException, SQLException {
        if (instance == null) {

            instance = new SQLController(path);

        }
        return instance;
    }

    

   
}
