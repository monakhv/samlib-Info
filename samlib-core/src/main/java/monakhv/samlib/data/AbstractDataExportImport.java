package monakhv.samlib.data;

import monakhv.samlib.db.AuthorController;
import monakhv.samlib.db.SQLController;
import monakhv.samlib.db.entity.Author;
import monakhv.samlib.db.entity.Book;
import monakhv.samlib.db.entity.SamLibConfig;
import monakhv.samlib.log.Log;

import java.io.*;
import java.nio.channels.FileChannel;
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
 * 15.07.15.
 */
public abstract class AbstractDataExportImport {
    private static final String DEBUG_TAG = "AbstractDataExportImport";
    private static final String DATE_FORMAT = "dd-MM-yyyy";
    private static final String DB_EXT = SQLController.DB_EXT;
    protected static final String DB_PREFIX = SQLController.DB_NAME;

    private static final String TXT_PREFIX = "Authors";
    private static final String TXT_EXT      = ".txt";
    private static final String HTM_EXT     = ".htm";
    private static final String HTML_EXT   = ".html";

    private AbstractSettings settings;
    private File backupDIR;


    public AbstractDataExportImport(AbstractSettings settings) {
        this.settings = settings;
        backupDIR=settings.getDataDirectory();
    }
    public  abstract File getDataBase();

    /**
     * Test whether file for the book is fresh enought
     *
     * @return true if we need update file
     */
    public boolean needUpdateFile(Book book) {

        File ff = settings.getBookFile4Read(book, book.getFileType());
        switch (book.getFileType()) {
            case HTML:
                return ff == null || !ff.exists() || ff.lastModified() < book.getModifyTime();
            case FB2:
                if (ff!= null) {
                    return ff.lastModified() < book.getModifyTime();
                } else {
                    book.setFileType(AbstractSettings.FileType.HTML);
                    ff = settings.getBookFile4Read(book, book.getFileType());
                    return ff == null || !ff.exists() || ff.lastModified() < book.getModifyTime();
                }
            default:
                throw new UnsupportedOperationException();
        }


    }
    public String exportDB() {

        return exportDB(backupDIR);
    }
    /**
     * Export current database
     * @param directory destination directory to export database to
     * @return tru if the import is successful
     */
    public String exportDB(File directory){
        String backupDBPath = null;
        try {


            if (directory.canWrite()) {
                backupDBPath = DB_PREFIX + "_" + getTimesuffix() + DB_EXT;
                File currentDB = getDataBase();
                File backupDB = new File(directory, backupDBPath);

                Log.d(DEBUG_TAG, "Copy to: " + directory.getAbsolutePath() + "   Can write: " + directory.canWrite());

                fileCopy(currentDB, backupDB);

            }
            else {
                Log.e(DEBUG_TAG, "Can not write to "+directory.toString() );
            }
        } catch (Exception e) {
            Log.e(DEBUG_TAG, "Error to Copy DB: ", e);
            return null;
        }
        return backupDBPath;
    }
    /**
     * Just copy file <b>src</b> to file <b>dst</b>
     *
     * @param srcFile source file
     * @param dstFile target file
     *
     * @throws IOException
     */
    private static void fileCopy(File srcFile, File dstFile) throws IOException {
        if (srcFile.exists()) {
            FileChannel src=null;
            FileChannel dst=null;
            try {
                src = new FileInputStream(srcFile).getChannel();
                dst = new FileOutputStream(dstFile).getChannel();
                dst.transferFrom(src, 0, src.size());
            }
            finally {
                close(src);
                close(dst);
            }


        }

    }
    private static void close(Closeable closeable){
        if (closeable != null){
            try {
                closeable.close();
            } catch (IOException e) {
                Log.e(DEBUG_TAG,"Close error",e);
            }
        }
    }
    private static String getTimesuffix() {
        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
        Date date = Calendar.getInstance().getTime();
        return df.format(date);
    }

    /**
     * Scan directory and return all files can be used to import DB from
     *
     *
     * @return arrays of file names
     */
    @SuppressWarnings("UnusedParameters")
    public  String[] getFilesToImportDB() {
        List<String> files = new ArrayList<>();
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
        List<String> files = new ArrayList<>();
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
     * Copy list of author's URLs to file and return the file name
     *
     *
     * @return File Name where the list of urls is stored
     */
    public String exportAuthorList(AuthorController authorController,File dir) {
        String backupTxtPath = null;
        BufferedWriter bw =null;
        try {

            if (dir.canWrite()) {
                backupTxtPath = TXT_PREFIX + "_" + getTimesuffix() + TXT_EXT;
                File backupTxt = new File(dir, backupTxtPath);

                bw = new BufferedWriter(new FileWriter(backupTxt));

                for (String u : getAuthorUrls(authorController)) {
                    bw.write(u);
                    bw.newLine();
                }
                bw.flush();
            }


        } catch (Exception e) {
            Log.e(DEBUG_TAG, "Error export author urls: ", e);
            return null;
        }
        finally {

            close(bw);
        }
        return backupTxtPath;

    }
    public  String exportAuthorList(AuthorController authorController) {
        return exportAuthorList(authorController,backupDIR);
    }
    private List<String> getAuthorUrls(AuthorController sql){
        List<String> res = new ArrayList<>();

        List<Author> authors = sql.getAll();
        for (Author a : authors) {
            res.add(a.getUrlForBrowser(settings));
        }
        return res;
    }

    /**
     * Replace working DB by DM from backup
     *
     *
     * @param fileToImport backup db used to import from
     * @return true if success
     */
    public  boolean importDB( String fileToImport) {

        File backupDB = new File(backupDIR, fileToImport);
        return importDB(backupDB);


    }

    /**
     * Import database
     *
     * @param backupDB database file to import
     * @return true if he import is successful
     */
    public  boolean importDB(File backupDB ) {
        File currentDB = getDataBase();

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




    /**
     * Delete books with expired LifeTime if it is restricted
     */
    public  void findDeleteBookFile() {
        findDeleteBookFile(  new File(backupDIR, AbstractSettings.BOOKS_DIR) );
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
                settings.checkDeleteBook(file);
            }

        }
    }

    public  ArrayList<String> importAuthorList(String fileToImport) {
        File backupTxt = new File(backupDIR, fileToImport);

        return importAuthorList(backupTxt);

    }
    public static  ArrayList<String> importAuthorList( File backupTxt) {

        ArrayList<String> urls = new ArrayList<>();
        BufferedReader in=null;
        try {
            in = new BufferedReader(new FileReader(backupTxt));
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
            return null;

        } catch (IOException ex) {
            Log.e(DEBUG_TAG, "Error Import URL list ", ex);
            return null;
        }
        finally {
            close(in);
        }

        return urls;

    }

}
