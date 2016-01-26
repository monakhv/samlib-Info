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
 *  18.01.16 12:40
 *
 */

package monakhv.android.samlib.service;

import android.os.Parcel;
import android.os.Parcelable;
import monakhv.samlib.service.SamlibService;

/**
 * Class to store data we need to update
 * Created by monakhv on 18.01.16.
 */
public class UpdateObject implements Parcelable {
    public static final UpdateObject UNDEF;
    public static final UpdateObject ACTIVITY_CALLER;

    static {
        UNDEF=new UpdateObject();
        UNDEF.mCALLER_type=AndroidGuiUpdater.CALLER_TYPE.CALLER_IS_UNDEF;
        UNDEF.mObjectType= SamlibService.UpdateObjectSelector.UNDEF;

        ACTIVITY_CALLER=new UpdateObject();
        ACTIVITY_CALLER.mCALLER_type=AndroidGuiUpdater.CALLER_TYPE.CALLER_IS_ACTIVITY;
        ACTIVITY_CALLER.mObjectType= SamlibService.UpdateObjectSelector.UNDEF;
    }
    private SamlibService.UpdateObjectSelector mObjectType;
    private AndroidGuiUpdater.CALLER_TYPE mCALLER_type;
    private int mObjectId;


    public UpdateObject(){
        mObjectType=SamlibService.UpdateObjectSelector.Tag;
        mCALLER_type= AndroidGuiUpdater.CALLER_TYPE.CALLER_IS_RECEIVER;

    }

    public  UpdateObject(SamlibService.UpdateObjectSelector selector, int id){
        mObjectType=selector;
        mObjectId =id;
        mCALLER_type= AndroidGuiUpdater.CALLER_TYPE.CALLER_IS_ACTIVITY;
    }

    protected UpdateObject(Parcel in) {
        mObjectType = SamlibService.UpdateObjectSelector.valueOf(in.readString());
        mCALLER_type= AndroidGuiUpdater.CALLER_TYPE.valueOf(in.readString());
        mObjectId=in.readInt();

    }

    public static final Creator<UpdateObject> CREATOR = new Creator<UpdateObject>() {
        @Override
        public UpdateObject createFromParcel(Parcel in) {
            return new UpdateObject(in);
        }

        @Override
        public UpdateObject[] newArray(int size) {
            return new UpdateObject[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mObjectType.name());
        dest.writeString(mCALLER_type.name());
        dest.writeInt(mObjectId);

    }

    public AndroidGuiUpdater.CALLER_TYPE getCALLER_type() {
        return mCALLER_type;
    }

    public SamlibService.UpdateObjectSelector getObjectType() {
        return mObjectType;
    }

    public int getObjectId() {
        return mObjectId;
    }

    public void setObjectId(int objectId) {
        mObjectId = objectId;
    }

    public boolean callerIsReceiver(){
        return mCALLER_type==AndroidGuiUpdater.CALLER_TYPE.CALLER_IS_RECEIVER;
    }
    public boolean callerIsActivity(){
        return mCALLER_type==AndroidGuiUpdater.CALLER_TYPE.CALLER_IS_ACTIVITY;
    }
}
