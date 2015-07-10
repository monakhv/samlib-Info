package monakhv.android.samlib.service;

import android.content.Context;
import android.content.Intent;
import monakhv.android.samlib.MainActivity;
import monakhv.android.samlib.R;
import monakhv.android.samlib.data.SettingsHelper;
import monakhv.samlib.db.entity.Author;
import monakhv.samlib.log.Log;
import monakhv.samlib.service.GuiUpdate;
import monakhv.samlib.service.AuthorService;

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
 * 09.07.15.
 */
public class AndroidGuiUpdater implements GuiUpdate {
    private static final String DEBUG_TAG="AndroidGuiUpdater";
    private Context context;
    private int currentCaller;

    public AndroidGuiUpdater(Context context,int currentCaller) {
        this.context = context;
        this.currentCaller = currentCaller;
    }

    @Override
    public void makeUpdateAuthors() {
        Intent broadcastIntent = new Intent();
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.setAction(MainActivity.UpdateActivityReceiver.ACTION_RESP);
        broadcastIntent.putExtra(MainActivity.UpdateActivityReceiver.ACTION, MainActivity.UpdateActivityReceiver.ACTION_REFRESH);
        broadcastIntent.putExtra(MainActivity.UpdateActivityReceiver.ACTION_REFRESH_OBJECT,
                MainActivity.UpdateActivityReceiver.ACTION_REFRESH_AUTHORS);
        context.sendBroadcast(broadcastIntent);

    }
    @Override
    public void makeUpdateTagList() {
        Intent broadcastIntent = new Intent();
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.setAction(MainActivity.UpdateActivityReceiver.ACTION_RESP);
        broadcastIntent.putExtra(MainActivity.UpdateActivityReceiver.ACTION, MainActivity.UpdateActivityReceiver.ACTION_REFRESH);
        broadcastIntent.putExtra(MainActivity.UpdateActivityReceiver.ACTION_REFRESH_OBJECT,
                MainActivity.UpdateActivityReceiver.ACTION_REFRESH_TAGS);

        context.sendBroadcast(broadcastIntent);

    }

    @Override
    public void makeUpdateBooks() {
        Intent broadcastIntent = new Intent();
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.setAction(MainActivity.UpdateActivityReceiver.ACTION_RESP);
        broadcastIntent.putExtra(MainActivity.UpdateActivityReceiver.ACTION, MainActivity.UpdateActivityReceiver.ACTION_REFRESH);
        broadcastIntent.putExtra(MainActivity.UpdateActivityReceiver.ACTION_REFRESH_OBJECT,
                MainActivity.UpdateActivityReceiver.ACTION_REFRESH_BOOKS);
        context.sendBroadcast(broadcastIntent);

    }

    @Override
    public void sendAuthorUpdateProgress(int total, int iCurrent, String name) {
        String str = " ["+iCurrent+"/"+total+"]:   "+name;
        Intent broadcastIntent = new Intent();
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.setAction(MainActivity.UpdateActivityReceiver.ACTION_RESP);
        broadcastIntent.putExtra(MainActivity.UpdateActivityReceiver.ACTION, MainActivity.UpdateActivityReceiver.ACTION_PROGRESS);
        broadcastIntent.putExtra(MainActivity.UpdateActivityReceiver.TOAST_STRING, str);
        context.sendBroadcast(broadcastIntent);

    }

    @Override
    public void finishUpdate(boolean result, List<Author> updatedAuthors) {
        Log.d(DEBUG_TAG, "Finish intent.");
        SettingsHelper settings = new SettingsHelper(context);
        if (settings.getLimitBookLifeTimeFlag()) {
            CleanBookServiceIntent.start(context);
        }
        if (currentCaller == UpdateServiceIntent.CALLER_IS_ACTIVITY) {//Call from activity

            CharSequence text;

            if (result) {//Good Call
                if (updatedAuthors.isEmpty()) {
                    text = context.getText(R.string.toast_update_good_empty);
                } else {
                    text = context.getText(R.string.toast_update_good_good);
                }

            } else {//Error call
                text = context.getText(R.string.toast_update_error);
            }
            Intent broadcastIntent = new Intent();
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            broadcastIntent.setAction(MainActivity.UpdateActivityReceiver.ACTION_RESP);
            broadcastIntent.putExtra(MainActivity.UpdateActivityReceiver.ACTION, MainActivity.UpdateActivityReceiver.ACTION_TOAST);
            broadcastIntent.putExtra(MainActivity.UpdateActivityReceiver.TOAST_STRING, text);
            context.sendBroadcast(broadcastIntent);
        }

        if (currentCaller == UpdateServiceIntent.CALLER_IS_RECEIVER) {//Call as a regular service


            if (result && updatedAuthors.isEmpty() && !settings.getDebugFlag()) {
                return;//no errors and no updates - no notification
            }

            if (!result && settings.getIgnoreErrorFlag()) {
                return;//error and we ignore them
            }

            NotificationData notifyData = NotificationData.getInstance(context);
            if (result) {//we have updates

                if (updatedAuthors.isEmpty()) {//DEBUG CASE
                    notifyData.notifyUpdateDebug(context);

                } else {

                    notifyData.notifyUpdate(context, updatedAuthors);
                }

            } else {//connection Error
                notifyData.notifyUpdateError(context);

            }
        }

    }

    @Override
    public void sendResult(String action,int numberOfAdded,int numberOfDeleted,int doubleAdd,int totalToAdd, long author_id) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.setAction(AuthorEditorServiceIntent.RECEIVER_FILTER);
        broadcastIntent.putExtra(AuthorEditorServiceIntent.EXTRA_ACTION_TYPE, action);
        broadcastIntent.putExtra(AuthorEditorServiceIntent.RESULT_ADD_NUMBER,numberOfAdded);
        broadcastIntent.putExtra(AuthorEditorServiceIntent.RESULT_DEL_NUMBER,numberOfDeleted);
        broadcastIntent.putExtra(AuthorEditorServiceIntent.RESULT_DOUBLE_NUMBER,doubleAdd);
        broadcastIntent.putExtra(AuthorEditorServiceIntent.RESULT_AUTHOR_ID, author_id);
        CharSequence msg="";
        if (action.equals(AuthorService.ACTION_ADD)){//ADD Action

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


        if (action.equals(AuthorService.ACTION_DELETE)){
            if (numberOfDeleted == 1){
                msg=context.getText(R.string.del_success);
            }
            else {
                msg=context.getText(R.string.del_error);
            }
        }
        broadcastIntent.putExtra(AuthorEditorServiceIntent.RESULT_MESSAGE,msg);

        context.sendBroadcast(broadcastIntent);

        if (numberOfAdded!=0 || numberOfDeleted != 0){
            SettingsHelper settings = new SettingsHelper(context);
            settings.requestBackup();
        }



    }


}
