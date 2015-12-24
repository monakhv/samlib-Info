package monakhv.samlib.db.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import monakhv.samlib.db.SQLController;

import java.io.Serializable;

/**
 * Entity to Store Book group
 * Created by monakhv on 24.12.15.
 */
@DatabaseTable(tableName = SQLController.TABLE_GROUP_BOOK)
public class GroupBook  implements Serializable {
    @DatabaseField(columnName = SQLController.COL_ID, generatedId = true)
    protected int id;
    @DatabaseField(columnName = SQLController.COL_BOOK_AUTHOR_ID,foreign = true,canBeNull = false)
    private Author author;
    @DatabaseField(columnName = SQLController.COL_NAME)
    protected String name;
    @DatabaseField(columnName = SQLController.COL_isnew)
    protected boolean isNew = false;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }
}
