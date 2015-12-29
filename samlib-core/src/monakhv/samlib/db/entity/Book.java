/*
 * Copyright 2013 Dmitry Monakhov.
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
 */
package monakhv.samlib.db.entity;

import java.io.Serializable;
import java.util.Calendar;
import java.util.regex.Matcher;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import monakhv.samlib.data.AbstractSettings;
import monakhv.samlib.db.SQLController;


/**
 * @author monakhv
 */
@DatabaseTable(tableName = SQLController.TABLE_BOOKS)
public class Book implements Serializable {
    SqlOperation mSqlOperation;


    // http://blog.millermedeiros.com/using-integers-to-store-multiple-boolean-values/
    private static final int OPT_SELECTED = 1 << 0;
    private static final int OPT_PRESERVE = 1 << 1;
    @DatabaseField(columnName = SQLController.COL_BOOK_TITLE)
    protected String title;
    @DatabaseField(columnName = SQLController.COL_BOOK_AUTHOR)
    protected String authorName;
    @DatabaseField(columnName = SQLController.COL_BOOK_LINK)
    protected String uri;
    @DatabaseField(columnName = SQLController.COL_BOOK_DESCRIPTION)
    protected String description;
    @DatabaseField(columnName = SQLController.COL_BOOK_FORM)
    protected String form;
    @DatabaseField(columnName = SQLController.COL_BOOK_SIZE)
    protected long size;
    @DatabaseField(columnName = SQLController.COL_BOOK_DELTA)
    protected long delta;
    @DatabaseField(columnName = SQLController.COL_BOOK_DATE)
    protected long updateDate;//read from samlib
    @DatabaseField(columnName = SQLController.COL_BOOK_MTIME)
    protected long modifyTime;//change in BD
    @DatabaseField(columnName = SQLController.COL_BOOK_ISNEW)
    protected boolean isNew;
    @DatabaseField(columnName = SQLController.COL_ID, generatedId = true)
    protected int id;
    @DatabaseField(columnName = SQLController.COL_BOOK_GROUP_ID, foreign = true)
    protected GroupBook mGroupBook;
    @DatabaseField(columnName = SQLController.COL_BOOK_OPT)
    private int options;
    @DatabaseField(columnName = SQLController.COL_BOOK_AUTHOR_ID, foreign = true, canBeNull = false)
    private Author author;

    private AbstractSettings.FileType fileType;

    /**
     * Default constructor
     */
    public Book() {
        mSqlOperation=SqlOperation.DELETE;
        isNew = false;
        modifyTime = Calendar.getInstance().getTime().getTime();
        fileType = AbstractSettings.FileType.HTML;
        options = 0;
        mGroupBook = new GroupBook();
    }

    public Book(Author a, Matcher bookMatcher) {
        this();
        mSqlOperation=SqlOperation.INSERT;
        author=a;
        uri = a.getUrl() + bookMatcher.group(1);
        uri = uri.replaceFirst("/", "").replaceFirst(".shtml", "");

        title = bookMatcher.group(2);
        try {
            size = Long.valueOf(bookMatcher.group(3));
        } catch (NumberFormatException ex) {
            size = 0;
        }
        mGroupBook = new GroupBook(a,bookMatcher.group(4));

        form = bookMatcher.group(5);
        if (form.equalsIgnoreCase("")) {
            form = null;
        }
        description = bookMatcher.group(7);
        if (description == null){
            description=" ";//space for compatibility with old method
        }
        description = description.replaceAll("\"", "&quot;");

    }

    public SqlOperation getSqlOperation() {
        return mSqlOperation;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public boolean isIsNew() {
        return isNew;
    }

    public void setIsNew(boolean isNew) {
        this.isNew = isNew;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public long getAuthorId() {
        return author.getId();
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public long getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(long modifyTime) {
        this.modifyTime = modifyTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public GroupBook getGroupBook() {
        return mGroupBook;
    }

    public void setGroupBook(GroupBook groupBook) {
        mGroupBook = groupBook;
    }

    public AbstractSettings.FileType getFileType() {
        return fileType;
    }

    public void setFileType(AbstractSettings.FileType fileType) {
        this.fileType = fileType;
    }

    public int getOptions() {
        return options;
    }

    public void setOptions(int options) {
        this.options = options;
    }

    public boolean isPreserve() {
        return isOptionSelected(OPT_PRESERVE);
    }

    public boolean isSelected() {
        return isOptionSelected(OPT_SELECTED);
    }
    private boolean isOptionSelected(int mask){
        return (options & mask) == mask;
    }

    /**
     *  Set Flag on/off for given option
     *
     * @param flag flag to set
     * @param current current value of the flag
     * @param mask option mask
     */
    private void setOptionFlag(boolean flag,boolean current, int mask){
        if (flag) {
            if (!current) {
                options |= mask;//add option
                return;
            } else {
                return;//do nothing just return
            }
        } else {
            if (current) {
                options ^= mask;//remove option
                return;
            } else {
                return;//do nothing just return
            }
        }
    }

    public void setPreserve(boolean flag) {
        boolean cur = isPreserve();
        setOptionFlag(flag,cur,OPT_PRESERVE);

    }

    public void setSelected(boolean flag) {
        boolean cur = isSelected();
        setOptionFlag(flag,cur,OPT_SELECTED);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Book book = (Book) o;

        return uri != null ? uri.equals(book.uri) : book.uri == null;

    }

    @Override
    public int hashCode() {
        return uri != null ? uri.hashCode() : 0;
    }


    /**
     *  Return false if book is updated!!
     *
     * @param o
     * @return
     */
    public boolean isNeedUpdate(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Book book = (Book) o;

        if (size != book.size) return false;

        return description != null ? description.equals(book.description) : book.description == null;

    }



    @Override
    public String toString() {

        return "Book{" + "uri=" + uri + ", size=" + size + '}';
    }


    /**
     * Get book url to open it using web browser
     *
     * @return String url to open book for reading
     */
    public String getUrlForBrowser(AbstractSettings context) {
        SamLibConfig sc = SamLibConfig.getInstance(context);
        return sc.getBookUrlForBrowser(this);
    }

    /**
     * Get file object to store book for offline reading
     *
     * @return File to store book
     */
//    public File getFile() {
//        return DataExportImport._getBookFile(this,fileType);
//    }
    public String getFileMime() {
        return fileType.mime;
    }





}
