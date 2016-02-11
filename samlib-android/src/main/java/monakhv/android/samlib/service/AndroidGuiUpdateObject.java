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
 *  11.02.16 13:35
 *
 */

package monakhv.android.samlib.service;

import android.os.Parcel;
import android.os.Parcelable;
import monakhv.samlib.service.GuiUpdateObject;

/**
 * Created by monakhv on 11.02.16.
 */
public class AndroidGuiUpdateObject  implements Parcelable  {
    private GuiUpdateObject mGuiUpdateObject;
    public AndroidGuiUpdateObject(GuiUpdateObject guiUpdateObject) {
        mGuiUpdateObject=guiUpdateObject;
    }

    protected AndroidGuiUpdateObject(Parcel in) {
        mGuiUpdateObject=new GuiUpdateObject();
        mGuiUpdateObject.setObjectId(in.readInt());
        mGuiUpdateObject.setObjectType(GuiUpdateObject.ObjectType.valueOf(in.readString()));
        mGuiUpdateObject.setUpdateType(GuiUpdateObject.UpdateType.valueOf(in.readString()));
        mGuiUpdateObject.setSortOrder(in.readInt());
    }

    public GuiUpdateObject getGuiUpdateObject() {
        return mGuiUpdateObject;
    }

    public static final Creator<AndroidGuiUpdateObject> CREATOR = new Creator<AndroidGuiUpdateObject>() {
        @Override
        public AndroidGuiUpdateObject createFromParcel(Parcel in) {
            return new AndroidGuiUpdateObject(in);
        }

        @Override
        public AndroidGuiUpdateObject[] newArray(int size) {
            return new AndroidGuiUpdateObject[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mGuiUpdateObject.getObjectId());
        dest.writeString(mGuiUpdateObject.getObjectType().name());
        dest.writeString(mGuiUpdateObject.getUpdateType().name());
        dest.writeInt(mGuiUpdateObject.getSortOrder());
    }
}
