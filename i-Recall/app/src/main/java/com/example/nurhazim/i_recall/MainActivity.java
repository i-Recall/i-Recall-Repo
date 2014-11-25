package com.example.nurhazim.i_recall;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;


public class MainActivity extends ActionBarActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private String[] mNavItems;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private CharSequence mTitle;
    private ActionBarDrawerToggle mDrawerToggle;
    private LinearLayout mLinearLayout;
    private CharSequence mDrawerTitle;

    private static final int NAV_ALL_DECKS = 0;
    private static final int NAV_SEARCH = 3;
    private static final int NAV_IMPORT = 4;


    private static final int NAV_COLLAB_STUDY = 1;
    private static final int NAV_BACKUP = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_drawer_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mNavItems = getResources().getStringArray(R.array.nav_drawer_items);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mLinearLayout = (LinearLayout) findViewById(R.id.linear_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close
        ){
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                getSupportActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); //creates call to onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu();
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerList.setAdapter(new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                mNavItems
        ));

        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mTitle = mDrawerTitle = getTitle();

        mDrawerLayout.setStatusBarBackgroundColor(R.attr.colorPrimary);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.content_frame, new DecksFragment())
                    .commit();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);

        if(menu.findItem(R.id.action_new_deck)!=null) {
            menu.findItem(R.id.action_new_deck) .setVisible(!drawerOpen);
        if(menu.findItem(R.id.action_new_deck) != null) {
            menu.findItem(R.id.action_new_deck).setVisible(!drawerOpen);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if(mDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position){
        Fragment fragment;
        FragmentManager fragmentManager = getFragmentManager();
        switch(position){
            case NAV_ALL_DECKS:
                fragment = new DecksFragment();
                break;
            case NAV_COLLAB_STUDY:
                fragment = new GameOptionsFragment();
                break;
            case NAV_BACKUP:
                fragment = null;
                break;
            case NAV_SEARCH:
                fragment = new SearchFragment();
                break;
            case NAV_IMPORT:
                fragment = null;
                break;
            default:
                fragment = null;
                break;
        }
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();

        mDrawerList.setItemChecked(position, true);
        setTitle(mNavItems[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        if(title != null && getSupportActionBar() != null) {
            mTitle = title;
            getSupportActionBar().setTitle(mTitle);
        }
        else{
            Log.e(LOG_TAG, "Either title is null or ActionBar is null");
        }
    }
}
