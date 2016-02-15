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
import monakhv.samlib.service.SamlibUpdateProgress;

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

    public CharSequence makeMessage(GuiUpdateObject guiUpdateObject) {
        Result result = (Result) guiUpdateObject.getObject();

        CharSequence msg = "";
        if (guiUpdateObject.getUpdateType() == GuiUpdateObject.UpdateType.ADD) {//ADD Action

            if (result.getTotalToAdd() == 1) {//add single author
                if (result.getNumberOfAdded() == 1) {
                    msg = mContext.getText(R.string.add_success);
                } else if (result.getDoubleAdd() == 1) {
                    msg = mContext.getText(R.string.add_error_double);
                } else {
                    msg = mContext.getText(R.string.add_error);
                }
            } else {//import list of authors
                msg = mContext.getText(R.string.add_success_multi) + " " + result.getNumberOfAdded();

                if (result.getDoubleAdd() != 0) {//double is here
                    msg = msg + "<br>" + mContext.getText(R.string.add_success_double) + " " + result.getDoubleAdd();
                }
            }
        }//end ADD Action


        if (guiUpdateObject.getUpdateType() == GuiUpdateObject.UpdateType.DELETE) {
            if (result.getNumberOfDeleted() == 1) {
                msg = mContext.getText(R.string.del_success);
            } else {
                msg = mContext.getText(R.string.del_error);
            }
        }
        return msg;
    }

    public void updateNotification(SamlibUpdateProgress progress) {
        if (mProgressNotification == null) {
            mProgressNotification = new ProgressNotification(mSettingsHelper, "text");
        }
        mProgressNotification.updateProgress(progress.getTotal(), progress.getCurrent(), progress.getName());
    }

    public void updateNotification(Author author) {
        if (mProgressNotification == null) {
            mProgressNotification = new ProgressNotification(mSettingsHelper, "text");
        }
        mProgressNotification.update(author);
    }


    private void showMessage(int res) {
        int duration = Toast.LENGTH_SHORT;
        CharSequence msg = mContext.getString(res);
        Toast toast = Toast.makeText(mContext, msg, duration);
        toast.show();
    }

    public void showUpdateMessage(Result res) {
        if (res.isRes()) {
            if (res.getNumberOfUpdated() == 0) {
                showMessage(R.string.toast_update_good_empty);
            } else {
                showMessage(R.string.toast_update_good_good);
            }

        } else {
            showMessage(R.string.toast_update_error);
        }

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
