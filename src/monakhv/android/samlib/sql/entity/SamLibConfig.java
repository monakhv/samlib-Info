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
import java.util.List;

/**
 *
 * @author monakhv
 */
public class SamLibConfig {

    static final String SAMLIB_URL = "http://samlib.ru";
    static final String BDCLB_URL = "http://budclub.ru";
    private static final String[] URLs = {SAMLIB_URL, BDCLB_URL};
    private static final String SLASH = "/";
    private static final String URLPTR = "/\\w/\\w+/";
    private static final String SAMLIB_PROTO = "http://";
    private static final String REQUEST_AUTHOR_TEXTS = "/cgi-bin/areader?q=razdel&order=date&object=";
    private static final String REQUEST_BOOK_TEXT = "/cgi-bin/areader?q=book&object=";

    /**
     * Test whether URL has a form http://<url>/w/www_w_w/ Must be ended by /
     * Must be begin with one of the valid URL
     *
     * @param txt
     * @return
     */
    public static boolean testFullUrl(String txt) {
        for (String uu : URLs) {
            Sz sz = new Sz(uu);
            if (sz.testFullUrl(txt)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Take URL check syntax
     *
     * @param str reduced URL or NULL if the syntax is wrong
     * @return
     */
    public static String reduceUrl(String str) {
        if (str.startsWith(SAMLIB_PROTO)) {//full URL case
            for (String uu : URLs) {
                Sz sz = new Sz(uu);
                if (sz.testFullUrl(str)) {
                    return str.replaceAll(uu, "");
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

    static List<String> getAuthorRequestURL(String url) {
        List<String> res = new ArrayList<String>();
        for (String uu : URLs) {
            Sz sz = new Sz(uu);
            res.add(sz.getAuthorRequestURL(url));
        }
        return res;
    }

    static List<String> getBookUrl(String uri) {
        List<String> res = new ArrayList<String>();
        for(String uu : URLs){
            Sz sz = new Sz(uu);
            res.add(sz.getBookURL(uri));
        }
        return res;
    }

    private static class Sz {

        private String url;

        private Sz(String url) {
            this.url = url;
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
            return SAMLIB_URL+REQUEST_BOOK_TEXT+uu;
        }
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
