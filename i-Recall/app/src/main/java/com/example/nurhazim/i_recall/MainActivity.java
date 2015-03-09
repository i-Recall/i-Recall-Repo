package com.example.nurhazim.i_recall;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;
import it.neokree.materialnavigationdrawer.elements.MaterialAccount;
import it.neokree.materialnavigationdrawer.elements.MaterialSection;


public class MainActivity extends MaterialNavigationDrawer {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private CharSequence mTitle;

    @Override
    public void init(Bundle bundle) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mTitle = getTitle();

        MaterialSection allDecks = newSection("All Decks", new DecksFragment());
        MaterialSection collaborativeStudying = newSection("Collaborative Studying", new GameOptionsFragment());
        MaterialSection backup = newSection("Backup", new BackupFragment());
        MaterialSection userPerformance = newSection("User Performance", new UserPerformanceFragment());
        MaterialSection search = newSection("Search", new SearchFragment());
        MaterialSection import_export = newSection("Import / Export", new ImportExportFragment());
        MaterialSection settings = newSection("Settings", new Intent(this, SettingsActivity.class));

        addSection(allDecks);
        addSection(collaborativeStudying);
        addSection(backup);
        addSection(userPerformance);
        addSection(search);
        addSection(import_export);
        addBottomSection(settings);

        allowArrowAnimation();

        MaterialAccount account = new MaterialAccount(this.getResources(), getName(), "", R.drawable.default_profile, R.drawable.default_cover);
        addAccount(account);
    }

    private String getName(){
        Cursor cursor = getContentResolver().query(
                ContactsContract.Profile.CONTENT_URI,
                null,
                null,
                null,
                null,
                null
        );
        int count = cursor.getCount();
        String[] columnNames = cursor.getColumnNames();
        cursor.moveToFirst();
        int position = cursor.getPosition();
        String columnValue = "";
        if(count == 1 && position == 0){
            columnValue = cursor.getString(cursor.getColumnIndex("display_name"));
        }
        cursor.close();
        return columnValue;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
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
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
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
