/*
 *  Copyright 2016 Dmitry Monakhov.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  16.02.16 18:19
 *
 */

package monakhv.samlib.service;

import monakhv.samlib.data.AbstractSettings;
import monakhv.samlib.db.entity.AuthorCard;
import monakhv.samlib.db.entity.SamLibConfig;
import monakhv.samlib.exception.SamlibSearchLimitExceeded;
import monakhv.samlib.http.HttpClientController;
import monakhv.samlib.log.Log;
import rx.Observable;

import java.text.Collator;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

/**
 * Observable to make Author search
 *
 * Created by monakhv on 16.02.16.
 */
public class SamlibSearchService {
    private static final String DEBUG_TAG = "SamlibSearchService";
    final private HttpClientController mHttpClientController;
    final private AbstractSettings mSettings;

    public SamlibSearchService(HttpClientController httpClientController, AbstractSettings settings) {
        mHttpClientController = httpClientController;
        mSettings = settings;
    }

    public Observable<AuthorCard> makeSearch(String pattern){


        return Observable.create(aSubscriber->{
            int resNumber=0;
            int page = 1;

            HashMap<String, ArrayList<AuthorCard>> colAuthors = null;
            try {
                colAuthors = mHttpClientController.searchAuthors(pattern, page);
            } catch (Exception ex) {
                Log.e(DEBUG_TAG, "makeSearch: searchAuthors error", ex);
                aSubscriber.onError(ex);
                return;
            }
            RuleBasedCollator russianCollator = (RuleBasedCollator) Collator.getInstance(new Locale("ru", "RU"));

            try {
                russianCollator = new RuleBasedCollator(mSettings.getCollationRule());
            } catch (ParseException ex) {
                Log.e(DEBUG_TAG, "makeSearch: Collator error", ex);
                aSubscriber.onError(ex);
                return;
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


                            aSubscriber.onNext(ac);
                            ++resNumber;

                            if (resNumber >= SamLibConfig.SEARCH_LIMIT) {
                                aSubscriber.onError(new SamlibSearchLimitExceeded(resNumber+" - "+SamLibConfig.SEARCH_LIMIT));
                                return ;
                            }
                        }

                    } else {
                        Log.d(DEBUG_TAG, "makeSearch: Search for " + pattern + " stop by substring  -   " + sKey + "   " + keys.length + "         " + iStart + "  -  " + ires);

                        aSubscriber.onCompleted();
                        return ;
                    }
                }


                ++page;
                try {
                    colAuthors = mHttpClientController.searchAuthors(pattern, page);
                } catch (Exception ex) {
                    Log.e(DEBUG_TAG,"makeSearch: searchAuthors2 error: "+ex);
                    aSubscriber.onError(ex);
                    return;
                }
            }
            aSubscriber.onCompleted();







        });
    }
}
