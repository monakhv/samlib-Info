package monakhv.android.samlib.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import monakhv.android.samlib.R;


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
        Intent intend = new Intent(ctx, UpdateLocalService.class);
        intend.putExtra(UpdateLocalService.ACTION_TYPE, UpdateLocalService.ACTION_STOP);
        PendingIntent pInt = PendingIntent.getService(ctx, 0, intend, 0);
        mBuilder
                .setOngoing(true)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .addAction(R.drawable.ic_cancel_white_36dp, ctx.getText(R.string.Cancel), pInt)
                .setDeleteIntent(pInt);

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
        mBuilder
                .setContentTitle(name)
                .setTicker(name)
                .setAutoCancel(false);
        if (total == 1) {//Single Author update
            mBuilder
                    .setProgress(0, 0, true)
                    .setContentText(name);

        } else {//Update by tag
            mBuilder
                    .setProgress(total, iCurrent, false)
                    .setContentText(" [" + iCurrent + "/" + total + "]:   " + name);
        }


        mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());

    }

    public void cancel() {
        mNotifyManager.cancel(NOTIFICATION_ID);
    }

}
