package monakhv.android.samlib;


import android.content.*;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;

import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.*;


import monakhv.android.samlib.data.SettingsHelper;
import monakhv.android.samlib.search.SearchAuthorActivity;
import monakhv.android.samlib.search.SearchAuthorsListFragment;
import monakhv.android.samlib.service.AndroidGuiUpdater;
import monakhv.android.samlib.service.AuthorEditorServiceIntent;
import monakhv.android.samlib.service.CleanNotificationData;
import monakhv.android.samlib.sortorder.AuthorSortOrder;

import monakhv.samlib.db.TagController;
import monakhv.samlib.db.entity.SamLibConfig;
import monakhv.samlib.db.entity.Tag;
import monakhv.samlib.service.SamlibService;


import java.io.Serializable;
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
        NavigationView.OnNavigationItemSelectedListener,
        AppBarLayout.OnOffsetChangedListener,
        AuthorFragment.Callbacks,
        AuthorTagFragment.AuthorTagCallback,
        BookFragment.Callbacks,
        AdapterView.OnItemSelectedListener {

    private static final String DEBUG_TAG = "MainActivity";

    public static final int ARCHIVE_ACTIVITY = 1;
    public static final int SEARCH_ACTIVITY = 2;
    public static final int PREFS_ACTIVITY = 3;
    public static final String CLEAN_NOTIFICATION = "CLEAN_NOTIFICATION";
    public static final String SELECTED_TAG_ID = "SELECTED_TAG_ID";
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


    private int selectedTagId = SamLibConfig.TAG_AUTHOR_ALL;
    private TagController tagSQL;
    private AppBarLayout mAppBarLayout;

    private DrawerLayout mDrawerLayout;
    private NavigationView navigationView;
    private ArrayAdapter<UITag> tagAdapter;
    private Spinner tagFilter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(DEBUG_TAG, "onCreate");
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


        authorFragment = (AuthorFragment) getSupportFragmentManager().findFragmentById(R.id.authorFragment);
        authorFragment.setHasOptionsMenu(true);


        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        twoPain = findViewById(R.id.two_pain) != null;
        if (twoPain) {
            Log.d(DEBUG_TAG, "onCreate: two pane");
            isTagShow = false;
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.listBooksFragment);
            if (fragment == null) {
                Log.d(DEBUG_TAG, "Initial construction: add BookFragment");
                bookFragment = new BookFragment();
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
        mAppBarLayout = (AppBarLayout) findViewById(R.id.appBarLayout);
        createDrawer();


    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

    }

    @Override
    public void drawerToggle() {
        mDrawerLayout.openDrawer(Gravity.LEFT);
    }


    private void openActionBar() {
        if (mAppBarLayout == null) {
            return;
        }
        mAppBarLayout.setExpanded(true);
    }

    /**
     * Create MaterialDrawer
     */
    private void createDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);

        //begin magic to make Home button available
        final ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, 0, 0);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
        //end magic to make Home button available


        navigationView = (NavigationView) findViewById(R.id.navigationView);

        navigationView.setCheckedItem(authorFragment.getSortOrder().getMenuId());
        navigationView.setNavigationItemSelectedListener(this);

        tagFilter = (Spinner) findViewById(R.id.tagList);

        ArrayList<UITag> tags = UITag.getPreList(this);

        tagAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tags);
        tagFilter.setAdapter(tagAdapter);
        tagAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tagFilter.setOnItemSelectedListener(this);


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
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        //menuItem.setChecked(true);

        mDrawerLayout.closeDrawers();
        int iSel = menuItem.getItemId();
        if (iSel == R.id.dr_search) {
            authorFragment.searchOrAdd();
        }
        if (iSel == R.id.dr_selected) {
            authorFragment.cleanSelection();
            onAuthorSelected(SamLibConfig.SELECTED_BOOK_ID);
            restoreTagSelection();
        }
        if (iSel == R.id.dr_data) {
            Log.d(DEBUG_TAG, "onItemClick: go to Archive");
            Intent prefsIntent = new Intent(getApplicationContext(),
                    ArchiveActivity.class);
            restoreTagSelection();

            startActivityForResult(prefsIntent, MainActivity.ARCHIVE_ACTIVITY);
        }

        if (iSel == R.id.dr_setting) {
            Intent prefsIntent = new Intent(getApplicationContext(),
                    SamlibPreferencesActivity.class);
            //prefsIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            restoreTagSelection();

            startActivityForResult(prefsIntent, MainActivity.PREFS_ACTIVITY);


        }

        if (iSel == R.id.author_sort_name) {
            authorFragment.setSortOrder(AuthorSortOrder.AuthorName);
        }

        if (iSel == R.id.author_sort_upadte) {

            authorFragment.setSortOrder(AuthorSortOrder.DateUpdate);
        }

        return true;
    }


    /**
     * Restore selection of the tag into Drawer
     */
    private void restoreTagSelection() {
        if (tagSQL.getById(selectedTagId) == null) {
            selectedTagId = SamLibConfig.TAG_AUTHOR_ALL;
        }
        //drResult.setSelection(selectedTagId + tagsShift, false);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(DEBUG_TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_TAG_ID, selectedTagId);


        outState.putLong(PROGRESS_TIME, Calendar.getInstance().getTimeInMillis());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        Log.d(DEBUG_TAG, "onRestoreInstanceState");
        super.onRestoreInstanceState(savedInstanceState);
        selectedTagId = savedInstanceState.getInt(SELECTED_TAG_ID, SamLibConfig.TAG_AUTHOR_ALL);


        Tag tag = tagSQL.getById(selectedTagId);
        if (tag != null) {
            authorFragment.selectTag(selectedTagId, tag.getName());
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
        Log.d(DEBUG_TAG, "onResume");
        super.onResume();

        IntentFilter updateFilter = new IntentFilter(AndroidGuiUpdater.ACTION_RESP);
        updateFilter.addCategory(Intent.CATEGORY_DEFAULT);
        updateReceiver = new UpdateActivityReceiver();


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


        if (mAppBarLayout != null) {
            mAppBarLayout.addOnOffsetChangedListener(this);
        }
        refreshTags();

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(updateReceiver);

        if (twoPain) {
            unregisterReceiver(downloadReceiver);
        }

        if (mAppBarLayout != null) {
            mAppBarLayout.removeOnOffsetChangedListener(this);
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
            //authorFragment.refresh(SamLibConfig.TAG_AUTHOR_ALL, null);
            return;
        }
        if (requestCode == ARCHIVE_ACTIVITY) {

            int res = data.getIntExtra(ArchiveActivity.UPDATE_KEY, -1);
            if (res == ArchiveActivity.UPDATE_LIST) {
                Log.d(DEBUG_TAG, "Reconstruct List View");
                restartApp();
            }
        }
        if (requestCode == SEARCH_ACTIVITY) {
            Log.v(DEBUG_TAG, "Start add Author");

            AuthorEditorServiceIntent.addAuthor(getApplicationContext(), data.getStringExtra(SearchAuthorsListFragment.AUTHOR_URL));
        }
        if (requestCode == PREFS_ACTIVITY) {
            restartApp();
            //finish();
        }
    }

    @Override
    public void onAuthorSelected(long id) {

        Log.d(DEBUG_TAG, "onAuthorSelected: go to Books");
        if (twoPain) {
            Log.i(DEBUG_TAG, "Two fragments Layout - set author_id: " + id);
            bookFragment.setAuthorId(id);
            tagFragment.setAuthor_id(id);

            if (isTagShow && (id == SamLibConfig.SELECTED_BOOK_ID)) {
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

    }

    @Override
    public void cleanBookSelection() {
        if (twoPain) {
            bookFragment.setAuthorId(0);//empty selection
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
        Log.d(DEBUG_TAG, "onBackPressed");
//        if (drResult != null && drResult.isDrawerOpen()) {
//            drResult.closeDrawer();
//            return ;
//        }
        if (authorFragment.getSelection() != SamLibConfig.TAG_AUTHOR_ALL) {
            authorFragment.refresh(SamLibConfig.TAG_AUTHOR_ALL, null);
            onTitleChange(getString(R.string.app_name));
            selectedTagId = SamLibConfig.TAG_AUTHOR_ALL;
            tagFilter.setSelection(0);
            openActionBar();
        } else {
            finish();
        }

    }

    @Override
    public void onFinish(long id) {
        authorFragment.refresh();
        Log.d(DEBUG_TAG, "Return to Books");
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.listBooksFragment, bookFragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        ft.commitAllowingStateLoss();
        isTagShow = false;
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        authorFragment.setAppBarOffset(verticalOffset);
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        UITag uitag = tagAdapter.getItem(position);
        if (uitag == null) {
            return;
        }
        authorFragment.selectTag(uitag.id, uitag.title);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

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


                    authorFragment.onRefreshComplete();

                }//

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

    public static class UITag implements Serializable {
        int id;
        String title;

        public UITag(int id, String title) {
            this.id = id;
            this.title = title;
        }

        public UITag(Tag tag) {
            this.id = tag.getId();
            this.title = tag.getName();
        }

        @Override
        public String toString() {
            return title;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            UITag uiTag = (UITag) o;

            return id == uiTag.id;

        }

        @Override
        public int hashCode() {
            return id;
        }

        public static ArrayList<UITag> getPreList(Context ctx) {
            ArrayList<UITag> tags = new ArrayList<>();
            //tags.add(new UITag(SamLibConfig.TAG_AUTHOR_ALL,ctx.getString(R.string.filter_all)));
            tags.add(new UITag(SamLibConfig.TAG_AUTHOR_ALL, ctx.getString(R.string.app_name)));
            tags.add(new UITag(SamLibConfig.TAG_AUTHOR_NEW, ctx.getString(R.string.filter_new)));
            return tags;
        }
    }

    private void refreshTags() {
        Log.d(DEBUG_TAG, "refreshTags: making refresh tags");
        tagAdapter.clear();
        tagAdapter.addAll(UITag.getPreList(this));
        for (Tag tag : tagSQL.getAll()) {
            tagAdapter.add(new UITag(tag));
        }
        tagAdapter.notifyDataSetChanged();

    }

    private void restartApp() {
        Intent i = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage(getBaseContext().getPackageName());

        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        finish();
        startActivity(i);
    }
}
