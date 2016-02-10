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
 *  10.02.16 14:28
 *
 */

package monakhv.android.samlib.service;

import android.os.Parcel;
import android.os.Parcelable;
import monakhv.samlib.db.entity.Author;
import monakhv.samlib.db.entity.Book;
import monakhv.samlib.db.entity.GroupBook;

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
        UPDATE_READ,
        ADD,
        DELETE,
        UPDATE_UPDATE
    }



    private ObjectType mObjectType;
    private UpdateType mUpdateType;
    private  int mObjectId;
    private int mSortOrder=-1;

    /**
     * Update Book Gui after book new mark changes
     *
     * @param book Book was changed
     * @param sort changed Book new position inside its group
     */
    public GuiUpdateObject(Book book,int sort){
        mObjectType=ObjectType.BOOK;
        mUpdateType=UpdateType.UPDATE_READ;
        mObjectId =book.getId();
        mSortOrder=sort;
    }
    /**
     * Update Author  Gui after author new mark changes
     *
     * @param author Author was changed
     * @param sort changed Author new position
     */
    public GuiUpdateObject(Author author,int sort){
        mObjectType=ObjectType.AUTHOR;
        mUpdateType=UpdateType.UPDATE_READ;
        mObjectId =author.getId();
        mSortOrder=sort;
    }
    /**
     * Update Book Gui after groupBook new mark changes
     *
     * @param groupBook GroupBook was changed
     * @param sort changed GroupBook new position
     */
    public GuiUpdateObject(GroupBook groupBook, int sort){
        mObjectType=ObjectType.GROUP;
        mUpdateType=UpdateType.UPDATE_READ;
        mObjectId =groupBook.getId();
        mSortOrder=sort;
    }

    /**
     *
     * @param sort last position of deleted Author
     */
    /**
     *  Update Author GUI after delete/Add  Author
     * @param id Author Id
     * @param sort old position for deleted or new position for added authors
     * @param updateType  Modification type
     */
    public GuiUpdateObject(int id, int sort, UpdateType updateType){
        mObjectType=ObjectType.AUTHOR;
        mObjectId =id;
        mSortOrder=sort;
        mUpdateType=updateType;
    }

    public boolean isBook(){
        return mObjectType==ObjectType.BOOK;
    }

    public boolean isAuthor(){
        return mObjectType==ObjectType.AUTHOR;
    }

    public boolean isGroup(){
        return mObjectType==ObjectType.GROUP;
    }

    public int getObjectId() {
        return mObjectId;
    }

    public int getSortOrder() {
        return mSortOrder;
    }

    public UpdateType getUpdateType() {
        return mUpdateType;
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
