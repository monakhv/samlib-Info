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
 *  08.02.16 14:41
 *
 */

package monakhv.android.samlib.service;

import android.os.Parcel;
import android.os.Parcelable;
import monakhv.samlib.db.entity.Author;
import monakhv.samlib.db.entity.Book;

/**
 * Class is used to inform GUI that data set is changed
 *
 * Created by monakhv on 08.02.16.
 */
public class GuiUpdateObject implements Parcelable {
    public enum ObjectType {
        AUTHOR,
        BOOK,
        GROUP
    }
    public enum UpdateType {
        UPDATE,
        ADD,
        DELETE
    }



    private ObjectType mObjectType;
    private UpdateType mUpdateType;
    private  int mObjectId;
    private int mSortOrder=-1;

    public GuiUpdateObject(Book book,int sort){
        mObjectType=ObjectType.BOOK;
        mUpdateType=UpdateType.UPDATE;
        mObjectId =book.getId();
        mSortOrder=sort;
    }
    public GuiUpdateObject(Author author,int sort){
        mObjectType=ObjectType.AUTHOR;
        mUpdateType=UpdateType.UPDATE;
        mObjectId =author.getId();
        mSortOrder=sort;
    }


    public boolean isBook(){
        return mObjectType==ObjectType.BOOK;
    }

    public boolean isAuthor(){
        return mObjectType==ObjectType.AUTHOR;
    }


    public int getObjectId() {
        return mObjectId;
    }

    public int getSortOrder() {
        return mSortOrder;
    }

    protected GuiUpdateObject(Parcel in) {
        mObjectId = in.readInt();
        mObjectType=ObjectType.valueOf(in.readString());
        mUpdateType=UpdateType.valueOf(in.readString());
        mSortOrder=in.readInt();
    }

    public static final Creator<GuiUpdateObject> CREATOR = new Creator<GuiUpdateObject>() {
        @Override
        public GuiUpdateObject createFromParcel(Parcel in) {
            return new GuiUpdateObject(in);
        }

        @Override
        public GuiUpdateObject[] newArray(int size) {
            return new GuiUpdateObject[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mObjectId);
        dest.writeString(mObjectType.name());
        dest.writeString(mUpdateType.name());
        dest.writeInt(mSortOrder);
    }




}
