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
import monakhv.samlib.service.GuiUpdate;
import monakhv.samlib.service.SamlibService;

import javax.inject.Inject;

/**
 * Version of SamlibService where we can download books after update
 * Created by monakhv on 18.01.16.
 */
public class SpecialSamlibService extends SamlibService {
    private final static String DEBUG_TAG="SpecialSamlibService";
    private final UpdateObject mUpdateObject;
    private final SettingsHelper mSettingsHelper;
    private final DataExportImport mDataExportImport;

    @Inject
    public SpecialSamlibService(AuthorController authorController, GuiUpdate guiUpdate, SettingsHelper settingsHelper, HttpClientController http, UpdateObject updateObject, DataExportImport dataExportImport) {
        super(authorController, guiUpdate, settingsHelper, http);
        mUpdateObject=updateObject;
        mSettingsHelper=settingsHelper;
        mDataExportImport=dataExportImport;
    }

    @Override
    public void loadBook(Author a) {

        if (mUpdateObject.callerIsReceiver()) {
            for (Book book : authorController.getBookController().getBooksByAuthor(a)) {//book cycle for the author to update
                if (book.isIsNew() && mSettingsHelper.testAutoLoadLimit(book) && mDataExportImport.needUpdateFile(book)) {
                    monakhv.samlib.log.Log.i(DEBUG_TAG, "loadBook: Auto Load book: " + book.getId());
                    DownloadBookServiceIntent.start(mSettingsHelper.getContext(), book.getId(), mUpdateObject);//we do not need GUI update
                }
            }

        }


    }

}