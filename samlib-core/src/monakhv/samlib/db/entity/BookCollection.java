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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author monakhv
 */
public class BookCollection {
    private List<String> urls;
    private HashMap<String,Book> data;
   
    
    private BookCollection(){
        urls = new ArrayList<String>();
        data = new HashMap<String,Book>();
   
    }
    public BookCollection(List<Book> books){
        this();
        
        for (Book book: books){
            String url = book.getUri();
            urls.add(url);
            data.put(url, book);
   
        }        
    }
    /**
     * Return Book removing url from the list
     * or null if not found
     * @param url
     * @return 
     */
    public Book take(String url){
        Book res = null;
        if (urls.contains(url)){
            res = data.get(url);
            urls.remove(url);
        }
        return res;
    }

    /**
     * Get book collection for remaining URLS
     * 
     * @return 
     */
    public List<Book> getLastBooks(){
        List<Book> res= new ArrayList<Book>();
        for (String url: urls){
            res.add(data.get(url));
        }
        return res;
    }
    
    
}
