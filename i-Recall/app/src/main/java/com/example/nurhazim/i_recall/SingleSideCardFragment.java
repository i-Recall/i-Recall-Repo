package com.example.nurhazim.i_recall;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by NurHazim on 05-Nov-14.
 */
public class SingleSideCardFragment extends Fragment {
    private static final String GAME_TAG = "game";
    private String textTerm = "This is a term";
    private String textDescription = "This is a description";

    TextView cardTerm;
    TextView cardDescription;

    public SingleSideCardFragment(){
    }

    public static SingleSideCardFragment newInstance(Bundle bundle){
        SingleSideCardFragment newFragment = new SingleSideCardFragment();
        newFragment.setArguments(bundle);

        return newFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_study_game, container, false
        );

        Bundle bundle = getArguments();
        if(bundle != null){
            textTerm = bundle.getString(StudyActivity.TERM_KEY);
            textDescription = bundle.getString(StudyActivity.DESCRIPTION_KEY);
        }

        CardView flashcard = (CardView) rootView.findViewById(R.id.flashcard);
        cardTerm = (TextView) flashcard.findViewById(R.id.flashcard_term);
        cardTerm.setText(textTerm);

        cardDescription = (TextView) flashcard.findViewById(R.id.flashcard_description);
        cardDescription.setText(textDescription);

        return rootView;
    }

    public TextView getCardDescription(){
        return cardDescription;
    }
}
