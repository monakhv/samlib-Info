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

import android.util.Log;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;
import monakhv.android.samlib.exception.AuthorParseException;
import monakhv.android.samlib.exception.BookParseException;
import monakhv.android.samlib.exception.SamLibIsBusyException;
import monakhv.android.samlib.sql.entity.Author;
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
 * @author monakhv
 */
public class HttpClientController {

    public static final int RETRY_LIMIT = 5;
    public static final int CONNECTION_TIMEOUT = 10000;
    public static final int READ_TIMEOUT = 10000;
    protected static final String ENCODING = "windows-1251";
    protected static final String USER_AGENT = "Android reader";
    private static String DEBUG_TAG = "HttpClientController";
    private static HttpHost proxy = null;
    private static AuthScope scope = null;
    private static UsernamePasswordCredentials pwd = null;
    private static HttpClientController instance = null;

    public static HttpClientController getInstance() {
        if (instance == null) {
            instance = new HttpClientController();
        }
        return instance;
    }

    /**
     * Construct Author object using reduced URL
     *
     * @param link
     * @return
     */
    public Author getAuthorByURL(String link) throws IOException, AuthorParseException {
        Author a = new Author();
        a.setUrl(link);
        String str = getURL(a.getRequestURL(), null);
        
        parseData(a, str);
        return a;
    }

    /**
     * The same as getAuthorByURL but calculate author name for use in addAuthor task
     * 
     * @param link
     * @return
     * @throws IOException
     * @throws AuthorParseException 
     */
    public Author addAuthor(String link)  throws IOException, AuthorParseException{
        Author a = getAuthorByURL(link);
        a.extractName();
        return a;
    }
    /**
     * Save book to appropriate file and make file transformation to make it
     * readable by android web Client
     *
     * @param book
     * @throws IOException
     * @throws AuthorParseException
     */
    public void downloadBook(Book book) throws IOException, AuthorParseException {
        File f = book.getFile();
        BufferedWriter bw = new BufferedWriter(new FileWriter(f));
        
        getURL(book.getURL(), f);
        SamLibConfig.transformBook(f);
    }

    private String getURL(List<String> urls, File f) throws IOException,  AuthorParseException  {
        String res = null;
        Exception ex = null;
        for (String surl: urls){
            try {
                URL url = new URL(surl);
                res = _getURL(url, f);
            }
            catch(IOException e) {
                ex = e;
                Log.e(DEBUG_TAG, "IOException: "+surl, e);
            }
            catch(AuthorParseException e) {
                ex = e;
                 Log.e(DEBUG_TAG, "AuthorParseException: "+surl, e);
            }
            
            if (ex == null){
                return res;
            }
        }
        throw new IOException("URL Limit exeeded");
    }
    private String _getURL(URL url, File f) throws IOException,  AuthorParseException {
        String res = null;
        boolean retry = true;
        int loopCount = 0;
        while (retry) {
            try {
                res = __getURL(url, f);
                retry = false;
            } catch (SamLibIsBusyException ex) {
                loopCount++;
                Log.w(DEBUG_TAG, "Retry number: " + loopCount + "  sleep 1 second");
                try {
                    TimeUnit.SECONDS.sleep(loopCount);
                } catch (InterruptedException ex1) {
                    Log.e(DEBUG_TAG, "Sleep interapted: ", ex);
                }
                if (loopCount >= RETRY_LIMIT) {
                    retry = false;
                    throw new IOException("Retry Limit exeeded");
                }
            }
        }
        return res;
    }
    
    private String __getURL(URL url, File f) throws IOException, SamLibIsBusyException, AuthorParseException {

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
        HttpResponse response = httpclient.execute(method);
        Log.d(DEBUG_TAG, "Status Response: " + response.getStatusLine().toString());
        int status = response.getStatusLine().getStatusCode();

        if (status == 503) {
            httpclient.getConnectionManager().shutdown();
            throw new SamLibIsBusyException("Need to retryException ");
        }
        if (status != 200) {
            httpclient.getConnectionManager().shutdown();
            throw new AuthorParseException("Status code: " + status);
        }

        InputStream content = response.getEntity().getContent();
        BufferedReader in = new BufferedReader(new InputStreamReader(content, ENCODING));

        String result = doReadPage(in, f);
        httpclient.getConnectionManager().shutdown();
        return result;

    }

    public static void setProxy(String host, int port, String user, String password) {
        proxy = new HttpHost(host, port);
        scope = new AuthScope(host, port);
        pwd = new UsernamePasswordCredentials(user, password);


    }

    public static void cleanProxy() {
        proxy = null;
        pwd = null;
        scope = null;
    }

    /**
     * Read buffer to string and return it or to BufferWriter for book download
     * @param in
     * @param bw
     * @return
     * @throws IOException 
     */
    protected static String doReadPage(BufferedReader in, File f) throws IOException {
        BufferedWriter bw;
        if (f != null){
             bw = new BufferedWriter(new FileWriter(f));
        }
        else {
            bw = null;
        }
       
        StringBuilder sb = new StringBuilder();
        String inputLine = in.readLine();
        while (inputLine != null) {
            if (bw == null) {
                sb.append(inputLine).append("\n");

            } else {
                bw.write(inputLine);
                bw.newLine();
            }
            inputLine = in.readLine();
        }
        if (bw == null) {
            return sb.toString();
        } else {
            bw.flush();
            bw.close();
            return null;
        }

    }

    private static void parseData(Author a, String text) throws AuthorParseException {
        String[] lines = text.split("\n");

        for (String line : lines) {

            if (Book.testSplit(line) < 9) {
                Log.e(DEBUG_TAG, "Line Book parse Error:  length=" + Book.testSplit(line) + "   line: " + line + " lines: " + lines.length);
                throw new AuthorParseException("Line Book parse Error:  length=" + Book.testSplit(line) + "   line: " + line + " lines: " + lines.length);
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
}
