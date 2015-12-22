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
package monakhv.samlib.db.entity;



import monakhv.samlib.data.AbstractSettings;
import monakhv.samlib.log.Log;

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
    public static final   String SLASH = "/";
    public static final   int      SEARCH_LIMIT=100;//maximum number of results can be returned by the search procedure
    public static final   int      SELECTED_BOOK_ID=-1;//Special id for selected book 
    public static final   int       TAG_AUTHOR_ALL     =-1;
    public static final   int       TAG_AUTHOR_NEW  =-2;
    public static final   int       TAG_AUTHOR_ID = -10;
    
    public static final String COLLATION_RULES_NEW = "&' '<'-'<'_'<','<';'<':'<'!'<'?'<'/'<'.'<0<1<2<3<4<5<6<7<8<9<a,A<b,B<c,C<d,D<ð,Ð<e,E<f,F<g,G<h,H<i,I<j,J<k,K<l,L<m,M<n,N<o,O"
                 + "<p,P<q,Q<r,R<s,S<t,T<u,U<v,V<w,W<x,X<y,Y<z,Z <а,А< б,Б<в,В<г,Г< д , Д<  е , Е<  ё , Ё< ж , Ж< з , З< и , И< й , Й< к , К< л ,Л<  м , М"
                 + "< н , Н< о , О< п , П< р , Р< с , С< т , Т< у , У< ф , Ф< х , Х< ц , Ц< ч , Ч< ш , Ш< щ , Щ< ъ , Ъ< ы , Ы< ь , Ь< э , Э< ю , Ю< я , Я";


    public static final String COLLATION_RULES_OLD = "<' '<'-'<'_'<','<';'<':'<'!'<'?'<'/'<'.'<0<1<2<3<4<5<6<7<8<9<a,A<b,B<c,C<d,D<ð,Ð<e,E<f,F<g,G<h,H<i,I<j,J<k,K<l,L<m,M<n,N<o,O"
                 + "<p,P<q,Q<r,R<s,S<t,T<u,U<v,V<w,W<x,X<y,Y<z,Z <а,А< б,Б<в,В<г,Г< д , Д<  е , Е<  ё , Ё< ж , Ж< з , З< и , И< й , Й< к , К< л ,Л<  м , М"
                 + "< н , Н< о , О< п , П< р , Р< с , С< т , Т< у , У< ф , Ф< х , Х< ц , Ц< ч , Ч< ш , Ш< щ , Щ< ъ , Ъ< ы , Ы< ь , Ь< э , Э< ю , Ю< я , Я";
    
    private static final  int      AUTHOR_PAGE_SIZE = 500;//page size for author search
    
//    private static final SamIzdat[] ForwardURLsOrder = {SamIzdat.SamLib, SamIzdat.BudClub};//Samizdat mirrors. Order is important this is the order mirror is selected by
//    private static final SamIzdat[] ReverseURLsOrder = {SamIzdat.BudClub,SamIzdat.SamLib };

    private static final SamIzdat[] ForwardURLOrder= {SamIzdat.SamLib, SamIzdat.ZhurnalLib};//Samizdat mirrors. Order is important this is the order mirror is selected by
    private static final SamIzdat[] ReverseURLOrder = {SamIzdat.ZhurnalLib,SamIzdat.SamLib };

    private static final SamIzdat[] AllUrl = {SamIzdat.SamLib,SamIzdat.ZhurnalLib,SamIzdat.BudClub};


    private static final String DEBUG_TAG = "SamLibConfig";
    
    private static final String     URLPTR = "/\\w/\\w+/";
    private static final String     SAMLIB_PROTO = "http://";
    private static final String     TMPL_ANUM="_ANUM_";
    private static final String     TMPL_PAGE="_PAGE_";
    private static final String     TMPL_PAGELEN ="_PAGELEN_";
    private static final String     REQUEST_AUTHOR_DATA           = "/cgi-bin/areader?q=razdel&order=date&object=";
    private static final String     REQUEST_BOOK_TEXT                = "/cgi-bin/areader?q=book&object=";
    private static final String     REQUEST_AUTHOR_SEARCH      = "/cgi-bin/areader?q=alpha&anum=_ANUM_&page=_PAGE_&pagelen=_PAGELEN_";
    private static final String     REQUEST_INDEXDATE="/indexdate.shtml";
    
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
        ABC = new HashMap<>();
        for (int i = 0; i < ABC_CODE.length; i++) {
            ABC.put(ABC_LETTER[i], ABC_CODE[i]);
        }
    }
        
    private static SamLibConfig instance = null;

    private final LinkedList<SamIzdat> linkedSZ;//actual list of Samizdat URLs
    private AbstractSettings settings;
    
    public static SamLibConfig getInstance(AbstractSettings settings){
        if (instance == null){
            instance = new SamLibConfig(settings);

        }        
        return instance;
    }
    
    
    private SamLibConfig(AbstractSettings settings){
        this.settings=settings;
        linkedSZ = new LinkedList<>();
        refreshData( );
    }

    /**
     * Load Samizdat data according to the preference data
     * We have two possible redefined orders
     */
    public void refreshData( ) {

        String fm =settings.getFirstMirror();
        linkedSZ.clear();
        if (fm.equals(SamIzdat.SamLib.getName())){
            linkedSZ.addAll(Arrays.asList(ForwardURLOrder));
        }
        else {
            linkedSZ.addAll(Arrays.asList(ReverseURLOrder));
        }

    }

    /**
     * Change order of the elements in the LinkedList the first element goes to the end of list
     */
    public void flipOrder(){
        SamIzdat  theFirst = linkedSZ.poll();
        linkedSZ.add(theFirst);
    }
    private Iterator<SamIzdat> getIterator(){
        return linkedSZ.listIterator();

    }
    /**
     * Small Internal class to store Samizdat mirrors data
     */
        private enum SamIzdat {
        SamLib("SamLib","samlib.ru","81.176.66.171"),
        BudClub("BudClub","budclub.ru","194.63.140.119"),
         ZhurnalLib("ZhurnalLib","zhurnal.lib.ru","81.176.66.169");
        private static final String ZIP =".zip" ;

        private final String name;
        private final Pattern pattern;//search url pattern
        private final String urlH;//Host URL for browser usage
        private final String urlIP;//For internal update usage
        SamIzdat(String name, String host, String ip) {

            this.name = name;

            urlH = SAMLIB_PROTO+host;
            urlIP=SAMLIB_PROTO+ip;
            pattern=Pattern.compile(".*("+urlH+"/\\w/\\w+)($|\\b)");
        }
        public String getName(){
            return name;
        }
        public Pattern getSearchPattern(){
            return pattern;
        }

        /**
         * Test whether URL has a form http://<url>/q/qqqq_qq_q/
         *
         * @param txt url to test
         * @return true id the success
         */
        private boolean testFullUrl(String txt) {
            //All URL must be closed by /
            if (!txt.endsWith(SLASH)) {
                txt = txt + SLASH;
            }
            String ptr = urlH + URLPTR;

            return txt.matches(ptr);
        }

        /**
         * Construct URL to get Author data
         * @param uu reduced author URL
         * @return  URL used to get author data from the site
         */
        private String getAuthorRequestURL(String uu) {
            return urlIP + REQUEST_AUTHOR_DATA +uu;

        }
        private String getAuthorIndexDate(String uu) {
            return urlIP + uu+REQUEST_INDEXDATE;
        }
        /**
         * Construct URL to download the book
         * @param uu book url
         * @return  URL to download the book
         */
        private String getBookURL(String uu,AbstractSettings.FileType fileType){
            switch (fileType){
                case HTML:
                    return urlIP+REQUEST_BOOK_TEXT+uu;
                case FB2:
                    return urlIP+SLASH+uu+fileType.ext+ZIP;
                default:
                    return null;
            }

        }
        /**
         * Construct URL to search Author
         * 
         * @param pattern string pattern to search
         * @param page number of page
         * @return  URL to make search
         */
        private String getSearchAuthorURL(String pattern,int page){
            //Log.i(DEBUG_TAG, "Got pattern: "+pattern);
            String res = urlIP+REQUEST_AUTHOR_SEARCH;
            //Log.i(DEBUG_TAG, "Template string: "+res);
            String first = pattern.substring(0, 1);
            first = first.toUpperCase();
            //Log.i(DEBUG_TAG, "The first letter "+first);
            //Log.i(DEBUG_TAG, "The code "+ABC.get(first));
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
         * @return  URL to open the book in browser
         */
        public  String getBookUrlForBrowser(Book book){

            return getDefaultURL() + SLASH + book.getUri() + ".shtml";
        }
        /**
         * Construct URL to open the book in WEB browser and to store bookmark list
         * 
         * @param author Author object
         * @return URL to open author page in browser
         */
        public  String getAuthorUrlForBrowser(Author author){

            return getDefaultURL() +  author.getUrl() ;
        }

    /**
     * Test whether URL has a form http://<url>/w/www_w_w/ Must be ended by /
     * Must be begin with one of the valid URL
     *
     * @param txt url to make test
     * @return true if success
     */
    /*
    public static boolean testFullUrl(String txt) {
        for (SamIzdat sz : URLs) {           
            if (sz.testFullUrl(txt)) {
                return true;
            }
        }

        return false;
    }
*/
    /**
     * Make parsed URL use for import bookmarks data into Database
     *
     *  See examples
     *  <PRE>
     *      Found Good SSN: http://samlib.ru/a/ab  --  http://samlib.ru/a/ab
     *      Found Good SSN: http://samlib.ru/a/ab/  --  http://samlib.ru/a/ab
     *      Found Good SSN: http://samlib.ru/a/ab/qwqwqwqw.html  --  http://samlib.ru/a/ab
     *      Found Good SSN: href = http://samlib.ru/a/ab  --  http://samlib.ru/a/ab
     *      Found Bad SSN: aaaa href = sdsds
     *      Found Bad SSN: /a/asasa_q
     *      Found Bad SSN: /a/asasa_q/
     *      Found Bad SSN: /a/asasa_q_e/
     *      Found Bad SSN: /asasas
     *      Found Bad SSN: /asasas/asasas/
     *      Found Good SSN: ><A HREF="http://samlib.ru/a/abwow_a_s/"  --  http://samlib.ru/a/abwow_a_s
     *  </PRE>
     *
     *
     * @param str  String to parse
     * @return parsed URL or null if Can not be parsed
     */
    public static String getParsedUrl(String str){
        String res ;
        for (SamIzdat sz : AllUrl) {
            Matcher m = sz.getSearchPattern().matcher(str);
            if (m.find()){
                res = m.group(1);
                return res;
            }
        }
        
        return null;
    }
    /**
     * Take URL check syntax
     *
     * @param str full URL String
     * @return reduced URL or NULL if the syntax is wrong
     */
    public static String reduceUrl(String str) {
         //All URL must be closed by /
        if (!str.endsWith(SamLibConfig.SLASH)) {
            str = str + SamLibConfig.SLASH;
        }
        
        
        if (str.startsWith(SAMLIB_PROTO)) {//full URL case
            for (SamIzdat sz : AllUrl) {
               
                if (sz.testFullUrl(str)) {
                    return str.replaceAll(sz.urlH, "");
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
        List<String> res = new ArrayList<>();
        Iterator<SamIzdat> itr = getIterator();
        while(itr.hasNext()){
            res.add(itr.next().getAuthorRequestURL(a.getUrl()));
        }
        return res;
    }
    public List<String> getAuthorIndexDate(Author a) {
        List<String> res = new ArrayList<>();
        Iterator<SamIzdat> itr = getIterator();
        while(itr.hasNext()){
            res.add(itr.next().getAuthorIndexDate(a.getUrl()));
        }
        return res;
    }


    /**
     * Get Default URL to use for browser  open intend
     * @return Default URL to use for browser  open intend
     */
    private String getDefaultURL(){
        Iterator<SamIzdat> itr = getIterator();
        return itr.next().urlH;
    }
    /**
     * Return the list of request URLs to search authors
     * @param pattern search pattern
     * @param page number of page
     * @return List of URL to make search
     */
    public List<String> getSearchAuthorURL(String pattern,int page){
        List<String> res = new ArrayList<>();
        Iterator<SamIzdat> itr = getIterator();
        while(itr.hasNext()){
            res.add(itr.next().getSearchAuthorURL(pattern, page));
        }
        return res;
    }

    /**
     * Get book url to download html content
     * @param b Book object
     * @return  List of URLs
     */
    public List<String> getBookUrl(Book b) {
        List<String> res = new ArrayList<>();
        Iterator<SamIzdat> itr = getIterator();
        while(itr.hasNext()){
            res.add(itr.next().getBookURL(b.getUri(),b.getFileType()));
        }
        return res;
    }


    public static void transformBook(File orig) throws IOException {
        File tmp = new File(orig.getAbsoluteFile() + ".tmp");

        if (! orig.renameTo(tmp)){
            Log.e(DEBUG_TAG, "Error to rename file to tmp");
        }

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

       if (!  tmp.delete()){
           Log.e(DEBUG_TAG,"Error to delete tmp file");
       }


    }
    public static int testSplit(String str) {
        String[] arr = str.split(SamLibConfig.SPLIT);
        return arr.length;
    }
}
