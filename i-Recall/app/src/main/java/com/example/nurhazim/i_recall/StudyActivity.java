package com.example.nurhazim.i_recall;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;


public class StudyActivity extends FragmentActivity {
    private static final String LOG_TAG = StudyActivity.class.getSimpleName();

    public static final int MODE_FLASHCARDS = 0;
    public static final int MODE_TRUE_FALSE = 1;
    public static final int MODE_GAME = 2;

    public static final String MODE_KEY = "study_mode";
    public static final String TERM_KEY = "term";
    public static final String DESCRIPTION_KEY = "description";

    private static String mCurrentDeck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study);

        //hide status bar
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        else {
            View decorView = getWindow().getDecorView();
            // Hide the status bar.
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            if(intent != null && intent.hasExtra(MODE_KEY) && intent.hasExtra(SingleDeckFragment.DECK_NAME_KEY)){
                mCurrentDeck = intent.getStringExtra(SingleDeckFragment.DECK_NAME_KEY);

                Fragment newStudy = new Fragment();
                switch(Integer.parseInt(intent.getStringExtra(MODE_KEY))){
                    case MODE_FLASHCARDS:
                        newStudy = new StudyFlashcardFragment();
                        break;
                    case MODE_TRUE_FALSE:
                        newStudy = new TrueFalseFragment();
                        break;
                    case MODE_GAME:
                        newStudy = new StudyGameFragment();
                        break;
                    default:
                        Log.e(LOG_TAG, "Invalid study mode");
                        break;
                }
                Bundle bundle = new Bundle();
                bundle.putString(SingleDeckFragment.DECK_NAME_KEY, mCurrentDeck);
                newStudy.setArguments(bundle);

                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.add(R.id.container, newStudy);
                ft.commit();
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