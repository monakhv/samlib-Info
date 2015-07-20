package monakhv.android.samlib.service;

import android.content.Context;
import android.content.Intent;


import java.util.ArrayList;


import monakhv.android.samlib.data.SettingsHelper;
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

    private static final String DEBUG_TAG="AddAuthorServiceIntent";
    private static final String EXTRA_ADD_AUTHOR_DATA="AddAuthorServiceIntent_EXTRA_ADD_AUTHOR_DATA";
    private static final String EXTRA_OBJECT_ID ="AddAuthorServiceIntent_EXTRA_OBJECT_ID";
    public static final String EXTRA_ACTION_TYPE="AddAuthorServiceIntent_EXTRA_ACTION_TYPE";

    public static final String ACTION_AUTHOR_READ="AddAuthorServiceIntent_ACTION_AUTHOR_READ";
    public static final String ACTION_BOOK_READ_FLIP="AddAuthorServiceIntent_ACTION_BOOK_READ_FLIP";
    public static final String ACTION_ALL_TAGS_UPDATE="AddAuthorServiceIntent_ACTION_ALL_TAGS_UPDATE";


    public AuthorEditorServiceIntent() {
        super(DEBUG_TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String action = intent.getStringExtra(EXTRA_ACTION_TYPE);
        Context context = this.getApplicationContext();
        SamlibService service = new SamlibService(getHelper(),new AndroidGuiUpdater(context,0), new SettingsHelper(context));

        Log.d(DEBUG_TAG, "Got intent");


        if (action.equalsIgnoreCase(SamlibService.ACTION_ADD)){
            Log.d(DEBUG_TAG, "Making Add Author");

            ArrayList<String> ll =intent.getStringArrayListExtra(EXTRA_ADD_AUTHOR_DATA);

            if (ll == null){
                Log.e(DEBUG_TAG,"Null add data - nothing to add!");
                return;
            }

            service.makeAuthorAdd(ll);

            return;

        }
        if (action.equals(SamlibService.ACTION_DELETE)){
            Log.d(DEBUG_TAG, "Making DEL Author");
            int id =intent.getIntExtra(EXTRA_OBJECT_ID,-1);
            if (id <0){
                Log.e(DEBUG_TAG,"Null del data - nothing to del!");
                return;
            }
            service.makeAuthorDel(id);

            return;
        }
        if (action.equals(ACTION_AUTHOR_READ)){
            int id =intent.getIntExtra(EXTRA_OBJECT_ID,-1);
            if (id <0){
                Log.e(DEBUG_TAG,"Null author data, can not make it read");
                return;
            }
            service.makeAuthorRead(id);

            return;
        }
        if (action.equals(ACTION_BOOK_READ_FLIP)){
            int id =intent.getIntExtra(EXTRA_OBJECT_ID,-1);
            if (id <0){
                Log.e(DEBUG_TAG,"Null book data, can not make it read/unread");
                return;
            }
            service.makeBookReadFlip(id);

            return;
        }
        //
        if (action.equals(ACTION_ALL_TAGS_UPDATE)){
            service.makeUpdateTags();

            return;
        }

        Log.e(DEBUG_TAG, "Wrong Action Type");

    }












    /**
     * Public method to add single user
     *  Used to add User directly or by search
     *
     * @param ctx Context
     * @param url author url
     */
    public static void addAuthor(Context ctx,String url) {
        ArrayList<String> ll = new ArrayList<>();
        ll.add(url);
        addAuthor(ctx,ll);
    }

    public static void addAuthor(Context ctx, ArrayList<String> urls) {
        Log.v(DEBUG_TAG,"Starting add service");
        Intent service = new Intent(ctx,AuthorEditorServiceIntent.class );
        service.putExtra(EXTRA_ACTION_TYPE, SamlibService.ACTION_ADD);
        service.putStringArrayListExtra(EXTRA_ADD_AUTHOR_DATA, urls);
        ctx.startService(service);

    }
    public static void delAuthor(Context ctx,int id){
        Log.v(DEBUG_TAG,"Starting del service");
        Intent service = new Intent(ctx,AuthorEditorServiceIntent.class );
        service.putExtra(EXTRA_ACTION_TYPE, SamlibService.ACTION_DELETE);
        service.putExtra(EXTRA_OBJECT_ID, id);
        ctx.startService(service);

    }
    public static void markAuthorRead(Context ctx,int id){
        Log.v(DEBUG_TAG,"Starting author read service");
        Intent service = new Intent(ctx,AuthorEditorServiceIntent.class );
        service.putExtra(EXTRA_ACTION_TYPE,ACTION_AUTHOR_READ);
        service.putExtra(EXTRA_OBJECT_ID, id);
        ctx.startService(service);
    }
    public static void markBookReadFlip(Context ctx,int id){
        Log.v(DEBUG_TAG,"Starting book read service");
        Intent service = new Intent(ctx,AuthorEditorServiceIntent.class );
        service.putExtra(EXTRA_ACTION_TYPE,ACTION_BOOK_READ_FLIP);
        service.putExtra(EXTRA_OBJECT_ID, id);
        ctx.startService(service);
    }
    public static void updateAllAuthorsTags(Context ctx) {
        Log.v(DEBUG_TAG, "Starting update all tags service");
        Intent service = new Intent(ctx,AuthorEditorServiceIntent.class );
        service.putExtra(EXTRA_ACTION_TYPE,ACTION_ALL_TAGS_UPDATE);
        service.putExtra(EXTRA_OBJECT_ID, 0);

        ctx.startService(service);
    }
}
