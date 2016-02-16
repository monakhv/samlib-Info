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
package monakhv.samlib.service;


import monakhv.samlib.data.AbstractSettings;
import monakhv.samlib.db.entity.*;
import monakhv.samlib.exception.SamlibInterruptException;
import monakhv.samlib.exception.SamlibParseException;
import monakhv.samlib.http.HttpClientController;
import monakhv.samlib.log.Log;

import javax.inject.Inject;
import java.io.IOException;
import java.text.Collator;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.util.*;



/**
 * Service to making check for author updates Can be called from activity or
 * from alarm manager
 *
 * @author monakhv
 */
public class SamlibService {
    public enum UpdateObjectSelector {
        Tag,
        Author,
        Book,
        UNDEF
    }

    private static final String DEBUG_TAG = "SamlibService";

   // protected final AuthorController authorController;
    private final GuiUpdate guiUpdate;
    private final AbstractSettings settingsHelper;
    private final HttpClientController http;


    @Inject
    public SamlibService(GuiUpdate guiUpdate, AbstractSettings settingsHelper, HttpClientController httpClientController) {
        this.guiUpdate = guiUpdate;
        this.settingsHelper = settingsHelper;
        http = httpClientController;

    }


    /**
     * Make author search according to the first part aof theAuthor name
     *
     * @param pattern part of the author name
     * @return List of found authors
     * @throws IOException
     * @throws SamlibParseException
     */
    public List<AuthorCard> makeSearch(String pattern) throws IOException, SamlibParseException, SamlibInterruptException {
        Log.i(DEBUG_TAG, "makeSearch: Search author with pattern: " + pattern);
        List<AuthorCard> result = new ArrayList<>();

        int page = 1;

        HashMap<String, ArrayList<AuthorCard>> colAuthors = http.searchAuthors(pattern, page);
        RuleBasedCollator russianCollator = (RuleBasedCollator) Collator.getInstance(new Locale("ru", "RU"));

        try {
            russianCollator = new RuleBasedCollator(settingsHelper.getCollationRule());
        } catch (ParseException ex) {
            Log.e(DEBUG_TAG, "makeSearch: Collator error", ex);

        }

        russianCollator.setStrength(Collator.IDENTICAL);
        russianCollator.setDecomposition(Collator.NO_DECOMPOSITION);


        while (colAuthors != null) {//page cycle while we find anything

            String[] keys = colAuthors.keySet().toArray(new String[1]);

            Arrays.sort(keys, russianCollator);
            int ires = Arrays.binarySearch(keys, pattern, russianCollator);
            Log.d(DEBUG_TAG, "makeSearch: Page number:" + page + "    search result " + ires + "   length is " + keys.length);

            int iStart;
            if (ires < 0) {
                iStart = -ires - 1;
            } else {
                iStart = ires;
            }
            for (int i = iStart; i < keys.length; i++) {
                String sKey = keys[i];
                if (sKey.toLowerCase().startsWith(pattern.toLowerCase())) {
                    for (AuthorCard ac : colAuthors.get(sKey)) {

                        result.add(ac);

                        if (result.size() >= SamLibConfig.SEARCH_LIMIT) {
                            return result;
                        }
                    }

                } else {
                    Log.d(DEBUG_TAG, "makeSearch: Search for " + pattern + " stop by substring  -   " + sKey + "   " + keys.length + "         " + iStart + "  -  " + ires);


                    return result;
                }
            }


            ++page;
            colAuthors = http.searchAuthors(pattern, page);
        }
        Log.d(DEBUG_TAG, "makeSearch: Results: " + result.size());

        return result;
    }




    public void downloadBook(Book book) {

        int book_id=book.getId();


        AbstractSettings.FileType ft = settingsHelper.getFileType();
        Log.d(DEBUG_TAG, "downloadBook: default type is  " + ft.toString());

        switch (ft) {
            case HTML:
                guiUpdate.finishBookLoad(getBook(book, AbstractSettings.FileType.HTML), AbstractSettings.FileType.HTML, book_id);
                break;
            case FB2:
                boolean rr = getBook(book, AbstractSettings.FileType.FB2);
                if (rr) {
                    guiUpdate.finishBookLoad(true, AbstractSettings.FileType.FB2, book_id);
                } else {
                    guiUpdate.finishBookLoad(getBook(book, AbstractSettings.FileType.HTML), AbstractSettings.FileType.HTML, book_id);
                }
                break;
        }
    }

    private boolean getBook(Book book, AbstractSettings.FileType ft) {
        book.setFileType(ft);

        try {
            http.downloadBook(book);
            return true;

        } catch (Exception ex) {

            settingsHelper.cleanBookFile(book);//clean file on error

            Log.e(DEBUG_TAG, "getBook: Download book error: " + book.getUri(), ex);

            return false;
        }
    }


}
