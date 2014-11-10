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

import com.example.nurhazim.i_recall.data.CardsContract;

/**
 * Created by NurHazim on 04-Nov-14.
 */

public class StudyFlashcardFragment extends Fragment {
    public static final String TERM_KEY = "term";
    public static final String DESCRIPTION_KEY = "description";

    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;

    private String mCurrentDeckName;
    private Cursor mCursor;

    public StudyFlashcardFragment(){
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.pager, container, false);

        Bundle bundle = getArguments();
        if(bundle != null){
            mCurrentDeckName = bundle.getString(SingleDeckFragment.DECK_NAME_KEY);
            mCursor = getActivity().getContentResolver().query(
                    CardsContract.CardEntry.buildCardWithDeckID(Utility.getDeckId(getActivity(), mCurrentDeckName)),
                    null,
                    null,
                    null,
                    null
            );
        }

        mPager = (ViewPager) rootView.findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(((FragmentActivity)getActivity()).getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        return rootView;
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter{
        public ScreenSlidePagerAdapter(FragmentManager fm){
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int i) {
            mCursor.moveToPosition(i);
            Bundle bundle = new Bundle();
            bundle.putString(TERM_KEY, mCursor.getString(mCursor.getColumnIndex(CardsContract.CardEntry.COLUMN_TERM)));
            bundle.putString(DESCRIPTION_KEY, mCursor.getString(mCursor.getColumnIndex(CardsContract.CardEntry.COLUMN_DESCRIPTION)));
            CardFragment cardFragment = new CardFragment();
            cardFragment.setArguments(bundle);
            return cardFragment;
        }

        @Override
        public int getCount() {
            return mCursor.getCount();
        }
    }
}
