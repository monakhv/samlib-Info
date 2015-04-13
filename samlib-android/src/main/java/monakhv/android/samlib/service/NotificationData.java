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


import monakhv.android.samlib.MainActivity;

import monakhv.android.samlib.R;
import monakhv.android.samlib.data.NamedObject;
import monakhv.android.samlib.data.SettingsHelper;
import monakhv.samlib.db.entity.Author;

/**
 *
 * @author monakhv
 */
public class NotificationData implements Serializable {

    private static final String VAR_NAME = "NotificationData";
    private static final String DEBUG_TAG = "NotificationData";
    private static final String DEBUG_MESSAGE = "DEBUG MESSAGE";
    
    //private static final String WHERE=SQLController.COL_STATE_VAR_NAME + "=\"" + VAR_NAME + "\"";
    private static final String DATE_FORMAT = "dd.MM.yyyy HH:mm:ss";
    
    public static final int LIST_UPDATE_NOTIFICATION = 120;
    public static final int LIST_UPDATE_ERROR = 121;
    private static NotificationData instance = null;
    private List<Author> authors;
    private List<String>  lines;
    //private NotificationCompat.InboxStyle inboxStyle; //Not serializable !!!
    private int num = 0;

    private NotificationData() {
        authors = new ArrayList<Author>();
        lines      =  new ArrayList<String>();
    }

    public static NotificationData getInstance(Context ctx) {
        instance = loadData(ctx);
        if (instance == null) {
            instance = new NotificationData();
        }
        return instance;
    }

    /**
     * Make default builder for all types of notification
     *
     * @param context
     * @return notification builder object
     */
    private NotificationCompat.Builder makeNotification(Context context) {
        Intent notificationIntent = new Intent(context,
                MainActivity.class);
        notificationIntent.putExtra(MainActivity.CLEAN_NOTIFICATION, MainActivity.CLEAN_NOTIFICATION);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                notificationIntent, 0);
        
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setContentIntent(contentIntent);

        mBuilder.setContentTitle(context.getText(R.string.notification_updates).toString());

        SettingsHelper helper = new SettingsHelper(context);
        mBuilder.setSound(helper.getNotificationRingToneURI());//sound
        mBuilder.setLights(0xff00ff00, 300, 100);//Indicator
        mBuilder.setAutoCancel(true);
        return mBuilder;
    }

    /**
     * Make notification for update successful all types
     *
     * @param context
     * @return
     */
    private NotificationCompat.Builder makeUpdateNotification(Context context) {
        NotificationCompat.Builder mBuilder = makeNotification(context);
        
        mBuilder.setDeleteIntent(PendingIntent.getService(context, 0, CleanNotificationData.getIntent(context), 0));
        
        mBuilder.setSmallIcon(R.drawable.note_book);
        mBuilder.setTicker(context.getText(R.string.notification_updates));
        return mBuilder;
    }

   
    /**
     * construct new or add to existing data for inbox style
     *
     * @param updatedAuthors list of authors could be null for debug output
     *
     * @return
     */
    private void addData(List<Author> updatedAuthors) {


        if (updatedAuthors == null) {//DEBUG case
            SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
            
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
     * @return 
     */
    private NotificationCompat.InboxStyle getinboxStyle(){
       NotificationCompat.InboxStyle inbox = new NotificationCompat.InboxStyle();
       for (String line: lines){
           inbox.addLine(line);
       }
       return inbox;
    }
    /**
     * Make notification for successful update when we have really news
     *
     * @param context
     * @param updatedAuthors
     */
    public void notifyUpdate(Context context, List<Author> updatedAuthors) {
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = makeUpdateNotification(context);

        String contentText = context.getText(R.string.author_update_number).toString();

        addData(updatedAuthors);
        
        NotificationCompat.InboxStyle inboxStyle  =getinboxStyle();
        if (updatedAuthors != null) {

            inboxStyle.setBigContentTitle(context.getText(R.string.notification_updates).toString());
            mBuilder.setContentText(contentText + " " + num);
            inboxStyle.setSummaryText(contentText + " " + num);
        } else {
            mBuilder.setContentText("DEBUG MESSAGE");
            inboxStyle.setSummaryText("DEBUG MESSAGE - " + num + " update");
        }

        mBuilder.setStyle(inboxStyle);
        saveData(context, this);
        notificationManager.notify(LIST_UPDATE_NOTIFICATION, mBuilder.build());
    }

    /**
     * Debug updated
     *
     * @param context
     */
    public void notifyUpdateDebug(Context context) {
        notifyUpdate(context, null);
    }

    /**
     * Notification about error during update
     *
     * @param context
     */
    public void notifyUpdateError(Context context) {
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = makeNotification(context);
        mBuilder.setSmallIcon(android.R.drawable.stat_notify_error);
        mBuilder.setTicker(context.getText(R.string.notification_error));
        mBuilder.setContentText(context.getText(R.string.notification_update_error_detais).toString());

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
        NotificationData data = (NotificationData) getNO(ctx).get();
        
        
        return data;
    }

    
}
