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
import java.util.Date;
import java.util.TimeZone;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import monakhv.samlib.data.AbstractSettings;
import monakhv.samlib.db.SQLController;
import monakhv.samlib.exception.BookParseException;

/**
 * @author monakhv
 */
@DatabaseTable(tableName = SQLController.TABLE_BOOKS)
public class Book implements Serializable {

    public static final int SELECTED_GROUP_ID = 1;

    private static final int BOOK_LINK = 0;
    private static final int BOOK_AUTHOR = 1;
    private static final int BOOK_TITLE = 2;
    private static final int BOOK_FORM = 3;
    private static final int BOOK_SIZE = 4;
    private static final int BOOK_DATE = 5;
    private static final int BOOK_VOTE_RESULT = 6;
    private static final int BOOK_VOTE_COUNT = 7;
    private static final int BOOK_DESCRIPTION = 8;

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
        isNew = false;
        updateDate = Calendar.getInstance().getTime().getTime();
        modifyTime = Calendar.getInstance().getTime().getTime();
        fileType = AbstractSettings.FileType.HTML;
        options = 0;
    }

    /**
     * Parsing HTTP get string and construct Book object
     *
     * @param string2parse input single string to parse
     * @throws monakhv.samlib.exception.BookParseException
     */
    public Book(String string2parse) throws BookParseException {
        this();
        String[] strs = string2parse.split(SamLibConfig.SPLIT);
        title = strs[BOOK_TITLE];
        authorName = strs[BOOK_AUTHOR];
        uri = strs[BOOK_LINK];
        description = strs[BOOK_DESCRIPTION];
        form = strs[BOOK_FORM];
        try {
            size = Long.valueOf(strs[BOOK_SIZE]);
        } catch (NumberFormatException ex) {
            size = 0;
            //System.out.println("NumberFormatException!");
            //System.out.println("- "+string2parse);
        }
        Calendar cal = string2Cal(strs[BOOK_DATE]);


        updateDate = cal.getTimeInMillis();


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

    public long getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(long updateDate) {
        this.updateDate = updateDate;
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
        return Book.isPreserved(options);
    }

    public boolean isSelected() {
        return Book.isSelected(options);
    }

    public void setPreserve(boolean flag) {
        boolean cur = isPreserve();
        if (flag) {
            if (!cur) {
                options |= OPT_PRESERVE;//add option
                return;
            } else {
                return;//do nothing just return
            }
        } else {
            if (cur) {
                options ^= OPT_PRESERVE;//remove option
                return;
            } else {
                return;//do nothing just return
            }
        }

    }

    public void setSelected(boolean flag) {
        boolean cur = isSelected();
        if (flag){
            if (!cur){
                options |= OPT_SELECTED;//add option
                return;
            }
            else {
                return;
            }

        }
        else {
            if(cur){
                options ^= OPT_SELECTED;//remove option
                return;
            }
            else {
                return;
            }

        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + (this.uri != null ? this.uri.hashCode() : 0);
        hash = 13 * hash + (int) (this.size ^ (this.size >>> 32));
        hash = 13 * hash + (int) (this.updateDate ^ (this.updateDate >>> 32));
        return hash;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || ((Object) this).getClass() != obj.getClass()) {
            return false;
        }
        final Book other = (Book) obj;
        if ((this.uri == null) ? (other.uri != null) : !this.uri.equals(other.uri)) {
            return false;
        }
        if ((this.description == null) ? (other.description != null) : !this.description.equals(other.description)) {
            return false;
        }
        if (this.size != other.size) {
            return false;
        }
        return this.updateDate == other.updateDate;
    }

    @Override
    public String toString() {
        Date d = new Date(updateDate);
        return "Book{" + "uri=" + uri + ", size=" + size + ", updateDate=" + d + '}';
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


    public static Calendar string2Cal(String str) throws BookParseException {
        String[] dd = str.split("/");

        if (dd.length != 3) {
            throw new BookParseException("Date string: " + str);
        }
        int day = Integer.valueOf(dd[0]);
        int month = Integer.valueOf(dd[1]);
        int year = Integer.valueOf(dd[2]);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.YEAR, year);


        return cal;
    }

    public static boolean isPreserved(int options) {
        return (options & OPT_PRESERVE) == OPT_PRESERVE;
    }

    public static boolean isSelected(int options) {
        return (options & OPT_SELECTED) == OPT_SELECTED;
    }
}
