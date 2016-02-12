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
 *  11.02.16 13:23
 *
 */

package monakhv.samlib.service;

import monakhv.samlib.db.entity.Author;
import monakhv.samlib.db.entity.Book;
import monakhv.samlib.db.entity.GroupBook;
import monakhv.samlib.db.entity.Tag;

/**
 * Class is used to inform GUI that data set is changed
 * <p/>
 * Created by monakhv on 08.02.16.
 */
public class GuiUpdateObject  {
    public enum ObjectType {
        AUTHOR,
        BOOK,
        GROUP,
        TAG,
        RESULT
    }

    public enum UpdateType {
        UPDATE_READ,
        ADD,
        DELETE,
        UPDATE_UPDATE
    }

    private Object mObject;
    protected ObjectType mObjectType;
    protected UpdateType mUpdateType;
    protected int mObjectId=-1;
    protected int mSortOrder = -1;

    public GuiUpdateObject(){

    }

    public GuiUpdateObject(Result result,UpdateType updateType){
        mObject=result;
        mUpdateType=updateType;
        mObjectType=ObjectType.RESULT;
    }
    public GuiUpdateObject(ObjectType objectType){
        mObjectType=objectType;
    }
    /**
     * Update Book Gui after book new mark changes
     *
     * @param book Book was changed
     * @param sort changed Book new position inside its group
     */
    public GuiUpdateObject(Book book, int sort) {
        mObject=book;
        mObjectType = ObjectType.BOOK;
        mUpdateType = UpdateType.UPDATE_READ;
        mObjectId = book.getId();
        mSortOrder = sort;
    }

    /**
     * Update Author  Gui after author new mark changes
     *
     * @param author Author was changed
     * @param sort   changed Author new position
     */
    public GuiUpdateObject(Author author, int sort) {
        mObject=author;
        mObjectType = ObjectType.AUTHOR;
        mUpdateType = UpdateType.UPDATE_READ;
        mObjectId = author.getId();
        mSortOrder = sort;
    }

    /**
     * Update Book Gui after groupBook new mark changes
     *
     * @param groupBook GroupBook was changed
     * @param sort      changed GroupBook new position
     */
    public GuiUpdateObject(GroupBook groupBook, int sort) {
        mObject=groupBook;
        mObjectType = ObjectType.GROUP;
        mUpdateType = UpdateType.UPDATE_READ;
        if (groupBook == null) {
            mObjectId = -1;
        } else {
            mObjectId = groupBook.getId();
        }

        mSortOrder = sort;
    }

    /**
     *
     * @param sort last position of deleted Author
     */
    /**
     * Update Author GUI after delete/Add  Author
     *
     * @param a        Author
     * @param sort       old position for deleted or new position for added authors
     * @param updateType Modification type
     */
    public GuiUpdateObject(Author a, int sort, UpdateType updateType) {
        mObject=a;
        mObjectType = ObjectType.AUTHOR;
        mObjectId = a.getId();
        mSortOrder = sort;
        mUpdateType = updateType;
    }

    public boolean isBook() {
        return mObjectType == ObjectType.BOOK;
    }

    public boolean isAuthor() {
        return mObjectType == ObjectType.AUTHOR;
    }

    public boolean isGroup() {
        return mObjectType == ObjectType.GROUP;
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

    public ObjectType getObjectType() {
        return mObjectType;
    }

    public Object getObject() {
        return mObject;
    }

    public void setObjectType(ObjectType objectType) {
        mObjectType = objectType;
    }

    public void setUpdateType(UpdateType updateType) {
        mUpdateType = updateType;
    }

    public void setObjectId(int objectId) {
        mObjectId = objectId;
    }

    public void setSortOrder(int sortOrder) {
        mSortOrder = sortOrder;
    }



}
