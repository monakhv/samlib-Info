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
package monakhv.android.samlib.data;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import monakhv.android.samlib.sql.AuthorController;
import monakhv.android.samlib.sql.SQLController;
import monakhv.android.samlib.sql.entity.Author;
import monakhv.android.samlib.sql.entity.Book;
import monakhv.android.samlib.tasks.AddAuthor;

/**
 *
 * @author monakhv
 */
public class DataExportImport {

    private static final String DATE_FORMAT = "dd-MM-yyyy";
    private static final String DATE_FORMAT_DEBUG = "dd-MM-yyyy HH:mm:ss";
    private static final String DEBUG_TAG = "DataExportImport";
    private static final String BACKUP_DIR = "//SamLib-Info//";
    private static final String BOOKS_DIR = "Book//";
    private static final String DB_EXT = ".db";
    private static final String DB_PREFIX = SQLController.DB_NAME;
    private static final String DEBUG_FILE = SQLController.DB_NAME + ".log";
    private static final File SD = Environment.getExternalStorageDirectory();
    private static final File backupDIR = new File(SD, BACKUP_DIR);
    private static final String TXT_PREFIX = "Authors";
    private static final String TXT_EXT = ".txt";
    
    /**
     * Setting file to store book content 
     * making parent directories if need
     * 
     * @param book
     * @return 
     */
    public static File _getBookFile(Book book){
                
        String ff=  BOOKS_DIR +"/"+    book.getUri()    +      ".html";
        
        File ss = new File(backupDIR, ff);
        File pp = ss.getParentFile();
        pp.mkdirs();
        //Log.d(DEBUG_TAG, "Path: "+ss.getAbsolutePath());
        return ss;
    }
    public static void findDeleteBookFile(SettingsHelper settings) {
        findDeleteBookFile( settings, new File(backupDIR,BOOKS_DIR) );
    }
    private static void findDeleteBookFile(SettingsHelper settings, File dir){
        
        
        File[] files =dir.listFiles();
        if (files == null){
            return;
        }
        for (File file : files){
            if ( file.isDirectory()){
                findDeleteBookFile(settings, file);
            }
            else {
                settings.checkDeleteBook(file);
            }
            
        }
    }
    
    public static String exportDB(Context context) {

        String backupDBPath = null;
        try {



            boolean mkres = backupDIR.mkdir();
            if (! mkres){
                Log.e(DEBUG_TAG, "Can not  create directory "+backupDIR.toString() );
            }
            
            if (backupDIR.canWrite()) {
                backupDBPath = DB_PREFIX + "_" + getTimesuffix() + DB_EXT;
                File currentDB = context.getDatabasePath(DB_PREFIX);
                File backupDB = new File(backupDIR, backupDBPath);

                Log.d(DEBUG_TAG, "Copy to: " + backupDB.getAbsolutePath() + "   Can write: " + backupDB.canWrite());

                fileCopy(currentDB, backupDB);

            }
            else {
                 Log.e(DEBUG_TAG, "Can not write to "+backupDIR.toString() );
            }
        } catch (Exception e) {
            Log.e(DEBUG_TAG, "Error to Copy DB: ", e);
            return null;
        }
        return backupDBPath;
    }

    /**
     * Copy list of author's URLs to file and return the file name
     *
     * @param applicationContext
     * @return
     */
    public static String exportAuthorList(Context applicationContext) {
        String backupTxtPath = null;
        try {
            backupDIR.mkdir();
            if (backupDIR.canWrite()) {
                backupTxtPath = TXT_PREFIX + "_" + getTimesuffix() + TXT_EXT;
                File backupTxt = new File(backupDIR, backupTxtPath);

                BufferedWriter bw = new BufferedWriter(new FileWriter(backupTxt));

                AuthorController sql = new AuthorController(applicationContext);

                List<Author> authors = sql.getAll();
                for (Author a : authors) {
                    bw.write(a.getUrl().toString());
                    bw.newLine();
                }
                bw.flush();
                bw.close();


            }


        } catch (Exception e) {
            Log.e(DEBUG_TAG, "Error export author urls: ", e);
            return null;
        }
        return backupTxtPath;

    }

    /**
     * Scan directory and return all files can be used to import DB from
     *
     * @param context
     * @return arrays of file names
     */
    public static String[] getFilesToImportDB(Context context) {
        List<String> files = new ArrayList<String>();
        backupDIR.mkdir();
        for (String file : backupDIR.list()) {
            if (file.startsWith(DB_PREFIX) && file.endsWith(DB_EXT)) {
                files.add(file);
            }
        }

        String[] res = new String[files.size()];
        return files.toArray(res);
    }

    public static String[] getFilesToImportTxt(Context context) {
        List<String> files = new ArrayList<String>();
        backupDIR.mkdir();
        for (String file : backupDIR.list()) {
            if (file.startsWith(TXT_PREFIX) && file.endsWith(TXT_EXT)) {
                files.add(file);
            }
        }

        String[] res = new String[files.size()];
        return files.toArray(res);
    }

    /**
     * Just copy file <b>src</b> to file <b>dst</b>
     *
     * @param srcFile
     * @param dstFile
     * @throws FileNotFoundException
     * @throws IOException
     */
    private static void fileCopy(File srcFile, File dstFile) throws FileNotFoundException, IOException {
        if (srcFile.exists()) {
            FileChannel src = new FileInputStream(srcFile).getChannel();
            FileChannel dst = new FileOutputStream(dstFile).getChannel();
            dst.transferFrom(src, 0, src.size());
            src.close();
            dst.close();
        }

    }

    /**
     * Replace working DB by DM from backup
     *
     * @param context
     * @param fileToImport backup db used to import from
     * @return
     */
    public static boolean importDB(Context context, String fileToImport) {
        File currentDB = context.getDatabasePath(DB_PREFIX);
        File backupDB = new File(backupDIR, fileToImport);
        try {
            fileCopy(backupDB, currentDB);
        } catch (FileNotFoundException ex) {
            Log.e(DEBUG_TAG, "Error to Import DB: ", ex);
            return false;
        } catch (IOException ex) {
            Log.e(DEBUG_TAG, "Error to Import DB: ", ex);
            return false;
        }
        return true;

    }

    private static String getTimesuffix() {
        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
        Date date = Calendar.getInstance().getTime();
        return df.format(date);
    }

    public static boolean importAuthorList(Context applicationContext, String fileToImport) {
        File backupTxt = new File(backupDIR, fileToImport);
        List<String> urls = new ArrayList<String>();
        try {
            BufferedReader in = new BufferedReader(new FileReader(backupTxt));


            String inputLine = in.readLine();
            while (inputLine != null) {
                urls.add(inputLine);
                inputLine = in.readLine();
            }

        } catch (FileNotFoundException ex) {
            Log.e(DEBUG_TAG, "Error Import URL list", ex);
            return false;
           
        } catch (IOException ex) {
            Log.e(DEBUG_TAG, "Error Import URL list ", ex);
            return false;
        }
        
        if (!urls.isEmpty()){
            AddAuthor adder = new AddAuthor(applicationContext);
            adder.execute(urls.toArray(new String[urls.size()]));
        }
        
        return true;
        
    }

    /**
     * Log output - do not take into account DEBUG Flag
     *
     * @param tag debug tag
     * @param msg message
     * @param ex Exception
     */
    static void log(String tag, String msg, Exception ex) {
        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT_DEBUG);
        File save = new File(backupDIR, DEBUG_FILE);
        FileOutputStream dst;
        Date date = Calendar.getInstance().getTime();

        try {
            dst = new FileOutputStream(save, true);
            PrintStream ps = new PrintStream(dst);
            ps.println(df.format(date) + "  " + tag + " " + msg);
            if (ex != null) {
                ex.printStackTrace(ps);
            }
            ps.flush();
            dst.flush();
            ps.close();
            dst.close();
        } catch (Exception ex1) {
            Log.e(DEBUG_TAG, "Log save error", ex1);
        }

    }

    static void log(String tag, String msg) {
        log(tag, msg, null);
    }
}
