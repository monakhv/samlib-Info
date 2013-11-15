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
package monakhv.android.samlib.sql.entity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author monakhv
 */
public class SamLibConfig {
    private static final SamIzdat[]   URLs = {SamIzdat.SamLib, SamIzdat.BudClub};//Samizdat mirrors. Order is important this is the order mirror is selected by
    
    private static final String     SLASH = "/";
    private static final String     URLPTR = "/\\w/\\w+/";
    private static final String     SAMLIB_PROTO = "http://";
    private static final String     REQUEST_AUTHOR_TEXTS = "/cgi-bin/areader?q=razdel&order=date&object=";
    private static final String     REQUEST_BOOK_TEXT = "/cgi-bin/areader?q=book&object=";
        
    private static SamLibConfig instance = null;
    private boolean order = true;//samlib is the first budclub is the second one
    private final LinkedList<SamIzdat> linkedSZ;
    
    
    public static SamLibConfig getInstance(){
        if (instance == null){
            instance = new SamLibConfig();
            instance.order=true;
        }        
        return instance;
    }
    
    
    private SamLibConfig(){
        linkedSZ = new LinkedList<SamIzdat>();
        linkedSZ.addAll(Arrays.asList(URLs));
        order = true;
    }
    public void flipOrder(){
        order = !order;
    }
    private Iterator<SamIzdat> getIterator(){
        if (order){
            return linkedSZ.listIterator();
        }
        else {
            return linkedSZ.descendingIterator();
        }
    }
    /**
     * Small Internal class to store Samizdat mirrors data
     */
        private static enum SamIzdat {
        SamLib("SamLib","http://samlib.ru"),
        BudClub("BudClub","http://budclub.ru");
        private final String url;
        private final String name;
        private final Pattern pattern;//search url pattern
        private SamIzdat(String name,String url) {
            this.url = url;
            this.name = name;
            pattern=Pattern.compile(".*("+url+"/\\w/\\w+)($|\\b)");
        }
        public Pattern getSearchPattern(){
            return pattern;
        }

        /**
         * Test whether URL has a form http://<url>/q/qqqq_qq_q/
         *
         * @param txt
         * @return
         */
        private boolean testFullUrl(String txt) {
            //All URL must be closed by /
            if (!txt.endsWith(SLASH)) {
                txt = txt + SLASH;
            }
            String ptr = url + URLPTR;

            return txt.matches(ptr);
        }

        private String getAuthorRequestURL(String uu) {
            return url + REQUEST_AUTHOR_TEXTS +uu;

        }
        private String getBookURL(String uu){
            return url+REQUEST_BOOK_TEXT+uu;
        }
    }
        //End SamIzdat class
        
        /**
         * Construct URL to open the book in WEB browser
         * 
         * @param book the Book object to open
         * @return 
         */
        public static String getBookUrlForBrowser(Book book){
            SamLibConfig slc=SamLibConfig.getInstance();
            return slc.getDefaultURL() + SLASH + book.getUri() + ".shtml";
        }
        /**
         * Construct URL to open the book in WEB browser and to store bookmark list
         * 
         * @param author
         * @return 
         */
        public static String getAuthorUrlForBrowser(Author author){
            SamLibConfig slc=SamLibConfig.getInstance();
            return slc.getDefaultURL() +  author.getUrl() ;
        }

    /**
     * Test whether URL has a form http://<url>/w/www_w_w/ Must be ended by /
     * Must be begin with one of the valid URL
     *
     * @param txt
     * @return
     */
    public static boolean testFullUrl(String txt) {
        for (SamIzdat sz : URLs) {           
            if (sz.testFullUrl(txt)) {
                return true;
            }
        }

        return false;
    }

    public static String getParsedUrl(String str){
        String res = null;
        for (SamIzdat sz : URLs) {       
            Matcher m = sz.getSearchPattern().matcher(str);
            if (m.find()){
                res = m.group(1);
                return res;
            }
        }
        
        return res;
    }
    /**
     * Take URL check syntax
     *
     * @param str reduced URL or NULL if the syntax is wrong
     * @return
     */
    public static String reduceUrl(String str) {
        if (str.startsWith(SAMLIB_PROTO)) {//full URL case
            for (SamIzdat sz : URLs) {
               
                if (sz.testFullUrl(str)) {
                    return str.replaceAll(sz.url, "");
                }
            }
            return null;
        } else {//reduced AUTHOR URL
            if (str.matches(URLPTR)) {//checking syntax
                return str;
            } else {
                return null;//wrong syntax retrn null
            }
        }

    }

    public List<String> getAuthorRequestURL(Author a) {
        List<String> res = new ArrayList<String>();
        Iterator<SamIzdat> itr = getIterator();
        while(itr.hasNext()){
            res.add(itr.next().getAuthorRequestURL(a.getUrl()));
        }
        return res;
    }
    
    public String getDefaultURL(){
        Iterator<SamIzdat> itr = getIterator();
        return itr.next().url;
    }

    /**
     * Get book url to download html content
     * @param b
     * @return 
     */
    public List<String> getBookUrl(Book b) {
        List<String> res = new ArrayList<String>();
        Iterator<SamIzdat> itr = getIterator();
        while(itr.hasNext()){
            res.add(itr.next().getBookURL(b.getUri()));
        }
        return res;
    }


    public static void transformBook(File orig) throws IOException {
        File tmp = new File(orig.getAbsoluteFile() + ".tmp");

        orig.renameTo(tmp);

        BufferedWriter bw = new BufferedWriter(new FileWriter(orig));
        BufferedReader br = new BufferedReader(new FileReader(tmp));

        String line = br.readLine();

        String[] str = line.split("\\|");
        bw.write("<html><head>");
        bw.write("<title>" + str[1] + "</title>");
        bw.write("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
        bw.write("</head><body>");
        bw.newLine();

        bw.write("<center><h3>" + str[0] + "</h3>");
        bw.write("<h2>" + str[1] + "</h2></center>");

        line = br.readLine();
        while (line != null) {
            bw.write(line);
            line = br.readLine();
        }
        bw.write("</body></html>");
        bw.flush();
        bw.close();
        br.close();

        tmp.delete();
    }
}
