package com.example.nurhazim.i_recall;

import android.app.Fragment;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.nurhazim.i_recall.data.CardsContract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by NurHazim on 12-Nov-14.
 */
public class StudyGameFragment extends Fragment {
    private static final String LOG_TAG = StudyGameFragment.class.getSimpleName();

    public static final String TERM_KEY = "term";
    public static final String DESCRIPTION_KEY = "description";

    private NoSwipeViewPager mPagerPlayer1;
    private NoSwipeViewPager mPagerPlayer2;
    private ScreenSlidePagerAdapter mPagerAdapterPlayer1;
    private ScreenSlidePagerAdapter mPagerAdapterPlayer2;

    private String mCurrentDeckName;
    private Cursor mCursorPlayer1;
    private Cursor mCursorPlayer2;

    private Cursor mCursorAnswer;

    private TextView scorePlayer1;
    private TextView scorePlayer2;

    private ImageView imgWinner;
    private ImageView imgTie;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.game_pager, container, false);

        imgWinner = (ImageView) rootView.findViewById(R.id.winner_image);
        imgTie = (ImageView) rootView.findViewById(R.id.tie_image);

        Bundle bundle = getArguments();
        if(bundle != null){
            mCurrentDeckName = bundle.getString(SingleDeckFragment.DECK_NAME_KEY);
            mCursorPlayer1 = getActivity().getContentResolver().query(
                    CardsContract.CardEntry.buildCardWithDeckID(Utility.getDeckId(getActivity(), mCurrentDeckName)),
                    null,
                    null,
                    null,
                    null
            );
            mCursorAnswer = mCursorPlayer2 = mCursorPlayer1;
        }

        mPagerPlayer1 = (NoSwipeViewPager) rootView.findViewById(R.id.pager_player1);
        mPagerPlayer2 = (NoSwipeViewPager) rootView.findViewById(R.id.pager_player2);
        mPagerAdapterPlayer1 = new ScreenSlidePagerAdapter(((FragmentActivity)getActivity()).getSupportFragmentManager(), mCursorPlayer1);
        mPagerAdapterPlayer2 = new ScreenSlidePagerAdapter(((FragmentActivity)getActivity()).getSupportFragmentManager(), mCursorPlayer2);
        mPagerPlayer1.setAdapter(mPagerAdapterPlayer1);
        mPagerPlayer2.setAdapter(mPagerAdapterPlayer2);

        scorePlayer1 = (TextView) rootView.findViewById(R.id.player1_score_text);
        scorePlayer2 = (TextView) rootView.findViewById(R.id.player2_score_text);

        final Button buttonTruePlayer1 = (Button) rootView.findViewById(R.id.button_player1_true);
        final Button buttonFalsePlayer1 = (Button) rootView.findViewById(R.id.button_player1_false);
        final Button buttonTruePlayer2 = (Button) rootView.findViewById(R.id.button_player2_true);
        final Button buttonFalsePlayer2 = (Button) rootView.findViewById(R.id.button_player2_false);

        buttonTruePlayer1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mPagerAdapterPlayer1.getAnswer(mPagerPlayer1.getCurrentItem())){
                    increaseScore(scorePlayer1);
                }
                if(isAtLastItem(mPagerPlayer1)){
                    buttonTruePlayer1.setVisibility(View.INVISIBLE);
                    buttonFalsePlayer1.setVisibility(View.INVISIBLE);
                    getWinner();
                }
                mPagerPlayer1.setCurrentItem(mPagerPlayer1.getCurrentItem() + 1);
            }
        });

        buttonFalsePlayer1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mPagerAdapterPlayer1.getAnswer(mPagerPlayer1.getCurrentItem())){
                    increaseScore(scorePlayer1);
                }
                if(isAtLastItem(mPagerPlayer1)){
                    buttonTruePlayer1.setVisibility(View.INVISIBLE);
                    buttonFalsePlayer1.setVisibility(View.INVISIBLE);
                    getWinner();
                }
                mPagerPlayer1.setCurrentItem(mPagerPlayer1.getCurrentItem() + 1);
            }
        });

        buttonTruePlayer2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mPagerAdapterPlayer2.getAnswer(mPagerPlayer2.getCurrentItem())){
                    increaseScore(scorePlayer2);
                }
                if(isAtLastItem(mPagerPlayer2)){
                    buttonTruePlayer2.setVisibility(View.INVISIBLE);
                    buttonFalsePlayer2.setVisibility(View.INVISIBLE);
                    getWinner();
                }
                mPagerPlayer2.setCurrentItem(mPagerPlayer2.getCurrentItem() + 1);
            }
        });

        buttonFalsePlayer2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mPagerAdapterPlayer2.getAnswer(mPagerPlayer2.getCurrentItem())){
                    increaseScore(scorePlayer2);
                }
                if(isAtLastItem(mPagerPlayer2)){
                    buttonTruePlayer2.setVisibility(View.INVISIBLE);
                    buttonFalsePlayer2.setVisibility(View.INVISIBLE);
                    getWinner();
                }
                mPagerPlayer2.setCurrentItem(mPagerPlayer2.getCurrentItem() + 1);
            }
        });

        return rootView;
    }

    private void increaseScore(TextView score){
        int currentScore = Integer.valueOf(score.getText().toString());
        currentScore++;
        String newScore = "0" + String.valueOf(currentScore);

        Animation beating = AnimationUtils.loadAnimation(getActivity(), R.anim.score_beat);
        score.startAnimation(beating);
        score.setText(newScore);
    }

    private boolean isAtLastItem(ViewPager viewPager){
        return viewPager.getCurrentItem() == mCursorAnswer.getCount() - 1;
    }

    private void getWinner(){
        if(isAtLastItem(mPagerPlayer1) && isAtLastItem(mPagerPlayer2)){
            if(Integer.valueOf(scorePlayer1.getText().toString()) > Integer.valueOf(scorePlayer2.getText().toString())){
                imgWinner.setVisibility(View.VISIBLE);
                Animation announceWinner = AnimationUtils.loadAnimation(getActivity(), R.anim.winner_animation);
                imgWinner.startAnimation(announceWinner);
                announceWinner.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        imgWinner.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                getActivity().finish();
                            }
                        });
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                Log.v(LOG_TAG, "Player 1 won!");
            }
            else if(Integer.valueOf(scorePlayer1.getText().toString()) < Integer.valueOf(scorePlayer2.getText().toString())){
                imgWinner.setRotation(180);
                imgWinner.setVisibility(View.VISIBLE);
                Animation announceWinner = AnimationUtils.loadAnimation(getActivity(), R.anim.winner_animation);
                imgWinner.startAnimation(announceWinner);
                announceWinner.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        imgWinner.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                getActivity().finish();
                            }
                        });
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                Log.v(LOG_TAG, "Player 2 won!");
            }
            else{
                imgTie.setVisibility(View.VISIBLE);
                Animation announceWinner = AnimationUtils.loadAnimation(getActivity(), R.anim.winner_animation);
                imgTie.startAnimation(announceWinner);
                announceWinner.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        imgTie.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                getActivity().finish();
                            }
                        });
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                Log.v(LOG_TAG, "It's a tie!");
            }
        }
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        private Cursor mCursor;
        private List<Integer> shuffledDescriptions;
        private List<Boolean> answers = new ArrayList<Boolean>();

        public ScreenSlidePagerAdapter(FragmentManager fm, Cursor cursor){
            super(fm);
            mCursor = cursor;
            shuffledDescriptions = shuffleList(mCursor.getCount());
        }

        @Override
        public android.support.v4.app.Fragment getItem(int i) {
            mCursor.moveToPosition(i);
            Bundle bundle = new Bundle();
            bundle.putString(TERM_KEY, mCursor.getString(mCursor.getColumnIndex(CardsContract.CardEntry.COLUMN_TERM)));
            mCursor.moveToPosition(shuffledDescriptions.get(i));
            bundle.putString(DESCRIPTION_KEY, mCursor.getString(mCursor.getColumnIndex(CardsContract.CardEntry.COLUMN_DESCRIPTION)));

            if(i == shuffledDescriptions.get(i)){
                answers.add(true);
            }
            else{
                answers.add(false);
            }

            GameCardFragment gameCardFragment = new GameCardFragment();
            gameCardFragment.setArguments(bundle);
            return gameCardFragment;
        }

        @Override
        public int getCount() {
            return mCursor.getCount();
        }

        private List<Integer> shuffleList(int totalCards){
            List<Integer> dataList = new ArrayList<Integer>();
            for (int i = 0; i < totalCards; i++) {
                dataList.add(i);
            }
            Collections.shuffle(dataList);
            return dataList;
        }

        public boolean getAnswer(int currentCard){
            return answers.get(currentCard);
        }
    }
}
