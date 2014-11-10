package com.example.nurhazim.i_recall;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class StudyActivity extends FragmentActivity {
    private static final String LOG_TAG = StudyActivity.class.getSimpleName();

    public static final int MODE_FLASHCARDS = 0;
    public static final int MODE_TRUE_FALSE = 1;
    public static final int MODE_GAME = 2;

    public static final String MODE_KEY = "study_mode";
    private static String mCurrentDeck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study);
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            if(intent != null && intent.hasExtra(MODE_KEY) && intent.hasExtra(SingleDeckFragment.DECK_NAME_KEY)){
                mCurrentDeck = intent.getStringExtra(SingleDeckFragment.DECK_NAME_KEY);
                switch(Integer.parseInt(intent.getStringExtra(MODE_KEY))){
                    case MODE_FLASHCARDS:
                        StudyFlashcardFragment newFlashcard = new StudyFlashcardFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString(SingleDeckFragment.DECK_NAME_KEY, mCurrentDeck);
                        newFlashcard.setArguments(bundle);

                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.add(R.id.container, newFlashcard);
                        ft.commit();
                        break;
                    default:
                        Log.e(LOG_TAG, "Invalid study mode");
                        break;
                }
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.study, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
