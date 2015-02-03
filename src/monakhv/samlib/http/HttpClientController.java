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
package monakhv.samlib.http;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import monakhv.android.samlib.data.DataExportImport;
import monakhv.android.samlib.data.SettingsHelper;
import monakhv.android.samlib.exception.SamlibParseException;
import monakhv.android.samlib.exception.BookParseException;
import monakhv.android.samlib.exception.SamLibIsBusyException;
import monakhv.android.samlib.exception.SamLibNullAuthorException;
import monakhv.android.samlib.sql.entity.Author;
import monakhv.android.samlib.sql.entity.AuthorCard;
import monakhv.android.samlib.sql.entity.Book;
import monakhv.android.samlib.sql.entity.SamLibConfig;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

/**
 *
 * @author Dmitry Monakhov
 *
 * The Class make all internet connection for SamLib Info project. Must be call
 * from Async tasks or Services only! Have 4 main method
 *
 * - addAuthor to add new Author to data base. The method is used by AuthorEditorServiceIntent
 *
 * - getAuthorByURL get Author object using http connection.The method is
 * used by Update service
 * - downloadBook to download book content to file in
 * HTML from. It is used by DownloadBook service
 * -searchAuthors used by SearchAuthor async task
 */
public class HttpClientController {
    public interface PageReader {
        public  String doReadPage(InputStream in) throws IOException ;
    }

    public static final int RETRY_LIMIT = 5;
    public static final int CONNECTION_TIMEOUT = 10000;
    public static final int READ_TIMEOUT = 10000;
    public static final String ENCODING = "windows-1251";
    protected static final String USER_AGENT = "Android reader";
    private static final String DEBUG_TAG = "HttpClientController";
    private  HttpHost proxy = null;
    private  AuthScope scope = null;
    private  UsernamePasswordCredentials pwd = null;
    private static HttpClientController instance = null;
    private final SamLibConfig slc;
    private final DataExportImport dataExportImport;
    private final SettingsHelper settingsHelper;

    public static HttpClientController getInstance(Context context) {
        if (instance == null){
            instance = new HttpClientController(context);
        }
        
        return instance;
    }

    private HttpClientController(Context context) {
        slc = SamLibConfig.getInstance(context);
        dataExportImport = new DataExportImport(context);
        settingsHelper = new SettingsHelper(context);
        settingsHelper.setProxy(this);
    }

    /**
     * Construct Author object using reduced.
     * URL Internet connection is made using set of mirrors
     *
     * This is the method for update service
     *
     * @param link reduced URL
     * @return Author object
     * @throws java.io.IOException
     * @throws monakhv.android.samlib.exception.SamlibParseException
     */
    public Author getAuthorByURL(String link) throws IOException, SamlibParseException {
        Author a = new Author();
        a.setUrl(link);
        String str = getURL(slc.getAuthorRequestURL(a), new StringReader());

        parseAuthorData(a, str);
        return a;
    }

    /**
     * Create Author object using internet data and reduced url string. 
     * The same as getAuthorByURL but calculate author name for use in addAuthor task
     * Internet connection is made using set of mirrors. This is the method for
     * AddAuthor task
     *
     * @param link reduced url
     * @return Author object
     * @throws IOException
     * @throws SamlibParseException
     */
    public Author addAuthor(String link) throws IOException, SamlibParseException {
        Author a = getAuthorByURL(link);
        a.extractName();
        return a;
    }

    /**
     * Save book to appropriate file and make file transformation to make it
     * readable by android applications like ALRead and CoolReader.
     * Internet connection is made using set of mirrors.
     *
     * This is the method for DownloadBook service
     *
     * @param book the book to download
     * @throws IOException connection problem occurred
     * @throws SamlibParseException remote host return status other then 200
     */
    public void downloadBook(Book book) throws IOException, SamlibParseException {
        File f = dataExportImport.getBookFile(book, book.getFileType());
        PageReader reader;
        switch (book.getFileType()){
            case HTML:
                reader=new TextFileReader(f);
                getURL(slc.getBookUrl(book), reader);
                SamLibConfig.transformBook(f);
                break;
            case FB2:
                reader=new Fb2ZipReader(f);
                getURL(slc.getBookUrl(book), reader);
                break;
            default:
                throw new IOException();
        }


    }
    /**
     * Making author search
     * @param pattern author name pattern to search
     * @param page number of page
     * @return Search Result
     * @throws IOException 
     * @throws monakhv.android.samlib.exception.SamlibParseException 
     */
    public HashMap<String, ArrayList<AuthorCard>> searchAuthors(String pattern, int page) throws IOException, SamlibParseException{
        String str;
        try {
            str = getURL(slc.getSearchAuthorURL(pattern, page), new StringReader());
        }
        catch (NullPointerException ex){
            throw new SamlibParseException("Pattern: "+pattern);
        }

        return parseSearchAuthorData(str);
    }

    /**
     * Make http connection and begin download data using list of mirrors URL
     *
     * @param urls list of mirrors URL
     * @param reader  file to download data to can be null
     * @return downloaded data in case file is null
     * @throws IOException connection problem
     * @throws SamlibParseException remote host return status other then 200
     */
    private String getURL(List<String> urls,PageReader reader) throws IOException, SamlibParseException {
        String res = null;
        IOException exio = null;
        SamlibParseException exparse = null;
        for (String surl : urls) {
            Log.i(DEBUG_TAG, "using urls: "+surl);
            settingsHelper.log(DEBUG_TAG, "using urls: " + surl);
            exio = null;
            exparse = null;
            try {
                URL url = new URL(surl);
                res = _getURL(url, reader);
            } catch (IOException e) {
                slc.flipOrder();
                exio = e;
                Log.e(DEBUG_TAG, "IOException: " + surl, e);
                settingsHelper.log(DEBUG_TAG, "IOException: " + surl, e);
            } catch (SamlibParseException e) {
                slc.flipOrder();
                exparse = e;
                Log.e(DEBUG_TAG, "AuthorParseException: " + surl, e);
                settingsHelper.log(DEBUG_TAG, "AuthorParseException: " + surl, e);
            }

            if (exio == null && exparse == null) {
                return res;
            }
        }
        if (exio != null) {
            throw exio;
        } else {
            throw exparse;
        }
    }

    /**
     * Row method to make http connection and begin download data Take into
     * account 503 return status make retry after one (1) second of sleep. Call
     * only by _getURL. Make internal call of __getURL
     *
     * @param url URL to download from
     * @param reader File to download to, can be null
     * @return Download data if "f" is null
     * @throws IOException connection problem
     * @throws SamlibParseException remote host return status other then 200 ad
     * 503
     */
    private String _getURL(URL url, PageReader reader) throws IOException, SamlibParseException {
        String res = null;
        boolean retry = true;
        int loopCount = 0;
        while (retry) {
            try {
                res = __getURL(url, reader);
                retry = false;
            } catch (SamLibIsBusyException ex) {
                loopCount++;
                Log.w(DEBUG_TAG, "Retry number: " + loopCount + "  sleep 1 second");
                settingsHelper.log(DEBUG_TAG, "Retry number: " + loopCount + "  sleep 1 second");
                try {
                    TimeUnit.SECONDS.sleep(loopCount);
                } catch (InterruptedException ex1) {
                    Log.e(DEBUG_TAG, "Sleep interapted: ", ex);
                }
                if (loopCount >= RETRY_LIMIT) {
                   // retry = false;
                    throw new IOException("Retry Limit exeeded");
                }
            }
        }
        return res;
    }

    /**
     * Very row method to make http connection and begin download data Call only
     * by _getURL
     *
     * @param url URL to download
     * @param reader File to download to can be null
     * @return Download data if "f" is null
     * @throws IOException connection problem
     * @throws SamLibIsBusyException host return 503 status
     * @throws SamlibParseException host return status other then 200 and 503
     */
    private String __getURL(URL url, PageReader reader) throws IOException, SamLibIsBusyException, SamlibParseException {

        HttpGet method = new HttpGet(url.toString());


        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, CONNECTION_TIMEOUT);
        HttpConnectionParams.setSoTimeout(httpParams, READ_TIMEOUT);

        DefaultHttpClient httpclient = new DefaultHttpClient(httpParams);

        if (pwd != null && scope != null) {
            httpclient.getCredentialsProvider().setCredentials(scope, pwd);
        }

        if (proxy != null) {
            httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        }

        method.setHeader("User-Agent", USER_AGENT);
        method.setHeader("Accept-Charset", ENCODING);
        HttpResponse response;
        try {
            response = httpclient.execute(method);
            Log.d(DEBUG_TAG, "Status Response: " + response.getStatusLine().toString());
        }
        catch(NullPointerException ex){
            Log.e(DEBUG_TAG, "Connection Error", ex);
            throw new IOException("Connection error: "+url.toString());
        }
        int status = response.getStatusLine().getStatusCode();

        if (status == 503) {
            httpclient.getConnectionManager().shutdown();
            throw new SamLibIsBusyException("Need to retryException ");
        }
        if (status != 200) {
            httpclient.getConnectionManager().shutdown();
            throw new SamlibParseException("URL:"+url.toString()+"  status code: " + status);
        }

        String result = reader.doReadPage(response.getEntity().getContent());
        httpclient.getConnectionManager().shutdown();
        return result;

    }

    public  void setProxy(String host, int port, String user, String password) {
        proxy = new HttpHost(host, port);
        scope = new AuthScope(host, port);
        pwd = new UsernamePasswordCredentials(user, password);


    }

    public  void cleanProxy() {
        proxy = null;
        pwd = null;
        scope = null;
    }



    /**
     * Parse String data to load Author object 
     * @param a Author object to load data to
     * @param text String data to parse
     * 
     * @throws SamlibParseException Error parsing
     */
    private static void parseAuthorData(Author a, String text) throws SamlibParseException {
        String[] lines = text.split("\n");

        for (String line : lines) {

            if (SamLibConfig.testSplit(line) < 9) {
                Log.e(DEBUG_TAG, "Line Book parse Error:  length=" + SamLibConfig.testSplit(line) + "   line: " + line + " lines: " + lines.length);
                throw new SamlibParseException("Line Book parse Error:  length=" + SamLibConfig.testSplit(line) + "   line: " + line + " lines: " + lines.length);
            }
            try {
                Book b = new Book(line);
                b.setAuthorId(a.getId());
                a.getBooks().add(b);
            } catch (BookParseException ex) {//parsing book update date handling
                //TODO: new put it to Book constructor
                Log.e(DEBUG_TAG, "Error parsing book: " + line + "  skip it.", ex);
            }
        }

    }

    private HashMap<String, ArrayList<AuthorCard>> parseSearchAuthorData(String text) throws  SamlibParseException {
        String[] lines = text.split("\n");
        HashMap<String, ArrayList<AuthorCard>> res = new HashMap<>();
         for (String line : lines) {
             if (SamLibConfig.testSplit(line) < 7){
                 Log.e(DEBUG_TAG, "Line Search parse Error:  length=" + SamLibConfig.testSplit(line) + "\nline: " + line + "\nlines: " + lines.length);
                 throw new SamlibParseException("Parse Search Author error\nline: "+line);
             }
             try {
                 AuthorCard card = new AuthorCard(line);
                String name = card.getName();
               
                if (res.containsKey(name)){
                    res.get(name).add(card);
                    
                }
                else {
                    ArrayList<AuthorCard> aa = new ArrayList<>();
                    aa.add(card);
                    res.put(name, aa);
                    
                } 
             }
             catch(SamLibNullAuthorException ex){
                 //Log.i(DEBUG_TAG,"Skip author with no book");
             }
                            
         }
         if (res.isEmpty()) {
            return null;
        }
        return res;
        
    }
}
