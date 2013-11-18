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

import android.util.Log;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
    public static final   String SPLIT = "\\|";//Use  To parse Book and author Card object data
    public static final String SLASH = "/";
    public static final   int       SEARCH_LIMIT=100;
    
    private static final  int       AUTHOR_PAGE_SIZE = 100;//page size for author search
    
    private static final SamIzdat[]   URLs = {SamIzdat.SamLib, SamIzdat.BudClub};//Samizdat mirrors. Order is important this is the order mirror is selected by
    
    private static final String DEBUG_TAG = "SamLibConfig";
    
    private static final String     URLPTR = "/\\w/\\w+/";
    private static final String     SAMLIB_PROTO = "http://";
    private static final String     TMPL_ANUM="_ANUM_";
    private static final String     TMPL_PAGE="_PAGE_";
    private static final String     TMPL_PAGELEN ="_PAGELEN_";
    private static final String     REQUEST_AUTHOR_DATA           = "/cgi-bin/areader?q=razdel&order=date&object=";
    private static final String     REQUEST_BOOK_TEXT                = "/cgi-bin/areader?q=book&object=";
    private static final String     REQUEST_AUTHOR_SEARCH      = "/cgi-bin/areader?q=alpha&anum=_ANUM_&page=_PAGE_&pagelen=_PAGELEN_";
    
    private static final String[]   ABC_LETTER = new String[]{
        "А", "Б", "В", "Г", "Д", "Е", "Ё", "Ж", "З", "И", "Й", "К", "Л",
        "М", "Н", "О", "П", "Р", "С", "Т", "У", "Ф", "Х", "Ц", "Ч",
        "Ш", "Щ", "Ъ", "Ы", "Ь", "Э", "Ю", "Я", "0", "1", "2", "3",
        "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F",
        "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R",
        "S", "T", "U", "V", "W", "X", "Y", "Z"};

    private static final String[] ABC_CODE = new String[]{
        "225", "226", "247", "231", "228", "229", "179", "246",
        "250", "233", "234", "235", "236", "237", "238", "239",
        "240", "242", "243", "244", "245", "230", "232", "227",
        "254", "251", "253", "255", "249", "248", "252", "224",
        "241", "048", "049", "050", "051", "052", "053", "054",
        "055", "056", "057", "065", "066", "067", "068", "069",
        "070", "071", "072", "073", "074", "075", "076", "077",
        "078", "079", "080", "081", "082", "083", "084", "085",
        "086", "087", "088", "089", "090"};
    private static final HashMap<String, String> ABC;

    static {
        ABC = new HashMap<String, String>();
        for (int i = 0; i < ABC_CODE.length; i++) {
            ABC.put(ABC_LETTER[i], ABC_CODE[i]);
        }
    }
        
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

        /**
         * Construct URL to get Author data
         * @param uu
         * @return 
         */
        private String getAuthorRequestURL(String uu) {
            return url + REQUEST_AUTHOR_DATA +uu;

        }
        /**
         * Construct URL to download the book
         * @param uu
         * @return 
         */
        private String getBookURL(String uu){
            return url+REQUEST_BOOK_TEXT+uu;
        }
        /**
         * Construct URL to search Author
         * 
         * @param pattern
         * @param page
         * @return 
         */
        private String getSearchAuthorURL(String pattern,int page){
            Log.i(DEBUG_TAG, "Got pattern: "+pattern);
            String res = url+REQUEST_AUTHOR_SEARCH;
            Log.i(DEBUG_TAG, "Template string: "+res);
            String first = pattern.substring(0, 1);
            first = first.toUpperCase();
            Log.i(DEBUG_TAG, "The first letter "+first);
            Log.i(DEBUG_TAG, "The code "+ABC.get(first));
            res = res.
                    replaceFirst(TMPL_ANUM, ABC.get(first)).
                    replaceFirst(TMPL_PAGE, String.valueOf(page)).
                    replaceFirst(TMPL_PAGELEN, String.valueOf(AUTHOR_PAGE_SIZE));
            return res;
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

    /**
     * Return the list of request URLs to get Author data
     * 
     * @param a the author object to get data for
     * @return the list of url
     */
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
     * Return the list of request URLs to search authors
     * @param pattern
     * @param page number of page
     * @return 
     */
    public List<String> getSearchAuthorURL(String pattern,int page){
        List<String> res = new ArrayList<String>();
        Iterator<SamIzdat> itr = getIterator();
        while(itr.hasNext()){
            res.add(itr.next().getSearchAuthorURL(pattern, page));
        }
        return res;
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
    public static int testSplit(String str) {
        String[] arr = str.split(SamLibConfig.SPLIT);
        return arr.length;
    }
}
