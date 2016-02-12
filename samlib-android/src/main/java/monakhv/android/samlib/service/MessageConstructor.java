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
import monakhv.android.samlib.R;
import monakhv.samlib.service.GuiUpdateObject;
import monakhv.samlib.service.Result;
import monakhv.samlib.service.SamlibService;

/**
 * Created by monakhv on 12.02.16.
 */
public class MessageConstructor {
    private Context mContext;

    public MessageConstructor(Context context) {
        mContext = context;
    }

    public CharSequence makeMessage(GuiUpdateObject guiUpdateObject){
        Result result = (Result) guiUpdateObject.getObject();

        CharSequence msg="";
        if (guiUpdateObject.getUpdateType()==GuiUpdateObject.UpdateType.ADD){//ADD Action

            if (result.getTotalToAdd() == 1){//add single author
                if (result.getNumberOfAdded() ==1 ) {
                    msg = mContext.getText(R.string.add_success);
                }
                else if (result.getDoubleAdd() ==1) {
                    msg = mContext.getText(R.string.add_error_double);
                }
                else {
                    msg = mContext.getText(R.string.add_error);
                }
            }
            else {//import list of authors
                msg = mContext.getText(R.string.add_success_multi)+" "+result.getNumberOfAdded();

                if (result.getDoubleAdd() != 0) {//double is here
                    msg = msg +"<br>"+ mContext.getText(R.string.add_success_double)+" "+result.getDoubleAdd();
                }
            }
        }//end ADD Action


        if (guiUpdateObject.getUpdateType()==GuiUpdateObject.UpdateType.DELETE){
            if (result.getNumberOfDeleted() == 1){
                msg= mContext.getText(R.string.del_success);
            }
            else {
                msg= mContext.getText(R.string.del_error);
            }
        }
        return msg;
    }
}
