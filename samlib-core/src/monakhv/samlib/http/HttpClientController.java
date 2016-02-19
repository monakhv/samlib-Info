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


import java.io.*;

import java.net.Authenticator;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;


import monakhv.samlib.data.AbstractSettings;
import monakhv.samlib.db.entity.*;
import monakhv.samlib.exception.*;
import monakhv.samlib.log.Log;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.subjects.Subject;


/**
 * @author Dmitry Monakhov
 *         <p>
 *         The Class make all internet connection for SamLib Info project. Must be call
 *         from Async tasks or Services only! Have 4 main method
 *         <p>
 *         - addAuthor to add new Author to data base. The method is used by AuthorEditorServiceIntent
 *         <p>
 *         - getAuthorByURL get Author object using http connection.The method is
 *         used by Update service
 *         - downloadBook to download book content to file in
 *         HTML from. It is used by DownloadBook service
 *         -searchAuthors used by SearchAuthor async task
 */
public class HttpClientController {

    public interface PageReader {
        String doReadPage(InputStream in) throws IOException;

    }

    public static final int RETRY_LIMIT = 5;
    public static final int CONNECTION_TIMEOUT = 10000;
    public static final int READ_TIMEOUT = 10000;
    public static final String ENCODING = "windows-1251";
    protected static final String USER_AGENT = "Android reader";
    private static final String DEBUG_TAG = "HttpClientController";
    private ProxyData mProxyData;
    private final SamLibConfig mSamLibConfig;

    private final AbstractSettings mSettingsHelper;
    private Call mCall;


    public HttpClientController(AbstractSettings context) {
        mSamLibConfig = SamLibConfig.getInstance(context);

        mSettingsHelper = context;
        mProxyData = mSettingsHelper.getProxy();
        mProxyData = mSettingsHelper.getProxy();
        setProxyData(mProxyData);

    }

    public void cancelAll() {
        if (mCall != null) {
            mCall.cancel();
        }
    }

    /**
     * Construct Author object using reduced.
     * URL Internet connection is made using set of mirrors
     * <p>
     * This is the method for update service
     *
     * @param link reduced URL
     * @param a    Clear Author object
     * @return Author object
     * @throws java.io.IOException
     * @throws SamlibParseException
     */
    public Author getAuthorByURL(String link, Author a) throws IOException, SamlibParseException, SamlibInterruptException {

        a.setUrl(link);
        String str = getURL(mSamLibConfig.getAuthorIndexDate(a), new StringReader());

        parseAuthorIndexDateData(a, str);
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
    public Author addAuthor(String link, Author a1) throws IOException, SamlibParseException, SamlibInterruptException {
        Author a = getAuthorByURL(link, a1);
        a.extractName();
        return a;
    }

    /**
     * Save book to appropriate file and make file transformation to make it
     * readable by android applications like ALRead and CoolReader.
     * Internet connection is made using set of mirrors.
     * <p>
     * This is the method for DownloadBook service
     *
     * @param book the book to download
     * @throws IOException          connection problem occurred
     * @throws SamlibParseException remote host return status other then 200
     */
    public void downloadBook(Book book,Subject<Integer, Integer> subject) throws IOException, SamlibParseException, SamlibInterruptException {
        File f = mSettingsHelper.getBookFile(book, book.getFileType());
        PageReader reader;
        switch (book.getFileType()) {
            case HTML:
                reader = new TextFileReader(f,book.getSize(),subject);
                getURL(mSamLibConfig.getBookUrl(book), reader);
                SamLibConfig.transformBook(f);
                break;
            case FB2:
                reader = new Fb2ZipReader(f,book.getSize(),subject);
                getURL(mSamLibConfig.getBookUrl(book), reader);
                break;
            default:
                throw new IOException();
        }


    }

    /**
     * Making author search
     *
     * @param pattern author name pattern to search
     * @param page    number of page
     * @return Search Result
     * @throws IOException
     * @throws SamlibParseException
     */
    public HashMap<String, ArrayList<AuthorCard>> searchAuthors(String pattern, int page) throws IOException, SamlibParseException, SamlibInterruptException {
        String str;
        try {
            str = getURL(mSamLibConfig.getSearchAuthorURL(pattern, page), new StringReader());
        } catch (NullPointerException ex) {
            Log.w(DEBUG_TAG, "searchAuthors: Search error for pattern: " + pattern, ex);
            throw new SamlibParseException("Pattern: " + pattern);
        }

        return parseSearchAuthorData(str);
    }

    /**
     * Make http connection and begin download data using list of mirrors URL
     *
     * @param urls   list of mirrors URL
     * @param reader file to download data to can be null
     * @return downloaded data in case file is null
     * @throws IOException          connection problem
     * @throws SamlibParseException remote host return status other then 200
     */
    private String getURL(List<String> urls, PageReader reader) throws IOException, SamlibParseException, SamlibInterruptException {
        String res = null;
        IOException ioException = null;//fatalError
        SamlibParseException samlibParseException = null;//skip update for given author
        for (String sUrl : urls) {
            Log.i(DEBUG_TAG, "getURL: using urls: " + sUrl);

            ioException = null;
            samlibParseException = null;
            try {
                URL url = new URL(sUrl);
                res = _getURL(url, reader);
            } catch (InterruptedIOException e) {
                if (Thread.interrupted()) {
                    Log.i(DEBUG_TAG, "getURL: thread is interrupted throw SamlibInterruptException", e);

                    throw new SamlibInterruptException("getURL:InterruptedIOException");
                }
                if (e instanceof SocketTimeoutException) {
                    mSamLibConfig.flipOrder();
                    ioException = e;
                    Log.i(DEBUG_TAG, "getURL: SocketTimeoutException make flip", e);

                } else {
                    Log.i(DEBUG_TAG, "getURL: thread is NOT interrupted throw InterruptedIOException", e);

                    throw new InterruptedIOException("getURL:InterruptedIOException");
                }

            } catch (IOException e) {
                mSamLibConfig.flipOrder();
                ioException = e;
                if (Thread.interrupted()) {
                    Log.i(DEBUG_TAG, "getURL:1 thread is interrupted throw SamlibInterruptException", e);

                    throw new SamlibInterruptException("getURL:IOException");
                }

                Log.e(DEBUG_TAG, "getURL: IOException: " + sUrl, e);

            } catch (SamlibParseException e) {
                mSamLibConfig.flipOrder();
                samlibParseException = e;
                Log.e(DEBUG_TAG, "AuthorParseException: " + sUrl, e);

            }

            if (ioException == null && samlibParseException == null) {
                return res;
            }
        }
        if (ioException != null) {
            throw ioException;
        } else {
            throw samlibParseException;
        }
    }

    /**
     * Row method to make http connection and begin download data Take into
     * account 503 return status make retry after one (1) second of sleep. Call
     * only by _getURL. Make internal call of __getURL
     *
     * @param url    URL to download from
     * @param reader File to download to, can be null
     * @return Download data if "f" is null
     * @throws IOException          connection problem
     * @throws SamlibParseException remote host return status other then 200 ad
     *                              503
     */
    private String _getURL(URL url, PageReader reader) throws IOException, SamlibParseException, SamlibInterruptException {
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

                try {
                    TimeUnit.SECONDS.sleep(loopCount);
                } catch (InterruptedException ex1) {
                    Log.w(DEBUG_TAG, "_getURL: InterruptedException throw SamlibInterruptException");

                    throw new SamlibInterruptException("_getURL:Sleep interrupted");
                }
                if (loopCount >= RETRY_LIMIT) {
                    // retry = false;
                    Log.e(DEBUG_TAG, "_getURL: Retry Limit exceeded");

                    throw new IOException("Retry Limit exceeded");
                }
            }
        }
        return res;
    }

    /**
     * Very row method to make http connection and begin download data Call only
     * by _getURL
     *
     * @param url    URL to download
     * @param reader File to download to can be null
     * @return Download data if "f" is null
     * @throws IOException           connection problem
     * @throws SamLibIsBusyException host return 503 status
     * @throws SamlibParseException  host return status other then 200 and 503
     */
    private String __getURL(URL url, PageReader reader) throws IOException, SamLibIsBusyException, SamlibParseException {

        final OkHttpClient client;
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS);

        if (mProxyData != null) {
            mProxyData.applyProxy(builder);
        }
        client = builder.build();

        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .header("Accept-Charset", ENCODING)
                .header("Connection", "close")
                .tag(DEBUG_TAG)
                .build();


        mCall = client.newCall(request);


        Response response;
        try {
            response = mCall.execute();
            Log.d(DEBUG_TAG, "__getURL: Status Response: " + response.message());

        } catch (NullPointerException ex) {
            Log.e(DEBUG_TAG, "__getURL: Connection Error", ex);

            throw new IOException("Connection error: " + url.toString());
        }
        int status = response.code();
        Log.d(DEBUG_TAG, "__getURL: Status - " + status);


        if (status == 503) {

            throw new SamLibIsBusyException("Need to retryException ");
        }
        if (status != 200) {

            throw new SamlibParseException("URL:" + url.toString() + "  status code: " + status);
        }


        return reader.doReadPage(response.body().byteStream());

    }

    public void setProxyData(ProxyData proxy1) {
        mProxyData = proxy1;
        if (proxy1 == null) {
            cleanProxy();

        }
        //Authenticator.setDefault(proxy1.getAuthenticator());

    }

    private void cleanProxy() {
        Authenticator.setDefault(null);
        mProxyData = null;

    }


    /**
     * Parse String data to load Author object
     *
     * @param a    Author object to load data to
     * @param text String data to parse
     */
    private void parseAuthorIndexDateData(Author a, String text) {
        String[] lines = text.split("\n");

        String authorName = null;
        List<GroupBook> groups = new ArrayList<>();
        int iBooks = 0;
        for (String line : lines) {
            Matcher nameMatcher = SamLibConfig.AUTHOR_NAME_PATTERN.matcher(line);
            Matcher bookMatcher = SamLibConfig.BOOK_PATTERN.matcher(line);

            if ((authorName == null) && nameMatcher.find()) {
                authorName = nameMatcher.group(1);
                Log.i(DEBUG_TAG, "Name = " + authorName);
            }

            if (bookMatcher.find()) {
                ++iBooks;
                Book book = new Book(a, bookMatcher);
                GroupBook g = book.getGroupBook();
                if (!groups.contains(g)) {
                    groups.add(g);
                }
                a.getBooks().add(book);


                if (authorName != null) {
                    book.setAuthorName(authorName);
                }
            }

            //Log.i(DEBUG_TAG,line);
        }
        a.setGroupBooks(groups);
        Log.i(DEBUG_TAG, "Books = " + iBooks);

    }


    private HashMap<String, ArrayList<AuthorCard>> parseSearchAuthorData(String text) throws SamlibParseException {
        String[] lines = text.split("\n");
        HashMap<String, ArrayList<AuthorCard>> res = new HashMap<>();
        for (String line : lines) {
            if (SamLibConfig.testSplit(line) < 7) {
                Log.e(DEBUG_TAG, "Line Search parse Error:  length=" + SamLibConfig.testSplit(line) + "\nline: " + line + "\nlines: " + lines.length);
                throw new SamlibParseException("Parse Search Author error\nline: " + line);
            }
            try {
                AuthorCard card = new AuthorCard(line);
                String name = card.getName();

                if (res.containsKey(name)) {
                    res.get(name).add(card);

                } else {
                    ArrayList<AuthorCard> aa = new ArrayList<>();
                    aa.add(card);
                    res.put(name, aa);

                }
            } catch (SamLibNullAuthorException ex) {
                //Log.i(DEBUG_TAG,"Skip author with no book");
            }

        }
        if (res.isEmpty()) {
            return null;
        }
        return res;

    }
}
