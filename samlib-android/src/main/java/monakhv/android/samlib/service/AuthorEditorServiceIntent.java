package monakhv.android.samlib.service;

import android.content.Context;
import android.content.Intent;


import java.util.ArrayList;


import monakhv.android.samlib.SamlibApplication;
import monakhv.samlib.db.entity.SamLibConfig;
import monakhv.samlib.log.Log;
import monakhv.samlib.service.SamlibService;

/*
 * Copyright 2014  Dmitry Monakhov
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
 * 12/25/14.
 */
public class AuthorEditorServiceIntent extends MyServiceIntent {
    public static final String RECEIVER_FILTER="AuthorEditorServiceIntent_RECEIVER_FILTER";

    private static final String DEBUG_TAG="AuthorEditorServiceIntent";
    private static final String EXTRA_ADD_AUTHOR_DATA="AuthorEditorServiceIntent_EXTRA_ADD_AUTHOR_DATA";
    private static final String EXTRA_OBJECT_ID ="AuthorEditorServiceIntent_EXTRA_OBJECT_ID";
    private static final String EXTRA_SUB_OBJECT_ID="AuthorEditorServiceIntent_EXTRA_SUB_OBJECT_ID";
    private static final String EXTRA_SORT_ORDER ="AuthorEditorServiceIntent_EXTRA_SORT_ORDER";
    private static final String EXTRA_SELECT_TAG ="AuthorEditorServiceIntent_EXTRA_SELECT_TAG";

    public static final String ACTION_AUTHOR_READ="AuthorEditorServiceIntent_ACTION_AUTHOR_READ";
    public static final String ACTION_BOOK_READ_FLIP="AuthorEditorServiceIntent_ACTION_BOOK_READ_FLIP";
    public static final String ACTION_GROUP_READ_FLIP="AuthorEditorServiceIntent_ACTION_GROUP_READ_FLIP";
    public static final String ACTION_ALL_TAGS_UPDATE="AuthorEditorServiceIntent_ACTION_ALL_TAGS_UPDATE";


    SamlibApplication mSamlibApplication;
    public AuthorEditorServiceIntent() {
        super(DEBUG_TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String action = intent.getAction();

        mSamlibApplication= (SamlibApplication) getApplication();

        SamlibService service =mSamlibApplication.getServiceComponent(UpdateObject.UNDEF,getHelper()).getSamlibService();
                //new SamlibService(new AuthorController(getHelper()),new AndroidGuiUpdater(mSettingsHelper,UpdateObject.UNDEF,null), mSettingsHelper,new HttpClientController(mSettingsHelper));

        Log.d(DEBUG_TAG, "Got intent for action: "+action);




        Log.e(DEBUG_TAG, "Wrong Action Type");

    }


}
