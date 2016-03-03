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
 *  02.03.16 10:22
 *
 */

package monakhv.samlib.service;

import monakhv.samlib.db.entity.AuthorCard;
import monakhv.samlib.http.HttpClientController;
import monakhv.samlib.impl.SettingsImpl;
import monakhv.samlib.log.Log;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Class to make search the authors
 * Created by monakhv on 02.03.16.
 */
public class AuthorSearchServiceTest {
    public static final String PATTERN = "монах";
    public static final String DEBUG_TAG = "AuthorSearchServiceTest";
    private static HttpClientController httpClientController;
    private static SettingsImpl settings;

    private AuthorSearchService mAuthorSearchService;
    private boolean mError = false;
    private List<AuthorCard> mAuthorCards;

    public AuthorSearchServiceTest() {
        mAuthorSearchService = new AuthorSearchService(httpClientController, settings);
    }

    @BeforeClass
    static public void setUp() throws Exception {
        settings = new SettingsImpl();
        httpClientController = new HttpClientController(settings);
        httpClientController.setProxyData(SettingsImpl.proxyData);
    }

    @Test(timeout = 10000)
    public void testMakeSearch() throws Exception {
        mAuthorCards = new ArrayList<>();
        mAuthorSearchService.makeSearch(PATTERN)
                .subscribe(
                        authorCard -> {
                            mAuthorCards.add(authorCard);
                        },
                        throwable -> {
                            mError = true;
                            Log.e(DEBUG_TAG, "Error", throwable);

                        });

        assertFalse(mError);
        assertFalse(mAuthorCards.isEmpty());
        for (AuthorCard authorCard : mAuthorCards) {
            Log.i(DEBUG_TAG, "> " + authorCard.getName());
        }
    }


}