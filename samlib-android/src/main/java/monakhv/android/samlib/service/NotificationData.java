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
package monakhv.android.samlib.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import static android.content.Context.NOTIFICATION_SERVICE;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


import monakhv.android.samlib.MainActivity;

import monakhv.android.samlib.R;
import monakhv.android.samlib.data.NamedObject;
import monakhv.android.samlib.data.SettingsHelper;
import monakhv.samlib.db.entity.Author;

/**
 *
 * @author monakhv
 */
class NotificationData implements Serializable {

    private static final String VAR_NAME = "NotificationData";
    private static final String DEBUG_TAG = "NotificationData";
    private static final String DEBUG_MESSAGE = "DEBUG MESSAGE";
    
    //private static final String WHERE=SQLController.COL_STATE_VAR_NAME + "=\"" + VAR_NAME + "\"";
    private static final String DATE_FORMAT = "dd.MM.yyyy HH:mm:ss";
    
    private static final int LIST_UPDATE_NOTIFICATION = 120;
    private static final int LIST_UPDATE_ERROR = 121;
    private List<Author> authors;
    private List<String>  lines;
    //private NotificationCompat.InboxStyle inboxStyle; //Not serializable !!!
    private int num = 0;

    private NotificationData() {
        authors = new ArrayList<>();
        lines      =  new ArrayList<>();
    }

    public static NotificationData getInstance(Context ctx) {
        NotificationData instance = loadData(ctx);
        if (instance == null) {
            instance = new NotificationData();
        }
        return instance;
    }

    /**
     * Make default builder for all types of notification
     *
     * @param helper SettingsHelper
     * @return notification builder object
     */
    private NotificationCompat.Builder makeNotification( SettingsHelper helper) {
        Intent notificationIntent = new Intent(helper.getContext(), MainActivity.class);
        notificationIntent.setAction(MainActivity.ACTION_CLEAN);
        PendingIntent contentIntent = PendingIntent.getActivity(helper.getContext(), 0,
                notificationIntent, PendingIntent.FLAG_ONE_SHOT);//because of autoCancel one shot must be good
        
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(helper.getContext());
        mBuilder.setContentIntent(contentIntent);

        mBuilder.setContentTitle( helper.getContext().getText(R.string.notification_updates).toString());


        mBuilder.setSound(helper.getNotificationRingToneURI());//sound
        mBuilder.setLights(0xff00ff00, 300, 100);//Indicator
        mBuilder.setAutoCancel(true);
        return mBuilder;
    }

    /**
     * Make notification for update successful all types
     *
     * @param settingsHelper Settings
     * @return Notification Builder
     */
    private NotificationCompat.Builder makeUpdateNotification( SettingsHelper settingsHelper) {
        NotificationCompat.Builder mBuilder = makeNotification(settingsHelper);

        mBuilder.setDeleteIntent(
                PendingIntent.getService(settingsHelper.getContext(), 0, CleanNotificationData.getIntent(settingsHelper.getContext()), PendingIntent.FLAG_ONE_SHOT)
        );//because of autoCancel one shot must be good
        
        mBuilder.setSmallIcon(R.drawable.note_book);
         mBuilder.setTicker(settingsHelper.getContext().getText(R.string.notification_updates));
        return mBuilder;
    }

   
    /**
     * construct new or add to existing data for inbox style
     *
     * @param updatedAuthors list of authors could be null for debug output
     *
     * */
    private void addData(List<Author> updatedAuthors) {


        if (updatedAuthors == null) {//DEBUG case
            SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT, Locale.FRENCH);
            
            lines.add(DEBUG_MESSAGE+": "+df.format(Calendar.getInstance().getTime()));
            ++num;
        } else {
            for (Author a : updatedAuthors) {
                if (!authors.contains(a)) {
                    ++num;
                    authors.add(a);
                    lines.add(a.getName());
                }
            }


        }

    }

    /**
     * load data from lines to inbox style object
     * @return Inbox Style
     */
    private NotificationCompat.InboxStyle getInboxStyle(){
       NotificationCompat.InboxStyle inbox = new NotificationCompat.InboxStyle();
       for (String line: lines){
           inbox.addLine(line);
       }
       return inbox;
    }
    /**
     * Make notification for successful update when we have really news
     *
     * @param settingsHelper Settings
     * @param updatedAuthors List of updated Authors
     */
    void notifyUpdate(SettingsHelper settingsHelper, List<Author> updatedAuthors) {
        NotificationManager notificationManager = (NotificationManager) settingsHelper.getContext()
                .getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = makeUpdateNotification(settingsHelper);

        String contentText =  settingsHelper.getContext().getText(R.string.author_update_number).toString();

        addData(updatedAuthors);
        
        NotificationCompat.InboxStyle inboxStyle  = getInboxStyle();
        if (updatedAuthors != null) {

            inboxStyle.setBigContentTitle( settingsHelper.getContext().getText(R.string.notification_updates).toString());
            mBuilder.setContentText(contentText + " " + num);
            inboxStyle.setSummaryText(contentText + " " + num);
        } else {
            mBuilder.setContentText("DEBUG MESSAGE");
            inboxStyle.setSummaryText("DEBUG MESSAGE - " + num + " update");
        }

        mBuilder.setStyle(inboxStyle);
        saveData(settingsHelper.getContext(), this);
        notificationManager.notify(LIST_UPDATE_NOTIFICATION, mBuilder.build());
    }

    /**
     * Debug updated
     *
     * @param settingsHelper Settings
     */
    void notifyUpdateDebug(SettingsHelper settingsHelper) {
        notifyUpdate(settingsHelper, null);
    }

    /**
     * Notification about error during update
     *
     * @param settingsHelper Settings
     */
    void notifyUpdateError(SettingsHelper settingsHelper) {
        NotificationManager notificationManager = (NotificationManager) settingsHelper.getContext()
                .getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = makeNotification(settingsHelper);
        mBuilder.setSmallIcon(android.R.drawable.stat_notify_error);
        mBuilder.setTicker(settingsHelper.getContext().getText(R.string.notification_error));
        mBuilder.setContentText(settingsHelper.getContext().getText(R.string.notification_update_error_detais).toString());

        notificationManager.notify(LIST_UPDATE_ERROR, mBuilder.build());

    }

    private static NamedObject getNO(Context ctx){
        return new NamedObject(ctx, VAR_NAME);
    }
    private static void saveData(Context ctx, NotificationData data) {
        Log.i(DEBUG_TAG, "saveData data call");
        
        getNO(ctx).save(data);
        
    }

    static void clean(Context ctx) {
        Log.i(DEBUG_TAG, "clean data call");
        getNO(ctx).clean();
        
    }

    private static NotificationData loadData(Context ctx) {
        Log.i(DEBUG_TAG, "loadData data call");


        return (NotificationData) getNO(ctx).get();
    }

    
}
