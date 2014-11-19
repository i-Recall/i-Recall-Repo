package com.example.nurhazim.i_recall;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by NurHazim on 05-Nov-14.
 */
public class CardFragment extends Fragment {
    private boolean showingFront = true;
    private String textTerm = "This is a term";
    private String textDescription = "This is a description";

    private boolean evaluated = false;

    TextView cardText;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_study_flashcards, container, false
        );

        Bundle bundle = getArguments();
        if(bundle != null){
            textTerm = bundle.getString(StudyActivity.TERM_KEY);
            textDescription = bundle.getString(StudyActivity.DESCRIPTION_KEY);
        }

        final Button btnCorrect = (Button) rootView.findViewById(R.id.button_correct);
        final Button btnWrong = (Button) rootView.findViewById(R.id.button_wrong);

        btnCorrect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnCorrect.setVisibility(View.INVISIBLE);
                btnWrong.setVisibility(View.INVISIBLE);
                evaluated = true;
            }
        });
        btnWrong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnCorrect.setVisibility(View.INVISIBLE);
                btnWrong.setVisibility(View.INVISIBLE);
                evaluated = true;
            }
        });

        final CardView flashcard = (CardView) rootView.findViewById(R.id.cardview_flashcard);

        cardText = (TextView) flashcard.findViewById(R.id.cardview_text);

        cardText.setText(textTerm);

        flashcard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation flashcardFlip = AnimationUtils.loadAnimation(getActivity(), R.anim.flashcard_flip);
                flashcard.startAnimation(flashcardFlip);
                ToggleText();

                if (!evaluated) {
                    btnCorrect.setVisibility(View.VISIBLE);
                    btnWrong.setVisibility(View.VISIBLE);
                }
            }
        });

        return rootView;
    }

    private void ToggleText(){
        if(showingFront){
            cardText.setText(textDescription);
            showingFront = false;
        }
        else{
            cardText.setText(textTerm);
            showingFront = true;
        }
    }
}
