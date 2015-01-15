package com.example.nurhazim.i_recall;

import android.app.Fragment;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

import com.example.nurhazim.i_recall.data.CardsContract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by NurHazim on 18-Nov-14.
 */
public class TrueFalseFragment extends Fragment {

    private String mCurrentDeckName;
    private Cursor mCursor;

    private NoSwipeViewPager mPager;
    private PagerAdapter mPagerAdapter;

    private Cursor mCursorAnswer;

    private ImageView mImageEvaluation;

    public TrueFalseFragment(){
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.true_false_pager, container, false);

        Bundle bundle = getArguments();
        if(bundle != null && bundle.containsKey(SingleDeckFragment.DECK_NAME_KEY)){
            mCurrentDeckName = bundle.getString(SingleDeckFragment.DECK_NAME_KEY);
            mCursorAnswer = mCursor = getActivity().getContentResolver().query(
                    CardsContract.CardEntry.buildCardWithDeckID(Utility.getDeckId(getActivity(), mCurrentDeckName)),
                    null,
                    null,
                    null,
                    null
            );
        }

        mPager = (NoSwipeViewPager) rootView.findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(((FragmentActivity)getActivity()).getSupportFragmentManager(), mCursor);
        mPager.setAdapter(mPagerAdapter);

        mImageEvaluation = (ImageView) rootView.findViewById(R.id.image_evaluation);

        Button btnTrue = (Button) rootView.findViewById(R.id.button_true);
        Button btnFalse = (Button) rootView.findViewById(R.id.button_false);

        btnTrue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(((ScreenSlidePagerAdapter)mPagerAdapter).getAnswer(mPager.getCurrentItem()) == true){
                    showEvaluation(true);
                }
                else {
                    showEvaluation(false);
                }
            }
        });

        btnFalse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(((ScreenSlidePagerAdapter)mPagerAdapter).getAnswer(mPager.getCurrentItem()) == false){
                    showEvaluation(true);
                }
                else{
                    showEvaluation(false);
                }
            }
        });

        return rootView;
    }

    private void showEvaluation(boolean evaluation){

        if(evaluation){
            mImageEvaluation.setImageResource(R.drawable.correct);
        }
        else{
            mImageEvaluation.setImageResource(R.drawable.wrong);
        }
        Animation evaluationAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.evaluation_fade_in_out);
        evaluationAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if(isAtLastItem(mPager)){
                    getActivity().finish();
                }
                else {
                    mPager.setCurrentItem(mPager.getCurrentItem() + 1);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mImageEvaluation.setVisibility(View.VISIBLE);
        mImageEvaluation.startAnimation(evaluationAnim);
    }

    private boolean isAtLastItem(ViewPager viewPager){
        return viewPager.getCurrentItem() == mCursorAnswer.getCount() - 1;
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
            bundle.putString(StudyActivity.TERM_KEY, mCursor.getString(mCursor.getColumnIndex(CardsContract.CardEntry.COLUMN_TERM)));
            mCursor.moveToPosition(shuffledDescriptions.get(i));
            bundle.putString(StudyActivity.DESCRIPTION_KEY, mCursor.getString(mCursor.getColumnIndex(CardsContract.CardEntry.COLUMN_DESCRIPTION)));

            if(i == shuffledDescriptions.get(i)){
                answers.add(true);
            }
            else{
                answers.add(false);
            }

            SingleSideCardFragment singleSideCardFragment = new SingleSideCardFragment();
            singleSideCardFragment.setArguments(bundle);
            return singleSideCardFragment;
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
