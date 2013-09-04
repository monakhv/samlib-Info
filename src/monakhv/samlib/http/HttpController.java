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
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import monakhv.android.samlib.exception.AuthorParseException;
import monakhv.android.samlib.exception.BookParseException;
import monakhv.android.samlib.exception.SamLibIsBusyException;
import monakhv.android.samlib.sql.entity.Author;
import monakhv.android.samlib.sql.entity.Book;

/**
 *
 * @author monakhv
 */
abstract public class HttpController {
    public  static final int CONNECTION_TIMEOUT=10000;
    public  static final int READ_TIMEOUT=10000;
    public  static final int RETRY_LIMIT = 5;
    //encoding returning by site depends on USER_AGENT
    protected static final String ENCODING    = "windows-1251";    
    protected static final String USER_AGENT  = "Android reader";
    private static final String DEBUG_TAG="HttpController";
    
    
 

    
    
    /**
     * Construct Author object using samlib URL
     *
     * @param link
     * @return
     */
    public Author getAuthorByURL(URL link) throws IOException, AuthorParseException {
        Author a = new Author();
        a.setUrl(link);
        String str = null;
        boolean retry = true;
        int loopCount = 0;
        while(retry){
            try {
                str = getAuthorPage(a);
                retry = false;
            }
            catch(SamLibIsBusyException ex){
                loopCount ++ ;
                Log.w(DEBUG_TAG, "Retry number: "+loopCount+"  sleep 1 second");
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException ex1) {
                    Log.e(DEBUG_TAG, "Sleep interapted: ",ex);
                }
                if (loopCount >= RETRY_LIMIT){
                    retry = false;
                    throw new IOException("Retry Limit exeeded");
                }
            }
        }
        
        parseData(a, str);
        return a;
    }

    /**
     * Update Author object using samlib data Does not call SQL store routine!!
     *
     * @param a
     * @return true if we need SQL update
     */
    public boolean updateAuthor(Author a) throws IOException, AuthorParseException {
        Author newA = getAuthorByURL(a.getUrl());

        return a.update(newA);

    }

    /**
     * Load Author html page maket
     *
     * @param a
     * @return
     */
    abstract protected String getAuthorPage(Author a) throws IOException,SamLibIsBusyException;

    private static void parseData(Author a, String text) throws AuthorParseException {
        String[] lines = text.split("\n");

        for (String line : lines) {
            
            if (Book.testSplit(line) < 9){
                Log.e(DEBUG_TAG, "Line Book parse Error:  length=" +Book.testSplit(line)+"   line: "+line);
                throw new AuthorParseException("Line Book parse Error:  length=" +Book.testSplit(line)+"   line: "+line);
            }
            try {
            Book b = new Book(line);
            b.setAuthorId(a.getId());
            a.getBooks().add(b);
            }
            catch (BookParseException ex){//parsing book update date handling
                //TODO: new put it to Book constructor
                Log.e(DEBUG_TAG, "Error paring book: " +line +"  skip it.", ex);
            }
        }
        a.extractName();

    }

    
    /**
     * Read buffer to string and return it
     *
     * @param in
     * @return
     * @throws IOException
     */
    protected static String doReadPage(BufferedReader in) throws IOException {
        StringBuilder sb = new StringBuilder();
        String inputLine = in.readLine();
        while (inputLine != null) {
            sb.append(inputLine).append("\n");
            inputLine = in.readLine();
        }
        return sb.toString();
    }
}
