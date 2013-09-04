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
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author monakhv
 */
public class SamLibConfig {
    public static final String SAMLIB_URL = "http://samlib.ru";
    public static final String SAMLIB_PROTO = "http://";
    private static final String REQUEST_AUTHOR_TEXTS = "/cgi-bin/areader?q=razdel&order=date&object=";
    private static final String REQUEST_BOOK_TEXT="/cgi-bin/areader?q=book&object=";
    
    static URL getAuthorRequestURL(URL url) throws MalformedURLException {
        StringBuilder sb = new StringBuilder();
        URL result;

        final URL authorURL = url;
        sb.append(SamLibConfig.SAMLIB_PROTO);
        sb.append(authorURL.getHost());
        sb.append(REQUEST_AUTHOR_TEXTS);
        sb.append(authorURL.getPath());
        result = new URL(sb.toString());

        return result;
    }
    static String getBookUrl(String bookUri) {
        return SAMLIB_URL+REQUEST_BOOK_TEXT+bookUri;
    }
    public static void  transformBook(File orig) throws IOException{
        File tmp = new File(orig.getAbsoluteFile()+".tmp");
        
        orig.renameTo(tmp);
        
        BufferedWriter bw = new BufferedWriter(new FileWriter(orig));        
        BufferedReader br = new BufferedReader(new FileReader(tmp));
        
        String line = br.readLine();
        
        String []  str = line.split("\\|");
        bw.write("<html><head>");
        bw.write("<title>"+str[1]+"</title>");
        bw.write("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
        bw.write("</head><body>");
        bw.newLine();
        
        bw.write("<center><h3>"+str[0]+"</h3>");
        bw.write("<h2>"+str[1]+"</h2></center>");
                
         line = br.readLine();
        while (line != null){
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
