package monakhv.samlib.data;

import monakhv.samlib.db.entity.Book;
import monakhv.samlib.http.ProxyData;
import monakhv.samlib.log.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
 * 2/12/15.
 */
public abstract class AbstractSettings {
    private static final String DEBUG_TAG = "AbstractSettings";
    private static final String DATE_FORMAT_BOOK_FILE = "dd-MM-yyyy_HH-mm-ss";
    public static final String BOOKS_DIR = "Book//";
    public static final String sep = System.getProperty("file.separator");
    public static final long INTERVAL_FIFTEEN_MINUTES = 15 * 60 * 1000;
    public static final long INTERVAL_HALF_HOUR = 2*INTERVAL_FIFTEEN_MINUTES;
    public static final long INTERVAL_HOUR = 2*INTERVAL_HALF_HOUR;
    public static final long INTERVAL_HALF_DAY = 12*INTERVAL_HOUR;
    public static final long INTERVAL_DAY = 2*INTERVAL_HALF_DAY;


    public  enum FileType {
        HTML(".html","text/html"),
        FB2(".fb2",null);
        public final String ext;
        public final String mime;

        FileType(String ext,String mime) {
            this.ext=ext;
            this.mime = mime;
        }
    }


    public abstract String getFirstMirror();
    public abstract ProxyData getProxy();

    public abstract  File getDataDirectory();//where to store books
    public abstract String getCollationRule();//get Collation rule string
    public abstract boolean isUpdateDelay();//make delay after each author update

    /**
     *
     * @return day number to live book into store
     */
    public abstract String getBookLifeTime();
    public abstract FileType getFileType();

    /**
     *  need we download new book during update process or not
     * @return true make download book
     */
    public abstract boolean getAutoLoadFlag();

    /**
     * Limit book file life time or not
     * @return true make life time limitation
     */
    public abstract boolean getLimitBookLifeTimeFlag();

    /**
     * Setting file to store book content
     * making parent directories if need
     *
     * @param book Book object to get File for
     * @return  File object to sore book to
     */

    public File getBookFile(Book book, FileType fileType) {
        String ff;
        if (book.isPreserve()){
            SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT_BOOK_FILE);
            ff=  BOOKS_DIR +sep+    book.getUri()    + "/" +   df.format(Calendar.getInstance().getTime())+ fileType.ext;
        }
        else {
            ff=  BOOKS_DIR +sep+    book.getUri()    +      fileType.ext;
        }


        File ss = new File(getDataDirectory(), ff);
        File pp = ss.getParentFile();
        boolean res =pp.mkdirs();
        Log.d(DEBUG_TAG, "getBookFile: parent directory Path: >" + pp.getAbsolutePath() + "< result is: " + res);
        return ss;

    }
    /**
     * Return absolute path data directory preference
     * get Default directory as SD-path + Samlib-Info
     * <p/>
     * Create if need
     *
     * @return Absolute path to the data directory
     */
    public String getDataDirectoryPath() {

        return getDataDirectory().getAbsolutePath();
    }


    /**
     * Create directory to store many versions for the book
     * Move existing version into the directory
     * @param book Book object
     */
    public void makePreserved(Book book){
        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT_BOOK_FILE);

        File dir =  new File(getDataDirectory(), BOOKS_DIR +sep+    book.getUri()    );
        boolean resMk= dir.mkdirs();
        boolean resEx = dir.exists();
        Log.d(DEBUG_TAG,"makePreserved: directory path:  >" +dir.getAbsolutePath() + "<   created = "+resMk+"  exist = "+resEx);


        File old = getBookFile(book,book.getFileType());
        if (old.exists()){
            Date lm = Calendar.getInstance().getTime();
            lm.setTime(old.lastModified());
            old.renameTo(new File(dir,df.format(lm)+book.getFileType().ext));
        }

    }

    /**
     * get All version for book files for read selection
     * @param book Book object
     * @return List of all versions of file
     */
    public  String[] getBookFileVersions(Book book){
        File dir =  new File(getDataDirectory(), BOOKS_DIR +sep+    book.getUri()    );
        List<String> files = new ArrayList<>();
        for (String fn : dir.list()){
            if (fn.endsWith(book.getFileType().ext)){
                files.add(fn);
            }
        }
//        if (files.isEmpty()){
//            return null;
//        }
        return files.toArray(new String[files.size()]);
    }


    /**
     * get Book file to read it
     * @param book Book object
     * @param fileType Type of file to opent
     * @return file object of book
     */
    public File getBookFile4Read(Book book,FileType fileType){
        if (book.isPreserve()){//choose latest version to read
            File dir =  new File(getDataDirectory(), BOOKS_DIR +sep+    book.getUri()    );
            File res = null;
            long lastmod=0L;
            for (String fn : dir.list()){
                if (fn.endsWith(fileType.ext)){
                    File file = new File(dir,fn);
                    Log.i(DEBUG_TAG,"test file "+fn+" - "+file.getAbsolutePath());
                    if (file.lastModified()>lastmod){
                        lastmod=file.lastModified();
                        res=file;
                    }
                }
            }//file cycle
            return res;
        }
        else {
            return getBookFile(book,fileType);//we have the only version just open it
        }

    }
    /**
     * Get URL to open book for offline reading
     * @return construct URL to start external program for offline reading
     */
    public String getBookFileURL(Book book) {
        return "file://" + getBookFile4Read(book, book.getFileType()).getAbsolutePath();
    }

    /**
     * Delete book file with expired life time
     * For book clean up usage only
     * @param file Book file
     */
    void checkDeleteBook(File file) {

        String str = getBookLifeTime();

        int limit;
        try {
            limit = Integer.parseInt(str);
        } catch (NumberFormatException ex) {
            Log.e(DEBUG_TAG, "Error parse Auto-load limit: " + str, ex);
            return;
        }


        long interval = INTERVAL_DAY * limit;
        long curTime = Calendar.getInstance().getTimeInMillis();

        //Log.d("checkDeleteBook", file.getAbsolutePath());
        if ((curTime - file.lastModified()) > interval) {
            Log.i("checkDeleteBook", "delete book: " + file.getAbsolutePath());
            if (!file.delete()) {
                Log.e(DEBUG_TAG, "Can not delete the book: " + file.getAbsolutePath());
            }
        }

    }

    /**
     * Get URL to open book for offline reading
     * To read  particular version of file
     * @param book Book object
     * @param file version file name
     * @return file URL to READ
     */
    public String getBookFileURL(Book book,String file) {
        File dir =  new File(getDataDirectory(), BOOKS_DIR +sep+    book.getUri()    );
        File f=new File(dir,file);
        return "file://" +f.getAbsolutePath();
    }
    /**
     * Clean downloaded files of any types
     * Find all book for read and delete them
     *
     * @param book  Book object
     */
    public void cleanBookFile(Book book){
        for (AbstractSettings.FileType ft : AbstractSettings.FileType.values()){
            File ff = getBookFile4Read(book, ft);

            if (ff!=null && ff.exists()) {
                ff.delete();
            }
        }
    }




}
