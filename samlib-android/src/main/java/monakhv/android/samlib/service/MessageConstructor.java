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
 *  12.02.16 15:57
 *
 */

package monakhv.android.samlib.service;

import android.content.Context;
import android.widget.Toast;
import monakhv.android.samlib.R;
import monakhv.android.samlib.data.SettingsHelper;
import monakhv.samlib.db.entity.Author;
import monakhv.samlib.service.GuiUpdateObject;
import monakhv.samlib.service.Result;
import monakhv.samlib.service.AuthorUpdateProgress;

/**
 * Make display messages and notifications
 * for update data
 * Created by monakhv on 12.02.16.
 */
public class MessageConstructor {
    private Context mContext;
    private ProgressNotification mProgressNotification;
    private SettingsHelper mSettingsHelper;

    public MessageConstructor(Context context, SettingsHelper settingsHelper) {
        mContext = context;
        mSettingsHelper = settingsHelper;
    }

    public String makeMessage(GuiUpdateObject guiUpdateObject) {
        Result result = (Result) guiUpdateObject.getObject();

        //CharSequence msg = "";
        StringBuilder sb = new StringBuilder();
        if (guiUpdateObject.getUpdateType() == GuiUpdateObject.UpdateType.ADD) {//ADD Action

            if (result.getTotalToAdd() == 1) {//add single author
                if (result.getNumberOfAdded() == 1) {
                     sb.append(mContext.getString(R.string.add_success));
                } else if (result.getDoubleAdd() == 1) {
                    sb.append(mContext.getString(R.string.add_error_double));
                } else {
                    sb.append(mContext.getString(R.string.add_error));
                }
            } else {//import list of authors
                sb.append(mContext.getString(R.string.add_success_multi)).append(" ").append(result.getNumberOfAdded());

                if (result.getDoubleAdd() != 0) {//double is here
                    sb.append("<br>").append(mContext.getString(R.string.add_success_double)).append(" ").append(result.getDoubleAdd());
                }
            }
        }//end ADD Action


        if (guiUpdateObject.getUpdateType() == GuiUpdateObject.UpdateType.DELETE) {
            if (result.getNumberOfDeleted() == 1) {
                sb.append(mContext.getString(R.string.del_success));
            } else {
                sb.append(mContext.getString(R.string.del_error));
            }
        }

        if (guiUpdateObject.getUpdateType()==GuiUpdateObject.UpdateType.UPDATE_UPDATE){
            if (result.isRes()) {
                if (result.getNumberOfUpdated() == 0) {
                    sb.append(mContext.getString(R.string.toast_update_good_empty));
                } else {
                    sb.append(mContext.getString(R.string.toast_update_good_good));
                }

            } else {
                sb.append(mContext.getString(R.string.toast_update_error));
            }
        }
        return sb.toString();
    }

    /**
     * Show Progress update Notification
     * @param progress current progress state
     */
    public void updateNotification(AuthorUpdateProgress progress) {
        if (mProgressNotification == null) {
            mProgressNotification = new ProgressNotification(mSettingsHelper, "text");
        }
        mProgressNotification.updateProgress(progress.getTotal(), progress.getCurrent(), progress.getName());
    }

    /**
     * Modify Progress Notification and put Update for the Author
     * @param author The Author who has update
     */
    public void updateNotification(Author author) {
        if (mProgressNotification == null) {
           return;
        }
        mProgressNotification.update(author);
    }


    private void showMessage(String msg) {
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(mContext, msg, duration);
        toast.show();
    }

    public void showMessage(GuiUpdateObject guiUpdateObject) {
        showMessage(makeMessage(guiUpdateObject));
    }

    /**
     * Show Notification for Update Result
     *
     * @param res Result status
     */
    public void showUpdateNotification(Result res){
        if (res.isRes() && res.getUpdatedAuthors().isEmpty() && !mSettingsHelper.getDebugFlag()) {
            return;//no errors and no updates - no notification
        }

        if (!res.isRes() && mSettingsHelper.getIgnoreErrorFlag()) {
            return;//error and we ignore them
        }

        NotificationData notifyData = NotificationData.getInstance(mContext);
        if (res.isRes()) {//we have updates

            if (res.getUpdatedAuthors().isEmpty()) {//DEBUG CASE
                notifyData.notifyUpdateDebug(mSettingsHelper);

            } else {

                notifyData.notifyUpdate(mSettingsHelper, res.getUpdatedAuthors());
            }

        } else {//connection Error
            notifyData.notifyUpdateError(mSettingsHelper);

        }
    }

    public void cancelProgress() {
        if (mProgressNotification != null) {
            mProgressNotification.cancel();
            mProgressNotification = null;
        }
    }
}
