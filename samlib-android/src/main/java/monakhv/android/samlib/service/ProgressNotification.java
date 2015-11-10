package monakhv.android.samlib.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import monakhv.android.samlib.MainActivity;


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
 * 10.11.15.
 */
public class ProgressNotification {
    public static final int NOTIFICATION_ID = 210;
    private final NotificationManager mNotifyManager;
    private final NotificationCompat.Builder mBuilder;

    public ProgressNotification(Context ctx) {


        mNotifyManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(ctx.getApplicationContext());
        mBuilder
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_MAX)
                .setCategory(Notification.CATEGORY_PROGRESS)
                .setSmallIcon(android.R.drawable.stat_sys_download);

    }


    public void updateProgress(int total, int iCurrent, String name) {
        mBuilder
                .setProgress(total, iCurrent, false)
                .setContentTitle(name)
                .setTicker(name)
                .setAutoCancel(false)
                .setContentText(" [" + iCurrent + "/" + total + "]:   " + name);


        mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());

    }

    public void cancel() {
        mNotifyManager.cancel(NOTIFICATION_ID);
    }

}
