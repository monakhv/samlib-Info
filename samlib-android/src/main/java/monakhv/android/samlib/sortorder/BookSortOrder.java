package monakhv.android.samlib.sortorder;

import android.content.Context;
import monakhv.android.samlib.R;
import monakhv.samlib.db.SQLController;

public enum BookSortOrder implements SortOrder {

    DateUpdate(R.string.sort_book_mtime, SQLController.COL_BOOK_MTIME + " DESC",R.id.sort_book_mtime),
    BookName(R.string.sort_book_title, SQLController.COL_BOOK_ISNEW + " DESC, " + SQLController.COL_BOOK_TITLE,R.id.sort_book_title),
    BookDate(R.string.sort_book_date, SQLController.COL_BOOK_ISNEW + " DESC, " + SQLController.COL_BOOK_DATE + " DESC",R.id.sort_book_date),
    BookSize(R.string.sort_book_size, SQLController.COL_BOOK_ISNEW + " DESC, " + SQLController.COL_BOOK_SIZE + " DESC",R.id.sort_book_date);

    private final int name;
    private final int menuId;
    private final String order;


    private BookSortOrder(int name, String order, int menu_id) {
        this.name = name;
        this.order = order;
        this.menuId=menu_id;
    }

    @Override
    public String getOrder() {
        return order;
    }

    public int getMenuId() {
        return menuId;
    }

    @Override
    public String getTitle(Context ctx) {
        return ctx.getString(name);
    }
    @Override
    public String getTag(){
        return this.name();
    }

    public static String[] getTitles(Context ctx) {
        String[] res = new String[values().length];
        int i = 0;
        for (BookSortOrder so : values()) {
            res[i] = ctx.getString(so.name);
            ++i;
        }
        return res;
    }
}
