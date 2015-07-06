package monakhv.samlib.desk.service;

import monakhv.samlib.db.SQLController;
import monakhv.samlib.db.entity.Author;
import monakhv.samlib.db.entity.SamLibConfig;
import monakhv.samlib.desk.data.Settings;
import monakhv.samlib.db.AuthorController;
import monakhv.samlib.desk.sql.DaoController;
import monakhv.samlib.exception.SamlibParseException;
import monakhv.samlib.http.HttpClientController;
import monakhv.samlib.log.Log;

import java.io.IOException;

import java.util.List;

/*
 * Copyright 2015  Dmitry Monakhov
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
 *
 * 30.06.15.
 */
public class ServiceOperation {
    private static final String DEBUG_TAG = "ServiceOperation";

    private Settings settings;
    private AuthorController sql;

    public ServiceOperation(Settings settings) {
        this.settings = settings;
        try {
            SQLController sqlController = SQLController.getInstance(settings.getDataDirectoryPath());
            sql=new AuthorController(DaoController.getInstance(sqlController));
        } catch (Exception e) {
            Log.e(DEBUG_TAG, "SQL Error", e);

        }
    }

    public void delete(Author author){

        sql.delete(author);
    }

    public void update_tes(List<Author> list) {


        HttpClientController http = HttpClientController.getInstance(settings);

        Author ess = sql.getById(118);

        Log.i(DEBUG_TAG, "Author: " + ess.getName() + " - " + ess.getBooks().size());
        Author newEss = sql.getEmptyObject();
        try {
            newEss = http.getAuthorByURL(ess.getUrl(), newEss);
        } catch (IOException ex) {
            Log.e(DEBUG_TAG, "Connection Error: " + ess.getUrl(), ex);

            return;

        } catch (SamlibParseException ex) {
            Log.e(DEBUG_TAG, "Error parsing url: " + ess.getUrl() + " skip update author ", ex);


        }
        Log.i(DEBUG_TAG, "Author: " + newEss.getName() + " - " + newEss.getBooks().size());


        if (ess.update(newEss)) {//we have update for the author

            Log.i(DEBUG_TAG, "We need update author: " + ess.getName() + " - " + ess.getId() + " : " + ess.getBooks().size());
            sql.update(ess);
        } else {
            Log.e(DEBUG_TAG, "Constant Author");
        }


//        for (Book b : ess.getBooks()) {
//
//            Log.i(DEBUG_TAG, "url: " + b.getUri());
//            Log.i(DEBUG_TAG, "size: " + b.getSize());
//            Log.i(DEBUG_TAG, "upd: " + b.getUpdateDate());
//            Log.i(DEBUG_TAG,"desc: "+b.getDescription());
//
//        }
//
//        for (Book b : newEss.getBooks()) {
//
//            Log.i(DEBUG_TAG, "url: " + b.getUri());
//            Log.i(DEBUG_TAG, "size: " + b.getSize());
//            Log.i(DEBUG_TAG, "upd: " + b.getUpdateDate());
//            Log.i(DEBUG_TAG,"desc: "+b.getDescription());
//        }


    }

    public void update(List<Author> list) {


        HttpClientController http = HttpClientController.getInstance(settings);
        Log.d(DEBUG_TAG,"Begin Author update");

        for (Author a : list) {

            String url = a.getUrl();
            Author newA = sql.getEmptyObject();


            try {
                newA = http.getAuthorByURL(url, newA);
            } catch (IOException ex) {
                Log.e(DEBUG_TAG, "Connection Error: " + url, ex);

                return;

            } catch (SamlibParseException ex) {
                Log.e(DEBUG_TAG, "Error parsing url: " + url + " skip update author ", ex);

                //++skippedAuthors;
                newA = a;
            }
            if (a.update(newA)) {//we have update for the author

                Log.i(DEBUG_TAG, "We need update author: " + a.getName() + " - " + a.getId() + " : " + a.getBooks().size());
               sql.update(a);
            } else {
                Log.e(DEBUG_TAG, "Constant Author");
            }


        }
        Log.d(DEBUG_TAG,"END Author update");

    }

    public void addAuthor(String url) {
        HttpClientController http = HttpClientController.getInstance(settings);

        Author a = loadAuthor(http, sql, url);
        if (a != null) {
            sql.insert(a);

        }
    }
    /**
     * URL syntax checkout
     *
     * @param url original URL
     * @return reduced URL without host prefix or NULL if the syntax is wrong
     *
     */
    private String testURL(String url)   {
        Log.d(DEBUG_TAG, "Got text: " + url);

        return SamLibConfig.reduceUrl(url);

    }
    private Author loadAuthor(HttpClientController http, AuthorController sql, String url) {
        Author a;
        String text;

        text = testURL(url);
        if (text == null){
            Log.e(DEBUG_TAG, "URL syntax error: " + url);

            return null;
        }
        Author ta = sql.getByUrl(text);
        if (ta != null) {
            Log.i(DEBUG_TAG, "Ignore Double entries: "+text);

            //++doubleAdd;
            return null;
        }

        try {
            a = http.addAuthor(text,sql.getEmptyObject());
        } catch (IOException ex) {
            Log.e(DEBUG_TAG, "DownLoad Error for URL: " + text, ex);

            return null;

        } catch (SamlibParseException ex) {
            Log.e(DEBUG_TAG, "Author parsing Error: " + text, ex);

            return null;
        } catch (IllegalArgumentException ex) {
            Log.e(DEBUG_TAG, "URL Parsing exception: " + text, ex);

            return null;
        }


        return a;
    }

}
