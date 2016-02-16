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
 *  18.01.16 13:40
 *
 */

package monakhv.android.samlib.service;

import monakhv.android.samlib.data.DataExportImport;
import monakhv.android.samlib.data.SettingsHelper;
import monakhv.samlib.db.AuthorController;
import monakhv.samlib.db.entity.Author;
import monakhv.samlib.db.entity.Book;
import monakhv.samlib.http.HttpClientController;
import monakhv.samlib.service.GuiEventBus;
import monakhv.samlib.service.SamlibUpdateService;

import javax.inject.Inject;

/**
 * Version of SamlibService where we can download books after update
 * Created by monakhv on 18.01.16.
 */
public class SpecialSamlibService extends SamlibUpdateService {
    private final static String DEBUG_TAG="SpecialSamlibService";

    private final SettingsHelper mSettingsHelper;
    private final AuthorController mAuthorController;
    private final DataExportImport mDataExportImport;
    private boolean callerIsReceiver=true;

    @Inject
    public SpecialSamlibService(AuthorController authorController, SettingsHelper settings, HttpClientController httpClientController, GuiEventBus guiEventBus,DataExportImport exportImport){
        super(authorController, settings, httpClientController, guiEventBus);
        mAuthorController=authorController;
        mSettingsHelper=settings;
        mDataExportImport=exportImport;
    }

    public void setCallerIsReceiver(boolean callerIsReceiver) {
        this.callerIsReceiver = callerIsReceiver;
    }

    @Override
    public void loadBook(Author a) {

        if (callerIsReceiver) {
            for (Book book : mAuthorController.getBookController().getBooksByAuthor(a)) {//book cycle for the author to update
                if (book.isIsNew() && mSettingsHelper.testAutoLoadLimit(book) && mDataExportImport.needUpdateFile(book)) {
                    monakhv.samlib.log.Log.i(DEBUG_TAG, "loadBook: Auto Load book: " + book.getId());
                    DownloadBookServiceIntent.start(mSettingsHelper.getContext(), book.getId(), callerIsReceiver);//we do not need GUI update
                }
            }

        }


    }

}