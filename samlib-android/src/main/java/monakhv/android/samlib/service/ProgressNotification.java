package monakhv.android.samlib.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import monakhv.android.samlib.MainActivity;
import monakhv.android.samlib.R;
import monakhv.android.samlib.data.SettingsHelper;
import monakhv.samlib.db.entity.Author;


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
    private final SettingsHelper mSettingsHelper;
    private int numberUpdated =0;
    private final Context mContext;
    NotificationCompat.InboxStyle mStyle;


    public ProgressNotification(SettingsHelper settingsHelper, String notificationTitle) {
        mSettingsHelper = settingsHelper;
        mContext =settingsHelper.getContext();
        mStyle=new NotificationCompat.InboxStyle();


        mNotifyManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(mContext.getApplicationContext());
        Intent intend = new Intent(mContext, UpdateLocalService.class);
        intend.setAction(UpdateLocalService.ACTION_STOP);
        PendingIntent stopService = PendingIntent.getService(mContext, 0, intend, PendingIntent.FLAG_ONE_SHOT);//this remove notification and one shot will be good

        Intent notificationIntent = new Intent(mContext, MainActivity.class);
        PendingIntent returnToActivity = PendingIntent.getActivity(mContext, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);//must be permanent because of update the same notification


        mBuilder
                .setOngoing(true)
                .setAutoCancel(false)
                .setSmallIcon(android.R.drawable.ic_popup_sync)
                .addAction(R.drawable.ic_cancel_white_36dp, mContext.getText(R.string.Cancel), stopService)
                .setContentTitle(notificationTitle)
                .setContentIntent(returnToActivity)
                .setStyle(mStyle)
                .setOngoing(true)
                .setDeleteIntent(stopService);


        initBuilder();
    }

    private void initBuilder() {
        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk >= 16) {
            builder16();
        }
        if (sdk >= 21) {
            builder21();
        }
    }
    @TargetApi(16)
    private void builder16(){
        mBuilder
                .setPriority(Notification.PRIORITY_MAX);//16
    }

    @TargetApi(21)
    private  void builder21(){
        mBuilder
                .setCategory(Notification.CATEGORY_PROGRESS);//21
    }


    public void updateProgress(int total, int iCurrent, String name) {


        if (mSettingsHelper.isNotifyTickerEnable()) {
            mBuilder.setTicker(name);
        }
        if (total == 1) {//Single Author update
            mBuilder.setProgress(0, 0, true);
            mStyle.setBigContentTitle(name);

        } else {//Update by tag
            mBuilder.setProgress(total, iCurrent, false);
            mStyle.setBigContentTitle(" [" + iCurrent + "/" + total + "]:   " + name);
        }


        mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());

    }

    public void cancel() {
        mNotifyManager.cancel(NOTIFICATION_ID);
    }

    public void update(Author a) {
        ++numberUpdated;
        mStyle.addLine(a.getName());
        mStyle.setSummaryText(mContext.getString(R.string.notification_summary)+" "+numberUpdated);
        mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());


    }
}
