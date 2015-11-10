package monakhv.android.samlib;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;

import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;


import com.mikepenz.iconics.typeface.FontAwesome;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.*;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.OnCheckedChangeListener;
import monakhv.android.samlib.data.SettingsHelper;
import monakhv.android.samlib.search.SearchAuthorActivity;
import monakhv.android.samlib.search.SearchAuthorsListFragment;
import monakhv.android.samlib.service.AndroidGuiUpdater;
import monakhv.android.samlib.service.AuthorEditorServiceIntent;
import monakhv.android.samlib.service.CleanNotificationData;
import monakhv.android.samlib.sortorder.AuthorSortOrder;
import monakhv.android.samlib.sortorder.BookSortOrder;
import monakhv.android.samlib.sortorder.RadioItems;
import monakhv.samlib.db.TagController;
import monakhv.samlib.db.entity.SamLibConfig;
import monakhv.samlib.db.entity.Tag;
import monakhv.samlib.service.SamlibService;


import java.util.ArrayList;
import java.util.Calendar;


/*
 * Copyright 2014  Dmitry Monakhov
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
 *
 * 12/5/14.
 */
public class MainActivity extends MyBaseAbstractActivity implements
        AuthorFragment.Callbacks, Drawer.OnDrawerItemClickListener, OnCheckedChangeListener,
        AuthorTagFragment.AuthorTagCallback, BookFragment.Callbacks {

    private static final String DEBUG_TAG = "MainActivity";

    public static final int ARCHIVE_ACTIVITY = 1;
    public static final int SEARCH_ACTIVITY = 2;
    public static final int PREFS_ACTIVITY = 3;
    public static final String CLEAN_NOTIFICATION = "CLEAN_NOTIFICATION";
    public static final String SELECTED_TAG_ID = "SELECTED_TAG_ID";
    public static final String PROGRESS_STRING = "PROGRESS_STRING";
    public static final String PROGRESS_TIME = "PROGRESS_TIME";
    private UpdateActivityReceiver updateReceiver;
    private AuthorFragment authorFragment;
    private BookFragment bookFragment;
    private AuthorTagFragment tagFragment;
    private SettingsHelper settingsHelper;
    private DownloadReceiver downloadReceiver;

    private boolean twoPain;
    private Toolbar toolbar;
    private boolean isTagShow = false;


    private Drawer drResult;
    private final int menu_add_search = 1;
    private final int menu_settings = 3;
    private final int menu_data = 5;
    private final int menu_sort_author = 7;
    private final int menu_sort_books = 9;
    private final int menu_selected = 11;
    private final int tagsShift = 100;

    private RadioItems authorSort, bookSort;
    private int selectedTagId = SamLibConfig.TAG_AUTHOR_ALL;
    private TagController tagSQL;
    private String progressString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(DEBUG_TAG,"onCreate");
        settingsHelper = new SettingsHelper(this);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setTheme(settingsHelper.getTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        Bundle bundle = getIntent().getExtras();

        String clean = null;
        if (bundle != null) {
            clean = bundle.getString(CLEAN_NOTIFICATION);
        }
        if (clean != null) {
            CleanNotificationData.start(this);
            //bundle = null;
        }
        progressString=null;

        authorFragment = (AuthorFragment) getSupportFragmentManager().findFragmentById(R.id.authorFragment);
        authorFragment.setHasOptionsMenu(true);


        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        twoPain = findViewById(R.id.two_pain) != null;
        if (twoPain) {
            Log.d(DEBUG_TAG, "onCreate: two pane");
            isTagShow = false;
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.listBooksFragment);
            if (fragment == null) {
                Log.d(DEBUG_TAG, "Initial construction: add BookFragment");
                bookFragment= new BookFragment();
                final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.add(R.id.listBooksFragment, bookFragment);
                ft.commit();
                tagFragment = new AuthorTagFragment();
            } else {
                if (fragment instanceof AuthorTagFragment) {
                    Log.d(DEBUG_TAG, "Secondary construction: create BookFragment");
                    tagFragment = (AuthorTagFragment) fragment;
                    bookFragment = new BookFragment();
                    final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.listBooksFragment, bookFragment);
                    ft.commit();
                } else {
                    Log.d(DEBUG_TAG, "Secondary construction: create AuthorTagFragment");
                    tagFragment = new AuthorTagFragment();
                    bookFragment = (BookFragment) fragment;

                }

            }
            bookFragment.setHasOptionsMenu(true);
            tagFragment.setHasOptionsMenu(true);


        } else {
            Log.i(DEBUG_TAG, "onCreate: one pane");
        }

        tagSQL = new TagController(getDatabaseHelper());
        //createDrawer();
        refreshTags();
    }

    /**
     * Create MaterialDrawer
     */
    private void createDrawer() {
        ArrayList<IDrawerItem> items = new ArrayList<>();

        items.add(new PrimaryDrawerItem().withName(R.string.menu_search).withIcon(FontAwesome.Icon.faw_search_plus).withIdentifier(menu_add_search));
        items.add(new PrimaryDrawerItem().withName(R.string.menu_selected_go).withIcon(FontAwesome.Icon.faw_star).withIdentifier(menu_selected));
        //Begin author group
        items.add(new SectionDrawerItem().withName(R.string.menu_tags));
        items.add(new SecondaryDrawerItem().withName(R.string.filter_all).withIdentifier(SamLibConfig.TAG_AUTHOR_ALL + tagsShift)
                .withTag(getString(R.string.filter_all)));
        items.add(new SecondaryDrawerItem().withName(R.string.filter_new).withIdentifier(SamLibConfig.TAG_AUTHOR_NEW + tagsShift)
                .withTag(getString(R.string.filter_new)));


        tagSQL.getAll();

        for (Tag tag : tagSQL.getAll()) {
            items.add(new SecondaryDrawerItem().withName(tag.getName())
                    .withIdentifier(tagsShift + tag.getId())
                    .withTag(tag.getName()));
        }
        //end author group


        items.add(new SectionDrawerItem().withName(R.string.dialog_title_sort_author));

        authorSort = new RadioItems(this, menu_sort_author, AuthorSortOrder.values()
                , authorFragment.getSortOrder().name());

        items.addAll(authorSort.getItems());


        if (twoPain) {
            items.add(new SectionDrawerItem().withName(R.string.dialog_title_sort_book));
            bookSort = new RadioItems(this, menu_sort_books, BookSortOrder.values()
                    , settingsHelper.getBookSortOrderString());
            items.addAll(bookSort.getItems());

        }

        items.add(new DividerDrawerItem());


        items.add(new PrimaryDrawerItem().withName(R.string.menu_archive).withIcon(FontAwesome.Icon.faw_archive).withIdentifier(menu_data));
        items.add(new PrimaryDrawerItem().withName(R.string.menu_settings).withIcon(FontAwesome.Icon.faw_cog).withIdentifier(menu_settings));


        drResult = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withActionBarDrawerToggle(true)
                .withActionBarDrawerToggleAnimated(true)
                .withCloseOnClick(true)
                .withHeader(R.layout.drawer_header)
                .addDrawerItems(items.toArray(new IDrawerItem[1]))
                .withOnDrawerItemClickListener(this)
                .build();

        restoreTagSelection();

    }

    @Override
    public void showTags(long author_id) {
        Log.d(DEBUG_TAG, "showTags: go to Tags author_id = " + author_id);

        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.listBooksFragment, tagFragment);
        ft.addToBackStack(null);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
        isTagShow = true;



    }


    @Override
    public void onCheckedChanged(IDrawerItem iDrawerItem, CompoundButton compoundButton, boolean b) {
        String sTag = (String) iDrawerItem.getTag();
        int iDent = iDrawerItem.getIdentifier();
        Log.i(DEBUG_TAG, "onCheckedChanged: tag - " + sTag + " - " + iDent + " - " + b);
    }


    @Override
    public boolean onItemClick(AdapterView<?> adapterView, View view, int i, long l, IDrawerItem iDrawerItem) {
        if (view == null){
            return true;
        }
        int ident = iDrawerItem.getIdentifier();
        Log.i(DEBUG_TAG, "onItemClick: Identifier = " + ident);
        if (ident > 90) {//tag selection section
            selectedTagId = ident - tagsShift;
            Log.d(DEBUG_TAG, "onItemClick: select tag = "+selectedTagId);
            authorFragment.selectTag(selectedTagId, (String) iDrawerItem.getTag());
        }
        if (ident == menu_selected) {
            authorFragment.cleanSelection();
            onAuthorSelected(SamLibConfig.SELECTED_BOOK_ID);
            restoreTagSelection();
        }
        if (ident == menu_settings) {
            Log.d(DEBUG_TAG, "onItemClick: go to Settings");
            Intent prefsIntent = new Intent(getApplicationContext(),
                    SamlibPreferencesActivity.class);
            //prefsIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            restoreTagSelection();
            drResult.closeDrawer();
            startActivityForResult(prefsIntent, MainActivity.PREFS_ACTIVITY);
        }
        if (ident == menu_data) {
            Log.d(DEBUG_TAG, "onItemClick: go to Archive");
            Intent prefsIntent = new Intent(getApplicationContext(),
                    ArchiveActivity.class);
            restoreTagSelection();
            drResult.closeDrawer();
            startActivityForResult(prefsIntent, MainActivity.ARCHIVE_ACTIVITY);
        }
        if (ident == menu_add_search) {
            Log.d(DEBUG_TAG, "onItemClick: go to add or search");
            drResult.setSelectionByIdentifier(SamLibConfig.TAG_AUTHOR_ALL + tagsShift);
            authorFragment.searchOrAdd();
        }
        if (ident == menu_sort_author) {
            SecondaryDrawerItem sItem = (SecondaryDrawerItem) iDrawerItem;
            if (sItem.getBadge() != null && sItem.getBadge().equals(RadioItems.SELECT_BADGE)) {//do nothing just select all and close
                restoreTagSelection();
                drResult.closeDrawer();
            } else {
                String sTag = (String) sItem.getTag();
                authorSort.selectItem(sTag);
                authorFragment.setSortOrder(AuthorSortOrder.valueOf(sTag));
                restoreTagSelection();
                drResult.getAdapter().notifyDataSetChanged();
            }

        }
        if (ident == menu_sort_books) {
            SecondaryDrawerItem sItem = (SecondaryDrawerItem) iDrawerItem;
            if (sItem.getBadge() != null && sItem.getBadge().equals(RadioItems.SELECT_BADGE)) {//do nothing just select all and close
                restoreTagSelection();
                drResult.closeDrawer();
            } else {
                String sTag = (String) sItem.getTag();
                bookSort.selectItem(sTag);
                bookFragment.setSortOrder(BookSortOrder.valueOf(sTag));
                restoreTagSelection();
                drResult.getAdapter().notifyDataSetChanged();
            }

        }
        return false;

    }

    /**
     * Restore selection of the tag into Drawer
     */
    private void restoreTagSelection() {
        if (tagSQL.getById(selectedTagId) == null) {
            selectedTagId = SamLibConfig.TAG_AUTHOR_ALL;
        }
        drResult.setSelectionByIdentifier(selectedTagId + tagsShift,false);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(DEBUG_TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_TAG_ID, selectedTagId);
        outState.putString(PROGRESS_STRING, progressString);


        outState.putLong(PROGRESS_TIME, Calendar.getInstance().getTimeInMillis());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        Log.d(DEBUG_TAG, "onRestoreInstanceState");
        super.onRestoreInstanceState(savedInstanceState);
        selectedTagId = savedInstanceState.getInt(SELECTED_TAG_ID, SamLibConfig.TAG_AUTHOR_ALL);
        long upt =savedInstanceState.getLong(PROGRESS_TIME);

        if   ( ( Calendar.getInstance().getTimeInMillis() - upt)   < 3000 ){
            progressString = savedInstanceState.getString(PROGRESS_STRING);
        }
        else {
            progressString = null;
        }

        Tag tag = tagSQL.getById(selectedTagId);
        if (tag != null){
            authorFragment.selectTag(selectedTagId,tag.getName());
            restoreTagSelection();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent == null) {
            return;
        }
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            String clean = bundle.getString(CLEAN_NOTIFICATION);
            if (clean != null) {
                CleanNotificationData.start(this);
            }
        }
    }

    @Override
    protected void onResume() {
        Log.d(DEBUG_TAG,"onResume");
        super.onResume();

        IntentFilter updateFilter = new IntentFilter(AndroidGuiUpdater.ACTION_RESP);
        updateFilter.addCategory(Intent.CATEGORY_DEFAULT);
        updateReceiver = new UpdateActivityReceiver();


        //getActionBarHelper().setRefreshActionItemState(refreshStatus);
        registerReceiver(updateReceiver, updateFilter);


        if (twoPain) {

            if (bookFragment == null) {
                Log.e(DEBUG_TAG, "Fragment is NULL for two pane layout!!");
            }
            downloadReceiver = new DownloadReceiver(bookFragment, getDatabaseHelper());
            IntentFilter filter = new IntentFilter(DownloadReceiver.ACTION_RESP);
            filter.addCategory(Intent.CATEGORY_DEFAULT);
            registerReceiver(downloadReceiver, filter);
        }
        //getSupportActionBar().setTitle(R.string.app_name);
        // authorFragment.refresh(null, null);

        if (progressString != null) {
            authorFragment.updateProgress(progressString);
        }
        //authorFragment.refresh();
        //refreshTags();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(updateReceiver);

        if (twoPain) {
            unregisterReceiver(downloadReceiver);
        }

        //Stop refresh status
        if (authorFragment.isRefreshing()){
            authorFragment.onRefreshComplete();
        }
        else {
            progressString=null;
        }

        //getActionBarHelper().setRefreshActionItemState(refreshStatus);
    }


    /**
     * Return from ArchiveActivity or SearchActivity
     *
     * @param requestCode request code
     * @param resultCode  result code
     * @param data        Intent data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            Log.d(DEBUG_TAG, "Wrong result code from onActivityResult");
            authorFragment.refresh(SamLibConfig.TAG_AUTHOR_ALL, null);
            return;
        }
        if (requestCode == ARCHIVE_ACTIVITY) {

            int res = data.getIntExtra(ArchiveActivity.UPDATE_KEY, -1);
            if (res == ArchiveActivity.UPDATE_LIST) {
                Log.d(DEBUG_TAG, "Reconstruct List View");
                authorFragment.refresh(SamLibConfig.TAG_AUTHOR_ALL, null);

            }
        }
        if (requestCode == SEARCH_ACTIVITY) {
            Log.v(DEBUG_TAG, "Start add Author");

            AuthorEditorServiceIntent.addAuthor(getApplicationContext(), data.getStringExtra(SearchAuthorsListFragment.AUTHOR_URL));
        }
        if (requestCode == PREFS_ACTIVITY) {
            finish();
        }
    }

    @Override
    public void onAuthorSelected(long id) {

        Log.d(DEBUG_TAG, "onAuthorSelected: go to Books");
        if (twoPain) {
            Log.i(DEBUG_TAG, "Two fragments Layout - set author_id: " + id);
            bookFragment.setAuthorId(id);
            tagFragment.setAuthor_id(id);

            if (isTagShow && (id==SamLibConfig.SELECTED_BOOK_ID)) {
                onFinish(id);
            }
        } else {
            Log.i(DEBUG_TAG, "One fragment Layout - set author_id: " + id);
            Intent intent = new Intent(this, BooksActivity.class);
            intent.putExtra(BookFragment.AUTHOR_ID, id);

            startActivity(intent);
        }
    }


    @Override
    public void onTitleChange(String lTitle) {
        Log.d(DEBUG_TAG, "set title: " + lTitle);

        getSupportActionBar().setTitle(lTitle);
//        if (authorFragment.getSelection() == null){
//            getSupportActionBar().setDisplayOptions(0, ActionBar.DISPLAY_HOME_AS_UP);
//        }
//        else {
//            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
//        }

    }

    @Override
    public void cleanBookSelection() {
        if (twoPain) {
            bookFragment.setAuthorId(0);//empty selection
        }
    }

    @Override
    public void setActionBarVisibility(boolean visible) {
        if (twoPain){
            return;//Do nothing for two pain layout
        }

        if (visible){

            ObjectAnimator anim = ObjectAnimator.ofFloat(toolbar, "translationY", 0);
            anim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    toolbar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    toolbar.setVisibility(View.VISIBLE);

                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            anim.start();
        }
        else {
            ObjectAnimator anim = ObjectAnimator.ofFloat(toolbar, "translationY", -toolbar.getHeight());
            anim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    toolbar.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            anim.start();
        }
    }

    /**
     * Add new Author to SQL Store
     *
     * @param view View
     */
    @SuppressWarnings("UnusedParameters")
    public void addAuthor(View view) {

        addAuthorFromText();

    }


    public void addAuthorFromText() {
        EditText editText = (EditText) findViewById(R.id.addUrlText);

        if (editText == null) {
            return;
        }
        if (editText.getText() == null) {
            return;
        }
        String text = editText.getText().toString();
        editText.setText("");


        View v = findViewById(R.id.add_author_panel);
        v.setVisibility(View.GONE);

        String url = SamLibConfig.getParsedUrl(text);
        if (url != null) {//add  Author by URL
            AuthorEditorServiceIntent.addAuthor(getApplicationContext(), url);

        } else {
            if (TextUtils.isEmpty(text)) {
                return;
            }
            //Start Search activity to make search and add selected Authors to Data Base
            Intent prefsIntent = new Intent(getApplicationContext(),
                    SearchAuthorActivity.class);
            prefsIntent.putExtra(SearchAuthorActivity.EXTRA_PATTERN, text);
            prefsIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

            startActivityForResult(prefsIntent, SEARCH_ACTIVITY);
        }

    }

    @Override
    public void onBackPressed() {
        Log.d(DEBUG_TAG,"onBackPressed");
        if (drResult != null && drResult.isDrawerOpen()) {
            drResult.closeDrawer();
            return ;
        }
        if (authorFragment.getSelection() != SamLibConfig.TAG_AUTHOR_ALL) {
            authorFragment.refresh(SamLibConfig.TAG_AUTHOR_ALL, null);
            onTitleChange(getString(R.string.app_name));
            selectedTagId = SamLibConfig.TAG_AUTHOR_ALL;
            restoreTagSelection();
        } else {
            finish();
        }

    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        Log.d(DEBUG_TAG,"onKeyDown");
//        if (keyCode == KeyEvent.KEYCODE_MENU){
//            Log.d(DEBUG_TAG,"onKeyDown - MENU");
//            return false;
//        }
//        return super.onKeyDown(keyCode, event);
//    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        Log.d(DEBUG_TAG,"onKeyDown");
//        if ((keyCode == KeyEvent.KEYCODE_BACK)) { //Back key pressed
//            Log.d(DEBUG_TAG,"onKeyDown - BACK");
//
//            if (drResult != null && drResult.isDrawerOpen()) {
//                drResult.closeDrawer();
//                return true;
//            }
//
//            if (authorFragment.getSelection() != SamLibConfig.TAG_AUTHOR_ALL) {
//                authorFragment.refresh(SamLibConfig.TAG_AUTHOR_ALL, null);
//                onTitleChange(getString(R.string.app_name));
//                selectedTagId = SamLibConfig.TAG_AUTHOR_ALL;
//                restoreTagSelection();
//            } else {
//                finish();
//            }
//
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }

    @Override
    public void onFinish(long id) {
        authorFragment.refresh();
        Log.d(DEBUG_TAG, "Return to Books");
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.listBooksFragment, bookFragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        ft.commit();
        isTagShow = false;
    }


    /**
     * Receive updates from  Services
     */
    public class UpdateActivityReceiver extends BroadcastReceiver {


        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getStringExtra(AndroidGuiUpdater.ACTION);
            if (action != null) {
                if (action.equalsIgnoreCase(AndroidGuiUpdater.ACTION_TOAST)) {
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, intent.getCharSequenceExtra(AndroidGuiUpdater.TOAST_STRING), duration);
                    toast.show();

                    progressString = null;
                    authorFragment.onRefreshComplete();

                }//
                if (action.equalsIgnoreCase(AndroidGuiUpdater.ACTION_PROGRESS)) {
                    progressString = intent.getStringExtra(AndroidGuiUpdater.TOAST_STRING);
                    authorFragment.updateProgress(progressString);
                }
                if (action.equalsIgnoreCase(AndroidGuiUpdater.ACTION_REFRESH)) {

                    int iObject = intent.getIntExtra(AndroidGuiUpdater.ACTION_REFRESH_OBJECT, AndroidGuiUpdater.ACTION_REFRESH_AUTHORS);
                    if ((iObject == AndroidGuiUpdater.ACTION_REFRESH_AUTHORS) ||
                            (iObject == AndroidGuiUpdater.ACTION_REFRESH_BOTH)) {
                        authorFragment.refresh();
                    }

                    if (twoPain && !isTagShow && (iObject == AndroidGuiUpdater.ACTION_REFRESH_BOTH)) {
                        bookFragment.refresh();
                    }
                    if (twoPain && (iObject == AndroidGuiUpdater.ACTION_REFRESH_TAGS)) {
                        refreshTags();
                    }

                }
                if (action.equals(SamlibService.ACTION_ADD)) {

                    long id = intent.getLongExtra(AndroidGuiUpdater.RESULT_AUTHOR_ID, 0);
                    Log.d(DEBUG_TAG, "onReceive: author add, id = " + id);
                    int duration = Toast.LENGTH_SHORT;
                    CharSequence msg = intent.getCharSequenceExtra(AndroidGuiUpdater.TOAST_STRING);
                    Toast toast = Toast.makeText(context, msg, duration);

                    authorFragment.refresh(id);

                    toast.show();
                    onAuthorSelected(id);

                }
                if (action.equals(SamlibService.ACTION_DELETE)) {
                    int duration = Toast.LENGTH_SHORT;
                    CharSequence msg = intent.getCharSequenceExtra(AndroidGuiUpdater.TOAST_STRING);
                    Toast toast = Toast.makeText(context, msg, duration);
                    Log.d(DEBUG_TAG, "onReceive: author del");
                    toast.show();
                }
            }
        }
    }

    private void refreshTags() {
        Log.d(DEBUG_TAG,"refreshTags: making refresh tags");
        createDrawer();
        authorFragment.makePulToRefresh();
    }
}
