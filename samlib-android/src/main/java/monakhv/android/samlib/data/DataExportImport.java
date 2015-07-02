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

import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import monakhv.android.samlib.service.AuthorEditorServiceIntent;

import monakhv.android.samlib.sql.DatabaseHelper;
import monakhv.samlib.db.AuthorController;
import monakhv.samlib.db.SQLController;
import monakhv.samlib.db.entity.Author;
import monakhv.samlib.db.entity.Book;
import monakhv.samlib.db.entity.SamLibConfig;


/**
 *
 * @author monakhv
 */
public class DataExportImport {


    private static final String DATE_FORMAT = "dd-MM-yyyy";
    private static final String DEBUG_TAG = "DataExportImport";
    public static final String BOOKS_DIR = "Book//";
    private static final String DB_EXT = ".db";
    private static final String DB_PREFIX = SQLController.DB_NAME;

    private static final String TXT_PREFIX = "Authors";
    private static final String TXT_EXT      = ".txt";
    private static final String HTM_EXT     = ".htm";
    private static final String HTML_EXT   = ".html";

    private SettingsHelper settingsHelper;
    private File backupDIR;
    private Context context;
    public  DataExportImport(Context context){
        this.context=context;
        settingsHelper = new SettingsHelper(context);
        backupDIR=settingsHelper.getDataDirectory();
    }
    



    /**
     * Test whether file for the book is fresh enought
     *
     *
     * @return true if we need update file
     */
    public boolean needUpdateFile(Book book) {

        File ff = settingsHelper.getBookFile4Read(book, book.getFileType());
        switch (book.getFileType()){
            case HTML:
                return ff==null || !ff.exists() || ff.lastModified() < book.getModifyTime();
            case FB2:
                if (ff.exists()){
                    return  ff.lastModified() < book.getModifyTime();
                }
                else {
                    book.setFileType(SettingsHelper.FileType.HTML);
                    ff = settingsHelper.getBookFile4Read(book,book.getFileType());
                    return !ff.exists() || ff.lastModified() < book.getModifyTime();
                }
            default:
                throw new UnsupportedOperationException();
        }



    }

    public  void findDeleteBookFile() {
        findDeleteBookFile(  new File(backupDIR,BOOKS_DIR) );
    }
    private  void findDeleteBookFile( File dir){
        
        
        File[] files =dir.listFiles();
        if (files == null){
            return;
        }
        for (File file : files){
            if ( file.isDirectory()){
                findDeleteBookFile( file);
            }
            else {
                settingsHelper.checkDeleteBook(file);
            }
            
        }
    }
    public static File getDataBase(Context context){
        return context.getDatabasePath(DB_PREFIX);
    }
    
    public String exportDB() {

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
     *
     * @return File Name where the list of urls is stored
     */
    public  String exportAuthorList(DatabaseHelper helper) {
        String backupTxtPath = null;
        try {
            @SuppressWarnings("UnusedDeclaration")
            boolean mkdir = backupDIR.mkdir();
            if (backupDIR.canWrite()) {
                backupTxtPath = TXT_PREFIX + "_" + getTimesuffix() + TXT_EXT;
                File backupTxt = new File(backupDIR, backupTxtPath);

                BufferedWriter bw = new BufferedWriter(new FileWriter(backupTxt));

                for (String u : getAuthorUrls(helper)) {
                    bw.write(u);
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
    public List<String> getAuthorUrls(DatabaseHelper helper){
        List<String> res = new ArrayList<String>();
        AuthorController sql = new AuthorController(helper);
        List<Author> authors = sql.getAll();
        for (Author a : authors) {
            res.add(a.getUrlForBrowser(new SettingsHelper(context)));
        }
        return res;
    }

    /**
     * Scan directory and return all files can be used to import DB from
     *
     *
     * @return arrays of file names
     */
    @SuppressWarnings("UnusedParameters")
    public  String[] getFilesToImportDB() {
        List<String> files = new ArrayList<String>();
        @SuppressWarnings("UnusedDeclaration")
        boolean re = backupDIR.mkdir();
        for (String file : backupDIR.list()) {
            if (file.startsWith(DB_PREFIX) && file.endsWith(DB_EXT)) {
                files.add(file);
            }
        }

        String[] res = new String[files.size()];
        return files.toArray(res);
    }

    @SuppressWarnings("UnusedParameters")
    public  String[] getFilesToImportTxt() {
        List<String> files = new ArrayList<String>();
        @SuppressWarnings("UnusedDeclaration")
          boolean re = backupDIR.mkdir();
        for (String file : backupDIR.list()) {
            if  (file.endsWith(TXT_EXT)   ||     file.endsWith(HTM_EXT)|| file.endsWith(HTML_EXT)   ) {
                files.add(file);
            }
        }

        String[] res = new String[files.size()];
        return files.toArray(res);
    }

    /**
     * Just copy file <b>src</b> to file <b>dst</b>
     *
     * @param srcFile source file
     * @param dstFile target file
     *
     * @throws IOException
     */
    private static void fileCopy(File srcFile, File dstFile) throws  IOException {
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
     *
     * @param fileToImport backup db used to import from
     * @return true if success
     */
    public  boolean importDB( String fileToImport) {
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

    public  boolean importAuthorList(String fileToImport) {
        File backupTxt = new File(backupDIR, fileToImport);
        ArrayList<String> urls = new ArrayList<String>();
        try {
            BufferedReader in = new BufferedReader(new FileReader(backupTxt));


            String inputLine = in.readLine();
            while (inputLine != null) {
                String resstr =  SamLibConfig.getParsedUrl(inputLine);
                if (resstr != null){
                    urls.add(resstr);
                }                
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
            AuthorEditorServiceIntent.addAuthor(context,urls);
        }
        
        return true;
        
    }



}
