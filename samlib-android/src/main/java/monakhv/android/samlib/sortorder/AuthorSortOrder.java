package monakhv.android.samlib.sortorder;

import android.content.Context;
import monakhv.android.samlib.R;
import monakhv.samlib.db.SQLController;

public enum AuthorSortOrder implements SortOrder{

    DateUpdate(R.string.sort_update_date, SQLController.COL_mtime + " DESC",R.id.author_sort_upadte),
    AuthorName(R.string.sort_author_name, SQLController.COL_isnew + " DESC, " + SQLController.COL_NAME,R.id.author_sort_name);
    private final int name;
    private final int menuId;
    private final String order;

    private AuthorSortOrder(int name, String order,int menu_id) {
        this.name = name;
        this.order = order;
        this.menuId=menu_id;
    }
    @Override
    public String getTitle(Context ctx) {
        return ctx.getString(name);
    }
    @Override
    public String getTag(){
        return this.name();
    }

    @Override
    public String getOrder() {
        return order;
    }

    public int getMenuId() {
        return menuId;
    }


}
