package monakhv.android.samlib.service;

import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;


import java.io.IOException;
import java.util.ArrayList;


import monakhv.android.samlib.R;
import monakhv.android.samlib.data.SettingsHelper;
import monakhv.samlib.db.AuthorController;
import monakhv.samlib.db.BookController;
import monakhv.samlib.exception.SamlibParseException;
import monakhv.samlib.db.entity.Author;
import monakhv.samlib.db.entity.Book;
import monakhv.samlib.db.entity.SamLibConfig;
import monakhv.samlib.http.HttpClientController;
import monakhv.samlib.log.Log;

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
    private static final String EXTRA_DEL_AUTHOR_DATA="AddAuthorServiceIntent_EXTRA_DEL_AUTHOR_DATA";
    public static final String EXTRA_ACTION_TYPE="AddAuthorServiceIntent_EXTRA_ACTION_TYPE";
    public static final String ACTION_ADD="AddAuthorServiceIntent_ACTION_ADD";
    public static final String ACTION_DELETE="AddAuthorServiceIntent_ACTION_DELETE";
    public static final String ACTION_AUTHOR_READ="AddAuthorServiceIntent_ACTION_AUTHOR_READ";
    public static final String ACTION_BOOK_READ_FLIP="AddAuthorServiceIntent_ACTION_BOOK_READ_FLIP";


    public static final String RESULT_DEL_NUMBER ="AddAuthorServiceIntent_RESULT_DEL_NUMBER";
    public static final String RESULT_ADD_NUMBER="AddAuthorServiceIntent_RESULT_ADD_NUMBER";
    public static final String RESULT_DOUBLE_NUMBER="AddAuthorServiceIntent_RESULT_DOUBLE_NUMBER";
    public static final String RESULT_AUTHOR_ID="AddAuthorServiceIntent_ RESULT_AUTHOR_ID";
    public static final String RESULT_MESSAGE="AddAuthorServiceIntent_ RESULT_RESULT_MESSAGE";

    private Context context;
    private  int numberOfAdded=0;
    private  int numberOfDeleted=0;
    private  int doubleAdd = 0;
    private int totalToAdd;
    private long author_id=0;
    private SettingsHelper settings;
    private String action;


    public AuthorEditorServiceIntent() {
        super(DEBUG_TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        action = intent.getStringExtra(EXTRA_ACTION_TYPE);
        context = this.getApplicationContext();
        settings=new SettingsHelper(context);
        Log.d(DEBUG_TAG, "Got intent");
        numberOfAdded=0;
        doubleAdd=0;
        numberOfDeleted=0;

        if (action.equalsIgnoreCase(ACTION_ADD)){
            Log.d(DEBUG_TAG, "Making Add Author");

            ArrayList<String> ll =intent.getStringArrayListExtra(EXTRA_ADD_AUTHOR_DATA);

            if (ll == null){
                Log.e(DEBUG_TAG,"Null add data - nothing to add!");
                return;
            }
            totalToAdd = ll.size();
            makeAuthorAdd(ll);
            return;

        }
        if (action.equals(ACTION_DELETE)){
            Log.d(DEBUG_TAG, "Making DEL Author");
            int id =intent.getIntExtra(EXTRA_DEL_AUTHOR_DATA,-1);
            if (id <0){
                Log.e(DEBUG_TAG,"Null del data - nothing to del!");
                return;
            }
            makeAuthorDel(id);
            sendRefresh();
            return;
        }
        if (action.equals(ACTION_AUTHOR_READ)){
            int id =intent.getIntExtra(EXTRA_DEL_AUTHOR_DATA,-1);
            if (id <0){
                Log.e(DEBUG_TAG,"Null author data, can not make it read");
                return;
            }
            makeAuthorRead(id);
            sendRefresh();
            return;
        }
        if (action.equals(ACTION_BOOK_READ_FLIP)){
            int id =intent.getIntExtra(EXTRA_DEL_AUTHOR_DATA,-1);
            if (id <0){
                Log.e(DEBUG_TAG,"Null book data, can not make it read/unread");
                return;
            }
            makeBookReadFlip(id);
            sendRefresh();
            return;
        }
        Log.e(DEBUG_TAG, "Wrong Action Type");

    }

    /**
     * Special method to make Author read, also make sure that all book re read either
     * @param id author id
     * @return true if success
     */
    private  boolean makeAuthorRead(int id) {
        AuthorController sql = new AuthorController(getHelper());
        Author a = sql.getById(id);

        if (a == null){
            Log.e(DEBUG_TAG, "Author not found to update");
            return false;
        }

        if (! a.isIsNew()) {
            Log.d(DEBUG_TAG, "Author is read - no update need");
            return false;
        }


        int i = sql.markRead(a);

        Log.d(DEBUG_TAG, "Update author status: "+i);

        return true;

    }

    /**
     * Invert read book flag
     * Adjust author flag either
     * @param id  book id
     */
    private  void makeBookReadFlip(int id) {
        BookController sql = new BookController(getHelper());
        AuthorController aCtl = new AuthorController(getHelper());
        Book book=sql.getById(id);
        if (book == null){
            Log.e(DEBUG_TAG,"makeBookReadFlip: book not found id = "+id);
            return;
        }

        if (book.isIsNew()){
            sql.markRead(book);
            Author a = aCtl.getById(book.getAuthor().getId());
            aCtl.testMarkRead(a);
        }
        else {
            sql.markUnRead(book);
            Author a = aCtl.getById(book.getAuthor().getId());
            aCtl.testMarkRead(a);
        }

    }

    /**
     * Delete author from DB
     * @param id author id
     */
    private void makeAuthorDel(int id){
        AuthorController sql = new AuthorController(getHelper());
        int res = sql.delete(sql.getById(id));
        Log.d(DEBUG_TAG, "Author id "+id+" deleted, status "+res);
        if (res == 1){
            ++numberOfDeleted;
        }

        sendResult();
    }
    /**
     * Add authors
     * @param urls list of author urls
     */
    private void makeAuthorAdd(ArrayList<String> urls){
        HttpClientController http = HttpClientController.getInstance(settings);
        AuthorController sql = new AuthorController(getHelper());
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, DEBUG_TAG);
        wl.acquire();
        for (String url : urls) {
            Author a = loadAuthor(http, sql, url);
            if (a != null) {
                author_id=sql.insert(a);
                ++numberOfAdded;
            }
        }
        sendResult();
        wl.release();
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

            ++doubleAdd;
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

    private void sendResult() {
        Intent broadcastIntent = new Intent();
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.setAction(RECEIVER_FILTER);
        broadcastIntent.putExtra(EXTRA_ACTION_TYPE, action);
        broadcastIntent.putExtra(RESULT_ADD_NUMBER,numberOfAdded);
        broadcastIntent.putExtra(RESULT_DEL_NUMBER,numberOfDeleted);
        broadcastIntent.putExtra(RESULT_DOUBLE_NUMBER,doubleAdd);
        broadcastIntent.putExtra(RESULT_AUTHOR_ID,author_id);
        CharSequence msg="";
        if (action.equals(ACTION_ADD)){//ADD Action

            if (totalToAdd == 1){//add single author
                if (numberOfAdded ==1 ) {
                    msg = context.getText(R.string.add_success);
                }
                else if (doubleAdd ==1) {
                    msg = context.getText(R.string.add_error_double);
                }
                else {
                    msg = context.getText(R.string.add_error);
                }
            }
            else {//import list of authors
                msg = context.getText(R.string.add_success_multi)+" "+numberOfAdded;

                if (doubleAdd != 0) {//double is here
                    msg = msg +"<br>"+context.getText(R.string.add_success_double)+" "+doubleAdd;
                }
            }
        }//end ADD Action


        if (action.equals(ACTION_DELETE)){
            if (numberOfDeleted == 1){
                msg=context.getText(R.string.del_success);
            }
            else {
                msg=context.getText(R.string.del_error);
            }
        }
        broadcastIntent.putExtra(RESULT_MESSAGE,msg);

        sendBroadcast(broadcastIntent);

        if (numberOfAdded!=0 || numberOfDeleted != 0){
            settings.requestBackup();
        }



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
        service.putExtra(EXTRA_ACTION_TYPE,ACTION_ADD);
        service.putStringArrayListExtra(EXTRA_ADD_AUTHOR_DATA, urls);
        ctx.startService(service);

    }
    public static void delAuthor(Context ctx,int id){
        Log.v(DEBUG_TAG,"Starting del service");
        Intent service = new Intent(ctx,AuthorEditorServiceIntent.class );
        service.putExtra(EXTRA_ACTION_TYPE,ACTION_DELETE);
        service.putExtra(EXTRA_DEL_AUTHOR_DATA, id);
        ctx.startService(service);

    }
    public static void markAuthorRead(Context ctx,int id){
        Log.v(DEBUG_TAG,"Starting author read service");
        Intent service = new Intent(ctx,AuthorEditorServiceIntent.class );
        service.putExtra(EXTRA_ACTION_TYPE,ACTION_AUTHOR_READ);
        service.putExtra(EXTRA_DEL_AUTHOR_DATA, id);
        ctx.startService(service);
    }
    public static void markBookReadFlip(Context ctx,int id){
        Log.v(DEBUG_TAG,"Starting book read service");
        Intent service = new Intent(ctx,AuthorEditorServiceIntent.class );
        service.putExtra(EXTRA_ACTION_TYPE,ACTION_BOOK_READ_FLIP);
        service.putExtra(EXTRA_DEL_AUTHOR_DATA, id);
        ctx.startService(service);
    }
}
